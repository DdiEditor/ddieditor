package org.ddialliance.ddieditor.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.ddialliance.ddi_3_0.xml.xmlbeans.conceptualcomponent.ConceptDocument;
import org.ddialliance.ddi_3_0.xml.xmlbeans.conceptualcomponent.ConceptGroupDocument;
import org.ddialliance.ddi_3_0.xml.xmlbeans.conceptualcomponent.ConceptSchemeDocument;
import org.ddialliance.ddi_3_0.xml.xmlbeans.datacollection.DataCollectionDocument;
import org.ddialliance.ddi_3_0.xml.xmlbeans.datacollection.QuestionItemDocument;
import org.ddialliance.ddi_3_0.xml.xmlbeans.datacollection.QuestionSchemeDocument;
import org.ddialliance.ddi_3_0.xml.xmlbeans.logicalproduct.CategorySchemeDocument;
import org.ddialliance.ddi_3_0.xml.xmlbeans.logicalproduct.CodeSchemeDocument;
import org.ddialliance.ddieditor.model.conceptual.ConceptualElement;
import org.ddialliance.ddieditor.model.conceptual.ConceptualType;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectListDocument;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;
import org.ddialliance.ddieditor.model.namespace.ddi3.Ddi3NamespaceHelper;
import org.ddialliance.ddieditor.persistenceaccess.ParamatizedXquery;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceManager;
import org.ddialliance.ddieditor.persistenceaccess.XQueryInsertKeyword;
import org.ddialliance.ddieditor.persistenceaccess.maintainablelabel.MaintainableLabelQuery;
import org.ddialliance.ddieditor.persistenceaccess.maintainablelabel.MaintainableLabelQueryResult;
import org.ddialliance.ddieditor.persistenceaccess.maintainablelabel.MaintainableLabelUpdateElement;
import org.ddialliance.ddieditor.util.DdiEditorRefUtil;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.log.Log;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;
import org.ddialliance.ddiftp.util.xml.XmlBeansUtil;
import org.perf4j.aop.Profiled;

/**
 * Defines accessors for the contents of a DDI document with the focus on
 * serving XmlObjects and LightXmlObjects
 */
public class DdiManager {
	private static Log log = LogFactory
			.getLog(LogType.SYSTEM, DdiManager.class);
	public static final String FUNCTION_NS_PREFIX = "ddieditor";
	public static String FUNCTION_NS = "http://ddialliance.org/ddieditor/ns";
	public static final String FUNCTION_NS_DECLARATION = "declare namespace "
			+ FUNCTION_NS_PREFIX + "= \"" + FUNCTION_NS + "\";";

	public static final String LIGHTXMLBEANS_CHILDCHILDELEMENT_PARENTID_PARENTCHILD = "lightxmlbeans_+++";
	public static final String LIGHTXMLBEANS_CHILDCHILDELEMENT_NO_PARENTID_PARENTCHILD = "lightxmlbeans_+-+";

	public static final String LIGHTXMLBEANS_NO_CHILDCHILDELEMENT_PARENTID_PARENTCHILD = "lightxmlbeans_-++";
	public static final String LIGHTXMLBEANS_NO_CHILDCHILDELEMENT_NO_PARENTID_PARENTCHILD = "lightxmlbeans_--+";

	public static final String LIGHTXMLBEANS_CHILDCHILDELEMENT_PARENTID_NO_PARENTCHILD = "lightxmlbeans_++-";
	public static final String LIGHTXMLBEANS_CHILDCHILDELEMENT_NO_PARENTID_NO_PARENTCHILD = "lightxmlbeans_+--";

	public static final String LIGHTXMLBEANS_NO_CHILDCHILDELEMENT_PARENTID_NO_PARENTCHILD = "lightxmlbeans_-+-";
	public static final String LIGHTXMLBEANS_NO_CHILDCHILDELEMENT_NO_PARENTID_NO_PARENTCHILD = "lightxmlbeans_---";

	public static final String QUERY_ELEMENT = "query-element";
	public static final String QUERY_ELEMENT_NO_PARENT = "query-element_-";

	private Ddi3NamespaceHelper ddi3NamespaceHelper;
	private static DdiManager instance;

	private DdiManager() {
	}

	public static synchronized DdiManager getInstance() {
		if (instance == null) {
			log.info("Initializing DDIManager");
			instance = new DdiManager();
			// initialize the ddi3 name space generator
			try {
				instance.ddi3NamespaceHelper = new Ddi3NamespaceHelper();
			} catch (Exception e) {
				new DDIFtpException("Error on genrating namespace by elements",
						e);
				e.printStackTrace();
			}
		}
		return instance;
	}

	/**
	 * Set the current working document
	 * 
	 * @param docName
	 *            of working document
	 * @throws DDIFtpException
	 */
	public void setWorkingDocument(String docName) throws DDIFtpException {
		PersistenceManager.getInstance().setWorkingResource(docName);
	}

	public Ddi3NamespaceHelper getDdi3NamespaceHelper() {
		return this.ddi3NamespaceHelper;
	}

	/**
	 * Generates and executes a XQuery for light xml beans objects
	 * 
	 * @param id
	 *            id of element
	 * @param version
	 *            version of element
	 * @param parentId
	 *            id of parent element
	 * @param parentVersion
	 *            version of parent element
	 * 
	 * @param rootElement
	 *            containing requested child elements
	 * @param parentChildElement
	 *            root child element
	 * @param childChildElement
	 *            child of child element
	 * @param labelElement
	 *            label element
	 * @return Light xml object list
	 * @throws DDIFtpException
	 */
	@Profiled(tag = "queryLightXmlBeans")
	protected LightXmlObjectListDocument queryLightXmlBeans(String id,
			String version, String parentId, String parentVersion,
			String rootElement, String parentChildElement,
			String childChildElement, String labelElement)
			throws DDIFtpException {
		ParamatizedXquery query = xQueryLightXmlBeans(parentId,
				childChildElement, parentChildElement);

		// functions
		int i = 1;
		if (childChildElement != null) {
			query
					.setObject(
							i++,
							ddi3NamespaceHelper
									.addFullyQualifiedNamespaceDeclarationToElements(childChildElement));
			query
					.setObject(
							i++,
							ddi3NamespaceHelper
									.addFullyQualifiedNamespaceDeclarationToElements(labelElement));
		} else {
			query
					.setObject(
							i++,
							ddi3NamespaceHelper
									.addFullyQualifiedNamespaceDeclarationToElements(labelElement));
		}

		if (parentId != null && !parentId.equals("")) {
			query.setObject(i++, PersistenceManager.getInstance()
					.getResourcePath());
			query
					.setObject(
							i++,
							ddi3NamespaceHelper
									.addFullyQualifiedNamespaceDeclarationToElements(rootElement));
			query.setString(i++, parentId);
			if (parentVersion != null && !parentVersion.equals("")) {
				query.setObject(i++, " and $x/@version ='" + parentVersion
						+ "' ");
			} else {
				query.setObject(i++, "");
			}
		}

		// query
		if (parentId == null || parentId.equals("")) {
			query.setObject(i++, PersistenceManager.getInstance()
					.getResourcePath());
			query.setObject(i++, ddi3NamespaceHelper
					.addFullyQualifiedNamespaceDeclarationToElements("//"
							+ rootElement));
		}

		if (parentChildElement != null) {
			query
					.setObject(
							i++,
							ddi3NamespaceHelper
									.addFullyQualifiedNamespaceDeclarationToElements(parentChildElement));

			// id and version parameters on parent child
			StringBuilder whereClause = new StringBuilder();
			boolean idExist = false;
			if (id != null && !id.equals("")) {
				idExist = !idExist;
			}
			boolean versionExist = false;
			if (version != null && !version.equals("")) {
				versionExist = !versionExist;
			}
			if (idExist || versionExist) {
				whereClause.append("where ");
			}
			if (idExist) {
				whereClause.append(" $y/@id = '");
				whereClause.append(id);
				whereClause.append("'");
			}
			if (idExist && versionExist) {
				whereClause.append(" and ");
			}
			if (versionExist) {
				whereClause.append(" $y/@version = '");
				whereClause.append(version);
				whereClause.append("'");
			}
			query.setObject(i++, whereClause.toString());
			query.setObject(i++, parentChildElement);
		} else {
			query.setObject(i++, rootElement);
		}

		List<String> search = PersistenceManager.getInstance().query(
				query.getParamatizedQuery());
		LightXmlObjectListDocument lightXmlObjectListDocument = null;
		if (!search.isEmpty()) {
			try {
				lightXmlObjectListDocument = LightXmlObjectListDocument.Factory
						.parse(search.get(0));
			} catch (XmlException e) {
				throw new DDIFtpException("Error on pasing light xml objects: "
						+ search.get(0));
			}
		}
		return lightXmlObjectListDocument;
	}

	@Profiled(tag = "xQueryLightXmlBeans")
	private ParamatizedXquery xQueryLightXmlBeans(String parentId,
			String childChildElement, String parentChildElement)
			throws DDIFtpException {
		if (parentId != null && parentId.equals("")) {
			parentId = null;
		}

		String xQueryName = null;
		if (childChildElement != null && parentId != null
				&& parentChildElement != null) {
			xQueryName = LIGHTXMLBEANS_CHILDCHILDELEMENT_PARENTID_PARENTCHILD;
		} else if (childChildElement == null && parentId != null
				&& parentChildElement != null) {
			xQueryName = LIGHTXMLBEANS_NO_CHILDCHILDELEMENT_PARENTID_PARENTCHILD;
		} else if (childChildElement == null && parentId == null
				&& parentChildElement != null) {
			xQueryName = LIGHTXMLBEANS_NO_CHILDCHILDELEMENT_NO_PARENTID_PARENTCHILD;
		} else if (childChildElement != null && parentId == null
				&& parentChildElement != null) {
			xQueryName = LIGHTXMLBEANS_CHILDCHILDELEMENT_NO_PARENTID_PARENTCHILD;
		} else if (childChildElement != null && parentId != null
				&& parentChildElement == null) {
			xQueryName = LIGHTXMLBEANS_CHILDCHILDELEMENT_PARENTID_NO_PARENTCHILD;
		} else if (childChildElement == null && parentId != null
				&& parentChildElement == null) {
			xQueryName = LIGHTXMLBEANS_NO_CHILDCHILDELEMENT_PARENTID_NO_PARENTCHILD;
		} else if (childChildElement == null && parentId == null
				&& parentChildElement == null) {
			xQueryName = LIGHTXMLBEANS_NO_CHILDCHILDELEMENT_NO_PARENTID_NO_PARENTCHILD;
		} else if (childChildElement != null && parentId == null
				&& parentChildElement == null) {
			xQueryName = LIGHTXMLBEANS_CHILDCHILDELEMENT_NO_PARENTID_NO_PARENTCHILD;
		}
		ParamatizedXquery xQuery = PersistenceManager.getInstance()
				.getParamatizedQuery(xQueryName);
		if (xQuery != null) {
			return xQuery;
		} else {

			XQuery query = new XQuery();
			query.namespaceDeclaration.append(FUNCTION_NS_DECLARATION);

			// functions
			if (childChildElement != null) {
				query.function.append("declare function ");
				query.function.append(FUNCTION_NS_PREFIX);
				query.function
						.append(":label_lang($element) {  for $z in $element?");
				// ddi3NamespaceGenerator.addFullyQualifiedNamespaceDeclarationToElements(childChildElement)
				query.function
						.append(" return if($z/@xml:lang/string()=\"\") then <Label>{ddieditor:label_text($z)}</Label> else <Label lang=\"{$z/@xml:lang/string()}\">{ddieditor:label_text($z)}</Label> }; declare function ");
				query.function.append(FUNCTION_NS_PREFIX);
				query.function
						.append(":label_text($element) { for $q in $element?");
				// ddi3NamespaceGenerator.addFullyQualifiedNamespaceDeclarationToElements(labelElement)
				query.function.append(" return $q/text() }; ");
			} else {
				query.function
						.append("declare function ddieditor:label_lang($element) { for $z in $element?");
				// ddi3NamespaceGenerator.addFullyQualifiedNamespaceDeclarationToElements(labelElement)
				query.function
						.append(" return if($z/@xml:lang/string()=\"\") then <Label>{$z/text()}</Label> else <Label lang=\"{$z/@xml:lang/string()}\">{$z/text()}</Label>}; ");
			}

			if (parentId != null) {
				// add filter for parent
				query.function.append("declare function ");
				query.function.append(FUNCTION_NS_PREFIX);
				query.function.append(":root_by_id() { for $x in ?/?");
				// PersistenceManager.getInstance().getResourcePath());
				// ddi3NamespaceGenerator.addFullyQualifiedNamespaceDeclarationToElements(rootElement));
				query.function.append(" where $x/@id = ?? return $x}; ");
				// parentId
				// if (parentVersion != null && !parentVersion.equals("")) {
				// " and $x/@ersion ='"+parentVersion+"' "
				// }
			}

			// query
			query.query
					.append("<dl:LightXmlObjectList xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"ddieditor-lightobject ddieditor-lightxmlobject.xsd\" xmlns:dl=\"ddieditor-lightobject\">{");

			if (parentId == null) {
				query.query.append(" for $x in ??");
				// PersistenceManager.getInstance().getResourcePath());
				// ddi3NamespaceGenerator.addFullyQualifiedNamespaceDeclarationToElements(rootElement));
			} else {
				query.query.append(" for $x in ");
				query.query.append(FUNCTION_NS_PREFIX);
				query.query.append(":root_by_id()");
			}

			if (parentChildElement != null) {
				query.query.append(" for $y in $x? ?");
				// ddi3NamespaceGenerator.addFullyQualifiedNamespaceDeclarationToElements(parentChildElement));
				// where $y/@id = 'qi_1' and $y/@version = '3.0'
				query.query
						.append(" return <LightXmlObject element=\"?\" id=\"{$y/@id/string()}\" version=\"{$y/@version/string()}\" parentId=\"{$x/@id/string()}\" parentVersion=\"{$x/@version/string()}\">{ddieditor:label_lang($y)}</LightXmlObject>}</dl:LightXmlObjectList>");
				// parentChildElement

			} else {
				query.query
						.append(" return <LightXmlObject element=\"?\" id=\"{$x/@id/string()}\" version=\"{$x/@version/string()}\" parentId=\"{$x/parent::node()/@id/string()}\" parentVersion=\"{$x/parent::node()/@version/string()}\">{ddieditor:label_lang($x)}</LightXmlObject>}</dl:LightXmlObjectList>");
				// rootElement
			}

			// construct paramatized query and store
			xQuery = new ParamatizedXquery(query.getFullQueryString());
			PersistenceManager.getInstance().setParamatizedQuery(xQueryName,
					xQuery);
			return xQuery;
		}
	}

	/**
	 * Generates and executes a XQuery for DDI elements
	 * 
	 * @param id
	 *            id of element
	 * @param version
	 *            version of element
	 * @param elementType
	 *            local name of DDI element
	 * @param parentId
	 *            id of parent element
	 * @param parentVersion
	 *            version of parent element
	 * @param parentElementType
	 *            local name of DDI parent element
	 * @return xml result as string
	 * @throws DDIFtpException
	 */
	protected String queryElement(String id, String version,
			String elementType, String parentId, String parentVersion,
			String parentElementType) throws DDIFtpException {
		List<String> result = PersistenceManager.getInstance().query(
				getQueryElementString(id, version, elementType, parentId,
						parentVersion, parentElementType));
		if (!result.isEmpty()) {
			return result.get(0);
		} else
			return "";
	}

	/**
	 * Generates a XQuery string for DDI elements
	 * 
	 * @param id
	 *            id of element
	 * @param version
	 *            version of element
	 * @param elementType
	 *            local name of DDI element
	 * @param parentId
	 *            id of parent element
	 * @param parentVersion
	 *            version of parent element
	 * @param parentElementType
	 *            local name of DDI parent element
	 * @return xml result as string
	 * @throws DDIFtpException
	 */
	protected String getQueryElementString(String id, String version,
			String elementType, String parentId, String parentVersion,
			String parentElementType) throws DDIFtpException {
		ParamatizedXquery xQuery = xQueryElement(id, version, elementType,
				parentId, parentVersion, parentElementType);
		int i = 1;
		StringBuilder whereClause = new StringBuilder(" where");

		xQuery.setObject(i++, PersistenceManager.getInstance()
				.getResourcePath());

		if (parentElementType != null) {
			xQuery.setObject(i++, getDdi3NamespaceHelper()
					.addFullyQualifiedNamespaceDeclarationToElements(
							parentElementType));
			xQuery.setObject(i++, getDdi3NamespaceHelper()
					.addFullyQualifiedNamespaceDeclarationToElements(
							elementType));
			// parent
			if (parentId != null && !parentId.equals("")) {
				whereClause.append(" $element/@id = '");
				whereClause.append(parentId);
				whereClause.append("'");
			}
			if (parentVersion != null && !parentVersion.equals("")) {
				if (whereClause.length() > 6) {
					whereClause.append(" and");
				}
				whereClause.append(" $element/@version = '");
				whereClause.append(parentVersion);
				whereClause.append("'");
			} else {
				if (whereClause.length() > 6) {
					whereClause.append(" and");
				}
				whereClause.append(" empty($element/@version)");
			}

			// child
			if (id != null && !id.equals("")) {
				if (whereClause.length() > 6) {
					whereClause.append(" and");
				}
				whereClause.append(" $child/@id = '");
				whereClause.append(id);
				whereClause.append("'");
			}
			if (version != null && !version.equals("")) {
				if (whereClause.length() > 6) {
					whereClause.append(" and");
				}
				whereClause.append(" $child/@version = '");
				whereClause.append(version);
				whereClause.append("'");
			} else {
				if (whereClause.length() > 6) {
					whereClause.append(" and");
				}
				whereClause.append(" empty($child/@version)");
			}
		} else {
			xQuery.setObject(i++, getDdi3NamespaceHelper()
					.addFullyQualifiedNamespaceDeclarationToElements(
							elementType));
			if (id != null && !id.equals("")) {
				whereClause.append(" $element/@id = '");
				whereClause.append(id);
				whereClause.append("'");
			}
			if (version != null && !version.equals("")) {
				if (whereClause.length() > 6) {
					whereClause.append(" and");
				}
				whereClause.append(" $element/@version = '");
				whereClause.append(version);
				whereClause.append("'");
			} else {
				if (whereClause.length() > 6) {
					whereClause.append(" and");
				}
				whereClause.append(" empty($element/@version)");
			}
		}

		// id -version -parentId -parentVersion
		if (whereClause.length() > 6) {
			xQuery.setObject(i, whereClause.toString());
		} else {
			if (whereClause.length() > 0) {
				xQuery.setObject(i, "");
			}
		}

		return xQuery.getParamatizedQuery();
	}

	private ParamatizedXquery xQueryElement(String id, String version,
			String elementType, String parentId, String parentVersion,
			String parentElementType) throws DDIFtpException {
		String xQueryName;
		if (parentElementType == null) {
			xQueryName = QUERY_ELEMENT;
		} else {
			xQueryName = QUERY_ELEMENT_NO_PARENT;
		}
		ParamatizedXquery xQuery = PersistenceManager.getInstance()
				.getParamatizedQuery(xQueryName);

		if (xQuery != null) {
			return xQuery;
		} else {
			StringBuilder query = new StringBuilder();
			query.append("for $element in ?/? ");
			if (parentElementType != null) {
				// query.append("for $child in $element/? ? return $child");
				query.append("for $child in "
						+ PersistenceManager.getInstance().getResourcePath()
						+ "/? ? return $child");
			} else {
				query.append("? return $element");
			}

			xQuery = new ParamatizedXquery(query.toString());
			PersistenceManager.getInstance().setParamatizedQuery(xQueryName,
					xQuery);
			return xQuery;
		}
	}

	/**
	 * Create an element
	 * 
	 * @param xmlObject
	 *            element to create of the xml object document type
	 * @param parentId
	 *            parent id
	 * @param parentVersion
	 *            version of parent
	 * @param parentElementType
	 *            type of parent element
	 * @throws Exception
	 */
	@Profiled(tag = "createElement")
	public void createElement(XmlObject xmlObject, String parentId,
			String parentVersion, String parentElementType)
			throws DDIFtpException {
		XmlBeansUtil.instanceOfXmlBeanDocument(xmlObject, new Throwable());
		LightXmlObjectListDocument lightXmlObjectList = null;

		// concept
		// check for concept group in concept scheme
		// try {
		// lightXmlObjectList = getConceptGroupsLight(null, null, parentId,
		// parentVersion);
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// System.out.println(lightXmlObjectList.getLightXmlObjectList().sizeOfLightXmlObjectArray());

		// insert xml object
		PersistenceManager.getInstance().insert(
				getDdi3NamespaceHelper().substitutePrefixesFromElements(
						xmlObject.xmlText()),
				XQueryInsertKeyword.INTO,
				xQueryCrudPosition(parentId, parentVersion, parentElementType,
						null, null, null));
	}

	/**
	 * Update an element
	 * 
	 * @param xmlObject
	 *            element to create of the xml object document type
	 * @param oldId
	 *            id of element to update
	 * @param oldVersion
	 *            version of element to update
	 * @throws Exception
	 */
	@Profiled(tag = "updateElement")
	public void updateElement(XmlObject xmlObject, String oldId,
			String oldVersion) throws DDIFtpException {
		String className = xmlObject.getClass().getName();
		int index = -1;
		index = className.lastIndexOf('.');
		className = className.substring(index + 1);
		index = className.lastIndexOf("Document");
		className = className.substring(0, index);

		// position
		XQuery position = xQueryCrudPosition(oldId, oldVersion, className,
				null, null, null);

		// query
		StringBuilder query = new StringBuilder();
		query.append(position.function.toString());
		query.append("replace node ");
		query.append(position.query.toString());
		query.append(" with ");
		query.append(xmlObject.xmlText());
		// query.append(substitutePrefixesFromElements(xmlObject.xmlText()));
		PersistenceManager.getInstance().updateQuery(query.toString());
	}

	/**
	 * Delete an element
	 * 
	 * @param xmlObject
	 *            element to create of the xml object document type
	 * @param parentId
	 *            parent id
	 * @param parentVersion
	 *            version of parent element
	 * @param parentElementType
	 *            type of parent element
	 * @throws Exception
	 */
	@Profiled(tag = "deleteElement")
	public void deleteElement(XmlObject xmlObject, String parentId,
			String parentVersion, String parentElementType)
			throws DDIFtpException {
		XmlObject xmlObjectType = XmlBeansUtil.getXmlObjectTypeFromXmlDocument(
				xmlObject, new Throwable());

		// position
		XQuery position = null;
		try {
			position = xQueryCrudPosition((String) DdiEditorRefUtil
					.invokeMethod(xmlObjectType, "getId", false, null),
					(String) DdiEditorRefUtil.invokeMethod(xmlObjectType,
							"getVersion", false, null), xmlObjectType
							.getDomNode().getLocalName(), parentId,
					parentVersion, parentElementType);
		} catch (Exception e) {
			throw new DDIFtpException("Error getting id and version", e);
		}

		// query
		StringBuilder query = new StringBuilder();
		query.append(position.function.toString());
		query.append("delete node");
		query.append(position.query.toString());
		PersistenceManager.getInstance().updateQuery(query.toString());
	}

	@Profiled(tag = "xQueryCrudPosition")
	private XQuery xQueryCrudPosition(String id, String version,
			String elementType, String parentId, String parentVersion,
			String parentElementType) throws DDIFtpException {
		XQuery query = new XQuery();
		// parent not defined
		if (parentElementType == null || parentId == null) {
			query.query.append(" for $element in ");
			query.query.append(PersistenceManager.getInstance()
					.getResourcePath());
			query.query.append("/");
			query.query
					.append(ddi3NamespaceHelper
							.addFullyQualifiedNamespaceDeclarationToElements(elementType));
			query.query.append(" where $element/@id = '");
			query.query.append(id);
			query.query.append("'");
			if (version != null && !version.equals("")) {
				query.query.append(" and $element/@version = '");
				query.query.append(parentVersion);
				query.query.append("'");
			} else {
				query.query.append(" and empty($element/@version)");
			}
			query.query.append(" return $element");
		}
		// use parent
		else {
			query.function.append(FUNCTION_NS_DECLARATION);

			query.function
					.append(" declare function ddieditor:child_search($parent, $id) {");
			query.function.append(" for $child in $parent/");
			query.function
					.append(ddi3NamespaceHelper
							.addFullyQualifiedNamespaceDeclarationToElements(elementType));
			query.function.append(" where $child/@id = $id");
			if (version != null && !version.equals("")) {
				query.function.append(" and $child/@version = '");
				query.function.append(version);
				query.function.append("'");
			} else {
				query.function.append(" and empty($child/@version)");
			}
			query.function.append(" return $child");
			query.function.append("};");

			query.query.append(" for $element in ");
			query.query.append(PersistenceManager.getInstance()
					.getResourcePath());
			query.query.append("/");
			query.query
					.append(ddi3NamespaceHelper
							.addFullyQualifiedNamespaceDeclarationToElements(parentElementType));
			query.query.append(" where $element/@id = '");
			query.query.append(parentId);
			query.query.append("'");
			if (parentVersion != null && !parentVersion.equals("")) {
				query.query.append(" and $element/@version = '");
				query.query.append(parentVersion);
				query.query.append("'");
			} else {
				query.query.append(" and empty($element/@version)");
			}
			query.query.append(" return ddieditor:child_search($element, '");
			query.query.append(id);
			query.query.append("')");
		}
		return query;
	}

	public MaintainableLabelQueryResult queryScheme(
			MaintainableLabelQuery schemeQuery) throws DDIFtpException {
		try {
			return PersistenceManager.getInstance().getPersistenceStorage()
					.queryMaintainableLabel(schemeQuery);
		} catch (Exception e) {
			throw new DDIFtpException("Error querying persistent storage", e);
		}
	}

	public void updateMaintainableLabel(
			MaintainableLabelQueryResult schemeQueryResult,
			List<MaintainableLabelUpdateElement> elements)
			throws DDIFtpException {
		String queryString = schemeQueryResult.getQuery();
		String updatePosition = "";
		String nodeValue = "";
		for (MaintainableLabelUpdateElement element : elements) {
			nodeValue = getDdi3NamespaceHelper()
					.substitutePrefixesFromElements(element.getValue());
			updatePosition = maintainableLabelUpdatePosition(element,
					schemeQueryResult);

			// delete
			if (element.getCrudValue() < 0) {
				PersistenceManager.getInstance().delete(
						queryString + updatePosition);
			}
			// replace
			else if (element.getCrudValue() > 0) {
				PersistenceManager.getInstance().updateNode(
						queryString + updatePosition, nodeValue);
			}
			// new
			else if (element.getCrudValue() == 0) {
				XQueryInsertKeyword insertKeyword = XQueryInsertKeyword.AFTER;
				if (updatePosition.equals("")) {
					insertKeyword = XQueryInsertKeyword.AS_FIRST_NODE;
				}
				PersistenceManager.getInstance().insert(nodeValue,
						insertKeyword, queryString + updatePosition);
			}

			// not specified
			if (element.getCrudValue() == null) {
				throw new DDIFtpException("Update value not specified for: "
						+ element, new Throwable());
			}
		}
	}

	private String maintainableLabelUpdatePosition(
			MaintainableLabelUpdateElement element,
			MaintainableLabelQueryResult mLqueryResult) throws DDIFtpException {
		int size = mLqueryResult.getResult().get(element.getLocalName()).size();

		// guard
		if ((size == 0 && element.getCrudValue() > 0)
				|| (size == 0 && element.getCrudValue() < 0)) {
			String errorLabel = "of element: '"
					+ getDdi3NamespaceHelper().getLocalSchemaName(
							element.getLocalName()) + "["
					+ element.getCrudValue()
					+ "]' not posible as element does not exist";
			throw new DDIFtpException(element.getCrudValue() > 0 ? "Update "
					+ errorLabel : "Deletion " + errorLabel, new Throwable());
		}

		// implement change into query result
		// update
		if (element.getCrudValue() > 0) {
			mLqueryResult.getResult().get(element.getLocalName()).set(
					element.getCrudValue(), element.getValue());
		}
		// delete
		else if (element.getCrudValue() < 0) {
			mLqueryResult.getResult().get(element.getLocalName()).remove(
					(element.getCrudValue() * -1) - 1);
		}
		// new
		else if (element.getCrudValue() == 0) {
			mLqueryResult.getResult().get(element.getLocalName()).addLast(
					element.getValue());
		}

		// compute insert position
		StringBuilder positionQuery = new StringBuilder();
		positionQuery.append("//");

		// update
		if (element.getCrudValue() > 0) {
			positionQuery.append(element.getLocalName());
			positionQuery.append("[");
			// update value
			positionQuery.append(element.getCrudValue());
			positionQuery.append("]");
		}
		// new
		if (element.getCrudValue() == 0) {
			if (size > 0) {
				positionQuery.append(element.getLocalName());
				positionQuery.append("[");
				positionQuery.append(size);
				positionQuery.append("]");
			}
		}
		// delete
		if (element.getCrudValue() < 0) {
			positionQuery.append(element.getLocalName());
			positionQuery.append("[");
			// update value
			positionQuery.append(element.getCrudValue() * -1);
			positionQuery.append("]");
		}
		// marked for deletion
		// } else if (privious != -1) {
		// positionQuery.append(elementNames[privious]);
		// }
		return getDdi3NamespaceHelper()
				.addFullyQualifiedNamespaceDeclarationToElements(
						positionQuery.toString());
	}

	//
	// ui overview
	//
	public List<ConceptualElement> getConceptualOverview() throws Exception {
		List<ConceptualElement> result = new ArrayList<ConceptualElement>();

		// study
		LightXmlObjectListDocument lightDoc = getStudyUnitLight(null, null,
				null, null);
		for (LightXmlObjectType lightElement : lightDoc.getLightXmlObjectList()
				.getLightXmlObjectList()) {
			result
					.add(new ConceptualElement(ConceptualType.STUDY,
							lightElement));
		}

		// logic
		// - universe
		// TODO

		// - concepts
		lightDoc = getConceptSchemeLight(null, null, null, null);
		for (LightXmlObjectType lightElement : lightDoc.getLightXmlObjectList()
				.getLightXmlObjectList()) {
			result.add(new ConceptualElement(ConceptualType.LOGIC_concepts,
					lightElement));
		}

		// - questions
		lightDoc = getQuestionSchemesLight(null, null, null, null);
		for (LightXmlObjectType lightElement : lightDoc.getLightXmlObjectList()
				.getLightXmlObjectList()) {
			result.add(new ConceptualElement(ConceptualType.LOGIC_questions,
					lightElement));
		}

		// - instumentation
		// TODO

		return result;
	}

	//
	// study unit
	//
	public LightXmlObjectListDocument getStudyUnitLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		LightXmlObjectListDocument lightXmlObjectListDocument = queryLightXmlBeans(
				id, version, parentId, parentVersion, "Group",
				"studyunit__StudyUnit", null, "reusable__Name");
		if (lightXmlObjectListDocument.getLightXmlObjectList()
				.getLightXmlObjectList().isEmpty()) {
			lightXmlObjectListDocument = queryLightXmlBeans(id, version,
					parentId, parentVersion, "*", "studyunit__StudyUnit", null,
					"reusable__Name");
		}
		return lightXmlObjectListDocument;
	}

	public MaintainableLabelQueryResult getStudyLabel(String id,
			String version, String parentId, String parentVersion)
			throws DDIFtpException {
		MaintainableLabelQuery schemeQuery = new MaintainableLabelQuery();
		schemeQuery
				.setQuery(getQueryElementString(id, version,
						"studyunit__StudyUnit", parentId, parentVersion,
						"DDIInstance"));

		// # [Reference] (r:Citation)
		// # [Reference] (Abstract) - max. unbounded
		// # [Reference] (r:UniverseReference) - max. unbounded
		// # [Reference] (r:SeriesStatement) - min. 0
		// # [Reference] (r:FundingInformation) - min. 0 - max. unbounded
		// # [Reference] (Purpose) - max. unbounded
		// # [Reference] (r:Coverage) - min. 0
		// # [Reference] (r:AnalysisUnit) - min. 0 - max. unbounded
		// # [Reference] (KindOfData) - min. 0 - max. unbounded
		// # [Reference] (r:OtherMaterial) - min. 0 - max. unbounded

		schemeQuery.setElementNames(new String[] { "reusable__Name", "Citation", "studyunit__Abstract",
				"reusable__UniverseReference", "SeriesStatement", "FundingInformation",
				"studyunit__Purpose", "Coverage", "AnalysisUnit", "KindOfData",
				"OtherMaterial" });
		schemeQuery.setMaintainableTarget("studyunit__StudyUnit");
		schemeQuery.setStopElementNames(new String[] { "ConceptualComponent",
				"DataCollection", "logicalproduct__LogicalProduct",
				"physicaldataproduct__PhysicalDataProduct", "PhysicalInstance",
				"Archive", "DDIProfile", "studyunit__DDIProfileReference" });

		MaintainableLabelQueryResult result = queryScheme(schemeQuery);
		if (result.getId() == null) {
			schemeQuery.setQuery(getQueryElementString(id, version,
					"studyunit__StudyUnit", parentId, parentVersion, "Group"));
			result = queryScheme(schemeQuery);
		}
		if (result.getId() == null) {
			schemeQuery.setQuery(getQueryElementString(id, version,
					"studyunit__StudyUnit", parentId, parentVersion, null));
			result = queryScheme(schemeQuery);
		}
		return result;
	}

	//
	// conceptual components
	//	
	public LightXmlObjectListDocument getConceptSchemeLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		LightXmlObjectListDocument lightXmlObjectListDocument = queryLightXmlBeans(
				id, version, parentId, parentVersion, "ConceptualComponent",
				"ConceptScheme", null, "reusable__Label");
		if (lightXmlObjectListDocument.getLightXmlObjectList()
				.getLightXmlObjectList().isEmpty()) {
			lightXmlObjectListDocument = queryLightXmlBeans(id, version,
					parentId, parentVersion, "//", "ConceptScheme", null,
					"reusable__Label");
		}
		return lightXmlObjectListDocument;
	}

	@Profiled(tag = "getConceptLight")
	public LightXmlObjectListDocument getConceptsLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		// ConceptScheme/Concept/Label
		return queryLightXmlBeans(id, version, parentId, parentVersion,
				"ConceptScheme", "Concept", null, "reusable__Label");
	}

	@Profiled(tag = "getConceptScheme")
	public ConceptSchemeDocument getConceptScheme(String id, String version,
			String parentId, String parentVersion) throws Exception {
		String text = queryElement(id, version, "ConceptScheme", parentId,
				parentVersion, "ConceptualComponent");
		return (text == "" ? null : ConceptSchemeDocument.Factory.parse(text));
	}

	@Profiled(tag = "getConcept")
	public ConceptDocument getConcept(String id, String version,
			String parentId, String parentVersion) throws Exception {
		String text = queryElement(id, version, "Concept", parentId,
				parentVersion, "ConceptScheme");
		return (text == "" ? null : ConceptDocument.Factory.parse(text));
	}

	public LightXmlObjectListDocument getConceptGroupsLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		// ConceptScheme/Concept/Label
		return queryLightXmlBeans(id, version, parentId, parentVersion,
				"ConceptScheme", "ConceptGroup", null, "reusable__Label");
	}

	public ConceptGroupDocument getConceptGroup(String id, String version,
			String parentId, String parentVersion) throws Exception {
		String text = queryElement(id, version, "ConceptGroup", parentId,
				parentVersion, "ConceptScheme");
		return (text == "" ? null : ConceptGroupDocument.Factory.parse(text));
	}

	//
	// data collection
	//
	@Profiled(tag = "getDataCollectionLight")
	public LightXmlObjectListDocument getDataCollectionLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		LightXmlObjectListDocument lightXmlObjectListDocument = queryLightXmlBeans(
				id, version, parentId, parentVersion, "studyunit__StudyUnit",
				"datacollection__DataCollection", null, "reusable__Name");
		if (lightXmlObjectListDocument.getLightXmlObjectList()
				.getLightXmlObjectList().isEmpty()) {
			lightXmlObjectListDocument = queryLightXmlBeans(id, version,
					parentId, parentVersion, "//",
					"datacollection__DataCollection", null, "reusable__Name");
		}
		return lightXmlObjectListDocument;
	}

	@Profiled(tag = "getDataCollection")
	public DataCollectionDocument getDataCollection(String id, String version,
			String parentId, String parentVersion) throws Exception {
		String text = queryElement(id, version, "studyunit__StudyUnit",
				parentId, parentVersion, "datacollection__DataCollection");
		if (text.equals("")) {
			text = queryElement(id, version, "datacollection__DataCollection",
					null, null, null);
		}
		return (text == "" ? null : DataCollectionDocument.Factory.parse(text));
	}

	@Profiled(tag = "getQuestionSchemesLight")
	public LightXmlObjectListDocument getQuestionSchemesLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		// QuestionScheme/Label
		LightXmlObjectListDocument lightXmlObjectListDocument = queryLightXmlBeans(
				id, version, parentId, parentVersion, "QuestionScheme", null,
				null, "reusable__Label");
		if (lightXmlObjectListDocument.getLightXmlObjectList()
				.getLightXmlObjectList().isEmpty()) {
			queryLightXmlBeans(id, version, parentId, parentVersion,
					"QuestionScheme", null, null, "reusable__Label");
		}
		return lightXmlObjectListDocument;
	}

	public MaintainableLabelQueryResult getQuestionSchemeLabel(String id,
			String version, String parentId, String parentVersion)
			throws DDIFtpException {
		MaintainableLabelQuery schemeQuery = new MaintainableLabelQuery();
		schemeQuery.setQuery(getQueryElementString(id, version,
				"QuestionScheme", parentId, parentVersion,
				"datacollection__DataCollection"));
		schemeQuery.setElementNames(new String[] { "reusable__Label" });
		schemeQuery.setMaintainableTarget("QuestionScheme");
		schemeQuery.setStopElementNames(new String[] { "QuestionItem" });

		return queryScheme(schemeQuery);
	}

	@Profiled(tag = "getQuestionScheme")
	public QuestionSchemeDocument getQuestionScheme(String id, String version,
			String parentId, String parentVersion) throws Exception {
		String text = queryElement(id, version, "QuestionScheme", parentId,
				parentVersion, "datacollection__DataCollection");
		return (text == "" ? null : QuestionSchemeDocument.Factory.parse(text));
	}

	@Profiled(tag = "getQuestionItemsLight")
	public LightXmlObjectListDocument getQuestionItemsLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		return queryLightXmlBeans(id, version, parentId, parentVersion,
				"QuestionScheme", "QuestionItem", "QuestionText",
				"LiteralText/Text");
	}

	@Profiled(tag = "getQuestionItem")
	public QuestionItemDocument getQuestionItem(String id, String version,
			String parentId, String parentVersion) throws Exception {
		String text = queryElement(id, version, "QuestionItem", parentId,
				parentVersion, "QuestionScheme");
		return (text == "" ? null : QuestionItemDocument.Factory.parse(text));
	}

	//
	// logical product
	//
	public LightXmlObjectListDocument getCodeSchemesLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		LightXmlObjectListDocument lightXmlObjectListDocument = queryLightXmlBeans(
				id, version, parentId, parentVersion,
				"logicalproduct__LogicalProduct", "CodeScheme", null,
				"reusable__Label");
		if (lightXmlObjectListDocument.getLightXmlObjectList()
				.getLightXmlObjectList().isEmpty()) {
			lightXmlObjectListDocument = queryLightXmlBeans(id, version,
					parentId, parentVersion, "//", "CodeScheme", null,
					"reusable__Label");
		}
		return lightXmlObjectListDocument;
	}

	public CodeSchemeDocument getCodeScheme(String id, String version,
			String parentId, String parentVersion) throws Exception {
		String text = queryElement(id, version, "CodeScheme", parentId,
				parentVersion, "logicalproduct__LogicalProduct");
		if (text == null) {
			queryElement(id, version, "CodeScheme", null, null, null);
		}
		return (text == "" ? null : CodeSchemeDocument.Factory.parse(text));
	}

	public LightXmlObjectListDocument getCategorySchemesLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		LightXmlObjectListDocument lightXmlObjectListDocument = queryLightXmlBeans(
				id, version, parentId, parentVersion,
				"logicalproduct__LogicalProduct", "CategoryScheme", null,
				"reusable__Label");
		if (lightXmlObjectListDocument.getLightXmlObjectList()
				.getLightXmlObjectList().isEmpty()) {
			lightXmlObjectListDocument = queryLightXmlBeans(id, version,
					parentId, parentVersion, "//", "CategoryScheme", null,
					"reusable__Label");
		}
		return lightXmlObjectListDocument;
	}

	public CategorySchemeDocument getCategoryScheme(String id, String version,
			String parentId, String parentVersion) throws Exception {
		String text = queryElement(id, version, "CategoryScheme", parentId,
				parentVersion, "logicalproduct__LogicalProduct");
		if (text == null) {
			queryElement(id, version, "CategoryScheme", null, null, null);
		}
		return (text == "" ? null : CategorySchemeDocument.Factory.parse(text));
	}
}
