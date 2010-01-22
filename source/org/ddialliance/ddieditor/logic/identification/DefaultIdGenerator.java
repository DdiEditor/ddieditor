package org.ddialliance.ddieditor.logic.identification;

import org.ddialliance.ddiftp.util.DDIFtpException;

public class DefaultIdGenerator implements IdentificationGenerator {
	private String delimiter = "-";
	
	@Override
	public String generateId(String prefix, String postfix)
			throws DDIFtpException {
		StringBuffer id = new StringBuffer();
		if (prefix != null) {
			id.append(prefix);
			id.append(delimiter);
		}
		id.append(generateId());
		
		if (postfix!=null) {
			id.append(delimiter);
			id.append(postfix);
		}
		return id.toString();
	}

	private String generateId() throws DDIFtpException {
		return "" + System.currentTimeMillis();
	}

	@Override
	public String getUserId() throws DDIFtpException {
		return System.getProperty("user.name");
	}
}
