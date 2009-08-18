package org.ddialliance.ddieditor.persistenceaccess.maintainablelabel;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.xmlbeans.XmlObject;

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

	public String[] getSubElementAsXml(String elementName) {
		return result.get(elementName).toArray(new String[] {});
	}

	public XmlObject[] getSubElement(String elementName) {
		int size = result.get(elementName).size();
		XmlObject[] xmlObjects = new XmlObject[size];
		for (String xmlString : result.get(elementName)) {
			// xml conersion
		}
		return xmlObjects;
	}

	public void cleanElements() {
		result.clear();
	}

	public Map<String, LinkedList<String>> getResult() {
		return result;
	}
}
