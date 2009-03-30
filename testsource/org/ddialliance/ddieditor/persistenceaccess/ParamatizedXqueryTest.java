package org.ddialliance.ddieditor.persistenceaccess;

import junit.framework.Assert;

import org.ddialliance.ddieditor.DdieditorTestCase;
import org.junit.Test;

public class ParamatizedXqueryTest extends DdieditorTestCase {

	@Test
	public void getParamatizedXquery() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		String query = "declare namespace ddieditor= \"http://ddialliance.org/ddieditor/ns\";declare function ddieditor:find_element($top_urn) { for $element in $top_urn where some $exact in $top_urn satisfies matches($element/@agency/string(), ?) and matches($element/@id/string(), ?) and matches($element/@version/string(), ?) return $element}; for $ddi_resource in "
				+ PersistenceManager.getInstance().getResourcePath()
				+ "//*[namespace-uri()='ddieditor-resoure-list' and local-name()='ResourceList']//*[namespace-uri()='ddieditor-resoure-list' and local-name()='Storage']//*[namespace-uri()='ddieditor-resoure-list' and local-name()='DDIResource'] where $ddi_resource/@orgName = ? return ddieditor:find_element($ddi_resource//*[namespace-uri()='ddieditor-resoure-list' and local-name()='TopURN'])";

		ParamatizedXquery parameterXquery = new ParamatizedXquery(query);
		parameterXquery.setString(1, "1");
		parameterXquery.setString(2, "2");
		parameterXquery.setString(3, "3");
		parameterXquery.setString(4, "4");
		PersistenceManager.getInstance().query(
				parameterXquery.getParamatizedQuery());

		// test clear parameters
		parameterXquery.clearParameters();
		
		// test getOnErrorXquery
		try {
			parameterXquery.getParamatizedQuery();
			Assert
					.fail("No error mgs on getParamatizedQuery with not all parameters set");
		} catch (Exception e) {
			// do nothing
		}
	}
}
