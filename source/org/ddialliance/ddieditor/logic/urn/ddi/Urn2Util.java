package org.ddialliance.ddieditor.logic.urn.ddi;

import org.apache.xmlbeans.XmlObject;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.ReferenceType;
import org.ddialliance.ddieditor.model.DdiManager;
import org.ddialliance.ddieditor.model.XQuery;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectListDocument;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;
import org.ddialliance.ddieditor.model.relationship.ElementDocument.Element;
import org.ddialliance.ddieditor.model.relationship.ParentDocument.Parent;
import org.ddialliance.ddieditor.model.resource.TopURNType;
import org.ddialliance.ddieditor.persistenceaccess.ParamatizedXquery;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceManager;
import org.ddialliance.ddieditor.util.DdiEditorRefUtil;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.log.Log;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;
import org.ddialliance.ddiftp.util.xml.Urn;
import org.ddialliance.ddiftp.util.xml.XmlBeansUtil;

public class Urn2Util {
	private static Log log = LogFactory.getLog(LogType.SYSTEM, Urn2Util.class);
	public final static String CHECK_INITIAL_TOPURN = "checkInitialTopUrn-elementversion";
	public final static String CHECK_INITIAL_TOPURN_NO_ELEMENTVERSION = "checkInitialTopUrn-no-elementVersion";
	public final static String ELEMENT_BY_TOPURN = "element-by-topUrn";
	public final static String PARENT_ELEMENT_BY_TOPURN = "parent-element-by-topurn";

	public Urn2Util() {
	}

	/**
	 * Create the URN based on a light XML object
	 * 
	 * @param lightXmlObject
	 *            to create URN for
	 * @return urn
	 * @throws DDIFtpException
	 */
	public static Urn getUrn(LightXmlObjectType lightXmlObject)
			throws DDIFtpException {
		return getUrn(lightXmlObject.getElement(), lightXmlObject.getId(),
				lightXmlObject.getVersion(), lightXmlObject.getParentId(),
				lightXmlObject.getParentVersion(), lightXmlObject.getAgency());
	}

	/**
	 * Create the URN for a DDI element
	 * 
	 * @param elementName
	 *            name of element
	 * @param id
	 *            of element
	 * @param version
	 *            of element
	 * @param parentId
	 *            id of parent element
	 * @param parentVersion
	 *            version of parent
	 * @param agency
	 * @return urn
	 * @throws DDIFtpException
	 */
	public static Urn getUrn(String elementName, String id, String version,
			String parentId, String parentVersion, String agency)
			throws DDIFtpException {
		Urn urn = new Urn();
		boolean isMaintainable = DdiManager.getInstance()
				.getDdi3NamespaceHelper().isMaintainable(elementName);

		// maintainable
		if (isMaintainable) {
			// type and id
			urn.setMaintainableElement(DdiManager.getInstance()
					.getDdi3NamespaceHelper()
					.getCleanedElementName(elementName));
			urn.setMaintainableId(id);
		}
		// contained
		else {
			// type and id
			urn.setContainedElement(DdiManager.getInstance()
					.getDdi3NamespaceHelper()
					.getCleanedElementName(elementName));
			urn.setContainedElementId(id);

		}

		// version
		if (version != null && !version.equals("")) {
			if (isMaintainable) {
				urn.setMaintainableVersion(version);
			} else {
				urn.setContainedElementVersion(version);
			}
		}

		// parent maintainable
		if (!isMaintainable) {
			Element elementUrnList = DdiManager.getInstance()
					.getDdi3NamespaceHelper().getElementParents(elementName);
			// TODO better selection than just first element
			for (Parent parent : elementUrnList.getParentList()) {
				urn.setMaintainableElement(parent.getId());
			}
			if (parentId != null && !parentId.equals("")) {
				urn.setMaintainableId(parentId);
			}
			if (parentVersion != null && !parentVersion.equals("")) {
				urn.setMaintainableVersion(parentVersion);
			}
		}

		// agency
		if (agency == null) {
			// query for agency on parent element
			try {
				LightXmlObjectType lightXmlObject = DdiManager.getInstance()
						.getAgency(parentId, parentVersion);
				agency = lightXmlObject.getAgency();
			} catch (Exception e) {
				throw new DDIFtpException(e);
			}
		}
		urn.setIdentifingAgency(agency);
		return urn;
	}

	/**
	 * Check the top urn to determine if element subject to URN resolution is
	 * part of top urn element tree
	 * 
	 * @param topUrn
	 *            top URN
	 * @param elementName
	 *            name of element
	 * @param elementId
	 *            id of element
	 * @param elementVersion
	 *            version of element
	 * @param initialParentElement
	 *            parent element of element
	 * @param parentId
	 *            parent id of element
	 * @return success
	 * @throws DDIFtpException
	 */
	private static boolean checkTopUrn(TopURNType topUrn, String elementName,
			String elementId, String elementVersion,
			String initialParentElement, String parentId)
			throws DDIFtpException {
		if (initialParentElement == null || initialParentElement.equals("")) {
			// test to see if topUrn equals element
			if (topUrn.getElement().equals(elementName)
					&& topUrn.getId().equals(elementId)) {
				return true;
			} else {
				return false;
			}
		}

		// initial parent and element lookup
		XQuery query = new XQuery();
		query.namespaceDeclaration.append(DdiManager.FUNCTION_NS_DECLARATION);
		query.function
				.append("declare function ddieditor:find_element($top_urn) {");
		query.function.append(" for $parent_element in $top_urn");
		query.function.append("/");
		query.function.append(DdiManager
				.getInstance()
				.getDdi3NamespaceHelper()
				.addFullyQualifiedNamespaceDeclarationToElements(
						initialParentElement));
		query.function.append(" where $parent_element/@id='");
		query.function.append(parentId);
		query.function.append("'");
		if (elementVersion != null && !elementVersion.equals("")) {
			query.function.append(" and $parent_element/@version='");
			query.function.append(elementVersion);
			query.function.append("'");
		}
		query.function.append(" return $parent_element/");
		query.function.append(DdiManager.getInstance().getDdi3NamespaceHelper()
				.addFullyQualifiedNamespaceDeclarationToElements(elementName));
		query.function.append("/@id='");
		query.function.append(elementId);
		query.function.append("'};");

		// topUrn lookup
		query.query.append(" for $x in ");
		query.query.append(PersistenceManager.getInstance().getResourcePath());
		query.query.append("/");
		query.query.append(DdiManager
				.getInstance()
				.getDdi3NamespaceHelper()
				.addFullyQualifiedNamespaceDeclarationToElements(
						topUrn.getElement()));
		query.query.append(" where $x/@id = '");
		query.query.append(topUrn.getId());
		query.query.append("' and $x/@version = '");
		query.query.append(topUrn.getVersion());
		query.query.append("' return ddieditor:find_element($x)");

		boolean result = PersistenceManager.getInstance().querySingleBoolean(
				query.getFullQueryString());
		return result;
	}

	private static boolean checkInitialTopUrn(TopURNType topUrn,
			String elementName, String elementId, String elementVersion)
			throws DDIFtpException {

		// element lookup
		ParamatizedXquery xQuery = null;
		if (elementVersion != null && !elementVersion.equals("")) {
			xQuery = PersistenceManager.getInstance().getParamatizedQuery(
					CHECK_INITIAL_TOPURN);
			if (xQuery == null) {
				xQuery = new ParamatizedXquery(
						xQueryCheckInitialTopUrnQuery(true));
				PersistenceManager.getInstance().setParamatizedQuery(
						CHECK_INITIAL_TOPURN, xQuery);
			}
		} else {
			xQuery = PersistenceManager.getInstance().getParamatizedQuery(
					CHECK_INITIAL_TOPURN_NO_ELEMENTVERSION);
			if (xQuery == null) {
				xQuery = new ParamatizedXquery(
						xQueryCheckInitialTopUrnQuery(false));
				PersistenceManager.getInstance().setParamatizedQuery(
						CHECK_INITIAL_TOPURN_NO_ELEMENTVERSION, xQuery);
			}
		}

		xQuery.setObject(1, DdiManager.FUNCTION_NS_DECLARATION);
		xQuery.setObject(2, DdiManager.getInstance().getDdi3NamespaceHelper()
				.addFullyQualifiedNamespaceDeclarationToElements(elementName));
		xQuery.setString(3, elementId);
		int i = 4;
		if (elementVersion != null && !elementVersion.equals("")) {
			xQuery.setString(i++, elementVersion);
		}
		xQuery.setObject(i++, PersistenceManager.getInstance()
				.getResourcePath());
		xQuery.setObject(
				i++,
				DdiManager
						.getInstance()
						.getDdi3NamespaceHelper()
						.addFullyQualifiedNamespaceDeclarationToElements(
								topUrn.getElement()));
		xQuery.setString(i++, topUrn.getId());
		xQuery.setString(i, topUrn.getVersion());

		boolean result = PersistenceManager.getInstance().querySingleBoolean(
				xQuery.getParamatizedQuery());
		return result;
	}

	private static String xQueryCheckInitialTopUrnQuery(boolean elementVersion) {
		XQuery query = new XQuery();
		query.function
				.append("? declare function ddieditor:find_element($top_urn) { for $element in $top_urn /? where $element/@id=?");

		if (elementVersion) {
			query.function.append(" and $element/@version=?");
		}
		query.function.append(" return $element};");

		// topUrn lookup
		query.query
				.append(" for $x in ?/? where $x/@id = ?  and $x/@version = ? return ddieditor:find_element($x)");
		return query.getFullQueryString();
	}

	public static XmlObject getXmlObjectByUrn(Urn urn) throws DDIFtpException {
		String xmlText = getElementByUrn(urn, false);
		StringBuilder className = new StringBuilder(DdiManager.getInstance()
				.getDdi3NamespaceHelper()
				.getModuleNameByElement(urn.getContainedElement()));
		className.append(".");
		className.append(urn.getContainedElement());
		return XmlBeansUtil.openDDI(xmlText, null, className.toString());
	}

	public static LightXmlObjectType getLightXmlObjectByUrn(Urn urn)
			throws DDIFtpException {
		String parentId = getElementByUrn(urn, true);

		// invoke by reflection
		StringBuilder methodName = new StringBuilder();
		// getQuestionItem(String id, String version, String parentId, String
		// parentVersion)
		methodName.append("get");
		methodName.append(urn.getContainedElement());
		methodName.append("sLight");

		LightXmlObjectListDocument lightXmlObjectList = null;
		try {
			lightXmlObjectList = (LightXmlObjectListDocument) DdiEditorRefUtil
					.invokeMethod(DdiManager.getInstance(),
							methodName.toString(), false,
							urn.getContainedElementId(), "", "", "");
		} catch (Exception e) {
			throw new DDIFtpException("Error on get light xml object by urn", e);
		}

		// which one ?
		// ParentElementList parentElementList =
		// getParentElementList(lightXmlObjectList
		// .getLightXmlObjectList().getLightXmlObjectArray(0).getElement());

		return (LightXmlObjectType) lightXmlObjectList;
	}

	private static String getElementByUrn(Urn urn, boolean lightXmlObject)
			throws DDIFtpException {
		// urn validation
		urn.parseUrn(urn.toUrnString());

		// check for top maintainables
		if (urn.getContainedElement().equals("DDIInstance")) {
			// fetch ddiinstance
			// return ddiinstance
		}
		if (urn.getContainedElementId() == null
				|| urn.getContainedElementId().equals("")) {
			// fetch maintainable
		}

		// create lookup for element with top maintainables
		String initialParentElement = null;
		// DdiManager.getInstance()
		// .getDdi3NamespaceHelper().getParentElementName(
		// urn.getContainedElement());
		//
		// List<TopURNType> topUrns = PersistenceManager.getInstance()
		// .getTopUrnsByIdAndVersionByWorkingResource(
		// urn.getIdentifingAgency(), urn.getMaintainableId(),
		// urn.getMaintainableVersion());

		// retrieve element
		ParamatizedXquery xQuery;
		if (lightXmlObject) {
			xQuery = PersistenceManager.getInstance().getParamatizedQuery(
					PARENT_ELEMENT_BY_TOPURN);
			if (xQuery == null) {
				xQuery = new ParamatizedXquery(xQueryParentElementByTopUrn());
				PersistenceManager.getInstance().setParamatizedQuery(
						PARENT_ELEMENT_BY_TOPURN, xQuery);
			}
		} else
		// xmlobject
		{
			xQuery = PersistenceManager.getInstance().getParamatizedQuery(
					ELEMENT_BY_TOPURN);
			if (xQuery == null) {
				xQuery = new ParamatizedXquery(xQueryElementByTopUrn());
				PersistenceManager.getInstance().setParamatizedQuery(
						ELEMENT_BY_TOPURN, xQuery);
			}
		}

		// List<String> result = new ArrayList<String>();
		String xmlText = null;
		String version = null;
		// for (TopURNType tmpTopUrn : topUrns) {
		// // element lookup
		// xQuery.clearParameters();
		// xQuery.setObject(1, DdiManager.getInstance()
		// .getDdi3NamespaceHelper()
		// .addFullyQualifiedNamespaceDeclarationToElements(
		// urn.getContainedElement()));
		// xQuery.setString(2, urn.getContainedElementId());
		// xQuery.setObject(3, PersistenceManager.getInstance()
		// .getResourcePath());
		// xQuery.setObject(4, DdiManager.getInstance()
		// .getDdi3NamespaceHelper()
		// .addFullyQualifiedNamespaceDeclarationToElements(
		// tmpTopUrn.getElement()));
		// xQuery.setString(5, tmpTopUrn.getId());
		// xQuery.setString(6, tmpTopUrn.getVersion());
		//
		// result = PersistenceManager.getInstance().query(
		// xQuery.getParamatizedQuery());
		//
		// // element with right version
		// if (!result.isEmpty()) {
		// for (String xmlResult : result) {
		// version = getVersion(xmlResult);
		// // no version on element- inherit from topUrn
		// if (version == null
		// && urn.getContainedElementVersion().equals(
		// urn.getMaintainableVersion())) {
		// xmlText = xmlResult;
		// break;
		// }
		//
		// // element version
		// if (version != null
		// && version.equals(urn.getContainedElementVersion())) {
		// xmlText = xmlResult;
		// break;
		// }
		// }
		// }
		// if (xmlText != null) {
		// break;
		// }
		// }
		if (log.isDebugEnabled()) {
			log.debug("Version: " + version + " xmlText: " + xmlText);
		}
		return xmlText;
	}

	private static String xQueryElementByTopUrn() {
		XQuery query = new XQuery();
		query.namespaceDeclaration.append(DdiManager.FUNCTION_NS_DECLARATION);
		query.function
				.append("declare function ddieditor:find_element($top_urn) {");
		query.function
				.append(" for $element in $top_urn/? where $element/@id=? return $element};");

		// topUrn lookup
		query.query
				.append(" for $x in ?/? where $x/@id =? and $x/@version =? return ddieditor:find_element($x)");
		return query.getFullQueryString();
	}

	private static String xQueryParentElementByTopUrn() {
		XQuery query = new XQuery();
		query.namespaceDeclaration.append(DdiManager.FUNCTION_NS_DECLARATION);
		query.function
				.append("declare function ddieditor:find_element($top_urn) {");
		query.function
				.append(" for $element in $top_urn/? where $element/@id=? return <parent id='{$element/parent::node()/@id/string()}' version='{$element/parent::node()/@version/string()}' />};");

		// topUrn lookup
		query.query
				.append(" for $x in ?/? where $x/@id =? and $x/@version =? return ddieditor:find_element($x)");
		return query.getFullQueryString();
	}

	private static String getVersion(String xml) throws DDIFtpException {
		return XmlBeansUtil.getXmlAttributeValue(xml, "versio=\"");
	}

	public static ReferenceResolution getIdByReference(ReferenceType reference) {
		// Attrs: URI, isExternal, isReference, lateBound, objectLanguage,
		// sourceContext
		// Content: Module?, Scheme?, (URN | (ID, IdentifyingAgency?, Version?))
		if (!reference.getIDList().isEmpty()) {
			return new ReferenceResolution(reference);
		}
		return null;
	}
}
