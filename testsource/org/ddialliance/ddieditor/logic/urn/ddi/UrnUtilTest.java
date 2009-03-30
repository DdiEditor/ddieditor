package org.ddialliance.ddieditor.logic.urn.ddi;

import org.ddialliance.ddieditor.DdieditorTestCase;
import org.ddialliance.ddieditor.model.DdiManager;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceManager;
import org.ddialliance.ddiftp.util.xml.Urn;
import org.junit.Assert;
import org.junit.Test;

public class UrnUtilTest extends DdieditorTestCase {

	@Test
	public void getUrn() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);

		String urn2 = "urn:ddi:3.0:DataCollection=dda.dk:dd[1.0]";
		Urn urn = Urn2Util.getUrn("datacollection__DataCollection", "dd", null, null, null);
		
		urn = Urn2Util.getUrn("QuestionItem", "qi_1", "", "qs_", "");
		Assert.assertEquals(
				"urn:ddi:3.0:QuestionItem=dda.dk:dda-4755(1.0).qi_1(1.0)", urn
						.toUrnString());

		urn = Urn2Util.getUrn("QuestionItem", "qi_1", "", "qs_-2", "1.0.1");
		Assert.assertEquals(
				"urn:ddi:3.0:QuestionItem=dda.dk:qs_-2(1.0.1).qi_1(1.0.1)", urn
						.toUrnString());

		urn = Urn2Util.getUrn("QuestionItem", "qi_1", "", "qs_-2", "1.0.2");
		Assert.assertEquals(
				"urn:ddi:3.0:QuestionItem=hungobongo:qs_-2(1.0.2).qi_1(1.0.2)",
				urn.toUrnString());

		urn = Urn2Util.getUrn("QuestionItem", "qi_1", "3.0", "qs_-2", "1.0.2");
		Assert.assertEquals(
				"urn:ddi:3.0:QuestionItem=hungobongo:qs_-2(1.0.2).qi_1(3.0)",
				urn.toUrnString());

		urn = Urn2Util.getUrn("DDIInstance", "dda-4755", "1.0", null, null);
		Assert.assertEquals("urn:ddi:3.0:DDIInstance=dda.dk:dda-4755(1.0)", urn
				.toUrnString());

		urn = Urn2Util.getUrn("Category", "cat_1", "", "cats_40", "");
		Assert.assertEquals(
				"urn:ddi:3.0:Category=dda.dk:dda-4755(1.0).cat_1(1.0)", urn
						.toUrnString());

		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.SINGLE_MANINTAINABLE_QS_FD_NS_DOC);
		urn = Urn2Util.getUrn("QuestionScheme", "qs_", "1.0", null, null);
		Assert.assertEquals("urn:ddi:3.0:QuestionScheme=dda.dk:qs_(1.0)", urn
				.toUrnString());
	}

	@Test
	public void getObjectByUrn() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);

		Urn urn = new Urn(
				"urn:ddi:3.0:DataCollection.QuestionItem=dda.dk:dd-2[2.0].qi_1[1.0.1]");
		// XmlObject xmlObject = Urn2Util.getXmlObjectByUrn(urn);
		// Assert.assertTrue(xmlObject instanceof QuestionItemDocument);
		// Assert.assertEquals("qi_1", ((QuestionItemDocument) xmlObject)
		// .getQuestionItem().getId());

		LightXmlObjectType lightXmlObject = Urn2Util.getLightXmlObjectByUrn(urn);
		Assert.assertEquals("qi_1", lightXmlObject.getId());

		lightXmlObject = DdiManager.getInstance().getQuestionSchemesLight(null,
				null, null, null).getLightXmlObjectList()
				.getLightXmlObjectList().get(2);

		urn = Urn2Util.getUrn(lightXmlObject);
		Assert.assertEquals(
				"urn:ddi:3.0:QuestionScheme=hungobongo:qs_-2(1.0.2)", urn
						.toUrnString());
	}
}
