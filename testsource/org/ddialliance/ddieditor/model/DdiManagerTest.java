package org.ddialliance.ddieditor.model;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.ddialliance.ddi_3_0.xml.xmlbeans.conceptualcomponent.ConceptDocument;
import org.ddialliance.ddi_3_0.xml.xmlbeans.datacollection.DataCollectionDocument;
import org.ddialliance.ddi_3_0.xml.xmlbeans.datacollection.QuestionItemDocument;
import org.ddialliance.ddi_3_0.xml.xmlbeans.datacollection.QuestionItemType;
import org.ddialliance.ddi_3_0.xml.xmlbeans.datacollection.QuestionSchemeDocument;
import org.ddialliance.ddi_3_0.xml.xmlbeans.logicalproduct.CategorySchemeDocument;
import org.ddialliance.ddi_3_0.xml.xmlbeans.logicalproduct.CodeSchemeDocument;
import org.ddialliance.ddi_3_0.xml.xmlbeans.reusable.InternationalStringType;
import org.ddialliance.ddieditor.DdieditorTestCase;
import org.ddialliance.ddieditor.model.conceptual.ConceptualElement;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectListDocument;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceManager;
import org.ddialliance.ddieditor.persistenceaccess.SchemeQuery;
import org.ddialliance.ddieditor.persistenceaccess.SchemeQueryResult;
import org.ddialliance.ddieditor.persistenceaccess.SchemeUpdateElement;
import org.ddialliance.ddieditor.persistenceaccess.dbxml.DbXmlManager;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class DdiManagerTest extends DdieditorTestCase {
	@Ignore
	public void getQuestionSchemesLight() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		Assert.assertEquals(3, DdiManager.getInstance()
				.getQuestionSchemesLight(null, null, null, null)
				.getLightXmlObjectList().getLightXmlObjectList().size());

		// single file
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.SINGLE_MANINTAINABLE_QS_FD_NS_DOC);
		Assert.assertEquals(1, DdiManager.getInstance()
				.getQuestionSchemesLight(null, null, null, null)
				.getLightXmlObjectList().getLightXmlObjectList().size());
	}

	@Ignore
	public void getQuestionScheme() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		String id = "qs_";
		String parentId = "dd";
		QuestionSchemeDocument test = DdiManager.getInstance()
				.getQuestionScheme(id, null, parentId, null);
		Assert.assertNotNull("Not found!", test);
		Assert.assertEquals(id, test.getQuestionScheme().getId());
	}

	@Ignore
	public void updateRetrieve() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);

		// qi_8
		QuestionItemDocument questionItem = DdiManager.getInstance()
				.getQuestionItem("qi_8", "", "qs_", "");
		InternationalStringType internationalString = questionItem
				.getQuestionItem().addNewName();
		internationalString.setStringValue("test");
		DdiManager.getInstance().updateElement(questionItem, "qi_8", "");

		questionItem = DdiManager.getInstance().getQuestionItem("qi_10", "",
				"qs_", "");
		Assert.assertNotNull(questionItem);
	}

	@Ignore
	public void insertUpdate() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);

		// insert
		QuestionItemDocument questionItem = QuestionItemDocument.Factory
				.newInstance();
		QuestionItemType questionItemType = questionItem.addNewQuestionItem();
		questionItemType.setId("newId");
		DdiManager.getInstance().createElement(questionItem, "qs_", null,
				"QuestionScheme");

		// update
		questionItem = DdiManager.getInstance().getQuestionItem("qi_8", "",
				"qs_", "");
		InternationalStringType internationalString = questionItem
				.getQuestionItem().addNewName();
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
				"qs_-2", "1.0.2");
		Assert.assertEquals(1, listDoc.getLightXmlObjectList()
				.getLightXmlObjectList().size());

		listDoc = DdiManager.getInstance().getQuestionItemsLight(null, null,
				"qs_", null);
		Assert.assertEquals(1468, listDoc.getLightXmlObjectList()
				.getLightXmlObjectList().size());

		listDoc = DdiManager.getInstance().getQuestionItemsLight("qi_1", "",
				"", "");
		Assert.assertEquals(4, listDoc.getLightXmlObjectList()
				.getLightXmlObjectList().size());
	}

	@Ignore
	public void getQuestionItem() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		QuestionItemDocument questionItemDocument = DdiManager.getInstance()
				.getQuestionItem("qi_1467", null, "qs_", null);
		Assert.assertNotNull(questionItemDocument);
		Assert.assertEquals("qi_1467", questionItemDocument.getQuestionItem()
				.getId());
	}

	@Ignore
	public void getConceptsLight() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		LightXmlObjectListDocument listDoc = DdiManager.getInstance()
				.getConceptsLight(null, null, null, null);
		Assert.assertEquals(13, listDoc.getLightXmlObjectList()
				.getLightXmlObjectList().size());
	}

	@Ignore
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

	@Ignore
	public void getDataCollectionLight() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.SINGLE_MANINTAINABLE_D_FD_NS_DOC);
		LightXmlObjectListDocument listDoc = DdiManager.getInstance()
				.getDataCollectionLight(null, null, null, null);
		Assert.assertEquals(1, listDoc.getLightXmlObjectList()
				.sizeOfLightXmlObjectArray());
	}

	@Ignore
	public void getCodeScheme() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		CodeSchemeDocument xmlObj = DdiManager.getInstance().getCodeScheme(
				"cods_7", null, "lp_1", null);
		Assert.assertNotNull(xmlObj.getCodeScheme());
	}

	@Ignore
	public void getCodeSchemesLight() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		LightXmlObjectListDocument listDoc = DdiManager.getInstance()
				.getCodeSchemesLight("", null, "lp_1", null);
		Assert.assertEquals(1180, listDoc.getLightXmlObjectList()
				.sizeOfLightXmlObjectArray());
	}

	@Ignore
	public void getCategorySchemesLight() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		LightXmlObjectListDocument listDoc = DdiManager.getInstance()
				.getCategorySchemesLight("", null, "", null);
		Assert.assertEquals(1181, listDoc.getLightXmlObjectList()
				.sizeOfLightXmlObjectArray());
	}

	@Ignore
	public void getCategoryScheme() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		CategorySchemeDocument xmlObj = DdiManager.getInstance()
				.getCategoryScheme("cats_7", null, "lp_1", null);
		Assert.assertNotNull(xmlObj.getCategoryScheme());
	}

	@Ignore
	public void getDataCollection() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		DataCollectionDocument dataCollection = DdiManager.getInstance()
				.getDataCollection("dd", null, "dda-4755", null);
		Assert.assertNotNull(dataCollection.getDataCollection());
	}

	@Ignore
	public void createElement() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		String id = "qs_";
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

	@Ignore
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

	@Ignore
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
	public void queryScheme() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);

		// query
		SchemeQueryResult result = DdiManager.getInstance()
				.getQuestionSchemeLabel("qs_", null, "dd", null);
		Assert.assertEquals("qs_", result.getId());
		Assert.assertEquals(2, result.getElements()[0].size());

		// crud setup
		result.cleanElements();
		SchemeUpdateElement update = new SchemeUpdateElement();
		update.setLocalName("Label");
		String updateValue = "godspeed";
		update.setValue("<r:Label xml:lang='da'>" + updateValue + "</r:Label>");
		update.setUpdateValue(1);

		// update
		List<SchemeUpdateElement> elements = new ArrayList<SchemeUpdateElement>();
		elements.add(update);
		DdiManager.getInstance().updateSchema(result, elements);;
		SchemeQueryResult test = DdiManager.getInstance()
				.getQuestionSchemeLabel("qs_", null, "dd", null);
		Assert.assertTrue("Not updated!", test.getElements()[0].get(0).indexOf(
				updateValue) > -1);

		// new
		elements.clear();
		update.setUpdateValue(SchemeUpdateElement.NEW);
		update.setValue("<r:Label xml:lang='en'>my new value</r:Label>");
		elements.add(update);
		DdiManager.getInstance().updateSchema(result, elements);
		test = DdiManager.getInstance().getQuestionSchemeLabel("qs_", null,
				"dd", null);
		Assert.assertEquals("New not inserted!", 3, test.getElements()[0]
				.size());

		// delete
		elements.clear();
		update.setUpdateValue(-3);
		elements.add(update);
		DdiManager.getInstance().updateSchema(result, elements);
		test = DdiManager.getInstance().getQuestionSchemeLabel("qs_", null,
				"dd", null);
		Assert.assertEquals("Not deleted!", 2, test.getElements()[0].size());
	}

	@Ignore
	public void querySchemeNoLabel() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);

		// test setup
		String query = "for $element in doc('dbxml:/big-doc.dbxml/big-doc.xml')//*[namespace-uri()='ddi:logicalproduct:3_0' and local-name()='LogicalProduct'] for $child in $element//*[namespace-uri()='ddi:logicalproduct:3_0' and local-name()='CategoryScheme']  where $element/@id = 'lp_1' and $child/@id = 'cats_7' and empty($child/@version) return $child";
		SchemeQuery schemeQuery = new SchemeQuery();
		schemeQuery.setQuery(query);
		schemeQuery.setElementNames(new String[] { "reusable__Label" });
		schemeQuery.setSchemeTarget("CategoryScheme");
		schemeQuery.setStopTag("Category");

		// result
		SchemeQueryResult result = DdiManager.getInstance().queryScheme(
				schemeQuery);
		Assert.assertEquals("cats_7", result.getId());
		Assert.assertEquals(0, result.getElements()[0].size());

		// crud setup
		result.cleanElements();
		SchemeUpdateElement update = new SchemeUpdateElement();
		update.setLocalName("Label");
		String updateValue = "godspeed";
		update.setValue("<r:Label xml:lang='da'>" + updateValue + "</r:Label>");
		update.setUpdateValue(0);

		// new
		List<SchemeUpdateElement> elements = new ArrayList<SchemeUpdateElement>();
		elements.clear();
		update.setUpdateValue(SchemeUpdateElement.NEW);
		update.setValue("<r:Label xml:lang='en'>my new value</r:Label>");
		elements.add(update);
		DdiManager.getInstance().updateSchema(result, elements);
		SchemeQueryResult test = DdiManager.getInstance().queryScheme(
				schemeQuery);
		Assert.assertEquals("New not inserted!", 1, test.getElements()[0]
				.size());
	}

	@Ignore
	public void getConceptSchemes() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				FULLY_DECLARED_NS_DOC);
		Assert.assertEquals(1, DdiManager.getInstance().getConceptSchemeLight(
				null, null, null, null).getLightXmlObjectList()
				.getLightXmlObjectList().size());
	}

	@Ignore
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
}
