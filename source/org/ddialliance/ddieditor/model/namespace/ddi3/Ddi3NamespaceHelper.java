package org.ddialliance.ddieditor.model.namespace.ddi3;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlObject;
import org.ddialliance.ddieditor.model.relationship.UrnRelationhipListDocument;
import org.ddialliance.ddieditor.model.relationship.ElementDocument.Element;
import org.ddialliance.ddieditor.model.relationship.UrnRelationhipListDocument.UrnRelationhipList;
import org.ddialliance.ddieditor.persistenceaccess.maintainablelabel.MaintainableLabelQuery;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.FileUtil;
import org.ddialliance.ddiftp.util.log.Log;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;
import org.perf4j.aop.Profiled;

/**
 * Associates DDI elements to DDI modules and DDI elements to parent DDI
 * elements
 */
public class Ddi3NamespaceHelper {
	private static Log log = LogFactory.getLog(LogType.SYSTEM,
			Ddi3NamespaceHelper.class);

	public static final String DDI_INSTANCE_URL = "resources/ddi/instance.xsd";

	public static File ELEMENT_NAMESPACE = new File("resources"
			+ File.separator + "element-namespace.properties");

	public static File ELEMENT_IDENTIFIABLE = new File("resources"
			+ File.separator + "element-identifiable.properties");

	public static File ELEMENT_NAME_LABEL = new File("resources"
			+ File.separator + "element-name-label.properties");

	public static File ELEMENT_URN_RELATIONSHIP = new File("resources"
			+ File.separator + "element-urn-relationship.xml");

	private static final String DELIMITER = "/";

	private Properties elementNamespace = new Properties();
	private Properties elementIdentifiable = new Properties();
	private Properties elementNameLabels = new Properties();

	public static final String MAINTAINABLE_TYPE = "M";
	public static final String VERSIONABLE_TYPE = "V";
	public static final String IDENTIFIABLE_TYPE = "I";

	private Set<String> maintainalbeList;
	private HashMap<String, String> qualifiedNsCache = new HashMap<String, String>();
	private UrnRelationhipList urnRelationhipList;

	// sub prefix variables
	private final String NO_SPACE = "";
	private Pattern startTagPattern = Pattern.compile("<[a-zA-Z]+[1-9]?:");
	private Pattern endTagPattern = Pattern.compile("</[a-zA-Z]+[1-9]?:");
	private Pattern elementPattern = Pattern.compile("<[a-zA-Z]+");

	// pattern xmlns:r="ddi:reusable:3_0"
	// xmlns:ddi="ddi:datacollection:3_0"
	private Pattern namespacePattern = Pattern
			.compile("xmlns[:]{1}[a-z]*[1-9]?=[[\"]|[']]{1}ddi:[a-z]*:[0-9]{1}_[0-9]{1}[[\"]|[']]{1}");

	// pattern xsi:type="d:CodeDomainType"
	private Pattern xsiPattern = Pattern
			.compile("xsi:type=[[\"]|[']]{1}[a-z]{1}:[a-zA-Z]*[[\"]|[']]{1}");

	// pattern xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	private Pattern xsiDefPattern = Pattern
			.compile("xmlns:xsi=[[\"]|[']]{1}http://www.w3.org/2001/XMLSchema-instance[[\"]|[']]{1}");

	/**
	 * Default constructor
	 */
	public Ddi3NamespaceHelper() throws DDIFtpException {
		loadDdiElements();
	}

	private void loadDdiElements() throws DDIFtpException {
		// inspect file status
		if (!ELEMENT_IDENTIFIABLE.exists() || !ELEMENT_NAMESPACE.exists()
				|| !ELEMENT_URN_RELATIONSHIP.exists()
				|| !ELEMENT_NAME_LABEL.exists()) {
			try {
				DdiSchemaIndexer ddiSchemaIndexer = new DdiSchemaIndexer(
						new File(DDI_INSTANCE_URL));
				ddiSchemaIndexer.index();
				ddiSchemaIndexer.indexDdiRelationship();
			} catch (Exception e) {
				throw new DDIFtpException(e);
			}
		}

		// load
		elementNamespace = FileUtil.loadProperties(ELEMENT_NAMESPACE);
		elementIdentifiable = FileUtil.loadProperties(ELEMENT_IDENTIFIABLE);
		elementNameLabels = FileUtil.loadProperties(ELEMENT_NAME_LABEL);
		try {
			urnRelationhipList = UrnRelationhipListDocument.Factory.parse(
					ELEMENT_URN_RELATIONSHIP).getUrnRelationhipList();
		} catch (Exception e) {
			throw new DDIFtpException("Marshal error", e);
		}
	}

	public Properties getIdentifiables() {
		return elementIdentifiable;
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
					+ " is not recongnized", new Throwable());
		}

		String namespace = elementNamespace.getProperty(elementName);
		if (namespace == null) {
			throw new DDIFtpException("Namespace for element: " + elementName
					+ " is not recongnized", new Throwable(), true);
		}
		return namespace;
	}

	/**
	 * Retrieve the module name of a DDI name space
	 * 
	 * @param elementName
	 * @return module name
	 * @throws DDIFtpException
	 */
	public String getModuleNameByElement(String elementName)
			throws DDIFtpException {
		String namespace = getNamespaceStringByElement(elementName);
		String[] result = namespace.split(":");
		return result[1];
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
	 * Convert a ddi element local name to duplicate conversion name via the
	 * elements qualified name
	 * 
	 * @param qName
	 *            qualified name of element to do conversion on
	 * @see javax.xml.namespace.QName
	 * @return duplicate conversion
	 * @throws DDIFtpException
	 */
	public String getDuplicateConvention(QName qName) throws DDIFtpException {
		StringBuilder search = new StringBuilder();
		search.append((qName.getNamespaceURI().split(":"))[1]);
		search.append("__");
		search.append(qName.getLocalPart());

		String result = (String) elementNamespace.get(search.toString());
		return (result == null ? qName.getLocalPart() : search.toString());
	}

	/**
	 * Add a fully qualified name space declaration to a XPath expression
	 * 
	 * @param query
	 *            XPath definition
	 * @return fully qualified element name space declaration
	 * @throws Exception
	 */
	@Profiled(tag = "addFullyQualifiedNamespaceDeclarationToElements")
	public String addFullyQualifiedNamespaceDeclarationToElements(String query)
			throws DDIFtpException {
		String cacheResult = qualifiedNsCache.get(query);
		if (cacheResult != null) {
			// if (log.isDebugEnabled()) {
			// log.debug("Qualified NS declaration for: '" + query
			// + "' found in qualifiedNsCache: '" + cacheResult + "'");
			// }
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

			if (elements[x].startsWith("*")) {
				result[x] = elements[x];
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
			log.debug("Org node:\n" + node);
		}

		Matcher startTagMatcher = null;
		Matcher endTagMatcher = null;
		Matcher elementMacher = null;

		// remove all xsi:type declarations
		Matcher xsiMacher = xsiPattern.matcher(node);
		node = xsiMacher.replaceAll(NO_SPACE);

		// remove all xmlns:xsi declarations
		Matcher xsiDefMacher = xsiDefPattern.matcher(node);
		node = xsiDefMacher.replaceAll(NO_SPACE);

		// index namespace prefix deceleration
		Map<String, String> prefixNamespaces = new HashMap<String, String>();
		Matcher namespaceMatcher = namespacePattern.matcher(node);
		String namespaceDecl = null;
		String value;
		int index = -1;
		if (namespaceMatcher.find()) {
			do {
				// xmlns:ddi1="ddi:datacollection:3_1"
				namespaceDecl = node.substring(namespaceMatcher.start(),
						namespaceMatcher.end());
				index = namespaceDecl.indexOf("=");
				value = namespaceDecl.substring(6, index);
				if (!prefixNamespaces.containsKey(value)) {
					prefixNamespaces.put(value, namespaceDecl.substring(
							index + 2, namespaceDecl.length() - 1));
				} else {
					continue;
				}
			} while (namespaceMatcher.find());
		}

		// remove all prefixes deceleration
		node = namespaceMatcher.replaceAll("");

		// remove all end prefix
		endTagMatcher = endTagPattern.matcher(node);
		StringBuilder element = new StringBuilder(endTagMatcher
				.replaceAll("</"));

		// search start prefix
		startTagMatcher = startTagPattern.matcher(element.toString());
		String prefix, namespace;
		if (startTagMatcher.find()) {
			do {
				prefix = element.substring(startTagMatcher.start() + 1,
						startTagMatcher.end() - 1);
				namespace = prefixNamespaces.get(prefix);

				// delete prefix
				element.delete(startTagMatcher.start() + 1, startTagMatcher
						.end());

				// current element
				elementMacher = elementPattern.matcher(element.toString());
				elementMacher.region(startTagMatcher.start(), element.length());
				elementMacher.find();
				String currentElement = element.substring(
						elementMacher.start() + 1, elementMacher.end());

				// insert prefix replacement, namespace declaration
				// <QuestionText xml:lang="no" ...
				if (namespace != null) {
					element.insert(elementMacher.end(), " xmlns=\"" + namespace
							+ "\"");
				} else {
					Matcher namespaceMacher = namespacePattern
							.matcher(currentElement);
					if (namespaceMacher.find()) {
						prefixNamespaces
								.put(prefix, currentElement.substring(
										namespaceMacher.start(),
										namespaceMacher.end()));
					}

					// define namespace
					namespace = null;
					Ddi3NamespacePrefix ddiPrefix = null;
					try {
						if (log.isDebugEnabled()) {
							log.debug("currentElement: " + currentElement);
						}
						ddiPrefix = getNamespaceObjectByElement(currentElement);
						namespace = ddiPrefix.getNamespace();
						element.insert(elementMacher.end(), " xmlns=\""
								+ namespace + "\"");
					} catch (DDIFtpException e) {
						// hack to circumvent unused unique element name to
						// namespace convention via using standard ddi namespace
						// prefixes
						ddiPrefix = Ddi3NamespacePrefix
								.getNamespaceByDefaultPrefix(prefix);
						if (ddiPrefix != null) {
							namespace = ddiPrefix.getNamespace();
							element.insert(elementMacher.end(), " xmlns=\""
									+ namespace + "\"");
						} else {
							throw new DDIFtpException(
									"Unsuccessfull namespace prefix substitution for prefix: '"
											+ prefix + "', at element: '"
											+ currentElement
											+ "', current node form: "
											+ element.toString(),
									new Throwable());
						}
					}
				}

				// reset start prefix matcher
				startTagMatcher = startTagPattern.matcher(element.toString());
			} while (startTagMatcher.find());
		}

		// remove all predefined ddi prefixed namespace declarations
		Matcher namespaceMacher = namespacePattern.matcher(element);
		node = namespaceMacher.replaceAll(NO_SPACE);
		if (log.isDebugEnabled()) {
			log.debug("Modified node:\n" + node);
		}
		return node;
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

	public boolean isMaintainable(XmlObject xmlObject) {
		QName qName = xmlObject.schemaType().getDocumentElementName();
		return isMaintainable(qName.getLocalPart());
	}

	public boolean isMaintainable(String localName) {
		for (String tmp : getMaintainableElementsList()) {
			if (tmp.indexOf(localName) > -1) {
				return true;
			}
		}
		return false;
	}

	public boolean isVersionable(XmlObject xmlObject) {
		QName qName = xmlObject.schemaType().getDocumentElementName();
		String localName = qName.getLocalPart();
		// TODO convert a localName to conversion name via QName to detect eg.
		// StudyUnit as studyunit_StudyUnit
		for (Entry<Object, Object> entry : elementIdentifiable.entrySet()) {
			if ((entry.getValue().equals(MAINTAINABLE_TYPE) || entry.getValue()
					.equals(VERSIONABLE_TYPE))
					&& entry.getKey().equals(localName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Transform the conversion name to local schema name
	 * 
	 * @param conversionName
	 *            to transform
	 * @return local schema name
	 */
	public String getLocalSchemaName(String conversionName) {
		int index = conversionName.indexOf("__");
		if (index > -1) {
			return conversionName.substring(index + 2);
		} else
			return conversionName;
	}

	/**
	 * Transform all conversion names in a array to local schema names
	 * 
	 * @param conversionNames
	 *            to transform
	 * @return local schema names
	 */
	public String[] getLocalSchemaNames(String[] conversionNames) {
		String[] localElementNames = new String[conversionNames.length];
		for (int i = 0; i < conversionNames.length; i++) {
			localElementNames[i] = getLocalSchemaName(conversionNames[i]);
		}
		return localElementNames;
	}

	public Set<Object> getLocalNameLabelNames() {
		return elementNameLabels.keySet();
	}
}
