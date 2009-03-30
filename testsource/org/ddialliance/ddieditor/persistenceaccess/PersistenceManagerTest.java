package org.ddialliance.ddieditor.persistenceaccess;

import java.util.List;

import org.ddialliance.ddieditor.DdieditorTestCase;
import org.ddialliance.ddieditor.model.resource.TopURNDocument;
import org.junit.Assert;
import org.junit.Test;

public class PersistenceManagerTest extends DdieditorTestCase {
	@Test
	public void getDocumentPath() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		String docPath = PersistenceManager.getInstance().getResourcePath();

		Assert.assertEquals("Not the same", "doc(\"dbxml:/"
				+ PersistenceManager.getInstance().getWorkingStorage()
						.getConnection() + "/"
				+ DdieditorTestCase.FULLY_DECLARED_NS_DOC + "\")", docPath);
	}
		
	@Test
	public void getTopUrnsByWorkingResource() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		List<TopURNDocument> topUrns = PersistenceManager.getInstance().getTopUrnsByWorkingResource();
		Assert.assertEquals(5, topUrns.size());
	}
	
	@Test
	public void getTopUrnByIdAndVersionByWorkingResource() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		List<TopURNDocument> topUrns = PersistenceManager.getInstance().getTopUrnsByIdAndVersionByWorkingResource("hungobongo", "qs_-2", "1.0.2");
		Assert.assertEquals("hungobongo", topUrns.get(0).getTopURN().getAgency());
	}
}
