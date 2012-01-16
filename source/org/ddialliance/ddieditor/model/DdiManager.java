package org.ddialliance.ddieditor.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.ddialliance.ddi3.xml.xmlbeans.archive.ArchiveDocument;
import org.ddialliance.ddi3.xml.xmlbeans.conceptualcomponent.ConceptDocument;
import org.ddialliance.ddi3.xml.xmlbeans.conceptualcomponent.ConceptGroupDocument;
import org.ddialliance.ddi3.xml.xmlbeans.conceptualcomponent.ConceptSchemeDocument;
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
import org.ddialliance.ddi3.xml.xmlbeans.logicalproduct.DataRelationshipDocument;
import org.ddialliance.ddi3.xml.xmlbeans.logicalproduct.VariableDocument;
import org.ddialliance.ddi3.xml.xmlbeans.logicalproduct.VariableSchemeDocument;
import org.ddialliance.ddieditor.logic.urn.ddi.ReferenceResolution;
import org.ddialliance.ddieditor.model.conceptual.ConceptualElement;
import org.ddialliance.ddieditor.model.conceptual.ConceptualType;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectListDocument;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;
import org.ddialliance.ddieditor.model.namespace.ddi3.Ddi3NamespaceHelper;
import org.ddialliance.ddieditor.model.namespace.ddi3.Ddi3NamespacePrefix;
import org.ddialliance.ddieditor.model.relationship.ElementDocument.Element;
import org.ddialliance.ddieditor.model.relationship.ParentDocument.Parent;
import org.ddialliance.ddieditor.persistenceaccess.DefineQueryPositionResult;
import org.ddialliance.ddieditor.persistenceaccess.ParamatizedXquery;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceManager;
import org.ddialliance.ddieditor.persistenceaccess.XQueryInsertKeyword;
import org.ddialliance.ddieditor.persistenceaccess.dbxml.DbXmlManager;
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

	private static Ddi3NamespaceHelper ddi3NamespaceHelper;
	private static DdiManager instance;
	private XmlOptions options;

	private DdiManager() {
		options = new XmlOptions();
		options.setSaveAggressiveNamespaces();
		options.setSaveOuter();
		options.setSavePrettyPrint();
	}

	public static synchronized DdiManager getInstance() throws DDIFtpException {
		if (instance == null) {
			log.info("Initializing DDIManager");
			instance = new DdiManager();
			// initialize the ddi3 name space generator
			try {
				instance.ddi3NamespaceHelper = new Ddi3NamespaceHelper();
			} catch (Exception e) {
				throw new DDIFtpException(
						"Error on generating namespace by elements", e);
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

	public XmlOptions getXmlOptions() {
		return options;
	}

	protected LightXmlObjectListDocument queryLightXmlBeans(String id,
			String version, String parentId, String parentVersion,
			String rootElement, String parentChildElement,
			String childChildElement, String labelElement, XQuery extraQuery)
			throws DDIFtpException {
		return queryLightXmlBeansImpl(id, version, parentId, parentVersion,
				rootElement, parentChildElement, childChildElement,
				labelElement, extraQuery);
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
		return queryLightXmlBeansImpl(id, version, parentId, parentVersion,
				rootElement, parentChildElement, childChildElement,
				labelElement, null);
	}

	private LightXmlObjectListDocument queryLightXmlBeansImpl(String id,
			String version, String parentId, String parentVersion,
			String rootElement, String parentChildElement,
			String childChildElement, String labelElement, XQuery extraQuery)
			throws DDIFtpException {
		// add extra parameter
		ParamatizedXquery query = xQueryLightXmlBeans(parentId,
				childChildElement, parentChildElement, extraQuery);

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
			String childChildElement, String parentChildElement,
			XQuery extraQuery) throws DDIFtpException {
		if (parentId != null && parentId.equals("")) {
			parentId = null;
		}

		if (extraQuery != null) {
			XQuery query = buildLightXmlBeansXquery(parentId,
					childChildElement, parentChildElement, extraQuery);
			return new ParamatizedXquery(query.getFullQueryString());
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
			XQuery query = buildLightXmlBeansXquery(parentId,
					childChildElement, parentChildElement, extraQuery);
			xQuery = new ParamatizedXquery(query.getFullQueryString());
			// construct paramatized query and store
			PersistenceManager.getInstance().setParamatizedQuery(xQueryName,
					xQuery);
			return xQuery;
		}
	}

	private XQuery buildLightXmlBeansXquery(String parentId,
			String childChildElement, String parentChildElement,
			XQuery extraQuery) {
		XQuery query = new XQuery();
		query.namespaceDeclaration.append(FUNCTION_NS_DECLARATION);

		// functions
		query.function.append(labelLangFunction(childChildElement));

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
					.append(" return <LightXmlObject element=\"?\" id=\"{$y/@id/string()}\" version=\"{$y/@version/string()}\" parentId=\"{$x/@id/string()}\" parentVersion=\"{$x/@version/string()}\">{ddieditor:label_lang($y)}");
			// parentChildElement

		} else {
			query.query
					.append(" return <LightXmlObject element=\"?\" id=\"{$x/@id/string()}\" version=\"{$x/@version/string()}\" parentId=\"{$x/parent::node()/@id/string()}\" parentVersion=\"{$x/parent::node()/@version/string()}");

			// rootElement
		}

		// add extra query
		if (extraQuery != null) {
			if (extraQuery.namespaceDeclaration.length() > 0) {
				query.namespaceDeclaration.insert(0,
						extraQuery.namespaceDeclaration + " ");
			}
			if (extraQuery.function.length() > 0) {
				query.function.append(" ");
				query.function.append(extraQuery.function);
			}
			if (extraQuery.query.length() > 0) {
				query.query.append(" ");
				query.query.append(extraQuery.query);
			}
		}

		// finish query
		if (parentChildElement != null) {
			query.query.append("</LightXmlObject>}</dl:LightXmlObjectList>");
		} else {
			query.query.append("\">{ddieditor:label_lang($x)}");
		}
		return query;
	}

	private String labelLangFunction(String childChildElement) {
		StringBuffer result = new StringBuffer();
		if (childChildElement != null) {
			result.append("declare function ");
			result.append(FUNCTION_NS_PREFIX);
			result.append(":label_lang($element) {  for $z in $element?");
			// ddi3NamespaceGenerator.addFullyQualifiedNamespaceDeclarationToElements(childChildElement)
			result.append(" return if($z/@xml:lang/string()=\"\") then <Label>{ddieditor:label_text($z)}</Label> else <Label lang=\"{$z/@xml:lang/string()}\">{ddieditor:label_text($z)}</Label> }; declare function ");
			result.append(FUNCTION_NS_PREFIX);
			result.append(":label_text($element) { for $q in $element?");
			// ddi3NamespaceGenerator.addFullyQualifiedNamespaceDeclarationToElements(labelElement)
			result.append(" return $q/text() }; ");
		} else {
			result.append("declare function ddieditor:label_lang($element) { for $z in $element?");
			// ddi3NamespaceGenerator.addFullyQualifiedNamespaceDeclarationToElements(labelElement)
			result.append(" return if($z/@xml:lang/string()=\"\") then <Label>{$z/text()}</Label> else <Label lang=\"{$z/@xml:lang/string()}\">{$z/text()}</Label>}; ");
		}
		return result.toString();
	}

	private LightXmlObjectListDocument queryLightXmlByReference(
			ReferenceResolution referenceResolution, String rootElement,
			String parentChildElement, String childChildElement,
			String labelElement) throws DDIFtpException {
		// get query
		ParamatizedXquery query = buildQueryLightXmlByReference(childChildElement);

		// set query
		String prefix = getDdi3NamespaceHelper().getNamespaceObjectByElement(
				parentChildElement).getPrefix();
		int i = 0;

		// label function
		query.setObject(++i, "/" + prefix + ":" + labelElement);

		// let
		query.setObject(++i, PersistenceManager.getInstance().getResourcePath());

		StringBuilder param1 = new StringBuilder();
		param1.append(prefix);
		param1.append(":");
		param1.append(parentChildElement);
		param1.append("/");
		param1.append(prefix);
		param1.append(":");
		param1.append(referenceResolution.getLocalName()+"Reference");
		param1.append("/r:ID");
		query.setObject(++i, param1.toString());

		// where
		query.setString(++i, referenceResolution.getId());

		// return
		query.setObject(++i, parentChildElement);
		// query.query.append(" id=\"{$x/ancestor::?:?/@id/string()}\"");
		query.setObject(++i, prefix);
		query.setObject(++i, parentChildElement);

		// query.query.append(" version=\"{$x/ancestor::?:?/@version/string()}\"");
		query.setObject(++i, prefix);
		query.setObject(++i, parentChildElement);

		// query.query.append(" parentId=\"{$x/ancestor::?:?/@id/string()}\"");
		query.setObject(++i, prefix);
		query.setObject(++i, rootElement);

		// query.query.append(" parentVersion=\"{$x/ancestor::?:?/@version/string()}\">");
		query.setObject(++i, prefix);
		query.setObject(++i, rootElement);

		// query.query.append("{ddieditor:label_lang($x/ancestor::?:?)}</LightXmlObject>");
		query.setObject(++i, prefix);
		query.setObject(++i, parentChildElement);

		// execute query
		List<String> search = PersistenceManager.getInstance().query(
				query.getParamatizedQuery());

		// marshal result
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

	private ParamatizedXquery buildQueryLightXmlByReference(
			String childChildElement) throws DDIFtpException {
		String xQueryName = "LIGHT_XML_BY_REF";
		if (childChildElement != null) {
			xQueryName += "_WITH_CHILD_CHILD";
		}

		// lookup param query
		ParamatizedXquery xQuery = PersistenceManager.getInstance()
				.getParamatizedQuery(xQueryName);
		if (xQuery != null) {
			return xQuery;
		}

		// build query
		else {
			XQuery query = new XQuery();
			query.namespaceDeclaration.append(FUNCTION_NS_DECLARATION);
			query.namespaceDeclaration.append(" declare namespace "
					+ Ddi3NamespacePrefix.REUSEABLE.getPrefix() + "=\""
					+ Ddi3NamespacePrefix.REUSEABLE.getNamespace() + "\";");
			query.namespaceDeclaration.append(" declare namespace "
					+ Ddi3NamespacePrefix.DATA_COLLECTION.getPrefix() + "=\""
					+ Ddi3NamespacePrefix.DATA_COLLECTION.getNamespace()
					+ "\";");
			query.namespaceDeclaration.append(" declare namespace "
					+ Ddi3NamespacePrefix.CONCEPTUAL_COMPONENTS.getPrefix()
					+ "=\""
					+ Ddi3NamespacePrefix.CONCEPTUAL_COMPONENTS.getNamespace()
					+ "\";");
			query.namespaceDeclaration
					.append(" declare namespace "
							+ Ddi3NamespacePrefix.LOGIAL_PRODUCT.getPrefix()
							+ "=\""
							+ Ddi3NamespacePrefix.LOGIAL_PRODUCT.getNamespace()
							+ "\";");
			query.namespaceDeclaration
					.append(" declare namespace dl=\"ddieditor-lightobject\"; ");

			// functions
			query.function.append(labelLangFunction(childChildElement));

			// query
			query.query.append("let $lightXml := for $x in ?//?");
			query.query.append(" where ? = data($x)");
			query.query.append(" return <LightXmlObject element=\"?\"");

			query.query.append(" id=\"{$x/ancestor::?:?/@id/string()}\"");
			query.query
					.append(" version=\"{$x/ancestor::?:?/@version/string()}\"");
			query.query.append(" parentId=\"{$x/ancestor::?:?/@id/string()}\"");

			query.query
					.append(" parentVersion=\"{$x/ancestor::?:?/@version/string()}\">");
			query.query
					.append("{ddieditor:label_lang($x/ancestor::?:?)}</LightXmlObject>");
			query.query
					.append(" return <dl:LightXmlObjectList>{$lightXml}</dl:LightXmlObjectList>");

			xQuery = new ParamatizedXquery(query.getFullQueryString());
			// store
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
	 * @return pramatized xquery
	 * @throws DDIFtpException
	 */
	protected ParamatizedXquery getQueryElement(String id, String version,
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

		return xQuery;
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
	 * @return XQuery string
	 * @throws DDIFtpException
	 */
	public String getQueryElementString(String id, String version,
			String elementType, String parentId, String parentVersion,
			String parentElementType) throws DDIFtpException {
		return getQueryElement(id, version, elementType, parentId,
				parentVersion, parentElementType).getParamatizedQuery();
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
		// reflect get light element list
		QName qName = xmlObject.schemaType().getDocumentElementName();
		StringBuilder operation = new StringBuilder("get");
		operation.append(qName.getLocalPart());
		operation.append("sLight");
		createElement(xmlObject, parentId, parentVersion, parentElementType,
				null, operation.toString());
	}

	public LightXmlObjectListDocument checkExistence(
			String lightXmlObjectMethodName, String parentId,
			String parentVersion) throws DDIFtpException {
		LightXmlObjectListDocument lightXmlObjectList = null;
		try {
			lightXmlObjectList = (LightXmlObjectListDocument) ReflectionUtil
					.invokeMethod(
							DdiManager.getInstance(),
							lightXmlObjectMethodName.toString(),
							false,
							new Object[] { "", "",
									parentId == null ? "" : parentId,
									parentVersion == null ? "" : parentVersion });
		} catch (Exception e) {
			throw new DDIFtpException(e);
		}
		return lightXmlObjectList;
	}

	public List<LightXmlObjectType> checkForParent(String namespaceUri,
			String localName) throws DDIFtpException {
		QName qName = new QName(namespaceUri, localName);
		Element element = getDdi3NamespaceHelper().getElementParents(
				getDdi3NamespaceHelper().getDuplicateConvention(qName));
		List<LightXmlObjectType> result = new ArrayList<LightXmlObjectType>();
		for (Parent parent : element.getParentList()) {
			getDdi3NamespaceHelper();
			// operation name
			StringBuilder operation = new StringBuilder("get");
			operation.append(Ddi3NamespaceHelper.getCleanedElementName(parent
					.getId()));
			operation.append("sLight");

			// check for parent xml object lights
			result.addAll(DdiManager.getInstance()
					.checkExistence(operation.toString(), "", "")
					.getLightXmlObjectList().getLightXmlObjectList());
		}
		return result;
	}

	public void createElement(XmlObject xmlObject, String parentId,
			String parentVersion, String parentElementType,
			String subParentElementType, String lightXmlObjectMethodName)
			throws DDIFtpException {
		XmlBeansUtil.instanceOfXmlBeanDocument(xmlObject, new Throwable());

		// get last element of same type to insert
		LightXmlObjectType lastElementOfSameType = null;

		// define last element of same type
		LightXmlObjectListDocument lightXmlObjectList = checkExistence(
				lightXmlObjectMethodName, parentId, parentVersion);
		if (lightXmlObjectList != null
				&& !lightXmlObjectList.getLightXmlObjectList()
						.getLightXmlObjectList().isEmpty()) {
			lastElementOfSameType = lightXmlObjectList
					.getLightXmlObjectList()
					.getLightXmlObjectList()
					.get(lightXmlObjectList.getLightXmlObjectList()
							.getLightXmlObjectList().size() - 1);
		}

		// insert xml object after last element of same type
		QName qName = xmlObject.schemaType().getDocumentElementName();
		XQuery xQuery = null;
		if (lastElementOfSameType != null) {
			String childConvention = getDdi3NamespaceHelper()
					.getDuplicateConvention(qName);
			if (subParentElementType != null) {
				// append function child_parent with subParentElementType
				childConvention += getDdi3NamespaceHelper()
						.addFullyQualifiedNamespaceDeclarationToElements(
								subParentElementType);
			}
			xQuery = xQueryCrudPosition(lastElementOfSameType.getId(),
					lastElementOfSameType.getVersion(), childConvention,
					parentId, parentVersion, parentElementType);

			PersistenceManager.getInstance().insert(
					getDdi3NamespaceHelper().substitutePrefixesFromElements(
							xmlObject.xmlText(DdiManager.getInstance()
									.getXmlOptions())),
					XQueryInsertKeyword.AFTER, xQuery);
			return;
		}

		// guard, last element of same type NULL
		xQuery = xQueryCrudPosition(parentId, parentVersion, parentElementType,
				null, null, null);
		if (subParentElementType != null) {
			xQuery.query.append(getDdi3NamespaceHelper()
					.addFullyQualifiedNamespaceDeclarationToElements(
							subParentElementType));
		}
		PersistenceManager.getInstance().insert(
				getDdi3NamespaceHelper().substitutePrefixesFromElements(
						xmlObject.xmlText(DdiManager.getInstance()
								.getXmlOptions())),
				XQueryInsertKeyword.AS_FIRST_NODE, xQuery);
	}

	// TODO add same functionality as createElement(XmlObject xmlObject ...
	public void createElement(String xml, String parentId,
			String parentVersion, String parentElementType,
			String subParentElementType) throws DDIFtpException {
		XQuery xQuery = xQueryCrudPosition(parentId, parentVersion,
				parentElementType, null, null, null);
		if (subParentElementType != null && !subParentElementType.equals("")) {
			xQuery.query.append(getDdi3NamespaceHelper()
					.addFullyQualifiedNamespaceDeclarationToElements(
							subParentElementType));
		}
		PersistenceManager.getInstance().insert(
				getDdi3NamespaceHelper().substitutePrefixesFromElements(xml),
				XQueryInsertKeyword.AFTER, xQuery);
	}

	public void createElement(XmlObject xmlObject, String parentId,
			String parentVersion, String parentElementType,
			String[] subElements, String[] stopElements, String[] jumpElements)
			throws DDIFtpException {
		XmlBeansUtil.instanceOfXmlBeanDocument(xmlObject, new Throwable());
		createElement(xmlObject.getDomNode().getChildNodes().item(0)
				.getLocalName(), xmlObject.xmlText(options), parentId,
				parentVersion, parentElementType, subElements, stopElements,
				jumpElements);
	}

	public void createElementInto(XmlObject xmlObject, String parentId,
			String parentVersion, String parentElementType)
			throws DDIFtpException {
		createElementInto(xmlObject.xmlText(options), parentId, parentVersion,
				parentElementType);
	}

	public void createElementInto(String xml, String parentId,
			String parentVersion, String parentElementType)
			throws DDIFtpException {
		XQuery xQuery = xQueryCrudPosition(parentId, parentVersion,
				parentElementType, null, null, null);
		PersistenceManager.getInstance().insert(
				getDdi3NamespaceHelper().substitutePrefixesFromElements(xml),
				XQueryInsertKeyword.AS_LAST_NODE, xQuery);
	}

	/**
	 * Create an element in a parent of after possible existence of sub
	 * elements.
	 * 
	 * @param elementType
	 *            localname of xml element type
	 * @param xml
	 *            element to create
	 * @param parentId
	 *            parent id
	 * @param parentVersion
	 *            parent version
	 * @param parentElementType
	 *            local element name
	 * @param parentSubElements
	 *            array of elements indicating possible insert positions below
	 *            parent element
	 * @param stopElements
	 *            array of elements to indicate the stop block of the scan for
	 *            the insert position/ XPath
	 * @param jumpElements
	 *            array of elements to indicate elements to jump over in the
	 *            scan for the insert position/ XPath Note: Jump Elements should
	 *            not be Stop Elements as well
	 * @throws DDIFtpException
	 */
	public void createElement(String elementType, String xml, String parentId,
			String parentVersion, String parentElementType,
			String[] parentSubElements, String[] stopElements,
			String[] jumpElements) throws DDIFtpException {
		XQuery xQuery = xQueryCrudPosition(parentId, parentVersion,
				parentElementType, null, null, null);
		DefineQueryPositionResult query = null;
		try {
			query = DbXmlManager.getInstance().defineQueryPosition(elementType,
					xQuery.getFullQueryString(), parentSubElements,
					stopElements, jumpElements);
		} catch (Exception e) {
			throw new DDIFtpException(e);
		}

		if (query.query.length() == xQuery.getFullQueryString().length()) {
			// default insert
			PersistenceManager.getInstance().insert(
					getDdi3NamespaceHelper()
							.substitutePrefixesFromElements(xml),
					query.insertKeyWord, query);
		} else {
			// custom insert
			XQuery customQuery = new XQuery();
			customQuery.query.append(query.query.toString());
			PersistenceManager.getInstance().insert(
					getDdi3NamespaceHelper()
							.substitutePrefixesFromElements(xml),
					query.insertKeyWord, customQuery);
		}
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
		query.append(getDdi3NamespaceHelper().substitutePrefixesFromElements(
				xmlObject.xmlText(getXmlOptions())));
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

	public void updateAttribute(LightXmlObjectType lightXmlObject,
			String parentElementType, String attributeName,
			String attributeValue) throws DDIFtpException {
		// position
		XQuery position = null;
		try {
			position = xQueryCrudPosition(lightXmlObject.getId(),
					lightXmlObject.getVersion(), lightXmlObject.getElement(),
					lightXmlObject.getParentId(),
					lightXmlObject.getParentVersion(), parentElementType);
		} catch (Exception e) {
			throw new DDIFtpException("Error getting id and version", e);
		}

		// query
		StringBuilder query = new StringBuilder();
		query.append(position.function.toString());
		query.append("replace value of node ");
		query.append(position.query.toString());
		query.append("/@");
		query.append(attributeName);
		query.append(" with '");
		query.append(attributeValue);
		query.append("'");

		PersistenceManager.getInstance().updateQuery(query.toString());
	}

	public void createAttribute(LightXmlObjectType lightXmlObject,
			String parentElementType, String attributeName,
			String attributeValue) throws DDIFtpException {
		// position
		XQuery position = null;
		try {
			position = xQueryCrudPosition(lightXmlObject.getId(),
					lightXmlObject.getVersion(), lightXmlObject.getElement(),
					lightXmlObject.getParentId(),
					lightXmlObject.getParentVersion(), parentElementType);
		} catch (Exception e) {
			throw new DDIFtpException("Error getting id and version", e);
		}

		// query
		StringBuilder query = new StringBuilder();
		query.append(position.function.toString());
		query.append("insert node attribute ");
		query.append(attributeName);
		query.append(" {'");
		query.append(attributeValue);
		query.append("'} into ");
		query.append(position.query.toString());

		PersistenceManager.getInstance().updateQuery(query.toString());
	}

	@Profiled(tag = "xQueryCrudPosition")
	private XQuery xQueryCrudPosition(String id, String version,
			String elementType, String parentId, String parentVersion,
			String parentElementType) throws DDIFtpException {
		XQuery query = new XQuery();
		// no defined parent - only one is expected!
		if (parentElementType == null || parentId == null) {
			query.query.append(" for $element in ");
			query.query.append(PersistenceManager.getInstance()
					.getResourcePath());
			query.query.append("/");
			query.query
					.append(ddi3NamespaceHelper
							.addFullyQualifiedNamespaceDeclarationToElements(elementType));
			if (id != null) {
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
		// Note: Delete from behind
		for (int i = elements.size() - 1; i >= 0; i--) {
			MaintainableLabelUpdateElement element = elements.get(i);
			// guard
			if (element.getCrudValue() == null) {
				throw new DDIFtpException("Update value not specified for: "
						+ element, new Throwable());
			}

			if ((element.getCrudValue() > 0 || element.getCrudValue() == 0)
					&& element.getValue() == null) {
				try {
					if (element.getXmlObject() != null) {
						element.setValue(element.getXmlObject()
								.xmlText(options));
					} else {
						throw new DDIFtpException("Value not specified for: "
								+ element, new Throwable());
					}
				} catch (Exception e) {
					throw new DDIFtpException(e);
				}
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

	public static LightXmlObjectType createLightXmlObject(String parentId,
			String parentVersion, String id, String version) {
		LightXmlObjectType lightXmlObject = LightXmlObjectType.Factory
				.newInstance();
		lightXmlObject.setParentId(parentId);
		lightXmlObject.setParentVersion(parentVersion);
		lightXmlObject.setId(id);
		lightXmlObject.setVersion(version);
		return lightXmlObject;
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

	public LightXmlObjectListDocument getResourcePackagesLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		LightXmlObjectListDocument lightXmlObjectListDocument = queryLightXmlBeans(
				id, version, parentId, parentVersion, "//", "ResourcePackage",
				null, "UserID");
		return lightXmlObjectListDocument;
	}

	public LightXmlObjectListDocument getGroupsLight(String id, String version,
			String parentId, String parentVersion) throws Exception {
		LightXmlObjectListDocument lightXmlObjectListDocument = queryLightXmlBeans(
				id, version, parentId, parentVersion, "//", "Group", null,
				"UserID");
		return lightXmlObjectListDocument;
	}

	public LightXmlObjectListDocument getSubGroupsLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		LightXmlObjectListDocument lightXmlObjectListDocument = queryLightXmlBeans(
				id, version, parentId, parentVersion, "//", "SubGroup", null,
				"UserID");
		return lightXmlObjectListDocument;
	}

	public LightXmlObjectListDocument getNotesLight(String id, String version,
			String parentId, String parentVersion) throws Exception {
		// LightXmlObjectListDocument lightXmlObjectListDocument =
		// queryLightXmlBeans(
		// id, version, parentId, parentVersion, parentElement, "Note",
		// null, "UserID");
		// return lightXmlObjectListDocument;
		LightXmlObjectListDocument doc = LightXmlObjectListDocument.Factory
				.newInstance();
		doc.addNewLightXmlObjectList();

		// TODO as notes are scattered in various places around the ddi3 return
		// empty list
		return doc;
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
		return executeStudyLabelQuery(id, version, parentId, parentVersion,
				new String[] { "Citation", "studyunit__Abstract",
						"UniverseReference", "SeriesStatement",
						"FundingInformation", "studyunit__Purpose", "Coverage",
						"AnalysisUnit",
						// TODO
						// "AnalysisUnitsCovered",
						// left out because of potential bug on declaration in
						// schema
						// studyunit.xsd
						// Bug notice mailed to DDI::TIC on 20090824
						"KindOfData", "OtherMaterial", "Note", "Embargo" },
				new String[] { "ConceptualComponent", "DataCollection",
						"LogicalProduct", "PhysicalDataProduct",
						"PhysicalInstance", "Archive", "DDIProfile",
						"DDIProfileReference" });
	}

	public MaintainableLabelQueryResult getStudyRationale(String id,
			String version, String parentId, String parentVersion)
			throws DDIFtpException {
		return executeStudyLabelQuery(id, version, parentId, parentVersion,
				new String[] { "UserID", "VersionResponsibility",
						"VersionRationale" }, new String[] { "Citation",
						"studyunit__Abstract", "UniverseReference",
						"SeriesStatement", "FundingInformation",
						"studyunit__Purpose", "Coverage", "AnalysisUnit",
						"KindOfData", "OtherMaterial", "Note", "Embargo",
						"ConceptualComponent", "DataCollection",
						"LogicalProduct", "PhysicalDataProduct",
						"PhysicalInstance", "Archive", "DDIProfile",
						"DDIProfileReference" });
	}

	private MaintainableLabelQueryResult executeStudyLabelQuery(String id,
			String version, String parentId, String parentVersion,
			String[] elementConversionNames, String[] stopElementNames)
			throws DDIFtpException {
		// set up query
		MaintainableLabelQuery maintainableLabelQuery = new MaintainableLabelQuery(
				parentId, parentVersion, null);
		maintainableLabelQuery
				.setQuery(getQueryElementString(id, version,
						"studyunit__StudyUnit", parentId, parentVersion,
						"DDIInstance"));
		maintainableLabelQuery
				.setElementConversionNames(elementConversionNames);
		maintainableLabelQuery.setMaintainableTarget("studyunit__StudyUnit");
		maintainableLabelQuery.setStopElementNames(stopElementNames);

		// result
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
					parentId, parentVersion, "*", "ConceptualComponent", null,
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
					parentId, parentVersion, "*", "ConceptScheme", null,
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
	public LightXmlObjectListDocument getDataCollectionsLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		LightXmlObjectListDocument lightXmlObjectListDocument = queryLightXmlBeans(
				id, version, parentId, parentVersion, "studyunit__StudyUnit",
				"datacollection__DataCollection", null, "reusable__Name");
		if (lightXmlObjectListDocument.getLightXmlObjectList()
				.getLightXmlObjectList().isEmpty()) {
			lightXmlObjectListDocument = queryLightXmlBeans(id, version,
					parentId, parentVersion, "*",
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

	public LightXmlObjectListDocument getMethodologysLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		return queryLightXmlBeans(id, version, parentId, parentVersion,
				"datacollection__DataCollection", "Methodology", null, "UserID");
	}

	public LightXmlObjectListDocument getCollectionEventsLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		return queryLightXmlBeans(id, version, parentId, parentVersion,
				"datacollection__DataCollection", "CollectionEvent", null,
				"UserID");
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
		// if (lightXmlObjectListDocument.getLightXmlObjectList()
		// .getLightXmlObjectList().isEmpty()) {
		// lightXmlObjectListDocument = queryLightXmlBeans(id, version,
		// parentId, parentVersion, "ResourcePacket", "QuestionScheme", null,
		// "reusable__Label");
		// }
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
		schemeQuery.setElementConversionNames(new String[] { "reusable__Label",
				"Description" });
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
		return queryLightXmlBeans(id, version, parentId, parentVersion,
				"QuestionScheme", "MultipleQuestionItem", null,
				"MultipleQuestionItemName");
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
				"MultipleQuestionItemName", "QuestionText",
				"datacollection__ConceptReference", "SubQuestionSequence" });

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
		return queryLightXmlBeans(id, version, parentId, parentVersion,
				"QuestionScheme", "QuestionItem", null, "QuestionItemName");
	}

	public LightXmlObjectListDocument getQuestionItemsLightByConcept(
			ReferenceResolution referenceResolution) throws Exception {

		return queryLightXmlByReference(referenceResolution, "QuestionScheme",
				"QuestionItem", null, "QuestionItemName");
	}

	public LightXmlObjectListDocument getQuestionItemsLightPlus(
			boolean deepReference, String id, String version, String parentId,
			String parentVersion) throws Exception {
		XQuery xquery = new XQuery();

		// func userid
		xquery.function
				.append(" declare function ddieditor:userid($element) {");
		xquery.function
				.append("for $z in $element/*[namespace-uri()='ddi:reusable:3_1' and local-name()='UserID'] ");
		xquery.function
				.append("return <Custom option=\"{$z/@type/string()}\">{$z/string()}</Custom>};");

		// func text
		xquery.function
				.append(" declare function ddieditor:queitext($element) { ");
		xquery.function
				.append("for $z in $element/*[namespace-uri()='ddi:datacollection:3_1' and local-name()='QuestionText'] ");
		xquery.function
				.append("return <Custom option=\"lang\"  value=\"{string($z/@xml:lang)}\">{string($z");
		xquery.function
				.append("/*[namespace-uri()='ddi:datacollection:3_1' and local-name()='LiteralText']");
		xquery.function
				.append("/*[namespace-uri()='ddi:datacollection:3_1' and local-name()='Text'])}</Custom>};");

		// func response domain
		xquery.function
				.append(" declare function ddieditor:queidomain($element) { ");
		xquery.function.append("for $element in $element ");
		xquery.function
				.append("return if(exists($element/*[namespace-uri()='ddi:datacollection:3_1' and local-name()='TextDomain'])) then <Custom>Text</Custom> else ");
		xquery.function
				.append("if(exists($element/*[namespace-uri()='ddi:datacollection:3_1' and local-name()='NumericDomain'])) then <Custom>Numeric</Custom> else ");
		xquery.function
				.append("if(exists($element/*[namespace-uri()='ddi:datacollection:3_1' and local-name()='CodeDomain'])) then <Custom>Code</Custom> else ");
		xquery.function
				.append("if(exists($element/*[namespace-uri()='ddi:datacollection:3_1' and local-name()='DateTimeDomain'])) then <Custom>DateTime</Custom> else ''};");

		// query
		xquery.query.append(" <CustomList type =\"UserID\">");
		xquery.query.append("{ddieditor:userid($y)}");
		xquery.query.append("</CustomList>");

		xquery.query.append(" <CustomList type =\"Text\">");
		xquery.query.append("{ddieditor:queitext($y)}");
		xquery.query.append("</CustomList>");

		xquery.query.append(" <CustomList type =\"ResponseDomain\">");
		xquery.query.append("{ddieditor:queidomain($y)}");
		xquery.query.append("</CustomList>");

		String elementType = "QuestionItem";
		if (deepReference) {
			elementType = "//" + elementType;
		}
		return queryLightXmlBeans(id, version, parentId, parentVersion,
				"QuestionScheme", elementType, null, "QuestionItemName", xquery);
	}

	@Profiled(tag = "getMultipleQuestionQuestionItemsLight")
	public LightXmlObjectListDocument getMultipleQuestionQuestionItemsLight(
			String id, String version, String parentId, String parentVersion)
			throws Exception {
		LightXmlObjectListDocument doc = queryLightXmlBeans(id, version,
				parentId, parentVersion, "MultipleQuestionItem",
				"SubQuestions/QuestionItem", "QuestionItemName", "");
		for (LightXmlObjectType lightXmlObjectType : doc
				.getLightXmlObjectList().getLightXmlObjectList()) {
			lightXmlObjectType.setElement("QuestionItem");
		}
		return doc;
	}

	@Profiled(tag = "getQuestionItem")
	public QuestionItemDocument getQuestionItem(String id, String version,
			String parentId, String parentVersion) throws Exception {
		String text = queryElement(id, version, "QuestionItem", parentId,
				parentVersion, "QuestionScheme");
		return (text == "" ? null : QuestionItemDocument.Factory.parse(text));
	}

	@Profiled(tag = "getMultipleQuestionQuestionItem")
	public QuestionItemDocument getMultipleQuestionQuestionItem(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		String text = queryElement(id, version, "SubQuestions/QuestionItem",
				parentId, parentVersion, "MultipleQuestionItem");
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
		ParamatizedXquery paramatizedXquery = getQueryElement(id, version,
				"ControlConstructScheme", parentId, parentVersion,
				"datacollection__DataCollection");

		if ((parentVersion == null || parentVersion.equals(""))
				&& (version == null || version.equals(""))) {
			paramatizedXquery.getParameters()[paramatizedXquery
					.getParameterSize() - 1] = "";
		} else if (version == null || version.equals("")) {
			String param = paramatizedXquery.getParameters()[paramatizedXquery
					.getParameterSize() - 1];
			paramatizedXquery.getParameters()[paramatizedXquery
					.getParameterSize() - 1] = param.replace(
					"and empty($child/@version)", "");
		} else if ((parentVersion == null || parentVersion.equals(""))) {
			String param = paramatizedXquery.getParameters()[paramatizedXquery
					.getParameterSize() - 1];
			paramatizedXquery.getParameters()[paramatizedXquery
					.getParameterSize() - 1] = param.replace(
					"empty($element/@version) and", "");
		}
		query.setQuery(paramatizedXquery.getParamatizedQuery());

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

	public Map<String, LightXmlObjectType> getControlConstructsLightasMap()
			throws Exception {
		Map<String, LightXmlObjectType> result = new HashMap<String, LightXmlObjectType>();

		// all control construct schemes
		LightXmlObjectListDocument listDoc = DdiManager.getInstance()
				.getControlConstructSchemesLight(null, null, null, null);

		for (LightXmlObjectType lightXmlObject : listDoc
				.getLightXmlObjectList().getLightXmlObjectList()) {
			// all control constructs in scheme
			MaintainableLightLabelQueryResult mlqr = DdiManager.getInstance()
					.getInstrumentLabel(lightXmlObject.getId(),
							lightXmlObject.getVersion(),
							lightXmlObject.getParentId(),
							lightXmlObject.getParentVersion());

			// map all cc by [id, value]
			for (LinkedList<LightXmlObjectType> lightXmlObjectList : mlqr
					.getResult().values()) {
				for (LightXmlObjectType ccLightXmlObject : lightXmlObjectList) {
					result.put(ccLightXmlObject.getId(), ccLightXmlObject);
				}
			}
		}
		return result;
	}

	public List<LightXmlObjectType> getControlConstructsLight()
			throws Exception {
		List<LightXmlObjectType> result = new ArrayList<LightXmlObjectType>();

		// all control construct schemes
		LightXmlObjectListDocument listDoc = DdiManager.getInstance()
				.getControlConstructSchemesLight(null, null, null, null);

		for (LightXmlObjectType lightXmlObject : listDoc
				.getLightXmlObjectList().getLightXmlObjectList()) {
			// all control constructs in scheme
			MaintainableLightLabelQueryResult mlqr = DdiManager.getInstance()
					.getInstrumentLabel(lightXmlObject.getId(),
							lightXmlObject.getVersion(),
							lightXmlObject.getParentId(),
							lightXmlObject.getParentVersion());
			for (LinkedList<LightXmlObjectType> lightXmlObjectList : mlqr
					.getResult().values()) {
				result.addAll(lightXmlObjectList);
			}
		}
		return result;
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
				"reusable__Label");
	}

	public QuestionConstructDocument getQuestionConstructByQuestionId(
			String queiId) throws Exception {
		StringBuilder query = new StringBuilder();
		query.append("for $element in ");
		query.append(PersistenceManager.getInstance().getResourcePath());
		query.append(getDdi3NamespaceHelper()
				.addFullyQualifiedNamespaceDeclarationToElements(
						"//QuestionConstruct"));
		query.append("where $element/");

		query.append(getDdi3NamespaceHelper()
				.addFullyQualifiedNamespaceDeclarationToElements(
						"datacollection__QuestionReference/"));

		query.append(getDdi3NamespaceHelper()
				.addFullyQualifiedNamespaceDeclarationToElements("ID"));
		query.append(" ='");
		query.append(queiId);
		query.append("' ");
		query.append(" return $element");

		List<String> result = PersistenceManager.getInstance().query(
				query.toString());

		QuestionConstructDocument doc = null;
		if (!result.isEmpty()) {
			return QuestionConstructDocument.Factory.parse(result.get(0));
		}
		return null;
	}

	public QuestionItemDocument getQuestionItembyUserId(String userId)
			throws Exception {
		StringBuilder query = new StringBuilder();
		query.append("for $element in ");
		query.append(PersistenceManager.getInstance().getResourcePath());
		query.append(getDdi3NamespaceHelper()
				.addFullyQualifiedNamespaceDeclarationToElements(
						"//QuestionItem"));
		query.append("where $element/");
		query.append(getDdi3NamespaceHelper()
				.addFullyQualifiedNamespaceDeclarationToElements("UserID"));
		query.append(" ='");
		query.append(userId);
		query.append("' ");
		query.append(" return $element");

		List<String> result = PersistenceManager.getInstance().query(
				query.toString());

		QuestionItemDocument doc = null;
		if (!result.isEmpty()) {
			return QuestionItemDocument.Factory.parse(result.get(0));
		}
		return null;
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
				"ControlConstructScheme", "Sequence", null, "reusable__Label");
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
				"ControlConstructScheme", "IfThenElse", null, "reusable__Label");
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
				"ControlConstructScheme", "RepeatUntil", null,
				"reusable__Label");
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
				"ControlConstructScheme", "RepeatWhile", null,
				"reusable__Label");
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
				"ControlConstructScheme", "Loop", null, "reusable__Label");
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
	public LightXmlObjectListDocument getLogicalProductsLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		LightXmlObjectListDocument lightXmlObjectListDocument = queryLightXmlBeans(
				id, version, parentId, parentVersion, "studyunit__StudyUnit",
				"logicalproduct__LogicalProduct", null, "reusable__Label");
		if (lightXmlObjectListDocument.getLightXmlObjectList()
				.getLightXmlObjectList().isEmpty()) {
			lightXmlObjectListDocument = queryLightXmlBeans(id, version,
					parentId, parentVersion, "*",
					"logicalproduct__LogicalProduct", null, "reusable__Label");
		}
		return lightXmlObjectListDocument;
	}

	public DataRelationshipDocument getDataRelationship(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		String text = queryElement(id, version, "DataRelationship", parentId,
				parentVersion, "logicalproduct__LogicalProduct");
		return (text == "" ? null : DataRelationshipDocument.Factory
				.parse(text));
	}

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
					parentId, parentVersion, "*", "CodeScheme", null,
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

	@Profiled(tag = "getCodesLight")
	public LightXmlObjectListDocument getCodesLight(String id, String version,
			String parentId, String parentVersion) throws Exception {
		return queryLightXmlBeans(id, version, parentId, parentVersion,
				"CodeScheme", "Code", null, "reusable__Label");
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

	public LightXmlObjectListDocument getCategorysLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		LightXmlObjectListDocument lightXmlObjectListDocument = queryLightXmlBeans(
				id, version, parentId, parentVersion, "CategoryScheme",
				"Category", null, "reusable__Label");
		if (lightXmlObjectListDocument.getLightXmlObjectList()
				.getLightXmlObjectList().isEmpty()) {
			lightXmlObjectListDocument = queryLightXmlBeans(id, version,
					parentId, parentVersion, "*", "Category", null,
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
				"reusable__Label");
		if (lightXmlObjectListDocument.getLightXmlObjectList()
				.getLightXmlObjectList().isEmpty()) {
			lightXmlObjectListDocument = queryLightXmlBeans(id, version,
					parentId, parentVersion, "//", "VariableScheme", null,
					"reusable__Label");
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

	public LightXmlObjectListDocument getVariablesLightPlus(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		XQuery xquery = new XQuery();
		xquery.function
				.append("declare function ddieditor:variname($element) { ");
		xquery.function
				.append("for $z in $element/*[namespace-uri()='ddi:logicalproduct:3_1' and local-name()='VariableName'] ");
		xquery.function
				.append("return <Custom option=\"lang\"  value=\"{string($z/@xml:lang)}\">{string($z/string())}</Custom>};");

		xquery.function
				.append("declare function ddieditor:varirep($element) { ");
		xquery.function
				.append("for $z in $element/*[namespace-uri()='ddi:logicalproduct:3_1' and local-name()='Representation'] ");
		xquery.function
				.append("return if(exists($z/*[namespace-uri()='ddi:logicalproduct:3_1' and local-name()='TextRepresentation'])) then <Custom>Text</Custom> else ");
		xquery.function
				.append("if(exists($z/*[namespace-uri()='ddi:logicalproduct:3_1' and local-name()='NumericRepresentation'])) then <Custom option=\"NumericTypeCodeType\" value=\"Double\">Numeric</Custom> else ");
		xquery.function
				.append("if(exists($z/*[namespace-uri()='ddi:logicalproduct:3_1' and local-name()='CodeRepresentation'])) then <Custom  value=\"{local-name($z/*/*)}\">{string($z/*/*)}</Custom> else ");
		xquery.function
				.append("if(exists($z/*[namespace-uri()='ddi:logicalproduct:3_1' and local-name()='DateTimeRepresentation'])) then <Custom>DateTime</Custom> else ''};");

		xquery.function
				.append("declare function ddieditor:queiref($element) { ");
		xquery.function
				.append("for $z in $element/*[namespace-uri()='ddi:logicalproduct:3_1' and local-name()='QuestionReference'] ");
		xquery.function.append("return ddieditor:ref($z)};");

		xquery.function.append("declare function ddieditor:ref($element) { ");
		xquery.function.append("for $a in $element/* ");
		xquery.function
				.append("return if(string($a)='') then '' else if(local-name($a) = 'Scheme') then ddieditor:refscheme($a) else  <Custom value=\"{local-name($a)}\">{string($a)}</Custom>};");

		xquery.function
				.append("declare function ddieditor:refscheme($element) { ");
		xquery.function.append("for $a in $element/* ");
		xquery.function
				.append("return if(string($a)='') then '' else <Custom type=\"Scheme\" value=\"{local-name($a)}\">{string($a)}</Custom>};");

		xquery.query.append("<CustomList type =\"Name\">");
		xquery.query.append("{ddieditor:variname($y)}");
		xquery.query.append("</CustomList>");

		xquery.query.append("<CustomList type =\"ValueRepresentation\">");
		xquery.query.append("{ddieditor:varirep($y)}");
		xquery.query.append("</CustomList>");

		xquery.query.append("<CustomList type =\"QuestionReference\">");
		xquery.query.append("{ddieditor:queiref($y)}");
		xquery.query.append("</CustomList>");

		LightXmlObjectListDocument lightXmlObjectListDocument = queryLightXmlBeans(
				id, version, parentId, parentVersion, "VariableScheme",
				"Variable", null, "reusable__Label", xquery);
		return lightXmlObjectListDocument;
	}

	public VariableDocument getVariableByVariableName(String varibleName)
			throws Exception {
		StringBuilder query = new StringBuilder();
		query.append("for $element in ");
		query.append(PersistenceManager.getInstance().getResourcePath());
		query.append(getDdi3NamespaceHelper()
				.addFullyQualifiedNamespaceDeclarationToElements("//Variable"));
		query.append("where $element/");
		query.append(getDdi3NamespaceHelper()
				.addFullyQualifiedNamespaceDeclarationToElements("VariableName"));
		query.append(" ='");
		query.append(varibleName);
		query.append("' ");
		query.append(" return $element");

		List<String> result = PersistenceManager.getInstance().query(
				query.toString());

		VariableDocument var = null;
		if (!result.isEmpty()) {
			return VariableDocument.Factory.parse(result.get(0));
		}
		return null;
	}

	//
	// physical data product
	//
	public MaintainableLightLabelQueryResult getRecordLayoutSchemeLabel(
			String id, String version, String parentId, String parentVersion)
			throws DDIFtpException {
		MaintainableLabelQuery query = new MaintainableLabelQuery(parentId,
				parentVersion, null);
		ParamatizedXquery paramatizedXquery = getQueryElement(id, version,
				"RecordLayoutScheme", parentId, parentVersion,
				"physicaldataproduct__PhysicalDataProduct");

		// if ((parentVersion == null || parentVersion.equals(""))
		// && (version == null || version.equals(""))) {
		// paramatizedXquery.getParameters()[paramatizedXquery
		// .getParameterSize() - 1] = "";
		// } else if (version == null || version.equals("")) {
		// String param = paramatizedXquery.getParameters()[paramatizedXquery
		// .getParameterSize() - 1];
		// paramatizedXquery.getParameters()[paramatizedXquery
		// .getParameterSize() - 1] = param.replace(
		// "and empty($child/@version)", "");
		// } else if ((parentVersion == null || parentVersion.equals(""))) {
		// String param = paramatizedXquery.getParameters()[paramatizedXquery
		// .getParameterSize() - 1];
		// paramatizedXquery.getParameters()[paramatizedXquery
		// .getParameterSize() - 1] = param.replace(
		// "empty($element/@version) and", "");
		// }
		query.setQuery(paramatizedXquery.getParamatizedQuery());

		String[] elements = { "RecordLayout", "ProprietaryRecordLayout" };
		query.setElementConversionNames(elements);

		query.setMaintainableTarget("RecordLayoutScheme");
		query.setStopElementNames(new String[] { "DataItem" });

		MaintainableLightLabelQueryResult maLightLabelQueryResult = queryMaintainableLightLabel(query);
		return maLightLabelQueryResult;
	}

	//
	// archive
	//
	public LightXmlObjectListDocument getArchivesLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		LightXmlObjectListDocument lightXmlObjectListDocument = queryLightXmlBeans(
				id, version, parentId, parentVersion, "studyunit__StudyUnit",
				"Archive", null, "reusable__Label");
		return lightXmlObjectListDocument;
	}

	public ArchiveDocument getAchive(String id, String version,
			String parentId, String parentVersion) throws Exception {
		String text = queryElement(id, version, "Archive", parentId,
				parentVersion, "studyunit__StudyUnit");
		return (text == "" ? null : ArchiveDocument.Factory.parse(text));
	}

	public LightXmlObjectListDocument getOrganizationsLight(String id,
			String version, String parentId, String parentVersion)
			throws Exception {
		LightXmlObjectListDocument lightXmlObjectListDocument = queryLightXmlBeans(
				id, version, parentId, parentVersion, "OrganizationScheme",
				"Organization", null, "OrganizationName");
		return lightXmlObjectListDocument;
	}
}
