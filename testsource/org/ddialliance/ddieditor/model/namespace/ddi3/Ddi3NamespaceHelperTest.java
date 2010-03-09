package org.ddialliance.ddieditor.model.namespace.ddi3;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.xmlbeans.XmlObject;
import org.ddialliance.ddieditor.DdieditorTestCase;
import org.ddialliance.ddieditor.model.DdiManager;
import org.junit.Assert;
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
}
