package org.ddialliance.ddieditor.persistenceaccess.filesystem;

import java.io.File;

import junit.framework.Assert;

import org.ddialliance.ddieditor.DdieditorTestCase;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceManager;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.junit.Test;

public class FilesystemMangerTest extends DdieditorTestCase {
	@Test
	public void exportResource() throws Exception {
		File file = new File("test.xml");
		file.deleteOnExit();
		PersistenceManager.getInstance().exportResoure(DdieditorTestCase.FULLY_DECLARED_NS_DOC, file);
		Assert.assertTrue(file.exists());
	}
	
	@Test
	public void addResorce() throws Exception {
		try {
			FilesystemManager.getInstance().addResource(new File("resources"+File.separator+SINGLE_MANINTAINABLE_QS_FD_NS_DOC));
		} catch (DDIFtpException e) {
			// expected
		}
	}
}
