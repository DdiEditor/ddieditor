package org.ddialliance.ddieditor.logic.urn.ddi;

import org.ddialliance.ddi3.xml.xmlbeans.reusable.ReferenceType;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;

/**
 * Utility to access to reference information.<br>
 * Wrapping localName, id, agency and version<br>
 * To be supported ddi:urn
 */
public class ReferenceResolution {
	String id;
	String version;
	String agency;
	ReferenceType ref;
	String localName;

	public ReferenceResolution(ReferenceType ref) {
		this.ref = ref;

		// TODO urn
		
		// safeguard
		if (ref == null) {
			return;
		}

		// id
		if (ref.getIDArray(0) != null) {
			id = ref.getIDArray(0).getStringValue();
		}

		// version
		if (!ref.getVersionList().isEmpty()) {
			version = ref.getVersionArray(0).getStringValue();
		}

		// agency
		if (!ref.getIdentifyingAgencyList().isEmpty()) {
			agency = ref.getIdentifyingAgencyArray(0);
		}
	}

	public ReferenceResolution(LightXmlObjectType lightXmlObject) {
		ref = ReferenceType.Factory.newInstance();
		localName = lightXmlObject.getElement();

		// agency
		if (lightXmlObject.getAgency() != null
				&& !lightXmlObject.getAgency().equals("")) {
			ref.addIdentifyingAgency(lightXmlObject.getAgency());
		}

		// id
		id = lightXmlObject.getId();
		ref.addNewID().setStringValue(lightXmlObject.getId());

		// version
		if (lightXmlObject.getVersion() != null
				&& !lightXmlObject.getVersion().equals("")) {
			ref.addNewVersion().setStringValue(lightXmlObject.getVersion());
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getAgency() {
		return agency;
	}

	public void setAgency(String agency) {
		this.agency = agency;
	}

	public String getLocalName() {
		return localName;
	}

	public ReferenceType getReference() {
		return ref;
	}
}
