package org.ddialliance.ddieditor.model.namespace.ddi3;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.xmlbeans.XmlException;
import org.ddialliance.ddieditor.model.relationship.UrnRelationhipListDocument;
import org.ddialliance.ddieditor.model.relationship.ElementDocument.Element;
import org.ddialliance.ddieditor.model.relationship.UrnRelationhipListDocument.UrnRelationhipList;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.FileUtil;
import org.ddialliance.ddiftp.util.log.Log;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;

/**
 * Associates DDI elements to DDI modules and DDI elements to parent DDI
 * elements
 */
public class Ddi3NamespaceGenerator {
	private static Log log = LogFactory.getLog(LogType.SYSTEM,
			Ddi3NamespaceGenerator.class);
	private static Log errorlog = LogFactory.getLog(LogType.EXCEPTION,
			Ddi3NamespaceGenerator.class);

	public static final String DDI_INSTANCE_URL = "http://www.icpsr.umich.edu/DDI/schema/ddi3.0/instance.xsd";

	public static File ELEMENT_NAMESPACE = new File("resources"
			+ File.separator + "element-namespace.properties");

	public static File ELEMENT_IDENTIFIABLE = new File("resources"
			+ File.separator + "element-identifiable.properties");

	public static File ELEMENT_URN_RELATIONSHIP = new File("resources"
			+ File.separator + "element-urn-relationship.xml");

	private static final String DELIMITER = "/";

	private Properties elementNamespace = new Properties();
	private Properties elementIdentifiable = new Properties();

	public static final String MAINTAINABLE_TYPE = "M";
	public static final String VERSIONABLE_TYPE = "V";
	public static final String IDENTIFIABLE_TYPE = "I";

	private Set<String> maintainalbeList;
	private HashMap<String, String> qualifiedNsCache = new HashMap<String, String>();
	UrnRelationhipList urnRelationhipList;

	/**
	 * Default constructor
	 */
	public Ddi3NamespaceGenerator() throws DDIFtpException {
		loadDdiElements();

	}

	private void loadDdiElements() throws DDIFtpException {
		// inspect file status
		if (!ELEMENT_IDENTIFIABLE.exists() || !ELEMENT_NAMESPACE.exists()
				|| !ELEMENT_URN_RELATIONSHIP.exists()) {
			try {
				DdiSchemaIndexer ddiSchemaIndexer = new DdiSchemaIndexer(
						new URL(DDI_INSTANCE_URL));
				ddiSchemaIndexer.index();
				ddiSchemaIndexer.indexDdiRelationship();
			} catch (Exception e) {
				throw new DDIFtpException(e);
			}
		}
		
		// load
		elementNamespace = FileUtil.loadProperties(ELEMENT_NAMESPACE);
		elementIdentifiable = FileUtil.loadProperties(ELEMENT_IDENTIFIABLE);
		try {
			urnRelationhipList = UrnRelationhipListDocument.Factory.parse(
					ELEMENT_URN_RELATIONSHIP).getUrnRelationhipList();
		} catch (Exception e) {
			throw new DDIFtpException("Marshal error", e);
		}
	}

	public Element getElementParents(String elementName) {
		for (Element element : urnRelationhipList.getElementList()) {
			if (element.getId().equals(elementName)) {
				return element;
			}
		}
		return null;
	}
	
	/**
	 * Retrieve a DDI namespace object by a DDI element
	 * 
	 * @param elementName
	 *            to retrieve name space upon
	 * @return namespace object
	 * @throws DDIFtpException
	 */
	public Ddi3NamespacePrefix getNamespaceObjectByElement(String elementName)
			throws DDIFtpException {
		return Ddi3NamespacePrefix.createNamespacePrefix(null,
				getNamespaceStringByElement(elementName));
	}

	/**
	 * Retrieve a DDI namespace string by a DDI element
	 * 
	 * @param elementName
	 *            to retrieve name space upon
	 * @return namespace string
	 * @throws DDIFtpException
	 */
	public String getNamespaceStringByElement(String elementName)
			throws DDIFtpException {
		if (elementName == null || elementName.equals("")) {
			throw new DDIFtpException("Namespace for element: " + elementName
					+ " is not recongnized");
		}

		String namespace = elementNamespace.getProperty(elementName);
		if (namespace == null) {
			throw new DDIFtpException("Namespace for element: " + elementName
					+ " is not recongnized");
		}
		return namespace;
	}

	public String getModuleNameByElement(String elementName)
			throws DDIFtpException {
		if (elementName == null || elementName.equals("")) {
			return null;
		}

		// needs implementation
		throw new DDIFtpException("Namespace for element: " + elementName
				+ " is not recongnized");
	}

	/**
	 * Clean an element name from duplicate convention
	 * 
	 * @see DdiSchemaIndexer
	 * @param elementName
	 *            to clean
	 * @return cleaned element name
	 */
	public String getCleanedElementName(String elementName) {
		int index = elementName
				.indexOf(DdiSchemaIndexer.ELEMENT_NAMESPACE_DELIMETER);
		if (index > -1) {
			return elementName.substring(index + 2);
		} else {
			return elementName;
		}
	}

	/**
	 * Add a fully qualified name space declaration to a XPath expression
	 * 
	 * @param query
	 *            XPath definition
	 * @return fully qualified element name space declaration
	 * @throws Exception
	 */
	public String addFullyQualifiedNamespaceDeclarationToElements(String query)
			throws DDIFtpException {
		String cacheResult = qualifiedNsCache.get(query);
		if (cacheResult != null) {
			if (log.isDebugEnabled()) {
				log.debug("Qualified NS declaration for: '" + query
						+ "' found in qualifiedNsCache: '" + cacheResult + "'");
			}
			return cacheResult;
		}

		String[] elements = query.split(DELIMITER);
		String[] result = new String[elements.length];

		if (log.isDebugEnabled()) {
			log.debug("No of query elements: " + elements.length);
		}

		int elementPositionStart = -1;
		int elementPositionEnd = -1;
		String elementPosition = null;
		StringBuilder elmentBuilder = new StringBuilder();
		for (int x = 0; x < elements.length; x++) {
			if (log.isDebugEnabled()) {
				log.debug("Work on: " + elements[x]);
			}

			// attribute
			if (elements[x].startsWith("@")) {
				result[x] = elements[x];
			}

			// copy out xpath position [x]
			elementPositionStart = elements[x].indexOf("[");
			if (elementPositionStart > -1) {
				elementPositionEnd = elements[x].indexOf("]");
				elementPosition = elements[x].substring(elementPositionStart,
						elementPositionEnd + 1);
				elements[x] = elements[x].substring(0, elementPositionStart);
				if (log.isDebugEnabled()) {
					log.debug("elementSubstring: " + elements[x]
							+ ", elementPosition: " + elementPosition);
				}
			}
			elementPositionStart = -1; // reset
			elementPositionEnd = -1; // reset

			// assign namespace to element
			if (result[x] == null && elements[x] != null
					&& !elements[x].equals("")) {
				elmentBuilder.append("*[namespace-uri()='");
				elmentBuilder.append(getNamespaceStringByElement(elements[x]));
				elmentBuilder.append("' and local-name()='");
				elmentBuilder.append(getCleanedElementName(elements[x]));
				elmentBuilder.append("']");
				if (elementPosition != null) {
					elmentBuilder.append(elementPosition);
				}
				result[x] = elmentBuilder.toString();
				if (log.isDebugEnabled()) {
					log.debug(elements[x] + ", sub result: " + result[x]);
				}
				elmentBuilder.delete(0, elmentBuilder.length());
			}
			elementPosition = null;
		}

		// build query
		boolean doubleSlash = false;
		for (int i = 0; i < result.length; i++) {
			if (!elements[i].equals("")) {
				elmentBuilder.append("/");
				doubleSlash = false;
			} else {
				if (doubleSlash) {
					elmentBuilder.append("/");
				}
				doubleSlash = true;
				continue;
			}
			elmentBuilder.append(result[i]);
		}

		String qualifiedNs = elmentBuilder.toString();
		if (log.isDebugEnabled()) {
			log.debug(qualifiedNs);
		}

		qualifiedNsCache.put(query, qualifiedNs);
		return qualifiedNs;
	}

	/**
	 * Substitute name space prefixes from a node expression with fully
	 * qualified name space declarations
	 * 
	 * @param node
	 * @return node with with fully qualified name space declarations
	 * @throws Exception
	 */
	public String substitutePrefixesFromElements(String node)
			throws DDIFtpException {
		if (log.isDebugEnabled()) {
			log.debug(node);
		}

		Pattern startTagPattern = Pattern.compile("<\\w:");
		Matcher startTagMatcher = null;

		Pattern endTagPattern = Pattern.compile("</\\w:");
		Matcher endTagMatcher = null;

		Pattern elementPattern = Pattern.compile("<[a-zA-Z]+");
		Matcher elementMacher = null;

		// remove all end prefix
		endTagMatcher = endTagPattern.matcher(node);
		StringBuilder element = new StringBuilder(endTagMatcher
				.replaceAll("</"));

		// search start prefix
		startTagMatcher = startTagPattern.matcher(element.toString());
		if (startTagMatcher.find()) {
			do {
				// delete prefix
				element.delete(startTagMatcher.start() + 1, startTagMatcher
						.end());

				// insert name space
				elementMacher = elementPattern.matcher(element.toString());
				elementMacher.region(startTagMatcher.start(), element.length());
				elementMacher.find();
				element.insert(elementMacher.end(), " xmlns=\""
						+ getNamespaceObjectByElement(
								element.substring(elementMacher.start() + 1,
										elementMacher.end())).getNamespace()
						+ "\"");

				// reset start prefix matcher
				startTagMatcher = startTagPattern.matcher(element.toString());
			} while (startTagMatcher.find());
		}

		if (log.isDebugEnabled()) {
			log.debug(element.toString());
		}
		return element.toString();
	}

	public Set<String> getMaintainableElementsList() {
		if (maintainalbeList == null) {
			maintainalbeList = new TreeSet<String>();
			for (Entry<Object, Object> entry : elementIdentifiable.entrySet()) {
				if (entry.getValue().equals(MAINTAINABLE_TYPE)) {
					maintainalbeList.add((String) entry.getKey());
				}
			}
		}
		return maintainalbeList;
	}

	public String getParentElementName(String elementName) {
		// parent
		String parentElementName = null;
		if (log.isDebugEnabled()) {
			log.debug("Parent element: " + parentElementName);
		}
		return parentElementName;
	}
}
