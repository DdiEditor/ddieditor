package org.ddialliance.ddieditor.model.namespace.ddi3;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ddialliance.ddieditor.model.relationship.UrnRelationhipListDocument;
import org.ddialliance.ddieditor.model.relationship.ElementDocument.Element;
import org.ddialliance.ddieditor.model.relationship.ParentDocument.Parent;
import org.ddialliance.ddieditor.model.relationship.SubParentDocument.SubParent;
import org.ddialliance.ddieditor.model.relationship.UrnRelationhipListDocument.UrnRelationhipList;
import org.ddialliance.ddieditor.util.XmlObjectUtil;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.FileUtil;
import org.ddialliance.ddiftp.util.LineScanner;
import org.ddialliance.ddiftp.util.log.Log;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.parser.XSOMParser;

/**
 * Indexes XML schema elements into element name ~ namespace<br>
 * If duplicate elements names of found in different schemas the following<br>
 * convention is issued:
 * <p>
 * namespace__name ~ namespace
 * </p>
 */
public class DdiSchemaIndexer {
	private static final Log log = LogFactory.getLog(LogType.SYSTEM,
			DdiSchemaIndexer.class.getName());
	public static File ELEMENT_NAMESPACE_INDEX_POST_ADD = new File("resources"
			+ File.separator + "element-namespace-post-index-add.properties");
	/**
	 * Delimiter used for separation of short namespece and element
	 */
	public static String ELEMENT_NAMESPACE_DELIMETER = "__";

	private Properties elementNamespace = new Properties();
	private Properties elementNamespaceDuplicates = new Properties();
	private Properties elementNamespaceDuplicatesDebug = new Properties();

	private Properties elementNameLabels = new Properties();
	private Properties elementIdentifiable = new Properties();

	private XSSchemaSet xssSchemaSet;
	private XSOMParser parser;

	private UrnRelationhipListDocument urnRelationhipListDocument;

	/**
	 * SAX error handler
	 */
	class DdiSchemaErrorHandler implements ErrorHandler {
		public void warning(SAXParseException e) throws SAXException {
			throw new SAXException("Warning encountered" + generateErrorMsg(e),
					e);
		}

		public void error(SAXParseException e) throws SAXException {
			throw new SAXException("Error encountered" + generateErrorMsg(e), e);
		}

		public void fatalError(SAXParseException e) throws SAXException {
			throw new SAXException("Fatal Error encountered"
					+ generateErrorMsg(e), e);
		}

		private String generateErrorMsg(SAXParseException e) {
			StringBuilder result = new StringBuilder();
			result.append(" at line: ");
			result.append(e.getLineNumber());
			result.append(", URI: ");
			result.append(e.getSystemId());
			result.append(", message: ");
			result.append(e.getMessage());
			return result.toString();
		}
	}

	/**
	 * SAX entity resolver for resolvement of referred XML schema files
	 **/
	class DdiSchemaEntityResolver implements EntityResolver {
		List<String> publicIds = new Vector<String>();

		public InputSource resolveEntity(String publicId, String systemId) {
			// avoid duplicates of different **/xml.xsd
			if (systemId.indexOf("xml.xsd") > -1) {
				log
						.debug("Adding pubId: http://www.w3.org/XML/1998/namespace, sysId: http://www.w3.org/2001/03/xml.xsd");
				publicIds.add(systemId);
				return new InputSource("http://www.w3.org/2001/03/xml.xsd");
			}

			if (publicIds.contains(systemId)) {
				if (log.isDebugEnabled()) {
					// log.debug("Skipping pubId: " + publicId + ", sysId: "
					// + systemId);
				}
				// does not work with **/xml.xsd

				// XSOM uses the value returned from InputSource.getSystemID()
				// to avoid loading the same document twice. So if the entity
				// resolver set doesn't set the system ID to the InputSource, it
				// reads the document again and again every time you import the
				// schema
				return null;
			}

			if (log.isDebugEnabled()) {
				log.debug("Adding pubId: " + publicId + ", sysId: " + systemId);
			}
			publicIds.add(systemId);
			return new InputSource(systemId);
		}
	}

	protected DdiSchemaIndexer() throws DDIFtpException {
		elementNamespace = FileUtil
				.loadProperties(Ddi3NamespaceHelper.ELEMENT_NAMESPACE);
		if (elementNamespace.isEmpty()) {
			throw new DDIFtpException("Properties not loaded!");
		}
		elementIdentifiable = FileUtil
				.loadProperties(Ddi3NamespaceHelper.ELEMENT_IDENTIFIABLE);
		if (elementIdentifiable.isEmpty()) {
			throw new DDIFtpException("Properties not loaded!");
		}
	}

	/**
	 * Initialize a XML schema file for parsing
	 * 
	 * @param file
	 *            to parse
	 * @throws Exception
	 */
	public DdiSchemaIndexer(File file) throws DDIFtpException {
		try {
			parse(file);
		} catch (Exception e) {
			throw new DDIFtpException("Initialization error", e);
		}
	}

	/**
	 * Initialize a XML schema file for parsing
	 * 
	 * @param url
	 *            of schema to parse
	 * @throws Exception
	 */
	public DdiSchemaIndexer(URL url) throws DDIFtpException {
		try {
			parse(url);
		} catch (Exception e) {
			throw new DDIFtpException("Initialization error", e);
		}
	}

	private void parse(Object obj) throws Exception {
		DdiSchemaEntityResolver ddiSchemaEntityResolver = new DdiSchemaEntityResolver();
		parser = new XSOMParser();
		parser.setErrorHandler(new DdiSchemaErrorHandler());
		parser.setEntityResolver(ddiSchemaEntityResolver);

		if (obj instanceof File) {
			parser.parse((File) obj);
		} else if (obj instanceof URL) {
			parser.parse((URL) obj);
		}

		if (log.isDebugEnabled()) {
			log.debug("Added shemas: " + ddiSchemaEntityResolver.publicIds);
		}
		xssSchemaSet = parser.getResult();
	}

	/**
	 * Index parsed XML schema file
	 * 
	 * @throws DDIFtpException
	 */
	public void index() throws DDIFtpException {
		Iterator<XSSchema> itr = xssSchemaSet.iterateSchema();

		// index ddi elements
		while (itr.hasNext()) {
			XSSchema xSschema = itr.next();
			String namespace = xSschema.getTargetNamespace();

			// ddi
			if (namespace.indexOf("ddi:") > -1) {
				indexDdiElements(xSschema, namespace);
			}
		}

		// post index add
		// Properties indexpostAdd = FileUtil
		// .loadProperties(ELEMENT_NAMESPACE_INDEX_POST_ADD);
		// for (Entry<Object, Object> entry : indexpostAdd.entrySet()) {
		// insertElement(entry.getKey().toString(), entry.getValue()
		// .toString(), elementNamespace, elementNamespaceDuplicates);
		// }

		// store to file
		elementNamespace.putAll(elementNamespaceDuplicates);
		FileUtil.storeProperties(Ddi3NamespaceHelper.ELEMENT_NAMESPACE,
				elementNamespace);
		if (log.isDebugEnabled()) {
			List list = new ArrayList(elementNamespaceDuplicatesDebug.keySet());
			Collections.sort(list);
			for (Object key : list) {
				System.out.println(key + " = "
						+ elementNamespaceDuplicatesDebug.get(key));
			}

			FileUtil.storeProperties(new File("resources" + File.separator
					+ "elementNamespaceDuplicates-debug.properties"),
					elementNamespaceDuplicatesDebug);
		}

		// index ddi identifiables
		Iterator<XSSchema> itr2 = xssSchemaSet.iterateSchema();
		while (itr2.hasNext()) {
			XSSchema xSschema = itr2.next();
			String namespace = xSschema.getTargetNamespace();

			// ddi
			if (namespace.indexOf("ddi:") > -1) {
				indexDdiIdentifiables(xSschema, namespace);
			}
		}

		// store to file
		FileUtil.storeProperties(Ddi3NamespaceHelper.ELEMENT_IDENTIFIABLE,
				elementIdentifiable);

		// index name and label
		indexLabelNames();

		// store to file
		FileUtil.storeProperties(Ddi3NamespaceHelper.ELEMENT_NAME_LABEL,
				elementNameLabels);
	}

	protected void indexLabelNames() throws DDIFtpException {
		for (Object key : elementIdentifiable.keySet()) {
			insertLabelName((String) key, (String) elementNamespace.get(key));
		}
	}

	public Properties getElementNamespace() {
		return elementNamespace;
	}

	public Properties getElementNamespaceDuplicates() {
		return elementNamespaceDuplicates;
	}

	public Properties getElementLabelNames() {
		return elementNameLabels;
	}

	private void indexDdiElements(XSSchema xSschema, String namespace) {
		Iterator<XSElementDecl> itr = xSschema.iterateElementDecls();
		while (itr.hasNext()) {
			XSElementDecl xsElementDecl = itr.next();
			log.debug("Indexing: " + xsElementDecl.getName());
			insertElement(xsElementDecl.getName(), namespace, elementNamespace,
					elementNamespaceDuplicates);
		}
	}

	private void indexDdiIdentifiables(XSSchema xSschema, String namespace)
			throws DDIFtpException {
		String identifiableType = null;

		// loop elements
		Iterator<XSElementDecl> itr = xSschema.iterateElementDecls();
		while (itr.hasNext()) {
			XSElementDecl xsElementDecl = itr.next();

			if (xsElementDecl.getType().isComplexType()) {
				XSComplexType complex = (XSComplexType) xsElementDecl.getType();

				// determine type
				identifiableType = null;
				for (Iterator<? extends XSAttributeUse> iterator = complex
						.iterateAttributeUses(); iterator.hasNext();) {
					String name = iterator.next().getDecl().getName();
					if (name.equals("isIdentifiable")) {
						identifiableType = Ddi3NamespaceHelper.IDENTIFIABLE_TYPE;
					} else if (name.equals("isVersionable")) {
						identifiableType = Ddi3NamespaceHelper.VERSIONABLE_TYPE;
					} else if (name.equals("isMaintainable")) {
						identifiableType = Ddi3NamespaceHelper.MAINTAINABLE_TYPE;
					}
				}

				if (identifiableType != null) {
					// check duplicate
					String elementName = xsElementDecl.getName();
					if (elementNamespace.getProperty(elementName) == null) {
						elementName = createDuplicateConvention(xsElementDecl
								.getName(), namespace);
					}

					// store
					if (log.isDebugEnabled()) {
						log.debug("elementName: " + elementName
								+ ", identifiableType: " + identifiableType);
					}
					elementIdentifiable.put(elementName, identifiableType);
				}
			}
		}
	}

	// TODO to be removed, not used
	private void indexLabels(XSSchema xSschema, String namespace)
			throws DDIFtpException {
		String identifiableType = null;

		// loop elements
		Iterator<XSElementDecl> itr = xSschema.iterateElementDecls();
		while (itr.hasNext()) {
			XSElementDecl xsElementDecl = itr.next();
			if (xsElementDecl.getType().isComplexType()) {
				XSComplexType type = (XSComplexType) xsElementDecl.getType();
				if (type.getName().equals("IfThenElse")) {
					log.debug("break");
				}
				if (type.getSubtypes().isEmpty()) {
					insertLabelName(type.getName(), type.getTargetNamespace());
				} else {
					for (XSComplexType sub : type.getSubtypes()) {
						insertLabelName(sub.getName(), sub.getTargetNamespace());
					}
				}
			} else if (xsElementDecl.getType().isSimpleType()) {
				XSSimpleType type = (XSSimpleType) xsElementDecl.getType();
				insertLabelName(type.getName(), type.getTargetNamespace());
			}

			if (identifiableType != null) {
				// check duplicate
				String elementName = xsElementDecl.getName();
				if (elementNamespace.getProperty(elementName) == null) {
					elementName = createDuplicateConvention(xsElementDecl
							.getName(), namespace);
				}

				// store
				if (log.isDebugEnabled()) {
					log.debug("elementName: " + elementName
							+ ", identifiableType: " + identifiableType);
				}
				elementIdentifiable.put(elementName, identifiableType);
			}
		}
	}

	Pattern nameList = Pattern.compile("get[a-zA-Z0-9]*NameList");
	public static String labelList = "getLabelList";
	private String label = "Label";

	private void insertLabelName(String localName, String namespace)
			throws DDIFtpException {
		Method[] methods = null;
		try {
			methods = XmlObjectUtil.getXmlObjectMethods(localName, namespace);
		} catch (DDIFtpException e) {
			// do nothing just for logging
			// TODO investigate: ncubes does not fit types or documents
			return;
		}

		boolean found = false;
		for (int i = 0; i < methods.length; i++) {
			// label
			if (methods[i].getName().equals(labelList)) {
				elementNameLabels.put(Ddi3NamespaceHelper
						.getCleanedElementName(localName), label);
				found = true;
				break;
			}

			// name
			Matcher matcher = nameList.matcher(methods[i].getName());
			if (matcher.matches()) {
				String match = matcher.group();
				elementNameLabels.put(Ddi3NamespaceHelper
						.getCleanedElementName(localName), match.substring(3,
						match.length() - 4));
				found = true;
				break;
			}
		}

		// logging non found
		if (log.isDebugEnabled()) {
			if (!found) {
				StringBuilder errorStr = new StringBuilder(localName);
				errorStr.append(": ");
				for (int i = 0; i < methods.length; i++) {
					errorStr.append(methods[i].getName());
					if (i < methods.length) {
						errorStr.append(", ");
					}
				}
				log.debug(errorStr.toString());
			}
		}
	}

	/**
	 * Inserts the element into properties. If a duplicates exists enforce
	 * duplicate convention
	 * 
	 * @param element
	 *            to insert
	 * @param value
	 *            value
	 * @param properties
	 *            to insert in
	 * @param duplicateProperties
	 *            if duplicate insert here
	 * @return resolved element name
	 */
	public String insertElement(String element, String value,
			Properties properties, Properties duplicateProperties) {
		String result = null;

		// check for duplicate
		Object tmpValue = properties.get(element);
		boolean tmpDupValue = false;
		String search = ELEMENT_NAMESPACE_DELIMETER + element;
		for (Iterator iterator = duplicateProperties.keySet().iterator(); iterator
				.hasNext();) {
			String next = (String) iterator.next();
			if (next.endsWith(search)) {
				if (log.isDebugEnabled()) {
					log.debug("Duplicate key: " + next + ", search: " + search);
				}
				tmpDupValue = true;
			}
		}

		// add duplicate convention
		if (tmpDupValue) {
			result = createDuplicateConvention(element, value);
			duplicateProperties.put(result, value);
		}
		if (tmpValue != null) {
			properties.remove(element);
			duplicateProperties.put(createDuplicateConvention(element,
					(String) tmpValue), tmpValue);
			result = createDuplicateConvention(element, value);
			duplicateProperties.put(result, value);
		}

		// no duplicate
		if (tmpValue == null && !tmpDupValue) {
			properties.put(element, value);
			result = element;
		}
		return element;
	}

	/**
	 * Creates duplicates convention namespace__elment eg. reuseable__name
	 * 
	 * @param elment
	 *            to create convention on
	 * @param namespace
	 *            namespace to use in convention
	 * @return result
	 */
	private String createDuplicateConvention(String element, String namespace) {
		String result = namespace.substring(4, namespace.length() - 4);
		if (log.isDebugEnabled()) {
			elementNamespaceDuplicatesDebug.put(element
					+ ELEMENT_NAMESPACE_DELIMETER + result, namespace);
		}
		return result + ELEMENT_NAMESPACE_DELIMETER + element;
	}

	public void indexDdiRelationship() throws Exception {
		// result
		urnRelationhipListDocument = UrnRelationhipListDocument.Factory
				.newInstance();
		UrnRelationhipList urnRelationhipList = urnRelationhipListDocument
				.addNewUrnRelationhipList();

		// process
		DdiElementsRelationshiplistScanner fileLineScanner = new DdiElementsRelationshiplistScanner();
		File file = new File("resources" + File.separator
				+ "ddi-elements-relationshiplist.txt");
		if (!file.exists()) {
			throw new Exception();
		}
		FileUtil.processFileLineByLine(file, fileLineScanner);

		// store
		urnRelationhipListDocument
				.save(Ddi3NamespaceHelper.ELEMENT_URN_RELATIONSHIP);

		// check
		boolean found = false;
		List<String> notFoundIdentifiables = new ArrayList<String>();
		for (Iterator iterator = elementIdentifiable.keySet().iterator(); iterator
				.hasNext();) {
			found = false;
			String type = (String) iterator.next();
			for (Element element : urnRelationhipListDocument
					.getUrnRelationhipList().getElementList()) {
				if (type.equals(element.getId())) {
					found = true;
				}
			}
			if (!found) {
				notFoundIdentifiables.add(type + " in schema: "
						+ elementNamespace.getProperty(type));
			}
		}
		if (!notFoundIdentifiables.isEmpty()) {
			if (log.isErrorEnabled()) {
				log.warn("Not found identifiables: ");
				for (String notFound : notFoundIdentifiables) {
					log.error(notFound);
				}
			}
			throw new DDIFtpException(
					"Identifiables nedding URN relationship coding!");
		}
	}

	class DdiElementsRelationshiplistScanner implements LineScanner {
		Element currentElement = null;
		Parent parents = null;
		String currentLine = null;

		@Override
		public void processLine(String line) throws Exception {
			currentLine = line;
			// scan
			Scanner scanner = new Scanner(line);
			if (scanner.hasNext()) {
				String element = scanner.next();
				if (element.indexOf("#") > -1) {
					// is comment, do nothing
					return;
				}
				addElement(processPart(element));
			} else {
				// blank line, do nothing
				return;
				// throw new DDIFtpException("Empty line", new Throwable());
			}

			// additional parents
			int level = 0;
			while (scanner.hasNext()) {
				String element = scanner.next();
				if (element.indexOf("#") > -1) {
					// is comment, do nothing
					return;
				} else {
					level++;
					if (level == 1) {
						addNewParent(processPart(element), level);
					} else {
						addParent(processPart(element), level);
					}
				}
			}
			scanner.close();
		}

		private void addElement(UrnElement urnElement) {
			for (Element element : urnRelationhipListDocument
					.getUrnRelationhipList().getElementList()) {
				if (element.getId().equals(urnElement.name)) {
					currentElement = element;
					return;
				}
			}
			currentElement = urnRelationhipListDocument.getUrnRelationhipList()
					.addNewElement();
			currentElement.setId(urnElement.name);
			currentElement.setIdentifiabletype(urnElement.identifiable);
		}

		private void addNewParent(UrnElement urnElement, int level) {
			parents = currentElement.addNewParent();
			parents.setId(urnElement.name);
			parents.setIdentifiabletype(urnElement.identifiable);
		}

		private void addParent(UrnElement urnElement, int level) {
			SubParent parentElement = parents.addNewSubParent();
			parentElement.setId(urnElement.name);
			parentElement.setIdentifiabletype(urnElement.identifiable);
			parentElement.setLevel(level);
		}

		private UrnElement processPart(String element) throws Exception {
			String elementType = element.substring(element.length() - 2,
					element.length() - 1);
			element = element.substring(0, element.length() - 3);

			// check duplicate
			String tmp = elementNamespace.getProperty(element);
			if (tmp == null) {
				throw new DDIFtpException("Element: " + element
						+ "\n is not recognized at line:  " + currentLine);
			}
			return new UrnElement(element, elementType);
		}

		class UrnElement {
			public UrnElement(String name, String identifiable) {
				this.name = name;
				this.identifiable = identifiable;
			}

			String name;
			String identifiable;
		}
	}
}
