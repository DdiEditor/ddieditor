package org.ddialliance.ddieditor.persistenceaccess;

import java.util.List;

public class SchemeQueryResult {
	private String id;
	private String version;
	private String agency;

	private String query; 
	private String elementNames[];
	private List<String>[] elements;

	public SchemeQueryResult() {
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

	public String[] getElementNames() {
		return elementNames;
	}

	public void setElementNames(String[] elementNames) {
		this.elementNames = elementNames;
	}

	public List<String>[] getElements() {
		return elements;
	}

	public void setElements(List<String>[] elements) {
		this.elements = elements;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
	
	public void cleanElements() {
		for (int i = 0; i < elements.length; i++) {
			List<String> element = elements[i];
			for (String string : element) {
				string = "";
			}
		}
	}
}
