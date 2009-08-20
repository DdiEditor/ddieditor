package org.ddialliance.ddieditor.persistenceaccess.maintainablelabel;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.xmlbeans.XmlObject;
import org.ddialliance.ddieditor.model.DdiManager;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.xml.XmlBeansUtil;

/**
 * Accessors for maintainable label query and its containing sub elements
 */
public class MaintainableLabelQueryResult {
	private String id;
	private String version;
	private String agency;

	private String query;
	private Map<String, LinkedList<String>> result = new HashMap<String, LinkedList<String>>();

	public MaintainableLabelQueryResult() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getAgency() {
		return agency;
	}

	public void setAgency(String agency) {
		this.agency = agency;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	/**
	 * Retrieve a sub elements as XML
	 * 
	 * @param elementName
	 *            sub element to retrieve
	 * @return array of XML
	 */
	public String[] getSubElementAsXml(String elementName) {
		return result.get(elementName).toArray(new String[] {});
	}

	/**
	 * Retrieve a sub elements as XmlObjects
	 * 
	 * @param elementName
	 *            sub element to retrieve
	 * @return array of XmlObjects
	 * @throws DDIFtpException
	 */
	public XmlObject[] getSubElement(String elementName) throws DDIFtpException {
		// guard
		if (result.get(elementName).isEmpty()) {
			return new XmlObject[] {};
		}

		// build class name
		StringBuilder className = new StringBuilder(DdiManager.getInstance()
				.getDdi3NamespaceHelper().getModuleNameByElement(elementName));
		className.append(".");
		className.append(DdiManager.getInstance().getDdi3NamespaceHelper()
				.getCleanedElementName(elementName));

		// transformation
		int count = 0;
		int size = result.get(elementName).size();
		XmlObject[] xmlObjects = new XmlObject[size];
		for (String xmlText : result.get(elementName)) {
			xmlObjects[count] = XmlBeansUtil.openDDI(DdiManager.getInstance()
					.getDdi3NamespaceHelper().substitutePrefixesFromElements(
							xmlText), null, className.toString());
			count++;
		}
		return xmlObjects;
	}

	/**
	 * Retrieve result of query
	 * 
	 * @return map with key sub element name, list of sub elements as XML
	 */
	public Map<String, LinkedList<String>> getResult() {
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("Map: ");
		result.append(result);
		result.append(", query: ");
		result.append(query);
		return result.toString(); 
	}
}
