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
	
	private String parentId, parentVersion,	agency;

	public MaintainableLabelQuery(String parentId, String parentVersion,
			String agency) {
		this.parentId = parentId;
		this.parentVersion = parentVersion;
		this.agency = agency;
	}

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

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getParentVersion() {
		return parentVersion;
	}

	public void setParentVersion(String parentVersion) {
		this.parentVersion = parentVersion;
	}

	public String getAgency() {
		return agency;
	}

	public void setAgency(String agency) {
		this.agency = agency;
	}
}
