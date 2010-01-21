package org.ddialliance.ddieditor.logic.identification;

import org.apache.xmlbeans.XmlOptions;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.DataCollectionDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.DataCollectionType;
import org.ddialliance.ddieditor.logic.identification.IdentificationManager;
import org.ddialliance.ddieditor.logic.identification.IdentificationManager.VersionChangeType;
import org.junit.Test;

public class IdentificationManagerTest {
	@Test
	public void addIdentification() throws Exception {
		DataCollectionDocument doc = DataCollectionDocument.Factory
				.newInstance();
		DataCollectionType type = doc.addNewDataCollection();
		type.setVersion("1.1.1");
		IdentificationManager.getInstance().addIdentification(type, "dataDoc", null);

		IdentificationManager.getInstance().addVersionInformation(type,
				VersionChangeType.MINIOR, "version change commet");

		XmlOptions ops = new XmlOptions();
		ops.put(XmlOptions.SAVE_PRETTY_PRINT);
		System.out.println(doc.xmlText(ops));
	}
}
