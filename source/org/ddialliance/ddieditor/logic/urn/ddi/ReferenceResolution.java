package org.ddialliance.ddieditor.logic.urn.ddi;

import org.ddialliance.ddi3.xml.xmlbeans.reusable.ReferenceType;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;

/**
 * Utility to access to reference information. Wrapping model, scheme, URN or id
 * reference
 */
public class ReferenceResolution {
	String id;
	ReferenceType ref;
	String localName;
	
	public ReferenceResolution(String id) {
		super();
		this.id = id;
	}

	public ReferenceResolution(ReferenceType ref) {
		this.ref = ref;

		// TODO urn

		// id
		if (ref.getIDArray(0) != null) {
			id = ref.getIDArray(0).getStringValue();
		}
	}

	public ReferenceResolution(LightXmlObjectType lightXmlObject) {
		ref = ReferenceType.Factory.newInstance();
		localName = lightXmlObject.getElement();
		
		// agency 
		// TODO ref.addIdentifyingAgency(lightXmlObject.getAgency());
		
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

	public String getLocalName() {
		return localName;
	}

	public ReferenceType getReference() {
		return ref;
	}
}
