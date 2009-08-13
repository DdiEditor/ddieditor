package org.ddialliance.ddieditor.model.namespace.ddi3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ddialliance.ddieditor.model.DdiManager;
import org.junit.Assert;
import org.junit.Test;

public class Ddi3NamespaceGeneratorTest {
	@Test
	public void namespaceLookup() throws Exception {
		String namespace = null;
		String element = null;
		List<String> errorList = new ArrayList<String>();

		// create indices for identifiables
		for (Iterator<Object> iterator = DdiManager.getInstance()
				.getIdentifiables().keySet().iterator(); iterator.hasNext();) {
			element = (String) iterator.next();

			// check for missing elements in element ~ namespace list
			try {
				namespace = DdiManager.getInstance().getNamespaceByElement(
						element).getNamespace();
			} catch (Exception e) {
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
}
