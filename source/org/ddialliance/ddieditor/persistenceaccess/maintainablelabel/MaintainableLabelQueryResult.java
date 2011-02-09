package org.ddialliance.ddieditor.persistenceaccess.maintainablelabel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlObject;
import org.ddialliance.ddieditor.model.DdiManager;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.xml.XmlBeansUtil;

/**
 * Accessors for maintainable label query and its containing sub elements
 */
public class MaintainableLabelQueryResult {
	private String localName;
	private String id;
	private String version;
	private String agency;
	/** Map of local element name ~ conversion element name **/
	private LinkedHashMap<String, String> localNamesToConversionLocalNames = new LinkedHashMap<String, String>();
	private String query;
	/** Map of local element name ~ list of XML **/
	private Map<String, LinkedList<String>> result = new HashMap<String, LinkedList<String>>();

	public LinkedHashMap<String, String> getLocalNamesToConversionLocalNames() {
		return localNamesToConversionLocalNames;
	}

	public void setLocalNamesToConversionLocalNames(
			LinkedHashMap<String, String> localNamesToConversionLocalNames) {
		this.localNamesToConversionLocalNames = localNamesToConversionLocalNames;
	}

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

	public String getLocalName() {
		return localName;
	}

	public void setLocalName(String localName) {
		this.localName = localName;
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
	public String[] getSubElementAsXml(String elementName)
			throws DDIFtpException {
		List<String> list = result.get(elementName);

		// null exception handling
		if (list == null) {
			StringBuilder use = new StringBuilder();
			for (Iterator<String> iterator = result.keySet().iterator(); iterator
					.hasNext();) {
				String key = iterator.next();
				use.append("'");
				use.append(key);
				use.append("'");
				if (iterator.hasNext()) {
					use.append(", ");
				}
			}
			throw new DDIFtpException("Sub element: '" + elementName
					+ "' does not exist. The following sub elements exists: "
					+ use.toString(), new Throwable());
		}

		return list.toArray(new String[] {});
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
		if (result.get(elementName) == null
				|| result.get(elementName).isEmpty()) {
			return new XmlObject[] {};
		}

		// build class name
		StringBuilder className = new StringBuilder(DdiManager
				.getInstance()
				.getDdi3NamespaceHelper()
				.getModuleNameByElement(
						localNamesToConversionLocalNames.get(elementName)));
		className.append(".");
		className.append(DdiManager.getInstance().getDdi3NamespaceHelper()
				.getCleanedElementName(elementName));

		// transformation
		int count = 0;
		int size = result.get(elementName).size();
		XmlObject[] xmlObjects = new XmlObject[size];
		for (String xmlText : result.get(elementName)) {
			xmlObjects[count] = XmlBeansUtil.openDDI(
					DdiManager.getInstance().getDdi3NamespaceHelper()
							.substitutePrefixesFromElements(xmlText), null,
					className.toString());
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
		StringBuilder result = new StringBuilder("Local name: ");
		result.append(localName);
		result.append(", map: ");
		result.append(this.result.size());
		result.append(", query: ");
		result.append(query);
		return result.toString();
	}
}
