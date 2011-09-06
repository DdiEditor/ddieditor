package org.ddialliance.ddieditor;

import java.io.File;

import org.ddialliance.ddieditor.model.DdiManager;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceManager;
import org.ddialliance.ddieditor.persistenceaccess.dbxml.DbXmlManager;
import org.ddialliance.ddieditor.persistenceaccess.filesystem.FilesystemManager;
import org.ddialliance.ddiftp.util.log.Log;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class DdieditorTestCase {
	private static Log log = LogFactory.getLog(LogType.SYSTEM,
			DdieditorTestCase.class);

	public static final String NON_DECLARED_NS_DOC = "dext.xml";
	public static final String FULLY_DECLARED_NS_DOC = "big-doc.xml";
	public static final String SINGLE_MANINTAINABLE_D_FD_NS_DOC = "datacollection.xml";
	public static final String SINGLE_MANINTAINABLE_QS_FD_NS_DOC = "questionscheme.xml";

	@BeforeClass
	public static void runOnceBeforeAllTests() throws Exception {

		try {
			// clean out dbxml
			File[] files = DbXmlManager.getInstance().getEnvHome().listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].getName().contains("__")) {
					log.debug("Deleting: " + files[i].getAbsolutePath());
					files[i].delete();
				}
				if (files[i].getName().contains("log.")) {
					log.debug("Deleting: " + files[i].getAbsolutePath());
					files[i].delete();
				}
				if (files[i].getName().contains("dbxml")) {
					log.debug("Deleting: " + files[i].getAbsolutePath());
					files[i].delete();
				}
			}

			// initialize
			PersistenceManager.getInstance();
			DdiManager.getInstance();

			// add resources
			FilesystemManager.getInstance().addResource(
					new File("resources" + File.separator
							+ FULLY_DECLARED_NS_DOC));
			FilesystemManager.getInstance()
					.addResource(
							new File("resources" + File.separator
									+ NON_DECLARED_NS_DOC));
			FilesystemManager.getInstance().addResource(
					new File("resources" + File.separator
							+ SINGLE_MANINTAINABLE_D_FD_NS_DOC));
			FilesystemManager.getInstance().addResource(
					new File("resources" + File.separator
							+ SINGLE_MANINTAINABLE_QS_FD_NS_DOC));

			// no commit
			System.setProperty("ddieditor.test", "true");

			// debug resources
			PersistenceManager.getInstance().exportResourceList(
					new File(PersistenceManager.RESOURCE_LIST_FILE));
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@AfterClass
	public static void runOnceAfterAllTests() throws Exception {
		log.debug("JUnit shutdown ...");
		PersistenceManager.getInstance().close();
	}

	@After
	public void afterEveryTest() throws Exception {
	}
}
