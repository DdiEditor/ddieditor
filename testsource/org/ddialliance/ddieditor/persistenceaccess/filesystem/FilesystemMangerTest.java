package org.ddialliance.ddieditor.persistenceaccess.filesystem;

import java.io.File;
import java.util.List;

import junit.framework.Assert;

import org.ddialliance.ddieditor.DdieditorTestCase;
import org.ddialliance.ddieditor.model.resource.DDIResourceDocument;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceManager;
import org.junit.Test;

public class FilesystemMangerTest extends DdieditorTestCase {
	@Test
	public void exportResource() throws Exception {
		File file = new File("test.xml");
		file.deleteOnExit();
		PersistenceManager.getInstance().exportResoure(DdieditorTestCase.FULLY_DECLARED_NS_DOC, file);
		Assert.assertTrue(file.exists());
	}
	
	public void addResorce() throws Exception {
		// delete
		String id = SINGLE_MANINTAINABLE_QS_FD_NS_DOC;
		int index = id.indexOf(".");
		id = id.substring(index)+".dbxml";
		
		// readd
		PersistenceManager.getInstance().deleteResource(id);
		FilesystemManager.getInstance().addResource(
				new File("resources" + File.separator
						+ SINGLE_MANINTAINABLE_QS_FD_NS_DOC));
		
		// query
		List<DDIResourceDocument> list = PersistenceManager.getInstance().getResourceById(id);
		if (list.isEmpty()) {
			Assert.fail(id+" not refound!");
		}
	}
}
