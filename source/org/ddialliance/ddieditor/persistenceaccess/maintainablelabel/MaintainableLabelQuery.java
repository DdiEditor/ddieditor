package org.ddialliance.ddieditor.persistenceaccess.maintainablelabel;

/**
 * Pojo for holding information on querying a maintainable for retrieval only of
 * label elements
 */
public class MaintainableLabelQuery {
	private String query;
	private String maintainableTarget;
	/** Holding values of conversion element names */
	private String[] elementConversionNames;
	private String[] stopElementNames;

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String[] getElementConversionNames() {
		return elementConversionNames;
	}

	public void setElementConversionNames(String[] elementConversionNames) {
		this.elementConversionNames = elementConversionNames;
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
