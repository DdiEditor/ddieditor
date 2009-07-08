package org.ddialliance.ddieditor.osgi;



import org.ddialliance.ddieditor.model.DdiManager;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceManager;
import org.ddialliance.ddieditor.persistenceaccess.dbxml.DbXmlManager;

public class DdiEditorCoreImpl implements IDdiEditorCore {
	private DbXmlManager dbXmlManager;
	private DdiManager ddiManager;
	private PersistenceManager persistenceManager;

	@Override
	public DbXmlManager getDbXmlManager() throws Exception {
		return DbXmlManager.getInstance();
	}

	@Override
	public DdiManager getDdiManager() throws Exception {
		return DdiManager.getInstance();
	}

	@Override
	public PersistenceManager getPersistenceManager() throws Exception {
		return PersistenceManager.getInstance();
	}

}
