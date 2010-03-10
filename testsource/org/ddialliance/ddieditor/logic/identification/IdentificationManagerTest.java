package org.ddialliance.ddieditor.logic.identification;

import org.apache.xmlbeans.XmlOptions;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.DataCollectionDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.DataCollectionType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.InstrumentDocument;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.ReferenceType;
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

		// CodeSchemeReferenceDocument doc = CodeSchemeReferenceDocument.Factory
		// .newInstance();
		// doc.addNewCodeSchemeReference();
		// doc.getCodeSchemeReference().addNewID();
		// doc.getCodeSchemeReference().getIDList().get(0).setStringValue(
		// "some-id");
		//
		// System.out.println(doc.getCodeSchemeReference().xmlText(ops));
		// System.out.println(doc.xmlText(ops));
	}

	@Test
	public void getVersionInformation() throws Exception {
		String xml = "<Instrument xmlns=\"ddi:datacollection:3_1\" id=\"inst-1268157656095\" agency=\"dk.dda\" version=\"1.0.0\" versionDate=\"2010-03-09T19:00:56.099+01:00\" >"
				+ "<ddi1:VersionResponsibility xmlns:ddi1=\"ddi:reusable:3_1\">ddajvj</ddi1:VersionResponsibility>"
				+ "<ddi1:VersionRationale translated=\"false\" translatable=\"true\" xml:lang=\"DK\" xmlns:ddi1=\"ddi:reusable:3_1\">Version: 1.0.0, date: 2010-03-09T19:00:56.099+01:00</ddi1:VersionRationale>"
				+ "</Instrument>";
		InstrumentDocument doc = InstrumentDocument.Factory.parse(xml);
		IdentificationManager.getInstance().getVersionInformation(doc);
	}
}
