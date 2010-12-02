package org.ddialliance.ddieditor.logic.identification;

import org.ddialliance.ddiftp.util.DDIFtpException;

/**
 * Defines identification handling
 */
public interface IdentificationGenerator {
	/**
	 * Generates an ID 
	 * @param prefix id prefix
	 * @param postfix id postfix
	 * @return generated id
	 * @throws DDIFtpException
	 */
	public String generateId(String prefix, String postfix)
			throws DDIFtpException;

	/**
	 * Access delimiter
	 * @return delimiter
	 */
	public String getDelimiter();
	
	/**
	 * Retrieve user identification
	 * @return user ID
	 * @throws DDIFtpException
	 */
	public String getUserId() throws DDIFtpException;
}
