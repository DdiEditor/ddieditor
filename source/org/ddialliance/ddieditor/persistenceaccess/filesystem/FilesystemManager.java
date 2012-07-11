package org.ddialliance.ddieditor.persistenceaccess.filesystem;

import java.io.File;
import java.util.List;

import org.ddialliance.ddieditor.model.resource.DDIResourceDocument;
import org.ddialliance.ddieditor.model.resource.DDIResourceType;
import org.ddialliance.ddieditor.model.resource.StorageDocument;
import org.ddialliance.ddieditor.model.resource.StorageType;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceManager;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceStorage;
import org.ddialliance.ddieditor.persistenceaccess.dbxml.DbXmlManager;
import org.ddialliance.ddieditor.persistenceaccess.maintainablelabel.MaintainableLabelQuery;
import org.ddialliance.ddieditor.persistenceaccess.maintainablelabel.MaintainableLabelQueryResult;
import org.ddialliance.ddieditor.persistenceaccess.maintainablelabel.MaintainableLightLabelQueryResult;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.log.Log;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;

public class FilesystemManager implements PersistenceStorage {
	private static Log logSystem = LogFactory.getLog(LogType.SYSTEM,
			FilesystemManager.class);

	private static PersistenceStorage instance;

	public static synchronized PersistenceStorage getInstance() {
		if (instance == null) {
			logSystem.info("Initializing");
			instance = new FilesystemManager();
		}
		return instance;
	}

	@Override
	public void addResource(Object obj) throws Exception {
		if (!(obj instanceof File)) {
			throw new DDIFtpException("Argument must be a file!");
		}
		File file = (File) obj;

		// check if file has been loaded
		if (PersistenceManager.getInstance().getStorageByResourceOrgName(
				file.getName()) != null) {
			throw new DDIFtpException("A resource with the same name: '"
					+ file.getName() + "' has already been added");
		}

		try {
			// file and container name handling
			String containerName = file.getName();
			containerName = containerName.replace(" ", "_");
			String fileName = containerName;
			if (file.getName().lastIndexOf(".")>-1) {
				containerName = file.getName().substring(0,
						file.getName().lastIndexOf("."));				
			}			
			
			// load file into db xml container
			String connection = containerName + ".dbxml";
			DbXmlManager.getInstance().addStorage(new File(connection));
			DbXmlManager.getInstance().addResource(file);

			// add storage
			StorageDocument storageDoc = StorageDocument.Factory.newInstance();
			StorageType storage = storageDoc.addNewStorage();
			storage.setId(containerName);
			storage.setConnection(connection);
			storage.setManager(DbXmlManager.class.getName());
			PersistenceManager.getInstance().createStorage(storageDoc);

			// add resource
			DDIResourceDocument ddiResourceDocument = DDIResourceDocument.Factory
					.newInstance();
			DDIResourceType ddiResource = ddiResourceDocument
					.addNewDDIResource();
			ddiResource.setOrgName(fileName);
			PersistenceManager.getInstance().createResource(
					ddiResourceDocument, containerName);
			PersistenceManager.getInstance().setWorkingResource(fileName);
			
			// index resource storage and add urns 
			PersistenceManager.getInstance().indexResourceUrns(true);
		} catch (Exception e) {
			throw e;
		} finally {
			// TODO reset doc
		}
	}

	@Override
	public MaintainableLightLabelQueryResult queryMaintainableLightLabel(
			MaintainableLabelQuery maintainableLabelQuery,
			MaintainableLightLabelQueryResult maintainableLightLabelQueryResult)
			throws Exception {
		// not implemented
		return null;
	}

	@Override
	public void houseKeeping() throws Exception {
		// do nothing
	}
	
	@Override
	public void close() throws Exception {
		// do nothing
	}

	@Override
	public void exportResource(String id, File file) throws Exception {
		// do nothing
	}

	@Override
	public void exportResources(String id, List<String> resources, File file) throws Exception {
		// do nothing
	}

	@Override
	public String getGlobalResourcePath(StorageType storage) throws Exception {
		// not implemented
		return null;
	}

	@Override
	public String getResourcePath(StorageType storage, String resource)
			throws Exception {
		// not implemented
		return null;
	}

	@Override
	public List<String> getResources() throws Exception {
		// not implemented
		return null;
	}

	@Override
	public List<String> query(String query) throws Exception {
		// not implemented
		return null;
	}

	@Override
	public MaintainableLabelQueryResult queryMaintainableLabel(
			MaintainableLabelQuery maintainableLabelQuery,
			MaintainableLabelQueryResult maintainableLabelQueryResult)
			throws Exception {
		// not implemented
		return null;
	}

	@Override
	public boolean querySingleBoolean(String query) throws Exception {
		// not implemented
		return false;
	}

	@Override
	public String querySingleString(String query) throws Exception {
		// not implemented
		return null;
	}

	@Override
	public void removeResource(String id) throws Exception {
		// not implemented
	}

	@Override
	public void setWorkingConnection(StorageType storage) throws Exception {
		// not implemented
	}

	@Override
	public void updateQuery(String query) throws Exception {
		// not implemented
	}

	@Override
	public void addStorage(Object obj) throws Exception {

	}

	@Override
	public List<String> getStorages() throws Exception {
		// not implemented
		return null;
	}

	@Override
	public void removeStorage(String id) throws Exception {
		// not implemented
	}
}
