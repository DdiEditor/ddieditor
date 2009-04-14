package org.ddialliance.ddieditor.persistenceaccess;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.xmlbeans.XmlOptions;
import org.ddialliance.ddieditor.model.DdiManager;
import org.ddialliance.ddieditor.model.XQuery;
import org.ddialliance.ddieditor.model.resource.DDIResourceDocument;
import org.ddialliance.ddieditor.model.resource.DDIResourceType;
import org.ddialliance.ddieditor.model.resource.ResourceListDocument;
import org.ddialliance.ddieditor.model.resource.StorageDocument;
import org.ddialliance.ddieditor.model.resource.StorageType;
import org.ddialliance.ddieditor.model.resource.TopURNDocument;
import org.ddialliance.ddieditor.model.resource.TopURNType;
import org.ddialliance.ddieditor.persistenceaccess.dbxml.DbXmlManager;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.ReflectionUtil;
import org.ddialliance.ddiftp.util.log.Log;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;
import org.perf4j.aop.Profiled;

import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;

/**
 * Accesses persistence storage
 */
public class PersistenceManager {
	private static Log log = LogFactory.getLog(LogType.SYSTEM,
			PersistenceManager.class);
	private Log queryLog = LogFactory.getLog(LogType.PERSISTENCE,
			PersistenceManager.class);
	private static PersistenceManager instance = null;

	public static final String RESOURCE_LIST_CONTAINER = "resource-list.dbxml";
	public static final String RESOURCE_LIST_FILE = "resource-list.xml";
	public static final String RESOURCE_LIST = "resource-list";

	private ResourceListDocument resourceList = null;

	private String workingResource = "not-set";
	private String tmpWorkingResource = "not-set";
	private StorageType workingStorage = null;
	private PersistenceStorage workingPersistenceStorage = null;
	private Set<String> openStorages = new TreeSet<String>();

	private Map<String, ParamatizedXquery> paramatizedQueryCache = new HashMap<String, ParamatizedXquery>();

	private PersistenceManager() {
	}

	/**
	 * Singleton access
	 * 
	 * @return instance
	 * @throws DDIFtpException
	 */
	public static synchronized PersistenceManager getInstance()
			throws DDIFtpException {
		if (instance == null) {
			instance = new PersistenceManager();

			// initialize storage cache
			try {
				log.info("Initializing PersistenceManager");
				instance.workingResource = RESOURCE_LIST_FILE;
				DbXmlManager.getInstance().getTransaction();
				DbXmlManager.getInstance().openContainer(
						new File(RESOURCE_LIST_CONTAINER));
				instance.commitWorkingResource();

				if (instance.getResourceList() == null) {
					// add empty resource list
					DbXmlManager.getInstance().getTransaction();
					DbXmlManager.getInstance().addResource(
							new File("resources" + File.separator
									+ RESOURCE_LIST_FILE));
					instance.commitWorkingResource();
				}
				instance.rebuildResources();
			} catch (Exception e) {
				DDIFtpException ddiFtpE = new DDIFtpException(
						"Error parsing project file: " + RESOURCE_LIST_FILE, e);
				throw ddiFtpE;
			} finally {
				// instance.resetWorkingResource();
			}

			// create paramatized queries to store in cache
			// cache is filled up lazily
		}
		return instance;
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// resource cache functions
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	/**
	 * Retrieve the persistence storage implementation based on working resource
	 * 
	 * @return persistence storage
	 * @throws DDIFtpException
	 */
	@Profiled(tag = "getPersistenceStorage")
	public PersistenceStorage getPersistenceStorage() throws DDIFtpException {
		if (workingPersistenceStorage != null) {
			return workingPersistenceStorage;
		} else {
			return setWorkingPersistenceStorage();
		}
	}

	@Profiled(tag = "setWorkingPersistenceStorage")
	private PersistenceStorage setWorkingPersistenceStorage()
			throws DDIFtpException {
		workingStorage = getStorageByResourceOrgName(workingResource);
		if (workingStorage == null) {
			throw new DDIFtpException("Working storage for resource: "
					+ workingResource
					+ "is not recognized check loaded resources");
		}

		String pStoreClassName = workingStorage.getManager();
		try {
			if (workingPersistenceStorage == null
					|| !(Class.forName(pStoreClassName)
							.isInstance(workingPersistenceStorage))) {
				PersistenceStorage pStorage = (PersistenceStorage) ReflectionUtil
						.invokeStaticMethod(pStoreClassName, "getInstance",
								null);
				workingPersistenceStorage = pStorage;
				openStorages.add(pStoreClassName);
			}
		} catch (Exception e) {
			throw new DDIFtpException(
					"Error on setting persistence storage for resource: "
							+ workingResource, e);
		}

		if (log.isDebugEnabled()) {
			log.debug("Working resource: " + workingResource);
			log.debug("Working storage: " + workingStorage.getId());
			log.debug("Working persistence manager: "
					+ workingPersistenceStorage.getClass().getName());
		}

		try {
			workingPersistenceStorage.setWorkingConnection(workingStorage);
		} catch (Exception e) {
			throw new DDIFtpException(
					"Error setting working connection for resource: "
							+ workingResource);
		}

		return workingPersistenceStorage;
	}

	/**
	 * Set the current working document
	 * 
	 * @param resource
	 *            of working document
	 * @throws DDIFtpException
	 */
	public void setWorkingResource(String resource) throws DDIFtpException {
		tmpWorkingResource = workingResource;
		workingResource = resource;
		setWorkingPersistenceStorage();
	}

	/**
	 * Retrieve the current working resource
	 * 
	 * @return current working resource
	 */
	public String getWorkingResource() {
		return workingResource;
	}

	public StorageType getWorkingStorage() {
		return workingStorage;
	}

	/**
	 * Reset the working persistence XML file to the previous working file
	 * 
	 * @throws DDIFtpException
	 */
	private void resetWorkingResource() throws DDIFtpException {
		setWorkingResource(tmpWorkingResource);
	}

	/**
	 * Retrieve the document path expressed in XQuery from persistence storage
	 * specified by current working document
	 * 
	 * @return document path
	 * @throws DDIFtpException
	 */
	public String getResourcePath() throws DDIFtpException {
		try {
			return getPersistenceStorage().getResourcePath(workingStorage,
					workingResource);
		} catch (Exception e) {
			throw new DDIFtpException("Error defining resource path", e);
		}
	}

	/**
	 * Retrieve the container path expressed in XQuery from persistence storage
	 * specified by current working document
	 * 
	 * @return container path
	 * @throws DDIFtpException
	 */
	public String getGlobalResourcePath() throws DDIFtpException {
		try {
			return getPersistenceStorage()
					.getGlobalResourcePath(workingStorage);
		} catch (Exception e) {
			throw new DDIFtpException("Error defining global path for:"
					+ workingResource);
		}
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// xquery functions
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	/**
	 * Commit the working resource transaction
	 * 
	 * @throws DDIFtpException
	 */
	public void commitWorkingResource() throws DDIFtpException {
		try {
			getPersistenceStorage().commitTransaction();
		} catch (Exception e) {
			throw new DDIFtpException("Error on commit", e);
		}
	}

	/**
	 * Roll back the working resource transaction
	 * 
	 * @throws DDIFtpException
	 */
	public void rollbackWorkingResource() throws DDIFtpException {
		if (log.isDebugEnabled()) {
			log.debug("Rollback working resource: " + getWorkingResource());
		}

		try {
			getPersistenceStorage().rollbackTransaction();
		} catch (Exception e) {
			throw new DDIFtpException("Error on rollback", e);
		}
	}

	/**
	 * Commit all resources
	 * 
	 * @throws DDIFtpException
	 */
	public void commitAllResources() throws DDIFtpException {
		List<DDIResourceType> resouses = getResources();
		for (DDIResourceType resource : resouses) {
			setWorkingResource(resource.getOrgName());
			commitWorkingResource();
		}
	}

	/**
	 * Roll back all resources
	 * 
	 * @throws DDIFtpException
	 */
	public void rollbackAllResources() throws DDIFtpException {
		List<DDIResourceType> resouses = getResources();
		for (DDIResourceType resource : resouses) {
			setWorkingResource(resource.getOrgName());
			rollbackWorkingResource();
		}
	}

	// insert, delete, replace and rename node
	// XQuery Update Facility http://www.w3.org/TR/xquery-update-10
	/**
	 * Insert a node on persistence storage
	 * 
	 * @param nodes
	 *            the content that to insert. It can be a string or an XQuery
	 *            selection statement
	 * @param insertKeyword
	 *            before, after, as first node, as last node, into
	 * @param position
	 *            XQuery expression that selects exactly one insert location
	 * @throws DDIFtpException
	 */
	public void insert(Object nodes, XQueryInsertKeyword insertKeyword,
			XQuery xQuery) throws DDIFtpException {
		// insert nodes [(node|nodes) keyword position]

		// e.g. insert nodes <b4>inserted child</b4> after
		// doc("dbxml:/container.dbxml/mydoc.xml")/a/b2
		XQuery query = new XQuery();

		// nodes
		if (nodes instanceof XQuery) {
			query.namespaceDeclaration
					.append(((XQuery) nodes).namespaceDeclaration.toString());
			query.function.append(((XQuery) nodes).function.toString());
			query.function.append(" ");
			query.query.append(" insert nodes ");
			query.query.append(((XQuery) nodes).query.toString());
		} else {
			query.query.append(" insert nodes ");
			query.query.append(nodes.toString());
		}

		// insert keyword
		query.query.append(" ");
		query.query.append(insertKeyword.getKeyWord());

		// position
		if (xQuery.namespaceDeclaration.length() > 1) {
			query.namespaceDeclaration.append(" ");
			query.namespaceDeclaration.append(xQuery.namespaceDeclaration
					.toString());
		}
		if (xQuery.function.length() > 1) {
			query.function.append(xQuery.function.toString());
		}
		query.query.append(xQuery.query.toString());

		String queryStr = query.getFullQueryString();
		queryLog.info(queryStr);

		// execute
		try {
			getPersistenceStorage().updateQuery(queryStr);
		} catch (Exception e) {
			throw new DDIFtpException("Error on XQuery insert execution", e);
		}
	}

	/**
	 * Update a node in persistence storage
	 * 
	 * @param xQuery
	 *            XQuery expression that selects exactly one location to update
	 * @param value
	 *            update value of node
	 * @throws DDIFtpException
	 */
	public void update(XQuery xQuery, String value) throws DDIFtpException {
		// replace value of node [oldNode] with [newNode]

		// e.g. replace node fn:doc("bib.xml")/books/book[1]/publisher with
		// fn:doc("bib.xml")/books/book[2]/publisher
		if (xQuery.query.indexOf("replace value of node") < 0) {
			xQuery.query.replace(0, 0, "replace value of node ");
		}
		xQuery.query.append(" with ");
		xQuery.query.append(value);

		String queryStr = xQuery.getFullQueryString();
		queryLog.info(queryStr);

		// execute
		try {
			getPersistenceStorage().updateQuery(queryStr);
		} catch (Exception e) {
			throw new DDIFtpException("Error on update query", e);
		}
	}

	/**
	 * Delete one or more nodes in the XML DB
	 * 
	 * @param xQuery
	 *            XQuery expression selecting node/ nodes to delete
	 */
	public void delete(XQuery xQuery) throws DDIFtpException {
		// delete (node|nodes) [node]
		// e.g. delete nodes /email/message [fn:currentDate() - date >
		// xs:dayTimeDuration("P365D")]

		if (xQuery.query.indexOf("delete node") < 0) {
			xQuery.query.replace(0, 0, "delete node ");
		}

		String queryStr = xQuery.getFullQueryString();
		queryLog.info(queryStr);

		try {
			getPersistenceStorage().updateQuery(queryStr);
		} catch (Exception e) {
			throw new DDIFtpException("Error on delete query", e);
		}
	}

	/**
	 * Query persistence storage
	 * 
	 * @param query
	 * @return result list
	 * @throws DDIFtpException
	 */
	@Profiled(tag = "query query_{$0} tt/n/n")
	public List<String> query(String query) throws DDIFtpException {
		queryLog.info(query);
		try {
			return getPersistenceStorage().query(query);
		} catch (Exception e) {
			throw new DDIFtpException("Error on query execution", e);
		}
	}

	@Profiled(tag = "updateQuery query_{$0}")
	public void updateQuery(String query) throws DDIFtpException {
		queryLog.info(query);
		try {
			getPersistenceStorage().updateQuery(query);
		} catch (Exception e) {
			throw new DDIFtpException("Error on execution of update query", e);
		}
	}

	public boolean querySingleBoolean(String query) throws DDIFtpException {
		queryLog.info(query);
		try {
			return getPersistenceStorage().querySingleBoolean(query);
		} catch (Exception e) {
			throw new DDIFtpException(
					"Error on execution query single boolean", e);
		}
	}

	public String querySingleString(String query) throws DDIFtpException {
		queryLog.info(query);
		try {
			return getPersistenceStorage().querySingleString(query);
		} catch (Exception e) {
			throw new DDIFtpException("Error on execution query single string",
					e);
		}
	}

	/**
	 * Close all open persistence storage connections
	 */
	public void close() throws DDIFtpException {
		PersistenceStorage pStorage = null;
		for (String pStoreClassName : openStorages) {
			pStorage = null;
			try {
				pStorage = (PersistenceStorage) ReflectionUtil
						.invokeStaticMethod(pStoreClassName, "getInstance",
								null);
			} catch (Exception e) {
				// do nothing logged in ReflectionUtil
			}
			if (pStorage != null) {
				try {
					pStorage.close();
				} catch (Exception e) {
					throw new DDIFtpException(
							"Error closing persistence storage: "
									+ pStoreClassName, e);
				}
			}
		}
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// resource management
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	protected void rebuildResources() throws DDIFtpException {
		resourceList = getResourceList();
		if (log.isDebugEnabled()) {
			log.debug("Rebuild resources: \n" + resourceList);
		}
	}

	/**
	 * Retrieve the complete resource list by querying the XML data base
	 * 
	 * @return resource list
	 * @throws DDIFtpException
	 */
	protected ResourceListDocument getResourceList() throws DDIFtpException {
		try {
			setResource();
			StringBuilder query = new StringBuilder();
			query.append(getResourcePath());
			query
					.append("//*[namespace-uri()='ddieditor-resoure-list' and local-name()='ResourceList']");
			List<String> list = query(query.toString());

			ResourceListDocument resourceList = null;
			for (String string : list) {
				resourceList = ResourceListDocument.Factory.parse(string);
			}
			return resourceList;
		} catch (Exception e) {
			throw new DDIFtpException("Error on creating storage: ", e);
		} finally {
			resetWorkingResource();
		}
	}

	/**
	 * Set the working persistence XML file to the resource editor file
	 * 
	 * @throws DDIFtpException
	 */
	private void setResource() throws DDIFtpException {
		tmpWorkingResource = getWorkingResource();
		setWorkingResource(RESOURCE_LIST_FILE);
	}

	/**
	 * Declare the default ddieditor-resource-list XQuery name space
	 * 
	 * @return name space declaration
	 */
	public String getDefaultResourceNs() {
		StringBuilder result = new StringBuilder(
				"declare default element namespace \"");
		result.append("ddieditor-resource-list");
		result.append("\"; ");
		return result.toString();
	}

	/**
	 * Create storage
	 * 
	 * @param storage
	 *            to create
	 * @throws DDIFtpException
	 */
	public void createStorage(StorageDocument storage) throws DDIFtpException {
		if (storage.getStorage().getId().equals(null)
				|| storage.getStorage().getId().equals("")) {
			throw new DDIFtpException("Id can not be null or ''");
		}
		if (storage.getStorage().getManager().equals(null)
				|| storage.getStorage().getManager().equals("")) {
			throw new DDIFtpException("Manager can not be null or ''");
		}

		if (log.isDebugEnabled()) {
			log.debug("Creating storage: " + storage.getStorage().getId());
		}

		try {
			setResource();
			XQuery position = new XQuery();
			position.query.append(" ");
			position.query.append(getResourcePath());
			position.query
					.append("//*[namespace-uri()='ddieditor-resoure-list' and local-name()='ResourceList'][1]");
			insert(storage.xmlText(), XQueryInsertKeyword.INTO, position);
		} catch (Exception e) {
			throw new DDIFtpException("Error on creating storage: "
					+ storage.getStorage().getId(), e);
		} finally {
			resetWorkingResource();
		}
		rebuildResources();
	}

	/**
	 * Delete a storage
	 * 
	 * @param id
	 *            of storage
	 * @throws DDIFtpException
	 */
	public void deleteStorage(String id) throws DDIFtpException {
		try {
			setResource();
			XQuery query = new XQuery();
			query.query.append(" for $storage in ");
			query.query.append(getResourcePath());
			query.query
					.append("//*[namespace-uri()='ddieditor-resoure-list' and local-name()='ResourceList']//*[namespace-uri()='ddieditor-resoure-list' and local-name()='Storage']");
			query.query.append(" where $storage/@id = '");
			query.query.append(id);
			query.query.append("'");
			query.query.append(" return $storage");

			delete(query);
		} catch (Exception e) {
			throw new DDIFtpException("Error on create storage", e);
		} finally {
			resetWorkingResource();
		}
		rebuildResources();
	}

	/**
	 * Create a DDI resource
	 * 
	 * @param ddiResource
	 *            to create
	 * @param storageId
	 *            id of storage the DDI resource belongs to
	 * @throws DDIFtpException
	 */
	public void createResource(DDIResourceDocument ddiResource, String storageId)
			throws DDIFtpException {
		XQuery query = new XQuery();
		try {
			setResource();
			query.query.append(" for $x in ");
			query.query.append(PersistenceManager.getInstance()
					.getResourcePath());
			query.query
					.append("//*[namespace-uri()='ddieditor-resoure-list' and local-name()='ResourceList']//*[namespace-uri()='ddieditor-resoure-list' and local-name()='Storage']");
			query.query.append(" where $x/@id = '");
			query.query.append(storageId);
			query.query.append("'");
			query.query.append(" return $x");
			insert(ddiResource.xmlText(), XQueryInsertKeyword.INTO, query);
		} catch (Exception e) {
			throw new DDIFtpException("Error on creating resource: "
					+ ddiResource.getDDIResource() != null ? ddiResource
					.getDDIResource().getOrgName() : "null", e);
		} finally {
			resetWorkingResource();
		}
		rebuildResources();
	}

	/**
	 * Delete a DDI resource
	 * 
	 * @param id
	 *            of DDI resource to delete
	 * @throws DDIFtpException
	 */
	public void deleteResource(String id) throws DDIFtpException {
		try {
			setResource();
			XQuery query = new XQuery();
			query.query.append(" for $x in ");
			query.query.append(getResourcePath());
			query.query
					.append("//*[namespace-uri()='ddieditor-resoure-list' and local-name()='ResourceList']//*[namespace-uri()='ddieditor-resoure-list' and local-name()='Storage']//*[namespace-uri()='ddieditor-resoure-list' and local-name()='DDIResource']");
			query.query.append(" where $x/@orgName = '");
			query.query.append(id);
			query.query.append("'");
			query.query.append(" return $x");

			delete(query);
		} catch (Exception e) {
			throw new DDIFtpException("Error on retrieve resource with id: "
					+ id, e);
		} finally {
			resetWorkingResource();
		}

		rebuildResources();
	}

	/**
	 * Retrieve a storage by a containing resource original name
	 * 
	 * @param orgName
	 *            of resource
	 * @return storage
	 * @throws DDIFtpException
	 */
	public StorageType getStorageByResourceOrgName(String orgName)
			throws DDIFtpException {
		// first time entry hack
		if (resourceList == null) {
			StorageDocument storageDocument = StorageDocument.Factory
					.newInstance();
			StorageType storage = storageDocument.addNewStorage();
			storage.setConnection(RESOURCE_LIST_CONTAINER);
			storage
					.setManager("org.ddialliance.ddieditor.persistenceaccess.dbxml.DbXmlManager");
			storage.setId("resource-list");
			return storage;
		}

		// hack to get storage without resource
		if (orgName.equals(RESOURCE_LIST_FILE)) {
			for (StorageType storageTmp : resourceList.getResourceList()
					.getStorageList()) {
				if (storageTmp.getId().equals(RESOURCE_LIST)) {
					return storageTmp;
				}
			}
		}

		// loop for ingested resources
		for (StorageType storageTmp : resourceList.getResourceList()
				.getStorageList()) {
			for (DDIResourceType ddiResource : storageTmp.getDDIResourceList()) {
				if (ddiResource.getOrgName().equals(orgName)) {
					return storageTmp;
				}
			}
		}
		return null;
	}

	/**
	 * Retrieve a list of all storages
	 * 
	 * @return list of storages
	 * @throws DDIFtpException
	 */
	public List<StorageType> getStorages() throws DDIFtpException {
		return resourceList.getResourceList().getStorageList();
	}

	/**
	 * Retrieve a DDI resource by original file name
	 * 
	 * @param original
	 *            file name
	 * @return DDI resource
	 * @throws DDIFtpException
	 */
	public DDIResourceType getResourceByOrgName(String orgName)
			throws DDIFtpException {
		for (StorageType storageTmp : resourceList.getResourceList()
				.getStorageList()) {
			for (DDIResourceType ddiResource : storageTmp.getDDIResourceList()) {
				if (ddiResource.getOrgName().equals(orgName)) {
					return ddiResource;
				}
			}
		}
		return null;
	}

	/**
	 * Retrieve a list of all DDI resources
	 * 
	 * @return list of DDI resources
	 * @throws DDIFtpException
	 */
	public List<DDIResourceType> getResources() throws DDIFtpException {
		List<DDIResourceType> result = new ArrayList<DDIResourceType>();
		for (StorageType storage : resourceList.getResourceList()
				.getStorageList()) {
			for (DDIResourceType resource : storage.getDDIResourceList()) {
				result.add(resource);
			}
		}
		return result;
	}

	/**
	 * Retrieve top URNs by working resource
	 * 
	 * @return list of top URNs
	 * @throws DDIFtpException
	 */
	public List<TopURNType> getTopUrnsByWorkingResource()
			throws DDIFtpException {
		String resourceId = PersistenceManager.getInstance()
				.getWorkingResource();
		for (DDIResourceType resource : getResources()) {
			if (resource.getOrgName().equals(resourceId)) {
				return resource.getTopURNList();
			}
		}
		return new ArrayList<TopURNType>();
	}

	/**
	 * Retrieve top URNs by working resource defined by agency, id and version
	 * 
	 * @param agency
	 *            name of responsible agency
	 * @param id
	 * @param version
	 * @return list top URNs
	 * @throws DDIFtpException
	 */
	public List<TopURNType> getTopUrnsByIdAndVersionByWorkingResource(
			String agency, String id, String version) throws DDIFtpException {
		List<TopURNType> result = new ArrayList<TopURNType>();
		for (TopURNType topURN : getTopUrnsByWorkingResource()) {
			if (topURN.getAgency().equals(agency) && topURN.getId().equals(id)
					&& topURN.getVersion().equals(version)) {
				result.add(topURN);
			}
		}
		return result;
	}

	/**
	 * Export resource list to file
	 * 
	 * @param file
	 *            to export to
	 * @throws DDIFtpException
	 */
	public void exportResourceList(File file) throws DDIFtpException {
		try {
			setResource();
			XQuery position = new XQuery();
			position.query.append(" ");
			position.query.append(getResourcePath());
			position.query
					.append("//*[namespace-uri()='ddieditor-resoure-list' and local-name()='ResourceList'][1]");
			List<String> result = query(position.getFullQueryString());
			ResourceListDocument resourceListDocument = ResourceListDocument.Factory
					.parse(result.get(0));

			if (!file.exists()) {
				file.createNewFile();
			}

			XmlOptions xmlOptions = new XmlOptions();
			xmlOptions.setSaveAggressiveNamespaces();
			xmlOptions.setSaveOuter();
			xmlOptions.setSavePrettyPrint();

			resourceListDocument.save(file, xmlOptions);

		} catch (Exception e) {
			throw new DDIFtpException("Error exporting resource list: ", e);
		} finally {
			resetWorkingResource();
		}
	}

	/**
	 * Export a resource from persistent storage to a file
	 * 
	 * @param resource
	 *            in persistent storage
	 * @param file
	 *            in file system
	 * @throws DDIFtpException
	 */
	public void exportResoure(String resource, File file)
			throws DDIFtpException {
		setWorkingResource(resource);
		try {
			workingPersistenceStorage.exportResource(resource, file);
		} catch (Exception e) {
			throw new DDIFtpException("Error exporting resource: " + resource);
		}
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// utils
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public ParamatizedXquery getParamatizedQuery(String queryName) {
		return paramatizedQueryCache.get(queryName);
	}

	public void setParamatizedQuery(String queryName,
			ParamatizedXquery paramatizedQuery) {
		this.paramatizedQueryCache.put(queryName, paramatizedQuery);
	}
}
