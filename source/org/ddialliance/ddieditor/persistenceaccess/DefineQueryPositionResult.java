package org.ddialliance.ddieditor.persistenceaccess;

public class DefineQueryPositionResult {
	public XQueryInsertKeyword insertKeyWord;
	public StringBuilder query;

	public DefineQueryPositionResult() {
	}

	public DefineQueryPositionResult(XQueryInsertKeyword insertKeyWord,
			StringBuilder query) {
		super();
		this.insertKeyWord = insertKeyWord;
		this.query = query;
	}
}
