package org.ddialliance.ddieditor.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.ddialliance.ddi3.xml.xmlbeans.conceptualcomponent.ConceptDocument;
import org.ddialliance.ddi3.xml.xmlbeans.conceptualcomponent.ConceptGroupDocument;
import org.ddialliance.ddi3.xml.xmlbeans.conceptualcomponent.ConceptSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.conceptualcomponent.ConceptualComponentDocument;
import org.ddialliance.ddi3.xml.xmlbeans.conceptualcomponent.UniverseDocument;
import org.ddialliance.ddi3.xml.xmlbeans.conceptualcomponent.UniverseSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.ComputationItemDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.ControlConstructSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.DataCollectionDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.IfThenElseDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.InstrumentDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.LoopDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.MultipleQuestionItemDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionConstructDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionItemDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.RepeatUntilDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.RepeatWhileDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.SequenceDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.StatementItemDocument;
import org.ddialliance.ddi3.xml.xmlbeans.logicalproduct.CategoryDocument;
import org.ddialliance.ddi3.xml.xmlbeans.logicalproduct.CategorySchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.logicalproduct.CodeSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.logicalproduct.VariableDocument;
import org.ddialliance.ddi3.xml.xmlbeans.logicalproduct.VariableSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.studyunit.StudyUnitType;
import org.ddialliance.ddieditor.model.conceptual.ConceptualElement;
import org.ddialliance.ddieditor.model.conceptual.ConceptualType;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectListDocument;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;
import org.ddialliance.ddieditor.model.namespace.ddi3.Ddi3NamespaceHelper;
import org.ddialliance.ddieditor.model.relationship.ElementDocument.Element;
import org.ddialliance.ddieditor.persistenceaccess.ParamatizedXquery;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceManager;
import org.ddialliance.ddieditor.persistenceaccess.XQueryInsertKeyword;
import org.ddialliance.ddieditor.persistenceaccess.maintainablelabel.MaintainableLabelQuery;
import org.ddialliance.ddieditor.persistenceaccess.maintainablelabel.MaintainableLabelQueryResult;
import org.ddialliance.ddieditor.persistenceaccess.maintainablelabel.MaintainableLabelUpdateElement;
import org.ddialliance.ddieditor.persistenceaccess.maintainablelabel.MaintainableLightLabelQueryResult;
import org.ddialliance.ddieditor.util.DdiEditorRefUtil;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.ReflectionUtil;
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
			query.setObject(
					i++,
					ddi3NamespaceHelper
							.addFullyQualifiedNamespaceDeclarationToElements(childChildElement));
			query.setObject(
					i++,
					ddi3NamespaceHelper
							.addFullyQualifiedNamespaceDeclarationToElements(labelElement));
		} else {
			query.setObject(
					i++,
					ddi3NamespaceHelper
							.addFullyQualifiedNamespaceDeclarationToElements(labelElement));
		}

		if (parentId != null && !parentId.equals("")) {
			query.setObject(i++, PersistenceManager.getInstance()
					.getResourcePath());
			query.setObject(
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
			query.setObject(
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
			xQuery.setObject(
					i++,
					getDdi3NamespaceHelper()
							.addFullyQualifiedNamespaceDeclarationToElements(
									parentElementType));
			xQuery.setObject(
					i++,
					getDdi3NamespaceHelper()
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
			xQuery.setObject(
					i++,
					getDdi3NamespaceHelper()
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
				query.append("for $child in $element/? ? return $child");
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

		// get last element of same type to insert
		LightXmlObjectType lastElementOfSameType = null;

		// reflect get light element list
		QName qName = xmlObject.schemaType().getDocumentElementName();
		StringBuilder operation = new StringBuilder("get");
		operation.append(qName.getLocalPart());
		operation.append("sLight");

		LightXmlObjectListDocument lightXmlObjectList = null;
		try {
			lightXmlObjectList = (LightXmlObjectListDocument) ReflectionUtil
					.invokeMethod(
							DdiManager.getInstance(),
							operation.toString(),
							false,
							new Object[] { "", "",
									parentId == null ? "" : parentId,
									parentVersion == null ? "" : parentVersion });
		} catch (Exception e) {
			throw new DDIFtpException(e);
		}

		// define last element of same type
		if (lightXmlObjectList != null
				&& !lightXmlObjectList.getLightXmlObjectList()
						.getLightXmlObjectList().isEmpty()) {
			lastElementOfSameType = lightXmlObjectList
					.getLightXmlObjectList()
					.getLightXmlObjectList()
					.get(lightXmlObjectList.getLightXmlObjectList()
							.getLightXmlObjectList().size() - 1);
		}

		XmlOptions options = new XmlOptions();
		options.setSaveAggressiveNamespaces();
		options.setSavePrettyPrint();

		// insert xml object after last element of same type
		if (lastElementOfSameType != null) {
			PersistenceManager.getInstance().insert(
					getDdi3NamespaceHelper().substitutePrefixesFromElements(
							xmlObject.xmlText(options)),
					XQueryInsertKeyword.AFTER,
					xQueryCrudPosition(
							lastElementOfSameType.getId(),
							lastElementOfSameType.getVersion(),
							getDdi3NamespaceHelper().getDuplicateConvention(
									qName), parentId, parentVersion,
							parentElementType));
			return;
		}

		// guard, last element of same type NULL
		PersistenceManager.getInstance().insert(
				getDdi3NamespaceHelper().substitutePrefixesFromElements(
						xmlObject.xmlText(options)),
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
			position = xQueryCrudPosition(
					(String) DdiEditorRefUtil.invokeMethod(xmlObjectType,
							"getId", false, null),
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
				query.query.append(version);
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

	public MaintainableLabelQueryResult queryMaintainableLabel(
			MaintainableLabelQuery schemeQuery) throws DDIFtpException {
		// conversion names to local element names
		LinkedHashMap<String, String> conversionToLocalName = new LinkedHashMap<String, String>();
		for (int i = 0; i < schemeQuery.getElementConversionNames().length; i++) {
			conversionToLocalName.put(
					getDdi3NamespaceHelper().getLocalSchemaName(
							schemeQuery.getElementConversionNames()[i]),
					schemeQuery.getElementConversionNames()[i]);
		}

		// result
		MaintainableLabelQueryResult result = new MaintainableLabelQueryResult();
		result.setLocalName(schemeQuery.getMaintainableTarget());
		result.setLocalNamesToConversionLocalNames(conversionToLocalName);
		result.setQuery(schemeQuery.getQuery());

		// prepare result list
		for (String key : conversionToLocalName.keySet()) {
			result.getResult().put(key, new LinkedList<String>());
		}

		// query
		try {
			return PersistenceManager.getInstance().getPersistenceStorage()
					.queryMaintainableLabel(schemeQuery, result);
		} catch (Exception e) {
			throw new DDIFtpException(
					"Error querying persistent storage on maintainable label",
					e);
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
			// guard
			if (element.getCrudValue() == null) {
				throw new DDIFtpException("Update value not specified for: "
						+ element, new Throwable());
			}

			if ((element.getCrudValue() > 0 || element.getCrudValue() == 0)
					&& element.getValue() == null) {
				throw new DDIFtpException(
						"Value not specified for: " + element, new Throwable());
			}

			if (element.getValue() != null) {
				nodeValue = getDdi3NamespaceHelper()
						.substitutePrefixesFromElements(element.getValue());
			}
			updatePosition = getMaintainableLabelCrudPosition(element,
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
		}
	}

	private String getMaintainableLabelCrudPosition(
			MaintainableLabelUpdateElement element,
			MaintainableLabelQueryResult result) throws DDIFtpException {
		String localName = null;
		for (Entry<String, String> entry : result
				.getLocalNamesToConversionLocalNames().entrySet()) {
			if (entry.getKey().equals(element.getLocalName())) {
				localName = entry.getKey();
			}
		}

		// guard
		if (localName == null) {
			throw new DDIFtpException("Element: '" + element.getLocalName()
					+ "[" + element.getCrudValue()
					+ "]' does not exist in result", new Throwable());
		}

		int size = 0;
		try {
			size = result.getResult().get(localName).size();
		} catch (NullPointerException e) {
			// ok
		}

		// guard
		if ((size == 0 && element.getCrudValue() > 0)
				|| (size == 0 && element.getCrudValue() < 0)) {
			String errorLabel = "of element: '" + localName + "["
					+ element.getCrudValue()
					+ "]' not posible as element does not exist";
			throw new DDIFtpException(element.getCrudValue() > 0 ? "Update "
					+ errorLabel : "Deletion " + errorLabel, new Throwable());
		}

		// compute insert position
		StringBuilder positionQuery = new StringBuilder();
		positionQuery.append("/");

		// update - delete
		if (element.getCrudValue() != 0) {
			positionQuery.append(result.getLocalNamesToConversionLocalNames()
					.get(element.getLocalName()));
			positionQuery.append("[");
			if (element.getCrudValue() < 0) {
				positionQuery.append(element.getCrudValue() * -1);
			} else {
				positionQuery.append(element.getCrudValue());
			}
			positionQuery.append("]");
		}

		// new
		if (element.getCrudValue() == 0) {
			// other element exists
			if (size > 0) {
				positionQuery.append(result
						.getLocalNamesToConversionLocalNames().get(
								element.getLocalName()));
				positionQuery.append("[");
				positionQuery.append(size);
				positionQuery.append("]");
			} else {
				// flashing new - compute previous element
				String[] localNames = new String[] {};
				String localNameNewPosition = null;
				localNames = result.getLocalNamesToConversionLocalNames()
						.keySet().toArray(localNames);

				for (int i = 0, count = 0; i < localNames.length; i++) {
					count = i;
					if (localName.equals(localNames[i])
							&& !(result.getResult().get(localNames[i])
									.isEmpty())) {
						localNameNewPosition = localNames[i];
						break;
					} else {
						count--;
						while (count > -1 || localNameNewPosition != null) {
							if (!result.getResult().get(localNames[count])
									.isEmpty()) {
								localNameNewPosition = localNames[count];
							}
							count--;
						}
						break;
					}
				}
				if (localNameNewPosition != null) {
					positionQuery.append(result
							.getLocalNamesToConversionLocalNames().get(
									localNameNewPosition));
				}
			}
		}

		// implement change into query result
		// update
		if (element.getCrudValue() > 0) {
			result.getResult().get(localName)
					.set(element.getCrudValue() - 1, element.getValue());
		}
		// delete
		else if (element.getCrudValue() < 0) {
			result.getResult().get(localName)
					.remove((element.getCrudValue() * -1) - 1);
		}
		// new
		else if (element.getCrudValue() == 0) {
			result.getResult().get(localName).addLast(element.getValue());
		}

		return getDdi3NamespaceHelper()
				.addFullyQualifiedNamespaceDeclarationToElements(
						positionQuery.toString());
	}

	public MaintainableLightLabelQueryResult queryMaintainableLightLabel(
			MaintainableLabelQuery schemeQuery) throws DDIFtpException {
		// conversion names to local element names
		LinkedHashMap<String, String> conversionToLocalName = new LinkedHashMap<String, String>();
		for (int i = 0; i < schemeQuery.getElementConversionNames().length; i++) {
			conversionToLocalName.put(
					getDdi3NamespaceHelper().getLocalSchemaName(
							schemeQuery.getElementConversionNames()[i]),
					schemeQuery.getElementConversionNames()[i]);
		}

		// result
		MaintainableLightLabelQueryResult result = new MaintainableLightLabelQueryResult(
				schemeQuery);

		// prepare result list
		for (String key : conversionToLocalName.keySet()) {
			result.getResult().put(key, new LinkedList<LightXmlObjectType>());
		}

		// query
		MaintainableLightLabelQueryResult maintainableLightLabelQueryResult;
		try {
			maintainableLightLabelQueryResult = PersistenceManager
					.getInstance().getPersistenceStorage()
					.queryMaintainableLightLabel(schemeQuery, result);
		} catch (Exception e) {
			throw new DDIFtpException(
					"Error querying persistent storage on maintainable label light",
					e);
		}

		// clean result of empty lists
		for (Iterator<Entry<String, LinkedList<LightXmlObjectType>>> iterator = maintainableLightLabelQueryResult
				.getResult().entrySet().iterator(); iterator.hasNext();) {
			Entry<String, LinkedList<LightXmlObjectType>> entry = iterator
					.next();
			if (entry.getValue().isEmpty()) {
				iterator.remove();
			}
		}
		return maintainableLightLabelQueryResult;
	}

	//
	// ui overview
	//
	public List<ConceptualElement> getConceptualOverview() throws Exception {
		List<ConceptualElement> result = new ArrayList<ConceptualElement>();

		// study
		LightXmlObjectListDocument lightDoc = getStudyUnitsLight(null, null,
				null, null);
		for (LightXmlObjectType lightElement : lightDoc.getLightXmlObjectList()
				.getLightXmlObjectList()) {
			result.add(new ConceptualElement(ConceptualType.STUDY, lightElement));
		}

		// logic
		// - universe
		lightDoc = getUniverseSchemesLight(null, null, null, null);
		for (LightXmlObjectType lightElement : lightDoc.getLightXmlObjectList()
				.getLightXmlObjectList()) {
			result.add(new ConceptualElement(ConceptualType.LOGIC_Universe,
					lightElement));
		}

		// - concepts
		lightDoc = getConceptSchemesLight(null, null, null, null);
		for (LightXmlObjectType lightElement : lightDoc.getLightXmlObjectList()
				.getLightXmlObjectList()) {
			result.add(new ConceptualElement(ConceptualType.LOGIC_concepts,
					lightElement));
		}

		// - categorie
		lightDoc = getCategorySchemesLight(null, null, null, null);
		for (LightXmlObjectType lightElement : lightDoc.getLightXmlObjectList()
				.getLightXmlObjectList()) {
			result.add(new ConceptualElement(ConceptualType.LOGIC_category,
					lightElement));
		}

		// - code
		lightDoc = getCodeSchemesLight(null, null, null, null);
		for (LightXmlObjectType lightElement : lightDoc.getLightXmlObjectList()
				.getLightXmlObjectList()) {
			result.add(new ConceptualElement(ConceptualType.LOGIC_code,
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
		lightDoc = getInstumentOverview();
		for (LightXmlObjectType lightElement : lightDoc.getLightXmlObjectList()
				.getLightXmlObjectList()) {
			result.add(new ConceptualElement(
					ConceptualType.LOGIC_instumentation, lightElement));
		}

		// - variable
		lightDoc = getVariableSchemesLight(null, null, null, null);
		for (LightXmlObjectType lightElement : lightDoc.getLightXmlObjectList()
				.getLightXmlObjectList()) {
			result.add(new ConceptualElement(ConceptualType.LOGIC_variable,
					lightElement));
		}

		return result;
	}

	public String getDDIXPathForElement(QName qName) throws Exception {
		String conversionName = DdiManager.getInstance()
				.getDdi3NamespaceHelper().getDuplicateConvention(qName);
		Element element = DdiManager.getInstance().getDdi3NamespaceHelper()
				.getElementParents(conversionName);

		return null;
	}

	//
	// import
	//
	public void importStudyUnit(
			String resource,
			org.ddialliance.ddi3.xml.xmlbeans.studyunit.StudyUnitDocument studyUnitDocument)
			throws Exception {
		DdiManager.getInstance().setWorkingDocument(resource);

		// avail study units
		LightXmlObjectListDocument studyUnits = getStudyUnitsLight(null, null,
				null, null);

		// import into existing study unit
		if (!studyUnits.getLightXmlObjectList().getLightXmlObjectList()
				.isEmpty()) {
			// set: id version agency

			// over write
			// TODO support multiple study units
			LightXmlObjectType selected = studyUnits.getLightXmlObjectList()
					.getLightXmlObjectArray(0);

			MaintainableLabelQueryResult labelQueryResult = getStudyLabel(
					selected.getId(), selected.getVersion(),
					selected.getParentId(), selected.getParentVersion());

			List<MaintainableLabelUpdateElement> updates = new ArrayList<MaintainableLabelUpdateElement>();
			StudyUnitType type = studyUnitDocument.getStudyUnit();
			XmlOptions xmlOptions = new XmlOptions();
			xmlOptions.setSavePrettyPrint();
			xmlOptions.setSaveOuter();

			// insert study level info
			LinkedList<String> existingObj = null;
			for (Entry<String, LinkedList<String>> entry : labelQueryResult
					.getResult().entrySet()) {
				log.debug(entry.getKey());

				// insert values
				Object importObj = null;
				try {
					existingObj = entry.getValue();
					importObj = ReflectionUtil.invokeMethod(type,
							"get" + entry.getKey(), false, null);
				} catch (Exception e) {
					try {
						importObj = ReflectionUtil.invokeMethod(type, "get"
								+ entry.getKey() + "List", false, null);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				// create
				if (importObj != null && existingObj.isEmpty()) {
					if (importObj instanceof List) {
						for (XmlObject xmlObject : ((List<XmlObject>) importObj)) {
							updates.add(new MaintainableLabelUpdateElement(
									xmlObject,
									MaintainableLabelUpdateElement.NEW));
						}
					} else {
						updates.add(new MaintainableLabelUpdateElement(
								(XmlObject) importObj,
								MaintainableLabelUpdateElement.NEW));
					}
				}
				// update
				else if (importObj != null && !existingObj.isEmpty()) {
					if (importObj instanceof List) {
						int count = 1;
						for (XmlObject xmlObject : ((List<XmlObject>) importObj)) {
							updates.add(new MaintainableLabelUpdateElement(
									xmlObject, count));
							count++;
						}
					} else {
						updates.add(new MaintainableLabelUpdateElement(
								(XmlObject) importObj, 1));
					}
				}
				// delete
				else if (importObj == null && !existingObj.isEmpty()) {
					if (importObj instanceof List) {
						int count = -1;
						for (XmlObject xmlObject : ((List<XmlObject>) importObj)) {
							updates.add(new MaintainableLabelUpdateElement(
									null, count));
							count--;
						}
					} else {
						updates.add(new MaintainableLabelUpdateElement(null, -1));
					}
				}
				existingObj = null;
			}
			if (log.isDebugEnabled()) {
				for (MaintainableLabelUpdateElement maintainableLabelUpdateElement : updates) {
					log.debug(maintainableLabelUpdateElement.toString());
				}
			}
			updateMaintainableLabel(labelQueryResult, updates);

			// insert conceptual component
			// create
			List<LightXmlObjectType> conceptCompLights = getConceptualComponentsLight(
					null, null, null, null).getLightXmlObjectList()
					.getLightXmlObjectList();
			if (conceptCompLights.isEmpty()) {
				ConceptualComponentDocument concDoc = ConceptualComponentDocument.Factory
						.newInstance();
				concDoc.setConceptualComponent(studyUnitDocument.getStudyUnit()
						.getConceptualComponentList().get(0));

				createElement(concDoc,
						studyUnitDocument.getStudyUnit().getId(),
						studyUnitDocument.getStudyUnit().getVersion(),
						"studyunit__StudyUnit");
			}
			UniverseSchemeDocument doc = getUniverseScheme(null, null, null,
					null);
			// create
			if (doc == null) {
				LightXmlObjectType concLight = conceptCompLights.get(0);
				UniverseSchemeDocument uniSDoc = UniverseSchemeDocument.Factory
						.newInstance();
				uniSDoc.setUniverseScheme(studyUnitDocument.getStudyUnit()
						.getConceptualComponentArray(0)
						.getUniverseSchemeArray(0));
				createElement(uniSDoc, concLight.getId(), concLight.getId(),
						concLight.getElement());
			}
			// replace
			else {
				// weed out unused universe ref

			}
			// insert archive

		} else {
			// avail ddi instance
			LightXmlObjectListDocument ddiInstances = getDdiInstanceLight(null,
					null, null, null);

			// import into existing ddi instance
			if (!ddiInstances.getLightXmlObjectList().getLightXmlObjectList()
					.isEmpty()) {
				// no study unit import all
				// insert all into ddi instance
				LightXmlObjectType lightXmlObject = ddiInstances
						.getLightXmlObjectList().getLightXmlObjectArray(0);
				createElement(studyUnitDocument, lightXmlObject.getId(),
						lightXmlObject.getVersion(),
						lightXmlObject.getElement());
			}
		}
		// TODO avail group

		// TODO avail resource package

		return;
	}

	//
	// ddi instance
	//
	public LightXmlObjectListDocument getDdiInstanceLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		// UserId as label is hack as ddi instanc does not have a label element
		LightXmlObjectListDocument lightXmlObjectListDocument = queryLightXmlBeans(
				id, version, parentId, parentVersion, "//", "DDIInstance",
				null, "UserID");

		return lightXmlObjectListDocument;
	}

	//
	// study unit
	//
	public LightXmlObjectListDocument getStudyUnitsLight(String id,
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
		MaintainableLabelQuery maintainableLabelQuery = new MaintainableLabelQuery(
				parentId, parentVersion, null);
		maintainableLabelQuery
				.setQuery(getQueryElementString(id, version,
						"studyunit__StudyUnit", parentId, parentVersion,
						"DDIInstance"));

		maintainableLabelQuery.setElementConversionNames(new String[] {
				"Citation", "studyunit__Abstract", "UniverseReference",
				"SeriesStatement", "FundingInformation", "studyunit__Purpose",
				"Coverage", "AnalysisUnit",
				// TODO
				// "AnalysisUnitsCovered",
				// left out because of potential bug on declaration in schema
				// studyunit.xsd
				// Bug notice mailed to DDI::TIC on 20090824
				"KindOfData", "OtherMaterial", "Note", "Embargo" });

		maintainableLabelQuery.setMaintainableTarget("studyunit__StudyUnit");
		maintainableLabelQuery.setStopElementNames(new String[] {
				"ConceptualComponent", "DataCollection", "LogicalProduct",
				"PhysicalDataProduct", "PhysicalInstance", "Archive",
				"DDIProfile", "DDIProfileReference" });

		MaintainableLabelQueryResult result = queryMaintainableLabel(maintainableLabelQuery);
		if (result.getId() == null) {
			maintainableLabelQuery.setQuery(getQueryElementString(id, version,
					"studyunit__StudyUnit", parentId, parentVersion, "Group"));
			result = queryMaintainableLabel(maintainableLabelQuery);
		}
		if (result.getId() == null) {
			maintainableLabelQuery.setQuery(getQueryElementString(id, version,
					"studyunit__StudyUnit", parentId, parentVersion, null));
			result = queryMaintainableLabel(maintainableLabelQuery);
		}
		return result;
	}

	//
	// conceptual components
	//
	public LightXmlObjectListDocument getConceptualComponentsLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		LightXmlObjectListDocument lightXmlObjectListDocument = queryLightXmlBeans(
				id, version, parentId, parentVersion, "studyunit__StudyUnit",
				"ConceptualComponent", null, "reusable__Label");
		if (lightXmlObjectListDocument.getLightXmlObjectList()
				.getLightXmlObjectList().isEmpty()) {
			lightXmlObjectListDocument = queryLightXmlBeans(id, version,
					parentId, parentVersion, "//", "ConceptualComponent", null,
					"reusable__Label");
		}
		return lightXmlObjectListDocument;
	}

	public LightXmlObjectListDocument getConceptSchemesLight(String id,
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

	@Profiled(tag = "getConceptScheme")
	public ConceptSchemeDocument getConceptScheme(String id, String version,
			String parentId, String parentVersion) throws Exception {
		String text = queryElement(id, version, "ConceptScheme", parentId,
				parentVersion, "ConceptualComponent");
		return (text == "" ? null : ConceptSchemeDocument.Factory.parse(text));
	}

	@Profiled(tag = "getConceptLight")
	public LightXmlObjectListDocument getConceptsLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		// ConceptScheme/Concept/Label
		return queryLightXmlBeans(id, version, parentId, parentVersion,
				"ConceptScheme", "Concept", null, "reusable__Label");
	}

	public MaintainableLabelQueryResult getConcetSchemeLabel(String id,
			String version, String parentId, String parentVersion)
			throws DDIFtpException {
		MaintainableLabelQuery maintainableLabelQuery = new MaintainableLabelQuery(
				parentId, parentVersion, null);
		maintainableLabelQuery
				.setQuery(getQueryElementString(id, version, "ConceptScheme",
						parentId, parentVersion, "ConceptualComponent"));

		maintainableLabelQuery.setElementConversionNames(new String[] {
				"reusable__Label", "Description" });

		maintainableLabelQuery.setMaintainableTarget("ConceptScheme");
		maintainableLabelQuery.setStopElementNames(new String[] { "Concept" });

		MaintainableLabelQueryResult result = queryMaintainableLabel(maintainableLabelQuery);
		if (result.getId() == null) {
			maintainableLabelQuery.setQuery(getQueryElementString(id, version,
					"ConceptScheme", parentId, parentVersion, "Group"));
			result = queryMaintainableLabel(maintainableLabelQuery);
		}
		if (result.getId() == null) {
			maintainableLabelQuery.setQuery(getQueryElementString(id, version,
					"ConceptScheme", parentId, parentVersion, null));
			result = queryMaintainableLabel(maintainableLabelQuery);
		}
		return result;
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
		// LightXmlObjectListDocument lightXmlObjectListDocument =
		// queryLightXmlBeans(
		// id, version, parentId, parentVersion,
		// "logicalproduct__LogicalProduct", "CodeScheme", null,
		// "reusable__Label");

		// QuestionScheme/Label
		LightXmlObjectListDocument lightXmlObjectListDocument = queryLightXmlBeans(
				id, version, parentId, parentVersion,
				"datacollection__DataCollection", "QuestionScheme", null,
				"reusable__Label");
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
		MaintainableLabelQuery schemeQuery = new MaintainableLabelQuery(
				parentId, parentVersion, null);
		schemeQuery.setQuery(getQueryElementString(id, version,
				"QuestionScheme", parentId, parentVersion,
				"datacollection__DataCollection"));
		schemeQuery
				.setElementConversionNames(new String[] { "reusable__Label" });
		schemeQuery.setMaintainableTarget("QuestionScheme");
		schemeQuery.setStopElementNames(new String[] { "QuestionItem" });

		return queryMaintainableLabel(schemeQuery);
	}

	@Profiled(tag = "getQuestionScheme")
	public QuestionSchemeDocument getQuestionScheme(String id, String version,
			String parentId, String parentVersion) throws Exception {
		String text = queryElement(id, version, "QuestionScheme", parentId,
				parentVersion, "datacollection__DataCollection");
		return (text == "" ? null : QuestionSchemeDocument.Factory.parse(text));
	}

	@Profiled(tag = "getMultipleQuestionItemsLight")
	public LightXmlObjectListDocument getMultipleQuestionItemsLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		// TODO Multiple Question Item has currently no Label - it should
		return queryLightXmlBeans(id, version, parentId, parentVersion,
				"QuestionScheme", "MultipleQuestionItem", null,
				"reusable__Label");
	}

	@Profiled(tag = "getMultipleQuestionItem")
	public MultipleQuestionItemDocument getMultipleQuestionItem(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		String text = queryElement(id, version, "MultipleQuestionItem",
				parentId, parentVersion, "QuestionScheme");
		return (text == "" ? null : MultipleQuestionItemDocument.Factory
				.parse(text));
	}

	@Profiled(tag = "getMultipleQuestionItemLabel")
	public MaintainableLabelQueryResult getMultipleQuestionItemLabel(String id,
			String version, String parentId, String parentVersion)
			throws DDIFtpException {
		MaintainableLabelQuery maintainableLabelQuery = new MaintainableLabelQuery(
				parentId, parentVersion, null);
		maintainableLabelQuery.setQuery(getQueryElementString(id, version,
				"MultipleQuestionItem", parentId, parentVersion,
				"datacollection__DataCollection"));

		maintainableLabelQuery.setElementConversionNames(new String[] {
				"QuestionText", "datacollection__ConceptReference",
				"SubQuestionSequence" });

		maintainableLabelQuery.setMaintainableTarget("MultipleQuestionItem");
		maintainableLabelQuery.setStopElementNames(new String[] {
				"QuestionItem", "ControlConstructScheme", "LogicalProduct" });

		MaintainableLabelQueryResult result = queryMaintainableLabel(maintainableLabelQuery);
		if (result.getId() == null) {
			maintainableLabelQuery.setQuery(getQueryElementString(id, version,
					"MultipleQuestionItem", parentId, parentVersion, "Group"));
			result = queryMaintainableLabel(maintainableLabelQuery);
		}
		if (result.getId() == null) {
			maintainableLabelQuery.setQuery(getQueryElementString(id, version,
					"MultipleQuestionItem", parentId, parentVersion, null));
			result = queryMaintainableLabel(maintainableLabelQuery);
		}
		return result;
	}

	@Profiled(tag = "getQuestionItemsLight")
	public LightXmlObjectListDocument getQuestionItemsLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		// TODO Question Item has currently no Label - it should
		return queryLightXmlBeans(id, version, parentId, parentVersion,
				"QuestionScheme", "QuestionItem", null, "reusable__Label");
	}

	@Profiled(tag = "getQuestionItem")
	public QuestionItemDocument getQuestionItem(String id, String version,
			String parentId, String parentVersion) throws Exception {
		String text = queryElement(id, version, "QuestionItem", parentId,
				parentVersion, "QuestionScheme");
		return (text == "" ? null : QuestionItemDocument.Factory.parse(text));
	}

	public LightXmlObjectListDocument getInstumentOverview() throws Exception {
		// instument
		LightXmlObjectListDocument result = getInstrumentsLight(null, null,
				null, null);

		// control construct scheme
		LightXmlObjectListDocument controlConstructs = getControlConstructSchemesLight(
				null, null, null, null);
		result.getLightXmlObjectList()
				.getLightXmlObjectList()
				.addAll(controlConstructs.getLightXmlObjectList()
						.getLightXmlObjectList());
		return result;
	}

	public MaintainableLightLabelQueryResult getInstrumentLabel(String id,
			String version, String parentId, String parentVersion)
			throws DDIFtpException {
		MaintainableLabelQuery query = new MaintainableLabelQuery(parentId,
				parentVersion, null);
		query.setQuery(getQueryElementString(id, version,
				"ControlConstructScheme", parentId, parentVersion,
				"datacollection__DataCollection"));

		String[] elements = { "ControlConstructScheme",

				// controls
				"IfThenElse", "RepeatUntil", "RepeatWhile", "Loop",

				// sequence
				"Sequence",

				"ComputationItem", "StatementItem",

				// question construct
				"QuestionConstruct" };
		query.setElementConversionNames(elements);

		query.setMaintainableTarget("ControlConstructScheme");
		query.setStopElementNames(new String[] { "LogicalProduct",
				"PhysicalDataProduct", "PhysicalInstance", "Archive",
				"DDIProfile", "DDIProfileReference" });

		MaintainableLightLabelQueryResult maLightLabelQueryResult = queryMaintainableLightLabel(query);

		// add instrument
		// LightXmlObjectListDocument lightDoc = null;
		// try {
		// lightDoc = getInstrumentsLight(id, version, parentId, parentVersion);
		// } catch (Exception e) {
		// throw new DDIFtpException(e);
		// }
		// maLightLabelQueryResult.getResult().put(
		// "Instrument",
		// new LinkedList<LightXmlObjectType>(lightDoc
		// .getLightXmlObjectList().getLightXmlObjectList()));
		return maLightLabelQueryResult;
	}

	public LightXmlObjectListDocument getInstrumentsLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		return queryLightXmlBeans(id, version, parentId, parentVersion,
				"datacollection__DataCollection", "Instrument", null,
				"reusable__Label");
	}

	public InstrumentDocument getInstrument(String id, String version,
			String parentId, String parentVersion) throws Exception {
		String text = queryElement(id, version, "Instrument", parentId,
				parentVersion, "datacollection__DataCollection");
		return (text == "" ? null : InstrumentDocument.Factory.parse(text));
	}

	public MaintainableLabelQueryResult getControlConstructSchemeLabel(
			String id, String version, String parentId, String parentVersion)
			throws DDIFtpException {
		MaintainableLabelQuery maintainableLabelQuery = new MaintainableLabelQuery(
				parentId, parentVersion, null);
		maintainableLabelQuery.setQuery(getQueryElementString(id, version,
				"ControlConstructScheme", parentId, parentVersion,
				"datacollection__DataCollection"));

		maintainableLabelQuery.setElementConversionNames(new String[] {
				"reusable__Label", "Description" });

		maintainableLabelQuery.setMaintainableTarget("ControlConstructScheme");
		maintainableLabelQuery.setStopElementNames(new String[] {
				"ComputationItem", "IfThenElse", "Loop", "QuestionConstruct",
				"RepeatUntil", "RepeatWhile", "Sequence", "StatementItem" });

		MaintainableLabelQueryResult result = queryMaintainableLabel(maintainableLabelQuery);
		if (result.getId() == null) {
			maintainableLabelQuery
					.setQuery(getQueryElementString(id, version,
							"ControlConstructScheme", parentId, parentVersion,
							"Group"));
			result = queryMaintainableLabel(maintainableLabelQuery);
		}
		if (result.getId() == null) {
			maintainableLabelQuery.setQuery(getQueryElementString(id, version,
					"ControlConstructScheme", parentId, parentVersion, null));
			result = queryMaintainableLabel(maintainableLabelQuery);
		}
		return result;
	}

	public ControlConstructSchemeDocument getControlConstructScheme(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		String text = queryElement(id, version, "ControlConstructScheme",
				parentId, parentVersion, "datacollection__DataCollection");
		return (text == "" ? null : ControlConstructSchemeDocument.Factory
				.parse(text));
	}

	public LightXmlObjectListDocument getControlConstructSchemesLight(
			String id, String version, String parentId, String parentVersion)
			throws Exception {
		return queryLightXmlBeans(id, version, parentId, parentVersion,
				"datacollection__DataCollection", "ControlConstructScheme",
				null, "reusable__Name");
	}

	public QuestionConstructDocument getQuestionConstruct(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		String text = queryElement(id, version, "QuestionConstruct", parentId,
				parentVersion, "ControlConstructScheme");
		return (text == "" ? null : QuestionConstructDocument.Factory
				.parse(text));
	}

	public LightXmlObjectListDocument getQuestionConstructsLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		return queryLightXmlBeans(id, version, parentId, parentVersion,
				"ControlConstructScheme", "QuestionConstruct", null,
				"reusable__Name");
	}

	public StatementItemDocument getStatementItem(String id, String version,
			String parentId, String parentVersion) throws Exception {
		String text = queryElement(id, version, "StatementItem", parentId,
				parentVersion, "ControlConstructScheme");
		return (text == "" ? null : StatementItemDocument.Factory.parse(text));
	}

	public LightXmlObjectListDocument getStatementItemsLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		return queryLightXmlBeans(id, version, parentId, parentVersion,
				"ControlConstructScheme", "StatementItem", null,
				"ConstructName");
	}

	public ComputationItemDocument getComputationItem(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		String text = queryElement(id, version, "ComputationItem", parentId,
				parentVersion, "ControlConstructScheme");
		return (text == "" ? null : ComputationItemDocument.Factory.parse(text));
	}

	public LightXmlObjectListDocument getComputationItemsLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		return queryLightXmlBeans(id, version, parentId, parentVersion,
				"ControlConstructScheme", "ComputationItem", null,
				"reusable__Name");
	}

	public SequenceDocument getSequence(String id, String version,
			String parentId, String parentVersion) throws Exception {
		String text = queryElement(id, version, "Sequence", parentId,
				parentVersion, "ControlConstructScheme");
		return (text == "" ? null : SequenceDocument.Factory.parse(text));
	}

	public LightXmlObjectListDocument getSequencesLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		return queryLightXmlBeans(id, version, parentId, parentVersion,
				"ControlConstructScheme", "Sequence", null, "reusable__Name");
	}

	public IfThenElseDocument getIfThenElse(String id, String version,
			String parentId, String parentVersion) throws Exception {
		String text = queryElement(id, version, "IfThenElse", parentId,
				parentVersion, "ControlConstructScheme");
		return (text == "" ? null : IfThenElseDocument.Factory.parse(text));
	}

	public LightXmlObjectListDocument getIfThenElsesLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		return queryLightXmlBeans(id, version, parentId, parentVersion,
				"ControlConstructScheme", "IfThenElse", null, "reusable__Name");
	}

	public RepeatUntilDocument getRepeatUntil(String id, String version,
			String parentId, String parentVersion) throws Exception {
		String text = queryElement(id, version, "RepeatUntil", parentId,
				parentVersion, "ControlConstructScheme");
		return (text == "" ? null : RepeatUntilDocument.Factory.parse(text));
	}

	public LightXmlObjectListDocument getRepeatUntilsLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		return queryLightXmlBeans(id, version, parentId, parentVersion,
				"ControlConstructScheme", "RepeatUntil", null, "reusable__Name");
	}

	public RepeatWhileDocument getRepeatWhile(String id, String version,
			String parentId, String parentVersion) throws Exception {
		String text = queryElement(id, version, "RepeatWhile", parentId,
				parentVersion, "ControlConstructScheme");
		return (text == "" ? null : RepeatWhileDocument.Factory.parse(text));
	}

	public LightXmlObjectListDocument getRepeatWhilesLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		return queryLightXmlBeans(id, version, parentId, parentVersion,
				"ControlConstructScheme", "RepeatWhile", null, "reusable__Name");
	}

	public LoopDocument getLoop(String id, String version, String parentId,
			String parentVersion) throws Exception {
		String text = queryElement(id, version, "Loop", parentId,
				parentVersion, "ControlConstructScheme");
		return (text == "" ? null : LoopDocument.Factory.parse(text));
	}

	public LightXmlObjectListDocument getLoopsLight(String id, String version,
			String parentId, String parentVersion) throws Exception {
		return queryLightXmlBeans(id, version, parentId, parentVersion,
				"ControlConstructScheme", "Loop", null, "reusable__Name");
	}

	public UniverseSchemeDocument getUniverseScheme(String id, String version,
			String parentId, String parentVersion) throws Exception {
		String text = queryElement(id, version, "UniverseScheme", parentId,
				parentVersion, "ConceptualComponent");
		return (text == "" ? null : UniverseSchemeDocument.Factory.parse(text));
	}

	public LightXmlObjectListDocument getUniverseSchemesLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		return queryLightXmlBeans(id, version, parentId, parentVersion,
				"ConceptualComponent", "UniverseScheme", null,
				"reusable__Label");
	}

	public MaintainableLabelQueryResult getUniverseSchemeLabel(String id,
			String version, String parentId, String parentVersion)
			throws DDIFtpException {
		MaintainableLabelQuery maintainableLabelQuery = new MaintainableLabelQuery(
				parentId, parentVersion, null);
		maintainableLabelQuery.setQuery(getQueryElementString(id, version,
				"UniverseScheme", parentId, parentVersion,
				"ConceptualComponent"));

		maintainableLabelQuery.setElementConversionNames(new String[] {
				"reusable__Label", "Description" });

		maintainableLabelQuery.setMaintainableTarget("UniverseScheme");
		maintainableLabelQuery.setStopElementNames(new String[] { "Universe" });

		MaintainableLabelQueryResult result = queryMaintainableLabel(maintainableLabelQuery);
		if (result.getId() == null) {
			maintainableLabelQuery.setQuery(getQueryElementString(id, version,
					"UniverseScheme", parentId, parentVersion, "Group"));
			result = queryMaintainableLabel(maintainableLabelQuery);
		}
		if (result.getId() == null) {
			maintainableLabelQuery.setQuery(getQueryElementString(id, version,
					"UniverseScheme", parentId, parentVersion, null));
			result = queryMaintainableLabel(maintainableLabelQuery);
		}
		return result;
	}

	public UniverseDocument getUniverse(String id, String version,
			String parentId, String parentVersion) throws Exception {
		String text = queryElement(id, version, "Universe", parentId,
				parentVersion, "UniverseScheme");
		return (text == "" ? null : UniverseDocument.Factory.parse(text));
	}

	public LightXmlObjectListDocument getUniversesLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		return queryLightXmlBeans(id, version, parentId, parentVersion,
		// "UniverseScheme", "Universe", null, "HumanReadable");
				"UniverseScheme", "Universe", null, "reusable__Label");
	}

	//
	// logical product
	//
	public MaintainableLabelQueryResult getCodeSchemeLabel(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		MaintainableLabelQuery maintainableLabelQuery = new MaintainableLabelQuery(
				parentId, parentVersion, null);
		maintainableLabelQuery.setQuery(getQueryElementString(id, version,
				"CodeScheme", parentId, parentVersion,
				"logicalproduct__LogicalProduct"));

		maintainableLabelQuery.setElementConversionNames(new String[] {
				"reusable__Label", "Description" });

		maintainableLabelQuery.setMaintainableTarget("CodeScheme");
		maintainableLabelQuery.setStopElementNames(new String[] { "Code" });

		MaintainableLabelQueryResult result = queryMaintainableLabel(maintainableLabelQuery);
		if (result.getId() == null) {
			maintainableLabelQuery.setQuery(getQueryElementString(id, version,
					"CodeScheme", parentId, parentVersion, "Group"));
			result = queryMaintainableLabel(maintainableLabelQuery);
		}
		if (result.getId() == null) {
			maintainableLabelQuery.setQuery(getQueryElementString(id, version,
					"CodeScheme", parentId, parentVersion, null));
			result = queryMaintainableLabel(maintainableLabelQuery);
		}
		return result;
	}

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
					parentId, parentVersion, "*", "CategoryScheme", null,
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

	public MaintainableLabelQueryResult getCategorySchemeLabel(String id,
			String version, String parentId, String parentVersion)
			throws DDIFtpException {
		MaintainableLabelQuery maintainableLabelQuery = new MaintainableLabelQuery(
				parentId, parentVersion, null);
		maintainableLabelQuery.setQuery(getQueryElementString(id, version,
				"CategoryScheme", parentId, parentVersion,
				"logicalproduct__LogicalProduct"));

		maintainableLabelQuery.setElementConversionNames(new String[] {
				"reusable__Label", "Description" });

		maintainableLabelQuery.setMaintainableTarget("CategoryScheme");
		maintainableLabelQuery.setStopElementNames(new String[] { "Category" });

		MaintainableLabelQueryResult result = queryMaintainableLabel(maintainableLabelQuery);
		if (result.getId() == null) {
			maintainableLabelQuery.setQuery(getQueryElementString(id, version,
					"CategoryScheme", parentId, parentVersion, "Group"));
			result = queryMaintainableLabel(maintainableLabelQuery);
		}
		if (result.getId() == null) {
			maintainableLabelQuery.setQuery(getQueryElementString(id, version,
					"CategoryScheme", parentId, parentVersion, null));
			result = queryMaintainableLabel(maintainableLabelQuery);
		}
		return result;
	}

	public VariableSchemeDocument getVariableScheme(String id, String version,
			String parentId, String parentVersion) throws Exception {
		String text = queryElement(id, version, "VariableScheme", parentId,
				parentVersion, "logicalproduct__LogicalProduct");
		if (text == null) {
			queryElement(id, version, "VariableScheme", null, null, null);
		}
		return (text == "" ? null : VariableSchemeDocument.Factory.parse(text));
	}

	public LightXmlObjectListDocument getVariableSchemesLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		LightXmlObjectListDocument lightXmlObjectListDocument = queryLightXmlBeans(
				id, version, parentId, parentVersion,
				"logicalproduct__LogicalProduct", "VariableScheme", null,
				"VariableSchemeName");
		if (lightXmlObjectListDocument.getLightXmlObjectList()
				.getLightXmlObjectList().isEmpty()) {
			lightXmlObjectListDocument = queryLightXmlBeans(id, version,
					parentId, parentVersion, "//", "VariableScheme", null,
					"VariableSchemeName");
		}
		return lightXmlObjectListDocument;
	}

	public MaintainableLabelQueryResult getVariableSchemeLabel(String id,
			String version, String parentId, String parentVersion)
			throws DDIFtpException {
		MaintainableLabelQuery maintainableLabelQuery = new MaintainableLabelQuery(
				parentId, parentVersion, null);
		maintainableLabelQuery.setQuery(getQueryElementString(id, version,
				"VariableScheme", parentId, parentVersion,
				"logicalproduct__LogicalProduct"));

		maintainableLabelQuery.setElementConversionNames(new String[] {
				"reusable__Label", "Description" });

		maintainableLabelQuery.setMaintainableTarget("VariableScheme");
		maintainableLabelQuery.setStopElementNames(new String[] { "Variable" });

		MaintainableLabelQueryResult result = queryMaintainableLabel(maintainableLabelQuery);
		if (result.getId() == null) {
			maintainableLabelQuery.setQuery(getQueryElementString(id, version,
					"VariableScheme", parentId, parentVersion, "Group"));
			result = queryMaintainableLabel(maintainableLabelQuery);
		}
		if (result.getId() == null) {
			maintainableLabelQuery.setQuery(getQueryElementString(id, version,
					"VariableScheme", parentId, parentVersion, null));
			result = queryMaintainableLabel(maintainableLabelQuery);
		}
		return result;
	}

	public LightXmlObjectListDocument getCategorysLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		LightXmlObjectListDocument lightXmlObjectListDocument = queryLightXmlBeans(
				id, version, parentId, parentVersion, "CategoryScheme",
				"Category", null, "reusable__Label");
		if (lightXmlObjectListDocument.getLightXmlObjectList()
				.getLightXmlObjectList().isEmpty()) {
			lightXmlObjectListDocument = queryLightXmlBeans(id, version,
					parentId, parentVersion, "//", "Category", null,
					"reusable__Label");
		}
		return lightXmlObjectListDocument;
	}

	public CategoryDocument getCategory(String id, String version,
			String parentId, String parentVersion) throws Exception {
		String text = queryElement(id, version, "Category", parentId,
				parentVersion, "CategoryScheme");
		if (text == null) {
			queryElement(id, version, "Category", null, null, null);
		}
		return (text == "" ? null : CategoryDocument.Factory.parse(text));
	}

	public VariableDocument getVariable(String id, String version,
			String parentId, String parentVersion) throws Exception {
		String text = queryElement(id, version, "Variable", parentId,
				parentVersion, "VariableScheme");
		if (text == null) {
			queryElement(id, version, "Variable", null, null, null);
		}
		return (text == "" ? null : VariableDocument.Factory.parse(text));
	}

	public LightXmlObjectListDocument getVariablesLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		LightXmlObjectListDocument lightXmlObjectListDocument = queryLightXmlBeans(
				id, version, parentId, parentVersion, "VariableScheme",
				"Variable", null, "reusable__Label");
		return lightXmlObjectListDocument;
	}

	//
	// archive
	//
	public LightXmlObjectListDocument getOrganizationsLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		LightXmlObjectListDocument lightXmlObjectListDocument = queryLightXmlBeans(
				id, version, parentId, parentVersion, "OrganizationScheme",
				"Organization", null, "OrganizationName");
		return lightXmlObjectListDocument;
	}
}
