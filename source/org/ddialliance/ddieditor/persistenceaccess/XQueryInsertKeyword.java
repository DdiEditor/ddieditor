package org.ddialliance.ddieditor.persistenceaccess;

/**
 * Keywords for inserting node/ nodes with using the XQuery update facility
 * 
 * before: The new content precedes the target node.
 * 
 * after: The new content follows the target node.
 * 
 * as first into: The new content becomes the first child of the target node.
 * 
 * as last into: The new content becomes the last child of the target node.
 * 
 * into: The new content is inserted as the last child of the target node,
 * provided that this keyword is not used in an update expression that also
 * makes use of the keywords noted above. It that happens, the node is inserted
 * so that it does not interfere with the indicated position of the other new
 * nodes.
 * 
 * XQuery Update Facility
 * 
 * @see http://www.w3.org/TR/xquery-update-10
 */
public enum XQueryInsertKeyword {
	BEFORE("before"), AFTER("after"), AS_FIRST_NODE("as first into"), AS_LAST_NODE(
			"as last into"), INTO("into");
	private String keyword;

	private XQueryInsertKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getKeyWord() {
		return this.keyword;
	}
}
