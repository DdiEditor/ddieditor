package org.ddialliance.ddieditor.model.namespace.ddi3;

/**
 * Enumeration representation of the DDI modules, with default prefixes:
 * 
 * <p>
 * xsi, http://www.w3.org/2001/XMLSchema-instance<br>
 * ddi, ddi:instance:3_0<br>
 * r, ddi:reusable:3_0<br>
 * xhtml, http://www.w3.org/1999/xhtml<br>
 * dce, ddi:dcelements:3_0<br>
 * dc, http://purl.org/dc/elements/1.1/<br>
 * a, ddi:archive:3_0<br>
 * g, ddi:group:3_0<br>
 * cm, ddi:comparative:3_0<br>
 * c, ddi:conceptualcomponent:3_0<br>
 * d, ddi:datacollection:3_0<br>
 * l, ddi:logicalproduct:3_0<br>
 * pd, ddi:physicaldataproduct:3_0<br>
 * ds, ddi:dataset:3_0<br>
 * pi, ddi:physicalinstance:3_0<br>
 * m1", "ddi:physicaldataproduct/ncube/normal:3_0<br>
 * m2", "ddi:physicaldataproduct/ncube/tabular:3_0<br>
 * m3", "ddi:physicaldataproduct/ncube/inline:3_0<br>
 * s, ddi:studyunit:3_0<br>
 * pr, ddi:profile:3_0
 * </p>
 */
public enum Ddi3NamespacePrefix {
	SCHEMA_INSTANCE("xsi", "http://www.w3.org/2001/XMLSchema-instance"), INSTANCE(
			"ns1", "ddi:instance:3_0"), REUSEABLE("r", "ddi:reusable:3_0"), XHTML(
			"xhtml", "http://www.w3.org/1999/xhtml"), DC_ELEMENTS("dce",
			"ddi:dcelements:3_0"), DUBLIN_CORE("dc",
			"http://purl.org/dc/elements/1.1/"), ARCHIVE("a", "ddi:archive:3_0"), GROUP(
			"g", "ddi:group:3_0"), COMPARATIVE("cm", "ddi:comparative:3_0"), CONCEPTUAL_COMPONENTS(
			"c", "ddi:conceptualcomponent:3_0"), DATA_COLLECTION("d",
			"ddi:datacollection:3_0"), LOGIAL_PRODUCT("l",
			"ddi:logicalproduct:3_0"), PHYSICAL_DATA_PRODUCT("pd",
			"ddi:physicaldataproduct:3_0"), DATA_SET("ds", "ddi:dataset:3_0"), PHYSICAL_INSTANCE(
			"pi", "ddi:physicalinstance:3_0"), NCUBE_NORMAL("m1",
			"ddi:physicaldataproduct/ncube/normal:3_0"), NCUBE_TABULAR("m2",
			"ddi:physicaldataproduct/ncube/tabular:3_0"), NCUBE_INLINE("m3",
			"ddi:physicaldataproduct/ncube/inline:3_0"), STUDY_UNIT("s",
			"ddi:studyunit:3_0"), PROFILE("pr", "ddi:profile:3_0");

	public static final String DEFAULT_NAMESPACE_URI = "http://www.ddialliance.org/DDI/schema/ddi3.0/instance.xsd";
	private String prefix;
	private String namespace;

	private Ddi3NamespacePrefix(String prefix, String namespace) {
		this.prefix = prefix;
		this.namespace = namespace;
	}

	public String getPrefix() {
		return this.prefix;
	}

	public String getNamespace() {
		return this.namespace;
	}

	public static Ddi3NamespacePrefix createNamespacePrefix(String prefix,
			String namespace) {
		Ddi3NamespacePrefix namespacePrefix = null;
		for (int i = 0; i < Ddi3NamespacePrefix.values().length; i++) {
			if (Ddi3NamespacePrefix.values()[i].namespace.equals(namespace)) {
				namespacePrefix = Ddi3NamespacePrefix.values()[i];
				if (prefix != null) {
					namespacePrefix.prefix = prefix;
				}
			}
		}
		return namespacePrefix;
	}

	public static Ddi3NamespacePrefix getNamespacePrefix(String namespace) {
		for (int i = 0; i < Ddi3NamespacePrefix.values().length; i++) {
			Ddi3NamespacePrefix namespacePrefix = Ddi3NamespacePrefix.values()[i];
			if (namespacePrefix.getNamespace().equals(namespace)) {
				return namespacePrefix;
			}
		}
		return null;
	}

	/**
	 * Retrieve the default DDI XQuery name space declaration
	 * 
	 * @return name space declaration
	 */
	public static String getDefaultDDINamespaceDeclaration() {
		StringBuffer result = new StringBuffer(
				"declare default element namespace \"");
		result.append(Ddi3NamespacePrefix.DEFAULT_NAMESPACE_URI);
		result.append("\"; ");
		return result.toString();
	}
	
	public static Ddi3NamespacePrefix getNamespaceByDefaultPrefix(String prefix) {
		for (int i = 0; i < Ddi3NamespacePrefix.values().length; i++) {
			Ddi3NamespacePrefix namespacePrefix = Ddi3NamespacePrefix.values()[i];
			if (namespacePrefix.getPrefix().equals(prefix)) {
				return namespacePrefix;
			}
		}
		return null;
	}
}
