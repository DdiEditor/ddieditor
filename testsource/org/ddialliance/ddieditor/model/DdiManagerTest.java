package org.ddialliance.ddieditor.model;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.xmlbeans.XmlObject;
import org.ddialliance.ddi3.xml.xmlbeans.conceptualcomponent.ConceptDocument;
import org.ddialliance.ddi3.xml.xmlbeans.conceptualcomponent.ConceptGroupDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.ControlConstructDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.ControlConstructSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.DataCollectionDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.InstrumentDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionItemDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionItemType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionSchemeType;
import org.ddialliance.ddi3.xml.xmlbeans.logicalproduct.CategorySchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.logicalproduct.CodeSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.logicalproduct.VariableDocument;
import org.ddialliance.ddi3.xml.xmlbeans.logicalproduct.VariableSchemeDocument;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.DateType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.InternationalStringType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.impl.CitationDocumentImpl;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.impl.NameDocumentImpl;
import org.ddialliance.ddieditor.DdieditorTestCase;
import org.ddialliance.ddieditor.logic.identification.IdentificationManager;
import org.ddialliance.ddieditor.model.conceptual.ConceptualElement;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectListDocument;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;
import org.ddialliance.ddieditor.model.namespace.ddi3.Ddi3NamespacePrefix;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceManager;
import org.ddialliance.ddieditor.persistenceaccess.maintainablelabel.MaintainableLabelQuery;
import org.ddialliance.ddieditor.persistenceaccess.maintainablelabel.MaintainableLabelQueryResult;
import org.ddialliance.ddieditor.persistenceaccess.maintainablelabel.MaintainableLabelUpdateElement;
import org.ddialliance.ddieditor.persistenceaccess.maintainablelabel.MaintainableLightLabelQueryResult;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.ReflectionUtil;
import org.ddialliance.ddiftp.util.Translator;
import org.junit.Assert;
import org.junit.Test;

public class DdiManagerTest extends DdieditorTestCase {
	@Test
	public void getDdiInstanceLight() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		Assert.assertEquals(1, DdiManager.getInstance().getDdiInstanceLight(
				null, null, null, null).getLightXmlObjectList()
				.getLightXmlObjectList().size());
	}

	@Test
	public void getConceptualComponentsLight() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				FULLY_DECLARED_NS_DOC);
		// FilesystemManager.getInstance().addResource(new
		// File("resources/import.xml"));
		LightXmlObjectListDocument result = DdiManager.getInstance()
				.getConceptualComponentsLight(null, null, null, null);

		Assert.assertEquals("Size not same", 1, result.getLightXmlObjectList()
				.getLightXmlObjectList().size());
		LightXmlObjectType type = result.getLightXmlObjectList()
				.getLightXmlObjectList().get(0);

		try {
			result = (LightXmlObjectListDocument) ReflectionUtil.invokeMethod(
					DdiManager.getInstance(), "getConceptualComponentsLight",
					false, new Object[] { "", "", type.getParentId(),
							type.getParentVersion() });
		} catch (Exception e) {
			throw new DDIFtpException(e);
		}
		Assert.assertEquals("Size not same", 1, result.getLightXmlObjectList()
				.getLightXmlObjectList().size());
	}

	@Test
	public void getQuestionSchemesLight() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		Assert.assertEquals(2, DdiManager.getInstance()
				.getQuestionSchemesLight(null, null, "dd", null)
				.getLightXmlObjectList().getLightXmlObjectList().size());

		// single file
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.SINGLE_MANINTAINABLE_QS_FD_NS_DOC);
		Assert.assertEquals(1, DdiManager.getInstance()
				.getQuestionSchemesLight(null, null, null, null)
				.getLightXmlObjectList().getLightXmlObjectList().size());
	}

	@Test
	public void getQuestionScheme() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		String id = "qs_1";
		String parentId = "dd";
		QuestionSchemeDocument test = DdiManager.getInstance()
				.getQuestionScheme(id, null, parentId, null);
		Assert.assertNotNull("Not found!", test);
		Assert.assertEquals(id, test.getQuestionScheme().getId());
	}

	@Test
	public void updateRetrieve() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);

		// qi_8
		QuestionItemDocument questionItem = DdiManager.getInstance()
				.getQuestionItem("qi_8", "", "qs_2", "");
		InternationalStringType internationalString = questionItem
				.getQuestionItem().addNewQuestionItemName();
		internationalString.setStringValue("test");
		DdiManager.getInstance().updateElement(questionItem, "qi_8", "");

		questionItem = DdiManager.getInstance().getQuestionItem("qi_10", "",
				"qs_2", "");
		Assert.assertNotNull(questionItem);
	}

	@Test
	public void insertUpdate() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);

		// insert
		QuestionItemDocument questionItem = QuestionItemDocument.Factory
				.newInstance();
		QuestionItemType questionItemType = questionItem.addNewQuestionItem();
		questionItemType.setId("newId");
		DdiManager.getInstance().createElement(questionItem, "qs_2", null,
				"QuestionScheme");

		// update
		questionItem = DdiManager.getInstance().getQuestionItem("qi_8", "",
				"qs_", "");
		InternationalStringType internationalString = questionItem
				.getQuestionItem().addNewQuestionItemName();
		internationalString.setStringValue("test");
		DdiManager.getInstance().updateElement(questionItem, "qi_8", "");

		// insert
		questionItem = QuestionItemDocument.Factory.newInstance();
		questionItemType = questionItem.addNewQuestionItem();
		questionItemType.setId("newNewId");
		DdiManager.getInstance().createElement(questionItem, "qs_", null,
				"QuestionScheme");

		// export
		PersistenceManager.getInstance().exportResoure(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC, new File("test.xml"));
	}

	@Test
	public void getQuestionItemsLight() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);

		LightXmlObjectListDocument listDoc = DdiManager.getInstance()
				.getQuestionItemsLight(null, null, null, null);
		Assert.assertEquals(1471, listDoc.getLightXmlObjectList()
				.getLightXmlObjectList().size());

		listDoc = DdiManager.getInstance().getQuestionItemsLight("qi_1", null,
				"qs_-2", "1.0.1");
		Assert.assertEquals(1, listDoc.getLightXmlObjectList()
				.getLightXmlObjectList().size());

		listDoc = DdiManager.getInstance().getQuestionItemsLight("qi_1", "3.0",
				"qs_2", "1.0.2");
		Assert.assertEquals(1, listDoc.getLightXmlObjectList()
				.getLightXmlObjectList().size());

		listDoc = DdiManager.getInstance().getQuestionItemsLight(null, null,
				"qs_2", null);
		Assert.assertEquals(1468, listDoc.getLightXmlObjectList()
				.getLightXmlObjectList().size());

		listDoc = DdiManager.getInstance().getQuestionItemsLight("qi_1", "",
				"", "");
		Assert.assertEquals(4, listDoc.getLightXmlObjectList()
				.getLightXmlObjectList().size());
	}

	@Test
	public void getQuestionItem() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		QuestionItemDocument questionItemDocument = DdiManager.getInstance()
				.getQuestionItem("qi_1467", null, "qs_2", null);
		Assert.assertNotNull(questionItemDocument);
		Assert.assertEquals("qi_1467", questionItemDocument.getQuestionItem()
				.getId());
	}

	@Test
	public void getConceptsLight() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		LightXmlObjectListDocument listDoc = DdiManager.getInstance()
				.getConceptsLight(null, null, null, null);
		Assert.assertEquals(13, listDoc.getLightXmlObjectList()
				.getLightXmlObjectList().size());
	}

	@Test
	public void getConcept() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		String id = "cs_1300";
		String parentId = "cs";
		ConceptDocument test = DdiManager.getInstance().getConcept(id, null,
				parentId, null);
		Assert.assertNotNull(test);
		Assert.assertEquals(id, test.getConcept().getId());
	}

	@Test
	public void getConceptGroup() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		String id = "csg_1";
		String parentId = "cs";
		ConceptGroupDocument test = DdiManager.getInstance().getConceptGroup(
				id, null, parentId, null);
		Assert.assertNotNull(test);
		Assert.assertEquals(id, test.getConceptGroup().getId());
	}

	@Test
	public void getConceptGroupsLight() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		String id = "csg_1";
		String parentId = "cs";
		LightXmlObjectListDocument listDoc = DdiManager.getInstance()
				.getConceptGroupsLight(null, null, null, null);
		Assert.assertEquals(1, listDoc.getLightXmlObjectList()
				.sizeOfLightXmlObjectArray());
	}

	@Test
	public void getDataCollectionLight() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.SINGLE_MANINTAINABLE_D_FD_NS_DOC);
		LightXmlObjectListDocument listDoc = DdiManager.getInstance()
				.getDataCollectionLight(null, null, null, null);
		Assert.assertEquals(1, listDoc.getLightXmlObjectList()
				.sizeOfLightXmlObjectArray());
	}

	@Test
	public void getCodeScheme() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		CodeSchemeDocument xmlObj = DdiManager.getInstance().getCodeScheme(
				"cods_7", null, "lp_1", null);
		Assert.assertNotNull(xmlObj.getCodeScheme());
	}

	@Test
	public void getCodeSchemesLight() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		LightXmlObjectListDocument listDoc = DdiManager.getInstance()
				.getCodeSchemesLight("", null, "lp_1", null);
		Assert.assertEquals(1180, listDoc.getLightXmlObjectList()
				.sizeOfLightXmlObjectArray());
	}

	@Test
	public void getCategorySchemesLight() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		LightXmlObjectListDocument listDoc = DdiManager.getInstance()
				.getCategorySchemesLight("", null, "", null);
		Assert.assertEquals(1181, listDoc.getLightXmlObjectList()
				.sizeOfLightXmlObjectArray());
	}

	@Test
	public void getCategoryScheme() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		CategorySchemeDocument xmlObj = DdiManager.getInstance()
				.getCategoryScheme("cats_7", null, "lp_1", null);
		Assert.assertNotNull(xmlObj.getCategoryScheme());
	}

	@Test
	public void getVariableScheme() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		VariableSchemeDocument xmlObj = DdiManager.getInstance()
				.getVariableScheme("vs_1", null, "lp_1", null);
		Assert.assertNotNull(xmlObj.getVariableScheme());
	}

	@Test
	public void getVariableSchemesLight() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		LightXmlObjectListDocument listDoc = DdiManager.getInstance()
				.getVariableSchemesLight("", null, "", null);
		Assert.assertEquals(1, listDoc.getLightXmlObjectList()
				.sizeOfLightXmlObjectArray());
	}

	@Test
	public void getVariable() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		VariableDocument xmlObj = DdiManager.getInstance().getVariable("v0001",
				null, "vs_1", null);
		Assert.assertNotNull(xmlObj.getVariable());
	}

	@Test
	public void getVariablesLight() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		LightXmlObjectListDocument listDoc = DdiManager.getInstance()
				.getVariableSchemesLight("", null, "", null);
		Assert.assertEquals(1, listDoc.getLightXmlObjectList()
				.sizeOfLightXmlObjectArray());
	}

	@Test
	public void getDataCollection() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		DataCollectionDocument dataCollection = DdiManager.getInstance()
				.getDataCollection("dd", null, "dda-4755", null);
		Assert.assertNotNull(dataCollection.getDataCollection());
	}

	@Test
	public void createLargeElement() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		String id = "qs2_";
		String newId = "qs_test";
		String parentId = "dd";
		QuestionSchemeDocument test = DdiManager.getInstance()
				.getQuestionScheme(id, null, parentId, null);
		test.getQuestionScheme().setId(newId);
		DdiManager.getInstance().createElement(test, parentId, null,
				"datacollection__DataCollection");

		LightXmlObjectListDocument lightXmlObjectListDocument = DdiManager
				.getInstance().getQuestionSchemesLight(null, null, null, null);
		Assert.assertEquals(4, lightXmlObjectListDocument
				.getLightXmlObjectList().getLightXmlObjectList().size());
	}

	@Test
	public void createScheme() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);

		String parentId = "dd";
		LightXmlObjectListDocument lightXmlObjectList = DdiManager
				.getInstance().getQuestionSchemesLight("", "", parentId, "");

		QuestionSchemeDocument doc = QuestionSchemeDocument.Factory
				.newInstance();
		QuestionSchemeType type = doc.addNewQuestionScheme();
		IdentificationManager.getInstance().addIdentification(type, "qs", null);
		IdentificationManager.getInstance().addVersionInformation(
				doc.getQuestionScheme(), null, null);

		DdiManager.getInstance().createElement(doc, parentId, null,
				"datacollection__DataCollection");

		LightXmlObjectListDocument lightXmlObjectListDocument = DdiManager
				.getInstance().getQuestionSchemesLight(null, null, null, null);
		Assert.assertEquals(3, lightXmlObjectListDocument
				.getLightXmlObjectList().getLightXmlObjectList().size());

		PersistenceManager.getInstance().exportResoure(FULLY_DECLARED_NS_DOC,
				new File("export-test.xml"));
	}

	@Test
	public void createElement() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);

		// retrieve template element
		String id = "qi_2000";
		String parentId = "qs2_";

		// question item
		QuestionItemDocument questionItemDoc = DdiManager.getInstance()
				.getQuestionItem("qi_1467", null, parentId, null);
		questionItemDoc.getQuestionItem().setId(id);

		// insert
		DdiManager.getInstance().createElement(questionItemDoc, parentId, null,
				"QuestionScheme");

		// test insert
		LightXmlObjectListDocument lightXmlObjectListDocument = DdiManager
				.getInstance().getQuestionItemsLight(id, null, parentId, null);
		Assert.assertTrue("Created object not found!",
				lightXmlObjectListDocument.getLightXmlObjectList()
						.getLightXmlObjectList().size() > 0);
		Assert
				.assertEquals(id, lightXmlObjectListDocument
						.getLightXmlObjectList().getLightXmlObjectList().get(0)
						.getId());

		// result ok, check for mulitiple questionitems

		// concepts
		id = "cs_1262";
		parentId = "cs";
		ConceptDocument concept = DdiManager.getInstance().getConcept(id, null,
				parentId, null);
		concept.getConcept().setId("cs_1300");
		DdiManager.getInstance().createElement(concept, parentId, null,
				"ConceptScheme");

		PersistenceManager.getInstance().exportResoure(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC,
				new File("createDocOut.xml"));
	}

	@Test
	public void updateElement() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.SINGLE_MANINTAINABLE_QS_FD_NS_DOC);

		String id = "qi_1";
		String newId = "new_id";
		String parentId = "qs_";
		QuestionItemDocument test = DdiManager.getInstance().getQuestionItem(
				id, null, parentId, "1.0");

		Assert.assertNotNull("Not found!", test);
		Assert
				.assertEquals("Id not equal!", id, test.getQuestionItem()
						.getId());

		test.getQuestionItem().setId(newId);
		DdiManager.getInstance().updateElement(test, id, null);

		test = DdiManager.getInstance().getQuestionItem(newId, null, parentId,
				"1.0");
		Assert.assertNotNull(test);
		Assert.assertEquals(newId, test.getQuestionItem().getId());
	}

	@Test
	public void deleteElement() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		String id = "qs_";
		String parentId = "dd";
		QuestionSchemeDocument test = DdiManager.getInstance()
				.getQuestionScheme(id, null, parentId, null);
		Assert.assertNotNull("Not found!", test);
		Assert.assertEquals(id, test.getQuestionScheme().getId());

		DdiManager.getInstance().deleteElement(test, parentId, null,
				"datacollection__DataCollection");
	}

	@Test
	public void queryMaintainableLabel() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		String localName = "reusable__Label";

		// query
		MaintainableLabelQueryResult result = DdiManager.getInstance()
				.getQuestionSchemeLabel("qs_", null, "dd", null);
		Assert.assertEquals("qs_", result.getId());
		Assert.assertEquals(2, result.getSubElementAsXml(localName).length);

		// update
		MaintainableLabelUpdateElement update = new MaintainableLabelUpdateElement();
		update.setLocalName(localName);
		String updateValue = "godspeed";
		update.setValue("<r:Label xml:lang='da'>" + updateValue + "</r:Label>");
		update.setCrudValue(1);

		List<MaintainableLabelUpdateElement> elements = new ArrayList<MaintainableLabelUpdateElement>();
		elements.add(update);
		DdiManager.getInstance().updateMaintainableLabel(result, elements);

		MaintainableLabelQueryResult test = DdiManager.getInstance()
				.getQuestionSchemeLabel("qs_", null, "dd", null);
		Assert.assertTrue("Not updated!", test.getSubElementAsXml(localName)[0]
				.indexOf(updateValue) > -1);

		// new
		elements.clear();
		update.setCrudValue(0);
		update.setValue("<r:Label xml:lang='en'>my new value</r:Label>");
		elements.add(update);
		DdiManager.getInstance().updateMaintainableLabel(result, elements);
		test = DdiManager.getInstance().getQuestionSchemeLabel("qs_", null,
				"dd", null);
		Assert.assertEquals("New not inserted!", 3, test
				.getSubElement(localName).length);

		// delete
		elements.clear();
		update.setCrudValue(-3);
		elements.add(update);
		DdiManager.getInstance().updateMaintainableLabel(result, elements);
		test = DdiManager.getInstance().getQuestionSchemeLabel("qs_", null,
				"dd", null);
		Assert.assertEquals("Not deleted!", 2,
				test.getSubElement(localName).length);
	}

	@Test
	public void queryMaintainableLabelWithNoLabel() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);

		// test setup
		String query = "for $element in doc('dbxml:/big-doc.dbxml/big-doc.xml')//*[namespace-uri()='ddi:logicalproduct:3_0' and local-name()='LogicalProduct'] for $child in $element//*[namespace-uri()='ddi:logicalproduct:3_0' and local-name()='CategoryScheme']  where $element/@id = 'lp_1' and $child/@id = 'cats_7' and empty($child/@version) return $child";
		MaintainableLabelQuery schemeQuery = new MaintainableLabelQuery("", "",
				"");
		schemeQuery.setQuery(query);
		schemeQuery
				.setElementConversionNames(new String[] { "reusable__Label" });
		schemeQuery.setMaintainableTarget("CategoryScheme");
		schemeQuery.setStopElementNames(new String[] { "Category" });

		// result
		MaintainableLabelQueryResult result = DdiManager.getInstance()
				.queryMaintainableLabel(schemeQuery);
		Assert.assertEquals("cats_7", result.getId());
		Assert.assertEquals(0, result.getSubElementAsXml("Label").length);

		// update
		MaintainableLabelUpdateElement crud = new MaintainableLabelUpdateElement();
		crud.setLocalName("Label");
		String updateValue = "godspeed";
		crud.setValue("<r:Label xml:lang='da'>" + updateValue + "</r:Label>");
		crud.setCrudValue(1);
		List<MaintainableLabelUpdateElement> elements = new ArrayList<MaintainableLabelUpdateElement>();
		elements.add(crud);

		try {
			// error on noting to update on
			DdiManager.getInstance().updateMaintainableLabel(result, elements);
			Assert.fail();
		} catch (DDIFtpException e) {
			// ok
			// e.printStackTrace();
		}

		crud.setCrudValue(-1);
		elements.add(crud);
		try {
			// error on no element to delete
			DdiManager.getInstance().updateMaintainableLabel(result, elements);
			Assert.fail();
		} catch (DDIFtpException e) {
			// ok
			// e.printStackTrace();
		}

		try {
			// error on wrong element to update on
			elements.get(0).setLocalName("hokuspokus");
			elements.get(0).setCrudValue(1);
			DdiManager.getInstance().updateMaintainableLabel(result, elements);
			Assert.fail();
		} catch (DDIFtpException e) {
			// ok
			// e.printStackTrace();
		}
	}

	@Test
	public void studyLabelEmptyNew() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				FULLY_DECLARED_NS_DOC);
		MaintainableLabelQueryResult result = DdiManager.getInstance()
				.getStudyLabel("dda-4755", null, "dda-4755", "1.0");

		NameDocumentImpl name = (NameDocumentImpl) result.getSubElement("Name")[0];

		// delete old
		MaintainableLabelUpdateElement crud = new MaintainableLabelUpdateElement();
		crud.setLocalName("Name");
		crud.setCrudValue(-1);
		List<MaintainableLabelUpdateElement> list = new ArrayList<MaintainableLabelUpdateElement>();
		list.add(crud);
		DdiManager.getInstance().updateMaintainableLabel(result, list);

		// new
		list.get(0).setCrudValue(0);
		list.get(0).setValue(name.xmlText());
		DdiManager.getInstance().updateMaintainableLabel(result, list);

		result = DdiManager.getInstance().getStudyLabel("dda-4755", null,
				"dda-4755", "1.0");
		Assert.assertEquals("Not inserted!", 1,
				result.getSubElement("Name").length);
	}

	@Test
	public void studyLabelCrud() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				FULLY_DECLARED_NS_DOC);
		MaintainableLabelQueryResult result = DdiManager.getInstance()
				.getStudyLabel("dda-4755", null, "dda-4755", "1.0");
		Assert.assertNotNull(result);

		// test exception on get non sub element
		try {
			result.getSubElementAsXml("hokuspokus");
			Assert.fail();
		} catch (Exception e) {
			// ok
			// e.printStackTrace();
		}

		// test all sub element are present
//		for (String key : result.getResult().keySet()) {
//			System.out.println(key + ": " + result.getResult().get(key) + "\n");
//			Assert.assertEquals("Unexpect size for: " + key, 1, result
//					.getResult().get(key).size());
//		}

		// update every first element
		List<MaintainableLabelUpdateElement> elements = new ArrayList<MaintainableLabelUpdateElement>();
		for (Entry<String, LinkedList<String>> entry : result.getResult()
				.entrySet()) {
			MaintainableLabelUpdateElement crud = new MaintainableLabelUpdateElement();
			crud.setCrudValue(1);
			crud.setLocalName(entry.getKey());
			Ddi3NamespacePrefix prefix = DdiManager.getInstance()
					.getDdi3NamespaceHelper().getNamespaceObjectByElement(
							result.getLocalNamesToConversionLocalNames().get(
									entry.getKey()));
			String xmlPrefix = prefix.getPrefix() + ":";
			crud.setValue("<" + xmlPrefix + entry.getKey() + ">test</"
					+ xmlPrefix + entry.getKey() + ">");
			elements.add(crud);
		}
		DdiManager.getInstance().updateMaintainableLabel(result, elements);

		// check update
		result = DdiManager.getInstance().getStudyLabel("dda-4755", null,
				"dda-4755", "1.0");
		for (Entry<String, LinkedList<String>> entry : result.getResult()
				.entrySet()) {
			LinkedList<String> list = entry.getValue();
			Assert.assertTrue("Empty list for " + entry.getKey() + "!", !list
					.isEmpty());
			Assert.assertTrue("Not updated " + entry.getKey() + "!", list
					.getFirst().indexOf("test") > -1);
		}

		// create element
		for (MaintainableLabelUpdateElement crud : elements) {
			crud.setCrudValue(MaintainableLabelUpdateElement.NEW);
		}
		DdiManager.getInstance().updateMaintainableLabel(result, elements);

		// check create
		result = DdiManager.getInstance().getStudyLabel("dda-4755", null,
				"dda-4755", "1.0");
		LinkedList<String> list = null;
		for (Entry<String, LinkedList<String>> entry : result.getResult()
				.entrySet()) {
			list = entry.getValue();
			Assert.assertEquals("Not create for " + entry.getKey() + "!", 2,
					list.size());
		}

		// delete every first element
		for (MaintainableLabelUpdateElement crud : elements) {
			crud.setCrudValue(-1);
		}
		DdiManager.getInstance().updateMaintainableLabel(result, elements);

		// check delete
		result = DdiManager.getInstance().getStudyLabel("dda-4755", null,
				"dda-4755", "1.0");
		list = null;
		for (Entry<String, LinkedList<String>> entry : result.getResult()
				.entrySet()) {
			list = entry.getValue();
			Assert.assertEquals("Not deleted " + entry.getKey() + "!", 1, list
					.size());
		}
	}

	@Test
	public void getStudyLabelAsXmlObjectPlusUpdate() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				FULLY_DECLARED_NS_DOC);
		MaintainableLabelQueryResult result = DdiManager.getInstance()
				.getStudyLabel("dda-4755", null, "dda-4755", "1.0");
		Assert.assertNotNull(result);
		XmlObject[] xmlObjs = null;
		for (String key : result.getResult().keySet()) {
			xmlObjs = result.getSubElement(key);
			Assert.assertNotNull(xmlObjs);
		}

		// update
		List<MaintainableLabelUpdateElement> list = new ArrayList<MaintainableLabelUpdateElement>();
		for (String key : result.getResult().keySet()) {
			list.add(new MaintainableLabelUpdateElement(result
					.getSubElement(key)[0], 1));
		}
		DdiManager.getInstance().updateMaintainableLabel(result, list);
	}

	@Test
	public void getStudyLabelCitationTest() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				FULLY_DECLARED_NS_DOC);
		MaintainableLabelQueryResult result = DdiManager.getInstance()
				.getStudyLabel("dda-4755", null, "dda-4755", "1.0");

		CitationDocumentImpl citation = (CitationDocumentImpl) result
				.getSubElement("Citation")[0];
		DateType date = citation.getCitation().addNewPublicationDate();
		date.setSimpleDate(Translator.formatIso8601DateTime(System
				.currentTimeMillis()));
		citation.getCitation().setLanguage("da");

		// update
		List<MaintainableLabelUpdateElement> list = new ArrayList<MaintainableLabelUpdateElement>();
		list.add(new MaintainableLabelUpdateElement(citation, 1));
		DdiManager.getInstance().updateMaintainableLabel(result, list);

		// export
		PersistenceManager.getInstance().exportResoure(FULLY_DECLARED_NS_DOC,
				new File("test-citation.xml"));
	}

	@Test
	public void getConceptSchemes() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				FULLY_DECLARED_NS_DOC);
		Assert.assertEquals(1, DdiManager.getInstance().getConceptSchemesLight(
				null, null, null, null).getLightXmlObjectList()
				.getLightXmlObjectList().size());
	}

	@Test
	public void getConceptualOverview() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				FULLY_DECLARED_NS_DOC);
		List<ConceptualElement> list = DdiManager.getInstance()
				.getConceptualOverview();
		Assert.assertNotNull(list);
		for (ConceptualElement conceptualElement : list) {
			System.out.println(conceptualElement);
		}
	}

	@Test
	public void getInstrument() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				FULLY_DECLARED_NS_DOC);
		InstrumentDocument instrument = DdiManager.getInstance().getInstrument(
				"i", null, "dd", null);
		Assert.assertNotNull(instrument);
	}

	@Test
	public void getInstrumentsLight() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		LightXmlObjectListDocument listDoc = DdiManager.getInstance()
				.getInstrumentsLight("i", null, "dd", null);
		Assert.assertTrue("No label!", !listDoc.getLightXmlObjectList()
				.getLightXmlObjectList().get(0).getLabelList().isEmpty());
	}

	@Test
	public void getInstrumentLabel() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		MaintainableLightLabelQueryResult result = DdiManager.getInstance()
				.getInstrumentLabel(null, null, null, null);
		System.out.println(result);
	}

	@Test
	public void getControlConstructScheme() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				FULLY_DECLARED_NS_DOC);
		ControlConstructSchemeDocument instrument = DdiManager.getInstance()
				.getControlConstructScheme("ctrl", null, "dd", null);
		Assert.assertNotNull(instrument);
	}

	@Test
	public void getControlConstructSchemeLight() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		LightXmlObjectListDocument listDoc = DdiManager.getInstance()
				.getControlConstructSchemesLight("ctrl", null, "dd", null);
		Assert.assertTrue("No label!", !listDoc.getLightXmlObjectList()
				.getLightXmlObjectList().get(0).getLabelList().isEmpty());
	}

	@Test
	public void getControlConstruct() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				FULLY_DECLARED_NS_DOC);
		ControlConstructDocument instrument = DdiManager.getInstance()
				.getQuestionConstruct("qc_1", null, "ctrl", null);
		Assert.assertNotNull("Not found!", instrument);
	}

	@Test
	public void getStatementItemLight() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		LightXmlObjectListDocument listDoc = DdiManager.getInstance()
				.getStatementItemsLight("si_998-1", null, "ctrl", null);
		System.out.println(listDoc.getLightXmlObjectList()
				.getLightXmlObjectList().get(0));
		Assert.assertTrue("No label!", !listDoc.getLightXmlObjectList()
				.getLightXmlObjectList().get(0).getLabelList().isEmpty());
	}
}
