package org.ddialliance.ddieditor.model;


public class XQuery {
	public StringBuilder function = new StringBuilder("");
	public StringBuilder query = new StringBuilder("");
	public StringBuilder namespaceDeclaration = new StringBuilder("");

	public String getFullQueryString() {
		StringBuilder result = new StringBuilder(namespaceDeclaration
				.toString());
		result.append(function.toString());
		result.append(query.toString());

		return result.toString();
	}
}
