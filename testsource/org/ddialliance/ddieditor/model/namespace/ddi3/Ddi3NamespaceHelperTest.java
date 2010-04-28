package org.ddialliance.ddieditor.model.namespace.ddi3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.xmlbeans.XmlObject;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.DataCollectionDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionSchemeDocument;
import org.ddialliance.ddieditor.DdieditorTestCase;
import org.ddialliance.ddieditor.model.DdiManager;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class Ddi3NamespaceHelperTest extends DdieditorTestCase {
	@Test
	public void namespaceLookup() throws Exception {
		String namespace = null;
		Ddi3NamespacePrefix ddi3NamespacePrefix;
		String element = null;
		List<String> errorList = new ArrayList<String>();

		// create indices for identifiables
		for (Iterator<Object> iterator = DdiManager.getInstance()
				.getDdi3NamespaceHelper().getIdentifiables().keySet()
				.iterator(); iterator.hasNext();) {
			element = (String) iterator.next();

			// check for missing elements in element ~ namespace list
			try {
				ddi3NamespacePrefix = DdiManager.getInstance()
						.getDdi3NamespaceHelper().getNamespaceObjectByElement(
								element);
				namespace = ddi3NamespacePrefix.getNamespace();
				System.out.println(ddi3NamespacePrefix);
			} catch (Exception e) {
				e.printStackTrace();
				errorList.add(element);
				continue;
			}
		}

		StringBuffer errorStr = new StringBuffer();
		for (String string : errorList) {
			errorStr.append(string + ", ");
			System.out.println("Namespacelookup error on: " + string);
		}
		Assert.assertTrue("Namespacelookup error on: " + errorStr.toString(),
				errorList.isEmpty());
	}

	// TODO:
	// Namespacelookup error on: physicaldataproduct_ncube_inline__RecordLayout,
	// physicaldataproduct_ncube_inline__NCubeInstance,
	// physicaldataproduct_ncube_tabular__NCubeInstance,
	// ProprietaryRecordLayout, DDIProfile,
	// physicaldataproduct_ncube_normal__NCubeInstance,
	// physicaldataproduct_ncube_tabular__RecordLayout,
	// physicaldataproduct_ncube_normal__RecordLayout,

	@Test
	public void isMaintainalbel() throws Exception {
		DdiManager.getInstance().setWorkingDocument(FULLY_DECLARED_NS_DOC);
		XmlObject xmlObject = DdiManager.getInstance().getConceptScheme("cs",
				null, null, null);
		Assert.assertTrue("Not !", DdiManager.getInstance()
				.getDdi3NamespaceHelper().isMaintainable(xmlObject));
	}

	@Test
	public void getDuplicateConvention() throws Exception {
		QuestionSchemeDocument qsDoc = QuestionSchemeDocument.Factory
				.newInstance();
		String result = DdiManager.getInstance().getDdi3NamespaceHelper()
				.getDuplicateConvention(
						qsDoc.schemaType().getDocumentElementName());

		Assert.assertEquals("QuestionScheme", result);

		DataCollectionDocument dcDoc = DataCollectionDocument.Factory
				.newInstance();
		result = DdiManager.getInstance().getDdi3NamespaceHelper()
				.getDuplicateConvention(
						dcDoc.schemaType().getDocumentElementName());

		Assert.assertEquals("datacollection__DataCollection", result);
	}

	@Test
	public void substitutePrefixesFromElements() throws Exception {
		String xml = "<ddi:QuestionItem id=\"quei-1271925620862\" version=\"1.0.0\" versionDate=\"2010-04-22T10:40:20.864+02:00\" xmlns:ddi=\"ddi:datacollection:3_1\"><ddi1:VersionResponsibility xmlns:ddi1=\"ddi:reusable:3_1\">ddajvj</ddi1:VersionResponsibility><ddi1:VersionRationale translated=\"false\" translatable=\"true\" xml:lang=\"DK\" xmlns:ddi1=\"ddi:reusable:3_1\">Version: 1.0.0, date: 2010-04-20T15:50:23.586+02:00</ddi1:VersionRationale><ddi:ConceptReference><ddi1:ID xmlns:ddi1=\"ddi:reusable:3_1\"/></ddi:ConceptReference></ddi:QuestionItem>";
		String result = DdiManager.getInstance().getDdi3NamespaceHelper()
				.substitutePrefixesFromElements(xml);
	}
	
	@Test
	public void substitutePrefixesFromEmtyElement() throws Exception {
		String xml = "<ddi1:ID xmlns:ddi1=\"ddi:reusable:3_1\"/>";
		String result = DdiManager.getInstance().getDdi3NamespaceHelper()
				.substitutePrefixesFromElements(xml);
	}
}
