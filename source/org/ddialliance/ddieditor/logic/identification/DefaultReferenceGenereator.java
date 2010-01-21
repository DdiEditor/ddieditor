package org.ddialliance.ddieditor.logic.identification;

import java.util.List;

import org.ddialliance.ddi3.xml.xmlbeans.reusable.ReferenceType;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;
import org.ddialliance.ddiftp.util.DDIFtpException;

/**
 * Default implementation
 */
public class DefaultReferenceGenereator implements ReferenceGenerator {
	@Override
	public ReferenceType addReferenceInformation(ReferenceType reference,
			LightXmlObjectType lightXmlObject, List rules)
			throws DDIFtpException {
		if (reference == null) {
			reference = createReference();
		}

		if (reference.getIDList().isEmpty()) {
			reference.addNewID();
		}

		reference.getIDList().get(0).setStringValue(lightXmlObject.getId());
		return reference;
	}

	@Override
	public ReferenceType createReference() {
		ReferenceType type = ReferenceType.Factory.newInstance();
		type.addNewID();

		return type;
	}
}
