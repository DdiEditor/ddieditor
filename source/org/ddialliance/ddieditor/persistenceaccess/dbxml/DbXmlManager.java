package org.ddialliance.ddieditor.persistenceaccess.dbxml;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.sql.rowset.spi.XmlReader;
import javax.xml.namespace.QName;

import org.apache.commons.io.IOUtils;
import org.ddialliance.ddieditor.model.DdiManager;
import org.ddialliance.ddieditor.model.lightxmlobject.LabelType;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectListDocument;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;
import org.ddialliance.ddieditor.model.namespace.ddi3.Ddi3NamespacePrefix;
import org.ddialliance.ddieditor.model.resource.StorageType;
import org.ddialliance.ddieditor.persistenceaccess.DefineQueryPositionResult;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceManager;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceStorage;
import org.ddialliance.ddieditor.persistenceaccess.XQueryInsertKeyword;
import org.ddialliance.ddieditor.persistenceaccess.maintainablelabel.MaintainableLabelQuery;
import org.ddialliance.ddieditor.persistenceaccess.maintainablelabel.MaintainableLabelQueryResult;
import org.ddialliance.ddieditor.persistenceaccess.maintainablelabel.MaintainableLightLabelQueryResult;
import org.ddialliance.ddieditor.util.DdiEditorConfig;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.log.Log;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;
import org.ddialliance.ddiftp.util.xml.XmlBeansUtil;
import org.perf4j.aop.Profiled;

import com.sleepycat.db.CheckpointConfig;
import com.sleepycat.db.Environment;
import com.sleepycat.db.EnvironmentConfig;
import com.sleepycat.db.LockDetectMode;
import com.sleepycat.db.TransactionConfig;
import com.sleepycat.dbxml.XmlContainer;
import com.sleepycat.dbxml.XmlContainerConfig;
import com.sleepycat.dbxml.XmlDocument;
import com.sleepycat.dbxml.XmlDocumentConfig;
import com.sleepycat.dbxml.XmlEventReader;
import com.sleepycat.dbxml.XmlException;
import com.sleepycat.dbxml.XmlIndexDeclaration;
import com.sleepycat.dbxml.XmlIndexSpecification;
import com.sleepycat.dbxml.XmlInputStream;
import com.sleepycat.dbxml.XmlManager;
import com.sleepycat.dbxml.XmlManagerConfig;
import com.sleepycat.dbxml.XmlQueryContext;
import com.sleepycat.dbxml.XmlResults;
import com.sleepycat.dbxml.XmlTransaction;
import com.sleepycat.dbxml.XmlUpdateContext;
import com.sleepycat.dbxml.XmlValue;

/**
 * DbXml interface
 */
public class DbXmlManager implements PersistenceStorage {
	private static Log logSystem = LogFactory.getLog(LogType.SYSTEM,
			DbXmlManager.class);
	private static Log logBug = LogFactory.getLog(LogType.BUG,
			DbXmlManager.class);
	private Log queryLog = LogFactory.getLog(LogType.PERSISTENCE,
			DbXmlManager.class);

	private static DbXmlManager instance = null;

	private Environment environment;
	private EnvironmentConfig environmentConfig;
	// public static final String ENVIROMENT_HOME = new File(
	// DdiEditorConfig.get(DdiEditorConfig.DBXML_ENVIROMENT_HOME))
	// .getAbsolutePath();

	private File envHome = null;

	private XmlManager xmlManager = null;
	private XmlManagerConfig xmlManagerConfig;
	private XmlQueryContext xmlQueryContext;
	private XmlContainerConfig xmlContainerConfig;
	private ThreadLocal<XmlTransaction> transaction = new ThreadLocal<XmlTransaction>();
	private String currentWorkingContainer = "";
	private HashMap<String, XmlContainer> openContainers = new HashMap<String, XmlContainer>();

	private int jumpLocation;
	private String jumpName;

	private DbXmlManager() {
	}

	/**
	 * Singleton access to the xml db manager
	 * 
	 * @return xml db manager
	 */
	public static synchronized DbXmlManager getInstance() {
		if (instance == null) {
			if (logSystem.isDebugEnabled()) {
				logSystem.debug("Initializing BDbXmlManager");
			}

			try {
				instance = new DbXmlManager();
				instance.envHome = new File(
						DdiEditorConfig
								.get(DdiEditorConfig.DBXML_ENVIROMENT_HOME));
				logSystem.info("New env home: "
						+ instance.envHome.getAbsolutePath());

				// xml manager
				instance.xmlManager = new XmlManager(instance.getEnvironment(),
						instance.getXmlManagerConfig());
			} catch (Exception e) {
				new DDIFtpException("Error on BDBXML startup with enviroment: "
						+ instance.envHome.getAbsolutePath(), e);
			}
		}
		return instance;
	}

	public File getEnvHome() {
		return envHome;
	}

	/**
	 * Resets the xml manager and rolls back the current transaction
	 * 
	 * @throws Exception
	 */
	public void reset() throws Exception {
		rollbackTransaction();
		close();
		instance = null;
		logSystem.info("Alle connections closed for envhome: "
				+ getEnvHome().getAbsolutePath());
	}

	/**
	 * Returns the Environment object. XmlManager is created if it does not yet
	 * exist.
	 * 
	 * @return environment
	 */
	private Environment getEnvironment() throws Exception {
		if (environment == null) {
			try {
				environment = new Environment(envHome, getEnvironmentConfig());
			} catch (Exception e) {
				throw new DDIFtpException(
						"Error on setting up BDBXML enviroment: "
								+ envHome.getAbsolutePath(), e);
			}
		}
		return environment;
	}

	/**
	 * Returns the EnvironmentConfig object. environmentConfig is created if it
	 * does not yet exist.
	 * 
	 * @return environmentConfig
	 */
	private EnvironmentConfig getEnvironmentConfig() throws Exception {
		if (environmentConfig == null) {
			environmentConfig = new EnvironmentConfig();

			// general environment
			environmentConfig.setAllowCreate(true);
			environmentConfig.setRunRecovery(true); // light recovery on startup
			// environmentConfig.setRunFatalRecovery(true); // heavy recovery on
			// startup
			environmentConfig.setJoinEnvironment(true); // reuse of environment:
			// ok
			environmentConfig.setThreaded(true);

			// log subsystem
			environmentConfig.setInitializeLogging(true);
			environmentConfig.setLogAutoRemove(true);
			// teori: cachesize/pagesize ~ cache logBuffer
			environmentConfig.setLogBufferSize(128 * 1024); // default 32KB
			environmentConfig.setInitializeCache(true); // shared memory region
			environmentConfig.setCacheSize(2 * 1024 * 1024); // 2MB cache

			// transaction
			environmentConfig.setTransactional(true);
			environmentConfig.setTxnMaxActive(20000);
			environmentConfig.setTxnWriteNoSync(true);
			environmentConfig.setTxnTimeout(0); // live forever, no timeout

			// locking subsystem
			environmentConfig.setInitializeLocking(true);
			environmentConfig.setMutexIncrement(22);
			environmentConfig.setMaxMutexes(200000);
			environmentConfig.setMaxLockers(200000);
			environmentConfig.setMaxLockObjects(200000); // default 1000
			environmentConfig.setMaxLocks(200000);

			// deadlock detection
			environmentConfig.setLockDetectMode(LockDetectMode.MINWRITE);
			// environmentConfig.setDsyncDatabases(true);

			// error stream
			// change to property definition of dbxml log level
			// XmlManager.setLogLevel(XmlManager.LEVEL_ALL, true);
			// XmlManager.setLogCategory(XmlManager.CATEGORY_CONTAINER
			// | XmlManager.CATEGORY_MANAGER | XmlManager.CATEGORY_QUERY,
			// true);
			// be aware of log level may affect execution to hang!
			// environmentConfig.setErrorStream(new Log4jLogOutputStream(
			// logPersistence, LogLevel.INFO));
		}
		return environmentConfig;
	}

	/**
	 * Returns the XmlManagerConfig object. XmlManagerConfig is created if it
	 * does not yet exist.
	 * 
	 * @return xmlManagerConfig
	 */
	private XmlManagerConfig getXmlManagerConfig() {
		if (xmlManagerConfig == null) {
			xmlManagerConfig = new XmlManagerConfig();
			xmlManagerConfig.setAdoptEnvironment(true);
			xmlManagerConfig.setAllowAutoOpen(true);
			xmlManagerConfig.setAllowExternalAccess(true);
		}

		return xmlManagerConfig;
	}

	/**
	 * Create a default XmlContainerConfigs
	 * 
	 * @return xmlContainerConfig
	 */
	private XmlContainerConfig getXmlContainerConfig() {
		if (xmlContainerConfig == null) {
			xmlContainerConfig = new XmlContainerConfig();
			xmlContainerConfig.setIndexNodes(XmlContainerConfig.On);
			xmlContainerConfig.setContainerType(XmlContainer.NodeContainer);
			xmlContainerConfig.setTransactional(true);
			if (DdiEditorConfig
					.getBoolean(DdiEditorConfig.DBXML_IMPORT_VALIDATE)) {
				xmlContainerConfig.setAllowValidation(true);
			}
		}
		return xmlContainerConfig;
	}

	private XmlUpdateContext getXmlUpdateContext() throws Exception {
		return xmlManager.createUpdateContext();
	}

	private XmlDocumentConfig getXmlDocumentConfig() throws Exception {
		return null;
	}

	private XmlContainer getContainer(String name) {
		return openContainers.get(name);
	}

	private XmlContainer getWorkingContainer() {
		return openContainers.get(currentWorkingContainer);
	}

	public void setWorkingConnection(StorageType storage) throws Exception {
		addStorage(new File(storage.getConnection()));
	}

	@Override
	public void addStorage(Object obj) throws Exception {
		File file = null;
		if (obj instanceof File) {
			file = (File) obj;
		} else {
			throw new DDIFtpException("Storage must point to a XML file: "
					+ obj, new Throwable());
		}

		if (getContainer(file.getName()) == null) {
			try {
				file = new File(envHome.getAbsoluteFile() + "/"
						+ file.getName());
				if (!file.exists()) {
					if (logSystem.isDebugEnabled()) {
						logSystem.debug("Creating dbxml container: "
								+ file.getName());
					}
					XmlContainer xmlContainer = xmlManager.createContainer(
							file.getName(), getXmlContainerConfig());
					openContainers.put(file.getName(), xmlContainer);

					// create indices
					if (!file.getName().equals(
							PersistenceManager.RESOURCE_LIST_FILE + ".dbxml")) {
						createIndices(xmlContainer);
						listIndices(xmlContainer);
					}
				} else {
					if (logSystem.isDebugEnabled()) {
						logSystem.debug("Env home: " + xmlManager.getHome());
						logSystem.debug("Opening dbxml container: "
								+ file.getName());
					}
					openContainers.put(file.getName(), xmlManager
							.openContainer(file.getName(),
									getXmlContainerConfig()));
				}
			} catch (Exception e) {
				throw new DDIFtpException(
						"Error on create/ open xml conatiner: "
								+ file.getName(), e);
			}
		} else if (logSystem.isDebugEnabled()) {
			logSystem.debug("Is open dbxml container: "
					+ file.getName());
		}
		this.currentWorkingContainer = file.getName();
	}

	@Override
	public List<String> getStorages() throws Exception {
		List<String> result = new ArrayList<String>(openContainers.keySet());
		return result;
	}

	@Override
	public void removeStorage(String id) throws Exception {
		String containerId = id + ".dbxml";

		// close container
		openContainers.get(containerId).close();

		// remove container
		xmlManager.removeContainer(getTransaction(), containerId);

		// clean up open connections
		openContainers.remove(containerId);
	}

	@Override
	public void houseKeeping() throws Exception {
		// create checkpoint
		logSystem.info("Begin Check Point");
		CheckpointConfig cpc = new CheckpointConfig();
		getEnvironment().checkpoint(cpc);
		logSystem.info("End Check Point");

		getEnvironment().removeOldLogFiles();
		logSystem.info("Log files removed");
	}

	public void close() throws Exception {
		// cleanup
		houseKeeping();

		// close all open containers
		try {
			getTransaction().abort();
			for (Iterator<String> iterator = openContainers.keySet().iterator(); iterator
					.hasNext();) {
				openContainers.get(iterator.next()).close();
			}
		} catch (Exception e) {
			DDIFtpException ddiFtpE = new DDIFtpException(
					"Error on XMLContainer close", e);
			throw ddiFtpE;
		} finally {
			try {
				// close the xml manager and eviroment
				xmlManager.delete();
			} catch (Exception e) {
				DDIFtpException ddiFtpE = new DDIFtpException(
						"Error on XMLManager delete, allso deletes the XML Eviroment ",
						e);
				throw ddiFtpE;
			}
		}
	}

	@Profiled(tag = "getTransaction")
	protected synchronized XmlTransaction getTransaction() throws Exception {
		if (transaction.get() == null) {
			TransactionConfig tc = new TransactionConfig();
			XmlTransaction t = xmlManager.createTransaction(null, tc);
			logSystem.info("Transaction created, id: "
					+ t.getTransaction().getId());
			transaction.set(t);
		} else if (logSystem.isDebugEnabled()) {
			logSystem.debug("Reusing transaction, id: "
					+ transaction.get().getTransaction().getId());
		}
		return transaction.get();
	}

	@Profiled(tag = "commitTransaction")
	protected synchronized void commitTransaction() throws Exception {
		if (transaction.get() != null) {
			if (System.getProperty("ddieditor.test") != null
					&& System.getProperty("ddieditor.test").equals("true")) {
				logSystem.debug("Testmode! Not commiting transaction, id: "
						+ transaction.get().getTransaction().getId());
			} else {
				logSystem.info("Commiting transaction, id: "
						+ getTransaction().getTransaction().getId());
				transaction.get().commit();
				transaction.get().delete();
				transaction.set(null);
				// if (currentWorikingContainer.length() > 1) {
				// getContainer(currentWorikingContainer).sync();
				// }
			}
		}
	}

	protected synchronized void rollbackTransaction() throws Exception {
		if (transaction.get() != null) {
			logSystem.info("Transaction rollback, id: "
					+ getTransaction().getTransaction().getId());
			transaction.get().abort();
			transaction.get().delete();
			transaction.set(null);
		}
	}

	public String getResourcePath(StorageType storage, String resource)
			throws DDIFtpException {
		StringBuilder result = new StringBuilder();
		result.append("doc(\"dbxml:/");
		result.append(storage.getConnection());
		result.append("/");
		result.append(resource);
		result.append("\")");
		return result.toString();
	}

	public String getGlobalResourcePath(StorageType storage) throws Exception {
		StringBuilder result = new StringBuilder();
		result.append("collection(\"dbxml:/");
		result.append(storage.getConnection());
		result.append("\")");
		return result.toString();
	}

	public void listIndices(XmlContainer xmlContainer) throws Exception {
		// indices
		XmlIndexSpecification indexSpecification = xmlContainer
				.getIndexSpecification(getTransaction());
		XmlIndexDeclaration indexDeclaration = null;
		if (logSystem.isDebugEnabled()) {
			while ((indexDeclaration = (indexSpecification.next())) != null) {
				logSystem.debug(indexDeclaration.name);
				logSystem.debug(indexDeclaration.index);
			}
		}
		indexSpecification.delete();
	}

	public void createIndices(XmlContainer xmlContainer) throws Exception {
		// index option
		if (!DdiEditorConfig.getBoolean(DdiEditorConfig.DBXML_DDI_INDEX)) {
			return;
		}

		// define indices
		String namespace[] = { "", "" };
		String nameOfXmlElement[] = { "id", "version" };
		String indexString[] = { "node-attribute-equality-string",
				"node-attribute-equality-string node-attribute-presence" };

		// tried
		// id node-attribute-substring-string = slow
		// deleting default index name unique-node-metadata-equality-string =
		// slow

		// create indexes
		XmlIndexSpecification indexSpecification = xmlContainer
				.getIndexSpecification(getTransaction());
		XmlUpdateContext xmlUpdateContext = getXmlUpdateContext();
		for (int i = 0; i < indexString.length; i++) {
			indexSpecification.addIndex(namespace[i], nameOfXmlElement[i],
					indexString[i]);
		}
		indexSpecification.addDefaultIndex("node-metadata-presence");

		// update xml container with indices
		xmlContainer.setIndexSpecification(getTransaction(),
				indexSpecification, xmlUpdateContext);
		commitTransaction();

		// clean up
		indexSpecification.delete();
	}

	public void addResource(Object obj) throws Exception {
		if (!(obj instanceof File)) {
			throw new DDIFtpException("Must be a file!");
		}
		File path = (File) obj;
		if (path == null || !path.exists()) {
			throw new DDIFtpException("File not found: "
					+ path.getAbsolutePath());
		}

		// file stream
		XmlInputStream xmlInputStream = xmlManager
				.createLocalFileInputStream(path.getAbsolutePath());

		// ingest
		XmlContainer xmlContainer = null;
		try {
			xmlContainer = getContainer(currentWorkingContainer);
			xmlContainer.putDocument(getTransaction(), path.getName(),
					xmlInputStream, getXmlDocumentConfig());
		} catch (XmlException e) {
			if (e.getErrorCode() == XmlException.UNIQUE_ERROR) {
				logSystem.warn("Xml document: " + path.getName()
						+ " exists in container!");
			}
			throw e;
		}
		xmlInputStream.delete();
	}

	@Override
	public void removeResource(String docName) throws Exception {
		List<String> documents = getResources();
		if (documents.contains(docName)) {
			if (logSystem.isInfoEnabled()) {
				logSystem.info("Remove doc: " + docName + " from container: "
						+ currentWorkingContainer);
			}

			// remove xml file
			getWorkingContainer().deleteDocument(getTransaction(), docName,
					getXmlUpdateContext());
		}
	}

	public List<String> getResources() throws Exception {
		List<String> result = new ArrayList<String>();

		XmlResults xmlResults = getWorkingContainer().getAllDocuments(
				getXmlDocumentConfig());

		XmlDocument xmlDocument;
		while (xmlResults.hasNext()) {
			xmlDocument = xmlResults.next().asDocument();
			result.add(xmlDocument.getName());
		}
		xmlResults.delete();
		return result;
	}

	@Profiled(tag = "query_{$0}")
	public List<String> query(String query) throws Exception {
		XmlResults rs = xQuery(query);
		List<String> result = new ArrayList<String>();
		while (rs.hasNext()) {
			result.add(rs.next().asString());
		}
		rs.delete();
		commitTransaction();
		return result;
	}

	@Profiled(tag = "updateQuery_{$0}")
	public void updateQuery(String query) throws Exception {
		XmlResults rs = null;
		XmlQueryContext xmlQueryContext = xmlManager.createQueryContext(
				XmlQueryContext.LiveValues, XmlQueryContext.Lazy);
		XmlDocumentConfig xmlDocumentConfig = new XmlDocumentConfig();

		for (int i = 0; i < Ddi3NamespacePrefix.values().length; i++) {
			xmlQueryContext.setNamespace(
					Ddi3NamespacePrefix.values()[i].getPrefix(),
					Ddi3NamespacePrefix.values()[i].getNamespace());
		}

		try {
			rs = xmlManager.query(getTransaction(), query, xmlQueryContext,
					xmlDocumentConfig);
			commitTransaction();
		} catch (Exception e) {
			rollbackTransaction();
			throw new DDIFtpException("Error on query execute of: " + query, e);
		}
		rs.delete();
	}

	public DefineQueryPositionResult defineQueryPosition(String elementType,
			String query, String[] subElements, String[] stopElements,
			String[] jumpElements) throws Exception {
		DefineQueryPositionResult result = new DefineQueryPositionResult();

		// query
		queryLog.info(query);
		XmlResults rs = xQuery(query);

		if (rs.isNull()) { // guard
			throw new DDIFtpException("No results for query: " + query,
					new Throwable());
		}

		// init value
		XmlValue xmlValue = rs.next();
		if (xmlValue == null) { // guard
			rs.delete();
			rs = null;
			commitTransaction();
			throw new DDIFtpException("No results for query: " + query,
					new Throwable());
		}

		String initStart = null, localName = null, foundName = null, located = null, jumpName = null;

		boolean stop = false, jumpEnd = false;
		int locatedCount = 0;

		if (xmlValue.isNode()) {
			XmlEventReader reader = xmlValue.asEventReader();
			while (reader.hasNext()) {
				int type = reader.next();

				// end element
				if (type == XmlEventReader.EndElement) {
					localName = reader.getLocalName();

					// guard
					if (initStart.equals(localName)) {
						break;
					}
				}

				// start element
				if (type == XmlEventReader.StartElement) {
					// parent:
					localName = reader.getLocalName();

					if (initStart == null) {
						initStart = localName;
					}

					// check against stop elements
					for (int i = 0; i < stopElements.length; i++) {
						// check if localName is part of stop list
						if (localName.equals(stopElements[i])) {
							if (localName.equals(initStart)) {
								// TODO reset all - normal insert with search
								// for equal elements
							}
							stop = true;
							located = localName;
							locatedCount = 1;

							// set insert key word
							if (elementType.equals(stopElements[i])) {
								result.insertKeyWord = XQueryInsertKeyword.AFTER;
							} else {
								result.insertKeyWord = XQueryInsertKeyword.BEFORE;
							}
							break;
						}
					}
					if (stop) {
						break;
					}

					// check against jump elements
					for (int i = 0; i < jumpElements.length; i++) {
						// check if localName is part of jump list
						if (localName.equals(jumpElements[i])) {
							jumpName = jumpElements[i];
							result.insertKeyWord = XQueryInsertKeyword.AFTER;
							locatedCount = 1;

							// check for subsidiary jump elements
							JumpResult jumpResult = scanForJumpElements(
									locatedCount, jumpName, jumpElements,
									stopElements, reader);
							located = jumpResult.getName();
							locatedCount = jumpResult.getLocation();
							jumpEnd = true;
							break;
						}
					}
					if (jumpEnd) {
						break;
					}

					// sub elements
					for (int i = 0; i < subElements.length; i++) {
						// check if localName is a parent sub-element
						if (localName.equals(subElements[i])) {
							foundName = subElements[i];
							break;
						}
					}
					if (foundName != null) {
						if (located != null && located.equals(foundName)) {
							locatedCount++;
						} else {
							located = foundName;
							locatedCount = 1;
						}
						foundName = null;
						continue;
					}
				}
			}
			reader.close();
		}
		rs.delete();
		rs = null;
		commitTransaction();

		// build query
		if (located == null) {
			return new DefineQueryPositionResult(XQueryInsertKeyword.INTO,
					new StringBuilder(query));
		} else {
			StringBuilder queryResult = new StringBuilder(query);
			try {
				queryResult.append(DdiManager
						.getInstance()
						.getDdi3NamespaceHelper()
						.addFullyQualifiedNamespaceDeclarationToElements(
								located));
			} catch (DDIFtpException e) {
				// TODO enhance namespace conventions on locale name with out
				// namespace, how build up and test against
				// Ddi3NamespaceHelper.namespace properties
				StringBuilder convention = new StringBuilder();
				convention.append(located.toLowerCase());
				convention.append("__");
				convention.append(located);
				queryResult.append(DdiManager
						.getInstance()
						.getDdi3NamespaceHelper()
						.addFullyQualifiedNamespaceDeclarationToElements(
								convention.toString()));
			}
			queryResult.append("[");
			queryResult.append(locatedCount);
			queryResult.append("]");
			result.query = queryResult;

			// guard
			if (result.insertKeyWord == null) {
				result.insertKeyWord = XQueryInsertKeyword.AFTER;
			}

			return result;
		}
	}

	private class JumpResult {
		int location;
		String name;

		JumpResult(int location, String name) {
			this.location = location;
			this.name = name;
		}

		public int getLocation() {
			return this.location;
		}

		public String getName() {
			return this.name;
		}
	}

	// Check if other Jump Elements follows
	private JumpResult scanForJumpElements(int locatedCount, String jumpName,
			String[] jumpElements, String[] stopElements, XmlEventReader reader)
			throws XmlException {
		String localName = null;

		// continue reading
		while (reader.hasNext()) {
			int type = reader.next();

			// start element
			if (type == XmlEventReader.StartElement) {
				localName = reader.getLocalName();

				for (int i = 0; i < jumpElements.length; i++) {
					// look for jump element
					if (jumpElements[i].equals(localName)) {
						if (localName.equals(jumpName)) {
							locatedCount++;
						} else {
							locatedCount = 1;
							jumpName = localName;
						}
						continue;
					}
				}

				// look for stop elements
				for (int i = 0; i < stopElements.length; i++) {
					// check if localName is part of stop list
					if (localName.equals(stopElements[i])) {
						break;
					}
				}
			}
		}
		// this.jumpName = jumpName; // temp!!!!!!
		// return locatedCount;
		return new JumpResult(locatedCount, jumpName);
	}

	@Profiled(tag = "xQuery")
	protected XmlResults xQuery(String query) throws Exception {
		XmlQueryContext xmlQueryContext = xmlManager.createQueryContext(
				XmlQueryContext.LiveValues, XmlQueryContext.Lazy);
		XmlDocumentConfig xmlDocumentConfig = new XmlDocumentConfig();
		// xmlDocumentConfig.setLazyDocs(false);
		// xmlDocumentConfig.setWellFormedOnly(true);
		// XmlQueryExpression used to requery
		// Cache idear: hash query and store hash and query expression in
		// hashmap
		// query reuse via lookup in hashmap
		// XmlQueryExpression xmlQueryExpression = null;
		// try {
		// xmlQueryExpression = xmlManager.prepare(getTransaction(), query,
		// xmlQueryContext);
		// logBug.info(xmlQueryExpression.getQueryPlan());
		// } catch (Exception e) {
		// if (xmlQueryContext != null) {
		// xmlQueryContext.delete();
		// }
		// throw new DDIFtpException("Error prepare query: " + query, e);
		// }

		// xmlQueryContext.setNamespace(prefix, uri)
		XmlResults rs = null;
		try {
			// rs = xmlQueryExpression.execute(getTransaction(),
			// xmlQueryContext);
			rs = xmlManager.query(getTransaction(), query, xmlQueryContext,
					xmlDocumentConfig);
		} catch (Exception e) {
			rollbackTransaction();
			throw new DDIFtpException("Error on query execute of: " + query, e);
		} finally {
			// if (xmlQueryContext != null) {
			// xmlQueryContext.delete();
			// }
			// xmlQueryExpression.delete();
		}
		return rs;
	}

	public boolean querySingleBoolean(String query) throws Exception {
		XmlResults rs = xQuery(query);
		boolean result = false;
		while (rs.hasNext()) {
			XmlValue xmlValue = rs.next();
			result = xmlValue.asBoolean();
		}
		rs.delete();
		rs = null;
		return result;
	}

	public String querySingleString(String query) throws Exception {
		XmlResults rs = xQuery(query);
		String result = "na";
		while (rs.hasNext()) {
			XmlValue xmlValue = rs.next();
			result = xmlValue.asString();
		}
		rs.delete();
		rs = null;
		return result;
	}

	public MaintainableLabelQueryResult queryMaintainableLabel(
			MaintainableLabelQuery maintainableLabelQuery,
			MaintainableLabelQueryResult maintainableLabelQueryResult)
			throws Exception {
		// query
		queryLog.info(maintainableLabelQuery.getQuery());
		XmlResults rs = xQuery(maintainableLabelQueryResult.getQuery());

		if (rs.isNull()) { // guard
			throw new DDIFtpException("No results for query: "
					+ maintainableLabelQueryResult.getQuery());
		}

		// init value
		XmlValue xmlValue = rs.next();
		if (xmlValue == null) { // guard
			rs.delete();
			rs = null;
			rollbackTransaction();
			return maintainableLabelQueryResult;
		}

		// populate result
		String localName;
		String localMaintainableName = DdiManager
				.getInstance()
				.getDdi3NamespaceHelper()
				.getLocalSchemaName(
						maintainableLabelQuery.getMaintainableTarget());
		String prevLocalName = "";

		if (xmlValue.isNode()) {
			XmlEventReader reader = xmlValue.asEventReader();
			while (reader.hasNext()) {
				int type = reader.next();
				if (type == XmlEventReader.StartElement) {
					localName = reader.getLocalName();

					// maintainable attrs
					if (localName.equals(localMaintainableName)) {
						// target scheme/@id @version @agency
						int attrs = reader.getAttributeCount();
						for (int i = 0; i < attrs; i++) {
							if (reader.getAttributeLocalName(i).equals("id")) {
								maintainableLabelQueryResult.setId(reader
										.getAttributeValue(i));
							}
							if (reader.getAttributeLocalName(i).equals(
									"version")) {
								maintainableLabelQueryResult.setVersion(reader
										.getAttributeValue(i));
							}
							if (reader.getAttributeLocalName(i)
									.equals("agency")) {
								maintainableLabelQueryResult.setAgency(reader
										.getAttributeValue(i));
							}
						}
					}

					// sub elements
					for (String queryLocalName : maintainableLabelQueryResult
							.getResult().keySet()) {
						// extract only name element below maintainable target
						if (localName.equals(queryLocalName)) {
							if (localName.equals("Name")) {
								if (!prevLocalName
										.equals(localMaintainableName)) {
									continue;
								}
							}

							// extract start tag
							StringBuffer element = new StringBuffer("<");
							String prefix = reader.getPrefix();
							if (prefix != null) {
								element.append(prefix);
								element.append(":");
							}
							element.append(localName);

							// attributes
							int attrs = reader.getAttributeCount();
							for (int i = 0; i < attrs; i++) {
								element.append(" ");
								String attrPrefix = reader
										.getAttributePrefix(i);
								String attrLocalname = reader
										.getAttributeLocalName(i);
								String attrValue = reader.getAttributeValue(i);

								if (attrPrefix != null) {
									element.append(attrPrefix);
									element.append(":");
								}
								element.append(attrLocalname);
								element.append("=\"");
								element.append(attrValue);
								element.append("\"");
							}

							// empty element
							if (reader.isEmptyElement()) {
								element.append("/>");
							} else {
								element.append(">");
							}

							// extract until end tag
							extractSubelementsOfSchemeQuery(element, reader,
									localName);

							// add to result
							String insertKey = null;
							for (String key : maintainableLabelQueryResult
									.getResult().keySet()) {
								if (key.indexOf(localName) > -1) {
									insertKey = key;
								}
							}
							maintainableLabelQueryResult.getResult()
									.get(localName).addLast(element.toString());
						}
					}

					// reset prev local name to target maintainable name
					if (localName.equals("Name")
							&& prevLocalName.equals(localMaintainableName)) {
						prevLocalName = localMaintainableName;
					} else {
						prevLocalName = localName;
					}

					// stop read at subelements
					boolean end = false;
					for (int i = 0; i < maintainableLabelQuery
							.getStopElementNames().length; i++) {
						if (localName.equals(maintainableLabelQuery
								.getStopElementNames()[i])) {
							end = true;
							break;
						}
					}
					if (end) {
						break;
					}
				}

				// stop read at end element of target maintainable
				else if (type == XmlEventReader.EndElement
						&& reader.getLocalName().equals(localMaintainableName)) {
					break;
				}
			}
			reader.close();
		}
		rs.delete();
		rs = null;
		commitTransaction();
		return maintainableLabelQueryResult;
	}

	public void indexForProfile() throws Exception {
		// query
		String query = "/*";
		queryLog.info(query);
		XmlResults rs = xQuery(query);

		if (rs.isNull()) { // guard
			rollbackTransaction();
			throw new DDIFtpException("No results for query: " + query);
		}

		// init value
		XmlValue xmlValue = rs.next();
		if (xmlValue == null) { // guard
			rs.delete();
			rs = null;
			rollbackTransaction();
		}

		// populate result
		String localName;
		String localMaintainableName = "";
		String prevLocalName = "";

		if (xmlValue.isNode()) {
			XmlEventReader reader = xmlValue.asEventReader();
			while (reader.hasNext()) {
				int type = reader.next();
				if (type == XmlEventReader.StartElement) {
					localName = reader.getLocalName();
					QName qName = new QName(reader.getNamespaceURI(), localName);

					// maintainable
					if (DdiManager.getInstance().getDdi3NamespaceHelper()
							.isMaintainable(localName)) {
						// DdiManager.getInstance().getDdi3NamespaceHelper().
					}

					// versionables

					// identifiable

				}
				reader.close();
			}
			rs.delete();
			rs = null;
			commitTransaction();
		}
	}

	private void extractSubelementsOfSchemeQuery(StringBuffer element,
			XmlEventReader reader, String stopElement) throws Exception {
		boolean characterEvent = false;
		boolean end = false;
		while (!end && reader.hasNext()) {
			int eventType = reader.next();
			switch (eventType) {
			case XmlEventReader.StartElement: {
				String localName = reader.getLocalName();
				String prefix = reader.getPrefix();

				StringBuffer startElement = new StringBuffer("<");
				if (prefix != null) {
					startElement.append(prefix);
					startElement.append(":");
				}
				startElement.append(localName);

				// attr
				StringBuilder attr = new StringBuilder();
				int attrSize = reader.getAttributeCount();
				if (attrSize > 0) {
					for (int i = 0; i < attrSize; i++) {
						attr.append(" ");
						if (reader.getAttributePrefix(i) != null
								&& !reader.getAttributePrefix(i).equals("")) {
							attr.append(reader.getAttributePrefix(i));
							attr.append(":");
						}
						attr.append(reader.getAttributeLocalName(i));
						attr.append("=\"");
						attr.append(reader.getAttributeValue(i));
						attr.append("\"");
					}
				}
				startElement.append(attr);

				if (reader.isEmptyElement()) {
					startElement.append("/>");
				} else {
					startElement.append(">");
				}

				element.append(startElement.toString());
				break;
			}
			case XmlEventReader.Characters: {
				characterEvent = true;
				element.append(reader.getValue());
				break;
			}
			case XmlEventReader.EndElement: {
				String localName = reader.getLocalName();
				String prefix = reader.getPrefix();

				StringBuffer endElement = new StringBuffer("</");
				if (prefix != null) {
					endElement.append(prefix);
					endElement.append(":");
				}
				endElement.append(localName);
				endElement.append(">");
				element.append(endElement.toString());

				if (stopElement.equals(localName)) {
					end = true;
				}
				break;
			}
			case XmlEventReader.CDATA: {
				break;
			}
			case XmlEventReader.Whitespace: {
				break;
			}
			}
		}
	}

	public MaintainableLightLabelQueryResult queryMaintainableLightLabel(
			MaintainableLabelQuery maintainableLabelQuery,
			MaintainableLightLabelQueryResult maintainableLightLabelQueryResult)
			throws Exception {

		// query
		queryLog.info(maintainableLabelQuery.getQuery());
		XmlResults rs = xQuery(maintainableLabelQuery.getQuery());

		if (rs.isNull()) { // guard
			throw new DDIFtpException("No results for query: "
					+ maintainableLabelQuery.getQuery());
		}

		// init value
		XmlValue xmlValue = rs.next();
		if (xmlValue == null) { // guard
			rs.delete();
			rs = null;
			commitTransaction();
			return maintainableLightLabelQueryResult;
		}

		// populate result
		String localName;
		String localMaintainableName = DdiManager
				.getInstance()
				.getDdi3NamespaceHelper()
				.getLocalSchemaName(
						maintainableLabelQuery.getMaintainableTarget());

		boolean end = false;
		String prevLocalName = "";
		LightXmlObjectType lightXmlObject = null;

		// target labels
		LightXmlObjectType tagetLabels;
		// TODO a bit of a hotfix with boiler plate code ...
		if (xmlValue.isNode()) {
			XmlEventReader reader = xmlValue.asEventReader();
			while (reader.hasNext()) {
				int type = reader.next();
				if (type == XmlEventReader.StartElement) {
					localName = reader.getLocalName();

					// target light xmlobject
					if (localName.equals(localMaintainableName)) {
						tagetLabels = LightXmlObjectListDocument.Factory
								.newInstance().addNewLightXmlObjectList()
								.addNewLightXmlObject();
						tagetLabels.setElement(localName);
						setLabelsOnMaintainableLightSubelement(reader,
								tagetLabels, maintainableLightLabelQueryResult
										.getResult().keySet());
						maintainableLightLabelQueryResult
								.setLabelList(tagetLabels.getLabelList());
						break;
					}
				}
			}
			reader.close();

			// maintainable target attr and sub elements
			reader = xmlValue.asEventReader();
			while (reader.hasNext()) {
				int type = reader.next();
				if (type == XmlEventReader.StartElement) {
					localName = reader.getLocalName();

					// maintainable attrs
					if (localName.equals(localMaintainableName)) {
						// target maintainable/@id @version @agency
						int attrs = reader.getAttributeCount();
						for (int i = 0; i < attrs; i++) {
							if (reader.getAttributeLocalName(i).equals("id")) {
								maintainableLightLabelQueryResult.setId(reader
										.getAttributeValue(i));
							}
							if (reader.getAttributeLocalName(i).equals(
									"version")) {
								maintainableLightLabelQueryResult
										.setVersion(reader.getAttributeValue(i));
							}
							if (reader.getAttributeLocalName(i)
									.equals("agency")) {
								maintainableLightLabelQueryResult
										.setAgency(reader.getAttributeValue(i));
							}
						}

						// skip sub elements of maintainable target
						continue;
					}

					// sub elements
					for (String queryLocalName : maintainableLightLabelQueryResult
							.getResult().keySet()) {
						if (queryLocalName.equals(localName)) {
							// insert
							lightXmlObject = extractLightXmlObject(localName,
									reader, maintainableLightLabelQueryResult);
							maintainableLightLabelQueryResult.getResult()
									.get(localName).addLast(lightXmlObject);
							break;
						}
					}

					// stop read at stop elements
					for (int i = 0; i < maintainableLabelQuery
							.getStopElementNames().length; i++) {
						if (localName.equals(maintainableLabelQuery
								.getStopElementNames()[i])) {
							end = true;
							break;
						}
					}
					if (end) {
						break;
					}

					// stop read at end element of target maintainable
					// else if (type == XmlEventReader.EndElement
					// && reader.getLocalName().equals(
					// localMaintainableName)) {
					// break;
					// }
				}
			}
		}
		rs.delete();
		rs = null;
		commitTransaction();
		return maintainableLightLabelQueryResult;
	}

	private LightXmlObjectType extractLightXmlObject(String localName,
			XmlEventReader reader,
			MaintainableLightLabelQueryResult maintainableLightLabelQueryResult)
			throws Exception {

		// init light xml object
		LightXmlObjectType lightXmlObject = LightXmlObjectType.Factory
				.newInstance();
		lightXmlObject.setParentId(maintainableLightLabelQueryResult.getId());
		lightXmlObject.setParentVersion(maintainableLightLabelQueryResult
				.getVersion());
		lightXmlObject.setElement(localName);

		// attributes
		int attrs = reader.getAttributeCount();
		for (int i = 0; i < attrs; i++) {
			if (reader.getAttributeLocalName(i).equals("id")) {
				lightXmlObject.setId(reader.getAttributeValue(i));
			}
			if (reader.getAttributeLocalName(i).equals("version")) {
				lightXmlObject.setVersion(reader.getAttributeValue(i));
			}
		}

		// labels
		setLabelsOnMaintainableLightSubelement(reader, lightXmlObject, null);
		return lightXmlObject;
	}

	private void setLabelsOnMaintainableLightSubelement(XmlEventReader reader,
			LightXmlObjectType lightXmlObject, Set<String> subElementLocalnames)
			throws Exception {
		// labels
		LabelType label = null;
		String localName, attrLang = "lang";
		String localLabelName = DdiManager.getInstance()
				.getDdi3NamespaceHelper().getLabelNames()
				.getProperty(lightXmlObject.getElement());
		if (localLabelName == null) {
			// guard
			return;
		}

		// scan xml
		while (reader.hasNext()) {
			int type = reader.next();
			if (type == XmlEventReader.StartElement) {
				localName = reader.getLocalName();

				// test when to break; when reaching sub elements ;- )
				if (subElementLocalnames != null) {
					for (String subLocalname : subElementLocalnames) {
						if (subLocalname.equals(localName)) {
							return;
						}
					}
				}

				if (localName.equals(localLabelName)) {
					label = LabelType.Factory.newInstance();
					// set attributes
					for (int h = 0; h < reader.getAttributeCount(); h++) {
						if (reader.getAttributeLocalName(h).equals(attrLang)) {
							label.setLang(reader.getAttributeValue(h));
						}
					}
				}
			} else if (label != null && type == XmlEventReader.Characters) {
				// set text on light label
				XmlBeansUtil.setTextOnMixedElement(label, reader.getValue());
				lightXmlObject.getLabelList().add(label);
				label = null;
			} else if (type == XmlEventReader.EndElement) {
				if (reader.getLocalName().equals(lightXmlObject.getElement())) {
					break;
				}
			}
		}
	}

	public void exportResource(String document, File file) throws Exception {
		// file
		if (file.exists()) {
			file.delete();
		}
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		FileChannel rafFc = raf.getChannel();
		XmlEventReader reader = null;

		// reader
		try {
//			String result = getWorkingContainer().getDocument(getTransaction(),
//					document).getContentAsString();
//			writeExportDocument(rafFc, result);
//			if (0 == 0) {
//				return;
//			}

			reader = getWorkingContainer().getDocument(getTransaction(),
					document).getContentAsEventReader();
		} catch (Exception e) {
			throw e;
		}
		boolean characterEvent = false;

		// xml events
		while (reader.hasNext()) {
			int eventType = reader.next();
			switch (eventType) {
			case XmlEventReader.StartElement: {
				int attrs = reader.getAttributeCount();

				// element
				String localname = reader.getLocalName();
				String prefix = reader.getPrefix();

				StringBuffer startElement = new StringBuffer("<");
				if (prefix != null) {
					startElement.append(prefix);
					startElement.append(":");
				}
				startElement.append(localname);

				// attributes
				for (int i = 0; i < attrs; i++) {
					startElement.append(" ");
					String attrPrefix = reader.getAttributePrefix(i);
					String attrLocalname = reader.getAttributeLocalName(i);
					String attrValue = reader.getAttributeValue(i);

					if (attrPrefix != null) {
						startElement.append(attrPrefix);
						startElement.append(":");
					}
					startElement.append(attrLocalname);
					startElement.append("=\"");
					startElement.append(attrValue);
					startElement.append("\"");
				}

				// empty element
				if (reader.isEmptyElement()) {
					startElement.append("/>");
				} else {
					startElement.append(">");
				}

				writeExportDocument(rafFc, startElement.toString());
				break;
			}
			case XmlEventReader.EndElement: {
				String localName = reader.getLocalName();
				String prefix = reader.getPrefix();

				StringBuffer endElement = new StringBuffer("</");
				if (prefix != null) {
					endElement.append(prefix);
					endElement.append(":");
				}
				endElement.append(localName);
				endElement.append(">");
				writeExportDocument(rafFc, endElement.toString());
				break;
			}
			case XmlEventReader.CDATA: {
			}
			case XmlEventReader.Characters: {
				characterEvent = true;
				String value = reader.getValue();
				value= value.replaceAll("&", "&amp;");
				value= value.replaceAll("<", "&lt;");
				value= value.replaceAll(">", "&gt;");
				value= value.replaceAll("\"", "&quot;");
				value= value.replaceAll("'", "&apos;");
				writeExportDocument(rafFc, value);
			}
			case XmlEventReader.Comment: {
				if (characterEvent != true) {
					StringBuffer comment = new StringBuffer("<!--");
					comment.append(reader.getValue());
					comment.append("-->");
					writeExportDocument(rafFc, comment.toString());
				}
			}
			case XmlEventReader.Whitespace: {
				break;
			}
			case XmlEventReader.StartDocument: {
				writeExportDocument(
						rafFc,
						"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<?xml-stylesheet type= \"text/xsl\" href=\"ddi3_1.xsl\"?>");
				break;
			}
			case XmlEventReader.EndDocument: {
				writeExportDocument(rafFc, "\n");
				break;
			}
			case XmlEventReader.DTD: {
				break;
			}
			case XmlEventReader.ProcessingInstruction: {
				StringBuffer process = new StringBuffer();
				process.append("\n<?");
				process.append(reader.getLocalName());
				process.append(" ");
				process.append(reader.getValue());
				process.append("?>\n");
				writeExportDocument(rafFc, process.toString());
				break;
			}
			case XmlEventReader.StartEntityReference: {
				reader.getReportEntityInfo();
				break;
			}
			case XmlEventReader.EndEntityReference: {
				break;
			}
			default:
				throw new DDIFtpException("Export to file, unknown event type");
			}
			characterEvent = false;
		}
		reader.close();
	}

	private void writeExportDocument(FileChannel rafFc, String value)
			throws Exception {
		rafFc.write(ByteBuffer.wrap(value.getBytes("utf-8")));
	}
}
