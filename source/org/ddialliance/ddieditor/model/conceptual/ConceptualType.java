package org.ddialliance.ddieditor.model.conceptual;

public enum ConceptualType {
	STUDY(null), LOGIC_CONCEPT(null), LOGIC_CODE(null), LOGIC_CATEGORY(
			null), LOGIC_VARIABLE(null), LOGIC_UNIVERSE(null), LOGIC_QUESTION(null), LOGIC_INSTRUMENT(
			null), LOGIC_CONTROL_CONSTRUCT(null);

	private String resourceId;

	private ConceptualType(String resourceId) {
		this.resourceId = resourceId;
	}

	public String getResourceId() {
		return resourceId;
	}

	public ConceptualType setResourceId(String resourceId) {
		this.resourceId = resourceId;
		return this;
	}

	@Override
	public String toString() {
		return this.name() + this.getResourceId();
	}
}
