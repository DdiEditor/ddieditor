package org.ddialliance.ddieditor.osgi;

import org.ddialliance.ddieditor.model.DdiManager;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceManager;
import org.ddialliance.ddieditor.persistenceaccess.dbxml.DbXmlManager;

public interface IDdiEditorCore {
	public DdiManager getDdiManager() throws Exception;
	public PersistenceManager getPersistenceManager() throws Exception;
	public DbXmlManager getDbXmlManager() throws Exception;
}
