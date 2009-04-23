package org.ddialliance.ddieditor.persistenceaccess;

public class SchemeQuery {
	private String query;
	private String schemeTarget;
	private String[] elementNames;
	private String stopTag;

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String[] getElementNames() {
		return elementNames;
	}

	public void setElementNames(String[] elementNames) {
		this.elementNames = elementNames;
	}

	public String getSchemeTarget() {
		return schemeTarget;
	}

	public void setSchemeTarget(String schemeTarget) {
		this.schemeTarget = schemeTarget;
	}

	public String getStopTag() {
		return stopTag;
	}

	public void setStopTag(String stopTag) {
		this.stopTag = stopTag;
	}
	
}
