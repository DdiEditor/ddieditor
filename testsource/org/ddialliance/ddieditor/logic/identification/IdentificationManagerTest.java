package org.ddialliance.ddieditor.logic.identification;

import static org.junit.Assert.*;

import org.apache.xmlbeans.XmlOptions;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.DataCollectionDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.DataCollectionType;
import org.ddialliance.ddi3.xml.xmlbeans.logicalproduct.CodeSchemeReferenceDocument;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.ReferenceType;
import org.ddialliance.ddieditor.logic.identification.IdentificationManager;
import org.ddialliance.ddieditor.logic.identification.IdentificationManager.VersionChangeType;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;
import org.junit.Test;

public class IdentificationManagerTest {
	@Test
	public void addIdentification() throws Exception {
		DataCollectionDocument doc = DataCollectionDocument.Factory
				.newInstance();
		DataCollectionType type = doc.addNewDataCollection();
		type.setVersion("1.1.1");
		IdentificationManager.getInstance().addIdentification(type, "dataDoc",
				null);

		IdentificationManager.getInstance().addVersionInformation(type,
				VersionChangeType.MINIOR, "version change commet");

		XmlOptions ops = new XmlOptions();
		ops.put(XmlOptions.SAVE_PRETTY_PRINT);
		System.out.println(doc.xmlText(ops));
	}

	@Test
	public void addReferenceInformation() throws Exception {
		LightXmlObjectType lightXmlObject = LightXmlObjectType.Factory
				.newInstance();
		lightXmlObject.setElement("CodeScheme");
		lightXmlObject.setId("some-id");

		ReferenceType ref = IdentificationManager.getInstance()
				.addReferenceInformation(null, lightXmlObject);
		XmlOptions ops = new XmlOptions();
		ops.put(XmlOptions.SAVE_PRETTY_PRINT);
		System.out.println(ref.xmlText(ops));

//		CodeSchemeReferenceDocument doc = CodeSchemeReferenceDocument.Factory
//				.newInstance();
//		doc.addNewCodeSchemeReference();
//		doc.getCodeSchemeReference().addNewID();
//		doc.getCodeSchemeReference().getIDList().get(0).setStringValue(
//				"some-id");
//
//		System.out.println(doc.getCodeSchemeReference().xmlText(ops));
//		System.out.println(doc.xmlText(ops));
	}
}
