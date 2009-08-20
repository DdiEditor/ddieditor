package org.ddialliance.ddieditor.persistenceaccess.maintainablelabel;

/**
 * Pojo for holding information on querying a maintainable for retrieval only of
 * label elements
 */
public class MaintainableLabelQuery {
	private String query;
	private String maintainableTarget;
	private String[] elementNames;
	private String[] stopElementNames;

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

	public String getMaintainableTarget() {
		return maintainableTarget;
	}

	public void setMaintainableTarget(String maintainableTarget) {
		this.maintainableTarget = maintainableTarget;
	}

	public String[] getStopElementNames() {
		return stopElementNames;
	}

	public void setStopElementNames(String[] stopElementNames) {
		this.stopElementNames = stopElementNames;
	}
}
