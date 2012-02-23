package org.ddialliance.ddieditor.logic.validation;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.ddialliance.ddieditor.model.marker.MarkerDocument.Marker;
import org.ddialliance.ddieditor.model.marker.MarkerListDocument;
import org.ddialliance.ddieditor.model.marker.MarkerType;
import org.ddialliance.ddieditor.model.marker.PositionDocument.Position;
import org.ddialliance.ddieditor.model.marker.ResourceDocument.Resource;
import org.ddialliance.ddieditor.model.marker.StateType;
import org.ddialliance.ddieditor.model.marker.XPathDocument.XPath;
import org.ddialliance.ddiftp.util.xml.XmlBeansUtil;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class DdiSchemaValidator {
	boolean hasError = false;
	QName tempQname = new QName("", "");
	Stack<QName> pathStack = new Stack<QName>();
	HashMap<QName, Integer> elementCount = new HashMap<QName, Integer>();

	Validator validator = null;
	SAXResult result = null;

	public MarkerListDocument markerListDoc = null;
	Resource resource = null;

	public DdiSchemaValidator() throws SAXException, MalformedURLException {
		// xml schema factory
		SchemaFactory factory = SchemaFactory
				.newInstance("http://www.w3.org/2001/XMLSchema");

		// compile schema either from file -url or javax transform source
		File schemaLocation = new File(
				"/home/ddajvj/app/ddi3/DDI_3_1_2009-10-18_Documentation_XMLSchema/XMLSchema/instance.xsd");
		// URL schemaLocation = new URL(
		// "http://www.ddialliance.org/sites/default/files/schema/ddi3.1/instance.xsd");
		Schema schema = factory.newSchema(schemaLocation);

		// validator from schema
		validator = schema.newValidator();
		DdiErrorHandler error = this.new DdiErrorHandler();
		validator.setErrorHandler(error);

		// sax result
		result = new SAXResult(this.new DdiContentHandler());
	}

	public void validate(File file, String resourceId,
			MarkerListDocument markerListDoc) throws SAXException, IOException {
		this.markerListDoc = markerListDoc;

		// clean up marker list
		for (Resource resource : markerListDoc.getMarkerList()
				.getResourceList()) {
			if (resource.getId().equals(resourceId)) {
				this.resource = resource;
				for (Iterator<Marker> iterator = resource.getMarkerList()
						.iterator(); iterator.hasNext();) {
					Marker marker = iterator.next();
					if (marker.getType().equals(MarkerType.SCHEMA_VALIDATION)) {
						iterator.remove();
					}
				}
			}
		}

		if (resource == null) { // guard
			this.resource = markerListDoc.getMarkerList().addNewResource();
			this.resource.setId(resourceId);
		}

		// parse source
		Source source = new SAXSource(new InputSource(file.getAbsolutePath()));

		// xml schema validate
		validator.validate(source, result);
	}

	private HashMap<QName, Integer> getElementCount() {
		return elementCount;
	}

	private void addMarker(StateType.Enum stateType, String msg, int line,
			int column) {
		Marker marker = resource.addNewMarker();
		marker.setType(MarkerType.SCHEMA_VALIDATION);

		// xpath
		StringBuilder xPathString = new StringBuilder();
		Object[] elements = pathStack.toArray();
		for (int i = 0; i < elements.length; i++) {
			xPathString.append("/*");
			// namespace
			xPathString.append("[namespace-uri()='");
			xPathString.append(((QName) elements[i]).getNamespaceURI());

			// local part
			xPathString.append("' and local-name()='");
			xPathString.append(((QName) elements[i]).getLocalPart());

			// count
			xPathString.append("'][");
			xPathString.append(elementCount.get((QName) elements[i]));
			xPathString.append("]");
		}
		Position position = marker.addNewPosition();
		XPath xPath = position.addNewXPath();
		xPath.setLine(line);
		xPath.setColumn(column);
		XmlBeansUtil.setTextOnMixedElement(xPath, xPathString.toString());

		// label
		String label = "";
		if (!pathStack.isEmpty()) {
			label = ((QName) elements[elements.length - 1]).getLocalPart();
		}
		XmlBeansUtil.setTextOnMixedElement(marker.addNewLabel(), label);

		// description
		XmlBeansUtil.setTextOnMixedElement(marker.addNewDescription(), msg);

		// state
		marker.setState(stateType);
	}

	public class DdiErrorHandler implements ErrorHandler {
		public void warning(SAXParseException ex) {
			hasError = true;
			addMarker(StateType.WARNING, ex.getMessage(), ex.getLineNumber(),
					ex.getColumnNumber());
		}

		public void error(SAXParseException ex) {
			hasError = true;
			addMarker(StateType.ERROR, ex.getMessage(), ex.getLineNumber(),
					ex.getColumnNumber());
		}

		public void fatalError(SAXParseException ex) throws SAXException {
			hasError = true;
			addMarker(StateType.FATAL, ex.getMessage(), ex.getLineNumber(),
					ex.getColumnNumber());
		}
	}

	public class DdiContentHandler implements ContentHandler {
		@Override
		public void characters(char[] arg0, int arg1, int arg2)
				throws SAXException {
			// do nothing
		}

		@Override
		public void endDocument() throws SAXException {
			// do nothing
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			tempQname = pathStack.pop();
		}

		@Override
		public void endPrefixMapping(String arg0) throws SAXException {
			// do nothing
		}

		@Override
		public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
				throws SAXException {
			// do nothing
		}

		@Override
		public void processingInstruction(String arg0, String arg1)
				throws SAXException {
			// do nothing
		}

		@Override
		public void setDocumentLocator(Locator arg0) {
			// do nothing
		}

		@Override
		public void skippedEntity(String arg0) throws SAXException {
			// do nothing
		}

		@Override
		public void startDocument() throws SAXException {
			// do nothing
		}

		@Override
		public void startElement(String uri, String localName, String qPrefix,
				Attributes atts) throws SAXException {
			QName qName = new QName(uri, localName);
			if (!tempQname.equals(qName)) {
				getElementCount().remove(qName);
				getElementCount().put(qName, 1);
			} else {
				Integer count = getElementCount().get(qName);
				if (count != null) {
					getElementCount().put(qName, count + 1);
				}
			}
			pathStack.push(qName);
		}

		@Override
		public void startPrefixMapping(String arg0, String arg1)
				throws SAXException {
			// do nothing
		}
	}
}