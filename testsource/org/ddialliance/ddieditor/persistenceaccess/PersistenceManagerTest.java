package org.ddialliance.ddieditor.persistenceaccess;

import java.util.List;

import org.ddialliance.ddieditor.DdieditorTestCase;
import org.ddialliance.ddieditor.model.resource.DDIResourceType;
import org.ddialliance.ddieditor.model.resource.ResourceListDocument;
import org.ddialliance.ddieditor.model.resource.StorageType;
import org.ddialliance.ddieditor.model.resource.TopURNType;
import org.junit.Assert;
import org.junit.Ignore;
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
	public void deleteResource() throws Exception {
		String orgName = SINGLE_MANINTAINABLE_D_FD_NS_DOC;
		PersistenceManager.getInstance().deleteResource(orgName);

		DDIResourceType ddiResource = PersistenceManager.getInstance()
				.getResourceByOrgName(orgName);
		Assert.assertNull("Not empty!", ddiResource);
	}

	@Test
	public void deleteStorage() throws Exception {
		String orgName = SINGLE_MANINTAINABLE_QS_FD_NS_DOC;
		StorageType storage = PersistenceManager.getInstance()
				.getStorageByResourceOrgName(orgName);
		String storageId = storage.getId();
		storage = null;

		PersistenceManager.getInstance().deleteStorage(storageId);
		List<StorageType> list = PersistenceManager.getInstance().getStorages();
		for (StorageType storageType : list) {
			Assert.assertNotSame(storageId, storageType.getId());
		}
	}

	@Test
	public void getStorages() throws Exception {
		List<StorageType> list = PersistenceManager.getInstance().getStorages();
	}

	@Test
	public void getResourceList() throws Exception {
		ResourceListDocument resourceList = PersistenceManager.getInstance()
				.getResourceList();
		Assert.assertNotNull("Not found!", resourceList);
	}
}
