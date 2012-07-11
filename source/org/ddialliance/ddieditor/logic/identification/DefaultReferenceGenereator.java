package org.ddialliance.ddieditor.logic.identification;

import java.util.List;

import org.ddialliance.ddi3.xml.xmlbeans.reusable.ReferenceType;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;
import org.ddialliance.ddieditor.util.DdiEditorConfig;
import org.ddialliance.ddiftp.util.DDIFtpException;

/**
 * Default implementation
 */
public class DefaultReferenceGenereator implements ReferenceGenerator {
	@Override
	public ReferenceType addReferenceInformation(ReferenceType reference,
			LightXmlObjectType lightXmlObject, List rules)
			throws DDIFtpException {
		// urn
		reference.getURNList().clear();

		// id
		reference.getIDList().clear();
		reference.addNewID().setStringValue(lightXmlObject.getId());

		// agency
		reference.getIdentifyingAgencyList().clear();
		if (lightXmlObject.getAgency() != null
				&& !lightXmlObject.getAgency().equals("")) {
			reference.addNewIdentifyingAgency().setStringValue(
					lightXmlObject.getAgency());
		} else {
			reference.addNewIdentifyingAgency().setStringValue(
					DdiEditorConfig.get(DdiEditorConfig.DDI_AGENCY));
		}

		// version
		reference.getVersionList().clear();
		reference.addNewVersion().setStringValue(lightXmlObject.getVersion());

		return reference;
	}

	@Override
	public void applyReferenceElements(ReferenceType reference) {
		if (reference.getIDList().isEmpty()) {
			reference.addNewID();
			reference.addNewVersion();
			reference.addNewIdentifyingAgency();
		}
	}
}
