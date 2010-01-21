package org.ddialliance.ddieditor.logic.identification;

import java.util.List;

import org.ddialliance.ddi3.xml.xmlbeans.reusable.ReferenceType;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;
import org.ddialliance.ddiftp.util.DDIFtpException;

/**
 * Defines reference handling
 */
public interface ReferenceGenerator {
	/**
	 * Add reference information 
	 * @param reference reference to add to, if NULL a new reference is created
	 * @param lightXmlObject reference to add from
	 * @param rules list of reference rules e.g. all ways use URN's 
	 * @return changed reference
	 * @throws DDIFtpException
	 */
	public ReferenceType addReferenceInformation(ReferenceType reference,
			LightXmlObjectType lightXmlObject, List rules) throws DDIFtpException;

	/**
	 * Creates a reference
	 * @return created reference
	 */
	public ReferenceType createReference();
}
