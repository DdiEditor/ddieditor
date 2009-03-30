package org.ddialliance.ddieditor.logic.urn.ddi;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.xmlbeans.XmlObject;
import org.ddialliance.ddieditor.model.DdiManager;
import org.ddialliance.ddieditor.model.XQuery;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectListDocument;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;
import org.ddialliance.ddieditor.model.resource.TopURNDocument;
import org.ddialliance.ddieditor.model.resource.TopURNType;
import org.ddialliance.ddieditor.persistenceaccess.ParamatizedXquery;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceManager;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.ReflectionUtil;
import org.ddialliance.ddiftp.util.log.Log;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;
import org.ddialliance.ddiftp.util.xml.Urn;
import org.ddialliance.ddiftp.util.xml.XmlBeansUtil;

import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

/**
 * URN utility
 */
public class UrnUtil {
	private static Log log = LogFactory.getLog(LogType.SYSTEM, UrnUtil.class);
	public final static String CHECK_INITIAL_TOPURN = "checkInitialTopUrn-elementversion";
	public final static String CHECK_INITIAL_TOPURN_NO_ELEMENTVERSION = "checkInitialTopUrn-no-elementVersion";
	public final static String ELEMENT_BY_TOPURN = "element-by-topUrn";
	public final static String PARENT_ELEMENT_BY_TOPURN = "parent-element-by-topurn";

	public UrnUtil() {}
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
				lightXmlObject.getParentVersion());
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
	 * @return urn
	 * @throws DDIFtpException
	 */
	public static Urn getUrn(String elementName, String id, String version,
			String parentId, String parentVersion) throws DDIFtpException {
		boolean elementIsTopUrn = false;

		// construct parent element list
		List<String> parentElements = new LinkedList<String>();
		String initialParentElement = DdiManager.getInstance()
				.getParentElementName(elementName);
		if (initialParentElement != null) {
			String tmpParentElement = initialParentElement;
			parentElements.add(tmpParentElement);
			do {
				tmpParentElement = DdiManager.getInstance()
						.getParentElementName(tmpParentElement);
				parentElements.add(tmpParentElement);
			} while (!tmpParentElement.equals("DDIInstance"));
		} else {
			parentElements.add(elementName);
			// workaround to include DDIInstance
			elementIsTopUrn = true;
		}

		// determine topURN
		List<TopURNDocument> topUrns = PersistenceManager.getInstance()
				.getTopUrnsByWorkingResource();

		// check if element is topUrn
		TopURNDocument topUrn = null;
		for (TopURNDocument tmpTopUrn : topUrns) {
			if (tmpTopUrn.getTopURN().getElement().equals(elementName)
					&& tmpTopUrn.getTopURN().getId().equals(id)) {
				if (version != null) {
					if (tmpTopUrn.getTopURN().getVersion().equals(version)) {
						topUrn = tmpTopUrn;
						elementIsTopUrn = true;
						break;
					} else {
						continue;
					}
				}
				topUrn = tmpTopUrn;
				elementIsTopUrn = true;
				break;
			}
		}

		// determine parent topUrn
		if (topUrn == null) {
			for (String haystack : parentElements) {
				for (TopURNDocument tmpTopUrn : topUrns) {
					String needle = tmpTopUrn.getTopURN().getElement();
					if (haystack.equals(needle)) {
						// check if a parent is the correct eg. scheme parent
						if (haystack.equals(initialParentElement)
								&& tmpTopUrn.getTopURN().getId().equals(
										parentId)) {
							// check if it is the right version of topUrn
							if (tmpTopUrn.getTopURN().getVersion() != null
									&& !tmpTopUrn.getTopURN().getVersion()
											.equals(parentVersion)) {
								continue;
							}
							if (checkInitialTopUrn(tmpTopUrn.getTopURN(),
									elementName, id, version)) {
								topUrn = tmpTopUrn;
								break;
							}
						}
						if (!haystack.equals(initialParentElement)) {
							if (checkTopUrn(tmpTopUrn.getTopURN(), elementName,
									id, version, initialParentElement, parentId)) {
								topUrn = tmpTopUrn;
								break;
							}
						}
					}
				}
				// when found break loop
				if (topUrn != null) {
					break;
				}
			}
		}

		// topUrn not determined - error
		if (topUrn == null) {
			throw new DDIFtpException(
					"URN for: "
							+ elementName
							+ " with id: "
							+ id
							+ " Could not be generated do to structural errors, check DDI markup");
		}

		// construct urn
		Urn urn = new Urn();
		urn.setContainedElement(elementName);
		if (!elementIsTopUrn) {
			urn.setVersionableElementId(id);
		}
		if (!elementIsTopUrn && version != null && !version.equals("")) {
			urn.setVersionableElementVersion(version);
		} else if (!elementIsTopUrn) {
			urn.setVersionableElementVersion(topUrn.getTopURN().getVersion());
		}
		urn.setIdentifingAgency(topUrn.getTopURN().getAgency());
		urn.setMaintainableId(topUrn.getTopURN().getId());
		urn.setMaintainableVersion(topUrn.getTopURN().getVersion());
		urn.setPrefix("ddi");
		urn.setSchemaVersion(topUrn.getTopURN().getDdi().toString());
		return urn;
	}

	private static ParentElementList getParentElementList(String elementName) {
		boolean elementIsTopUrn = false;

		// construct parent element list
		List<String> parentElements = new LinkedList<String>();
		String initialParentElement = DdiManager.getInstance()
				.getParentElementName(elementName);
		if (initialParentElement != null) {
			String tmpParentElement = initialParentElement;
			parentElements.add(tmpParentElement);
			do {
				tmpParentElement = DdiManager.getInstance()
						.getParentElementName(tmpParentElement);
				parentElements.add(tmpParentElement);
			} while (!tmpParentElement.equals("DDIInstance"));
		} else {
			parentElements.add(elementName);
			// workaround to include DDIInstance
			elementIsTopUrn = true;
		}
		
		UrnUtil u = new UrnUtil();
		return u.new ParentElementList(parentElements, elementIsTopUrn);
	}
	
	private class ParentElementList {
		public ParentElementList(List<String> parentElements, boolean elementIsTopUrn) {
			this.parentElements = parentElements;
			this.elementIsTopUrn = elementIsTopUrn;
		}
		
		public boolean elementIsTopUrn = false;
		public List<String> parentElements = new LinkedList<String>();
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
		query.function.append(DdiManager.getInstance()
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
		query.function.append(DdiManager.getInstance()
				.addFullyQualifiedNamespaceDeclarationToElements(elementName));
		query.function.append("/@id='");
		query.function.append(elementId);
		query.function.append("'};");

		// topUrn lookup
		query.query.append(" for $x in ");
		query.query.append(PersistenceManager.getInstance().getResourcePath());
		query.query.append("/");
		query.query.append(DdiManager.getInstance()
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
		xQuery.setObject(2, DdiManager.getInstance()
				.addFullyQualifiedNamespaceDeclarationToElements(elementName));
		xQuery.setString(3, elementId);
		int i = 4;
		if (elementVersion != null && !elementVersion.equals("")) {
			xQuery.setString(i++, elementVersion);
		}
		xQuery.setObject(i++, PersistenceManager.getInstance()
				.getResourcePath());
		xQuery.setObject(i++, DdiManager.getInstance()
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
			lightXmlObjectList = (LightXmlObjectListDocument)ReflectionUtil.invokeMethod(DdiManager.getInstance(),
					methodName.toString(), false, urn.getElementId(), "", "", "");
		} catch (Exception e) {
			throw new DDIFtpException("Error on get light xml object by urn", e);
		}

		// which one ?
		ParentElementList parentElementList = getParentElementList(lightXmlObjectList.getLightXmlObjectList().getLightXmlObjectArray(0).getElement());
		
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
		if (urn.getElementId() == null || urn.getElementId().equals("")) {
			// fetch maintainable
		}

		// create lookup for element with top maintainables
		String initialParentElement = DdiManager.getInstance()
				.getParentElementName(urn.getContainedElement());

		List<TopURNDocument> topUrns = PersistenceManager.getInstance()
				.getTopUrnsByIdAndVersionByWorkingResource(
						urn.getIdentifingAgency(), urn.getMaintainableId(),
						urn.getMaintainableVersion());

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

		List<String> result = new ArrayList<String>();
		String xmlText = null;
		String version = null;
		for (TopURNDocument tmpTopUrn : topUrns) {
			// element lookup
			xQuery.clearParameters();
			xQuery.setObject(1, DdiManager.getInstance()
					.addFullyQualifiedNamespaceDeclarationToElements(
							urn.getContainedElement()));
			xQuery.setString(2, urn.getElementId());
			xQuery.setObject(3, PersistenceManager.getInstance()
					.getResourcePath());
			xQuery.setObject(4, DdiManager.getInstance()
					.addFullyQualifiedNamespaceDeclarationToElements(
							tmpTopUrn.getTopURN().getElement()));
			xQuery.setString(5, tmpTopUrn.getTopURN().getId());
			xQuery.setString(6, tmpTopUrn.getTopURN().getVersion());

			result = PersistenceManager.getInstance().query(
					xQuery.getParamatizedQuery());

			// element with right version
			if (!result.isEmpty()) {
				for (String xmlResult : result) {
					version = getVersion(xmlResult);
					// no version on element- inherit from topUrn
					if (version == null
							&& urn.getVersionableElementVersion().equals(
									urn.getMaintainableVersion())) {
						xmlText = xmlResult;
						break;
					}

					// element version
					if (version != null
							&& version.equals(urn.getVersionableElementVersion())) {
						xmlText = xmlResult;
						break;
					}
				}
			}
			if (xmlText != null) {
				break;
			}
		}
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
		// change to regexe
		// Pattern idPattern =
		// Pattern.compile("(([0-9])+(\\.([a-zA-Z0-9])+)*)");
		// Matcher matcher = idPattern.matcher(version);
		// matcher.matches();

		// boolean singleQuote = false;
		// int index = -1;
		// index = xml.indexOf("version=\"");
		// if (index < 0) {
		// index = xml.indexOf("version='");
		// singleQuote = !singleQuote;
		// }
		//
		// if (index > -1) {
		// int endIndex = -1;
		// if (singleQuote) {
		// endIndex = xml.indexOf("'", index);
		// } else {
		// endIndex = xml.indexOf("\"", index);
		// }
		// if (endIndex > -1) {
		// return xml.substring(index, endIndex);
		// }
		// }
		if (log.isDebugEnabled()) {
			log.debug("xml: " + xml);
		}

		try {
			VTDGen vg = new VTDGen();
			vg.setDoc(xml.getBytes());
			vg.parse(true);
			VTDNav vn = vg.getNav();
			
			int attrIndex = vn.getAttrVal("version");
			if (attrIndex > -1) {
				return vn.toNormalizedString(attrIndex);
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new DDIFtpException("Error getting version", e);
		}
	}
}
