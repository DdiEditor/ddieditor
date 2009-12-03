package org.ddialliance.ddieditor.persistenceaccess.filesystem;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import org.ddialliance.ddieditor.logic.urn.ddi.FastUrnUtil;
import org.ddialliance.ddieditor.model.resource.DDIResourceDocument;
import org.ddialliance.ddieditor.model.resource.DDIResourceType;
import org.ddialliance.ddieditor.model.resource.StorageDocument;
import org.ddialliance.ddieditor.model.resource.StorageType;
import org.ddialliance.ddieditor.model.resource.TopURNType;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceManager;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceStorage;
import org.ddialliance.ddieditor.persistenceaccess.dbxml.DbXmlManager;
import org.ddialliance.ddieditor.persistenceaccess.maintainablelabel.MaintainableLabelQuery;
import org.ddialliance.ddieditor.persistenceaccess.maintainablelabel.MaintainableLabelQueryResult;
import org.ddialliance.ddieditor.persistenceaccess.maintainablelabel.MaintainableLightLabelQueryResult;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.FileUtil;
import org.ddialliance.ddiftp.util.log.Log;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;
import org.ddialliance.ddiftp.util.xml.Urn;

public class FilesystemManager implements PersistenceStorage {
	private static Log logSystem = LogFactory.getLog(LogType.SYSTEM,
			FilesystemManager.class);

	private static PersistenceStorage instance;

	public static synchronized PersistenceStorage getInstance() {
		if (instance == null) {
			logSystem.info("Initializing BDbXmlManager");
			instance = new FilesystemManager();
		}
		return instance;
	}

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

		// strip file of DDIInstance - left out!, until now :- )

		try {
			// load file into db xml container
			String containerName = file.getName().substring(0,
					file.getName().lastIndexOf("."));
			String connection = containerName + ".dbxml";
			DbXmlManager.getInstance().openContainer(new File(connection));
			DbXmlManager.getInstance().addResource(file);

			// add storage
			StorageDocument storageDoc = StorageDocument.Factory.newInstance();
			StorageType storage = storageDoc.addNewStorage();
			storage.setId(containerName);
			storage.setConnection(connection);
			storage.setManager(DbXmlManager.class.getName());
			PersistenceManager.getInstance().createStorage(storageDoc);

			// index storage and add resources
			FastUrnUtil fastUrnUtil = new FastUrnUtil(FileUtil.readFile(file
					.getAbsolutePath()));
			List<Urn> urns = fastUrnUtil.generateManintainablesUrns(true);
			DDIResourceDocument ddiResourceDocument = DDIResourceDocument.Factory
					.newInstance();
			DDIResourceType ddiResource = ddiResourceDocument
					.addNewDDIResource();
			ddiResource.setOrgName(file.getName());

			for (Urn urn : urns) {
				TopURNType topURN = ddiResource.addNewTopURN();
				topURN.setElement(urn.getContainedElement());
				topURN.setId(urn.getMaintainableId());
				topURN.setAgency(urn.getIdentifingAgency());
				topURN.setVersion(urn.getMaintainableVersion());
				topURN.setUrn(urn.toUrnString());
			}
			PersistenceManager.getInstance().createResource(
					ddiResourceDocument, containerName);
			// PersistenceManager.getInstance().commitWorkingResource();
		} catch (Exception e) {
			throw e;
		} finally {
			// reset doc
		}
	}

	public void close() throws Exception {
		// TODO Auto-generated method stub

	}

	// public void commitTransaction() throws Exception {
	// }

	public String getGlobalResourcePath(StorageType storage) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public String getResourcePath(StorageType storage, String resource)
			throws DDIFtpException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getTransaction() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> query(String query) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean querySingleBoolean(String query) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	public String querySingleString(String query) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public MaintainableLabelQueryResult queryMaintainableLabel(
			MaintainableLabelQuery maintainableLabelQuery,
			MaintainableLabelQueryResult maintainableLabelQueryResult)
			throws Exception {
		return null;
	}

	public void setWorkingConnection(StorageType storage) throws Exception {
		// TODO Auto-generated method stub

	}

	public void updateQuery(String query) throws Exception {
		// TODO Auto-generated method stub

	}

	public void exportResource(String id, File file) throws Exception {
		// TODO Auto-generated method stub

	}

	public List<String> getResources() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeResource(String id) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public MaintainableLightLabelQueryResult queryMaintainableLightLabel(
			MaintainableLabelQuery maintainableLabelQuery,
			MaintainableLightLabelQueryResult maintainableLightLabelQueryResult)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
