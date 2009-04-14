package org.ddialliance.ddieditor.persistenceaccess;

import static org.junit.Assert.*;

import java.util.List;

import org.ddialliance.ddieditor.DdieditorTestCase;
import org.ddialliance.ddieditor.model.resource.DDIResourceDocument;
import org.ddialliance.ddieditor.model.resource.DDIResourceType;
import org.ddialliance.ddieditor.model.resource.ResourceListDocument;
import org.ddialliance.ddieditor.model.resource.StorageDocument;
import org.ddialliance.ddieditor.model.resource.StorageType;
import org.ddialliance.ddieditor.model.resource.TopURNDocument;
import org.ddialliance.ddieditor.model.resource.TopURNType;
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
		List<TopURNType> topUrns = PersistenceManager.getInstance()
				.getTopUrnsByWorkingResource();
		Assert.assertEquals(5, topUrns.size());
	}

	@Test
	public void getTopUrnByIdAndVersionByWorkingResource() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		List<TopURNType> topUrns = PersistenceManager.getInstance()
				.getTopUrnsByIdAndVersionByWorkingResource("hungobongo",
						"qs_-2", "1.0.2");
		Assert.assertEquals("hungobongo", topUrns.get(0)
				.getAgency());
	}

	@Test
	public void deleteResource() throws Exception {
		String orgName = SINGLE_MANINTAINABLE_QS_FD_NS_DOC + ".xml";
		PersistenceManager.getInstance().deleteResource(orgName);
		PersistenceManager.getInstance().commitAllResources();

		DDIResourceType  ddiResource= PersistenceManager.getInstance()
				.getResourceByOrgName(orgName);
		Assert.assertNotNull("Not empty!", ddiResource);
	}
	
	@Test
	public void getStorages() throws Exception {
		List<StorageType> list = PersistenceManager.getInstance().getStorages();
	}
	
	@Test
	public void getResourceList() throws Exception {
		ResourceListDocument resourceList = PersistenceManager.getInstance().getResourceList();
		Assert.assertNotNull("Not found!", resourceList);
	}
}
