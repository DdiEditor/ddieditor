package org.ddialliance.ddieditor.persistenceaccess.dbxml;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.ddialliance.ddieditor.model.DdiManager;
import org.ddialliance.ddieditor.model.lightxmlobject.LabelType;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;
import org.ddialliance.ddieditor.model.namespace.ddi3.Ddi3NamespacePrefix;
import org.ddialliance.ddieditor.model.resource.StorageType;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceManager;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceStorage;
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
 * Accesses the Oracle Berkeley XML data base
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
	public static final String ENVIROMENT_HOME = new File(DdiEditorConfig
			.get(DdiEditorConfig.DBXML_ENVIROMENT_HOME)).getAbsolutePath();

	private XmlManager xmlManager = null;
	private XmlManagerConfig xmlManagerConfig;
	private XmlQueryContext xmlQueryContext;
	private XmlContainerConfig xmlContainerConfig;
	private ThreadLocal<XmlTransaction> transaction = new ThreadLocal<XmlTransaction>();
	private String currentWorikingContainer = "";
	private HashMap<String, XmlContainer> openContainers = new HashMap<String, XmlContainer>();

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

				// xml manager
				instance.xmlManager = new XmlManager(instance.getEnvironment(),
						instance.getXmlManagerConfig());
			} catch (Exception e) {
				new DDIFtpException("Error on BDBXML startup", e);
			}
		}
		return instance;
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
				environment = new Environment(new File(
						DbXmlManager.ENVIROMENT_HOME), getEnvironmentConfig());
			} catch (Exception e) {
				throw new DDIFtpException(
						"Error on setting up BDBXML enviroment", e);
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
			environmentConfig.setLogBufferSize(128 * 1024); // default 32KB
			environmentConfig.setInitializeCache(true); // shared memory region
			environmentConfig.setCacheSize(100 * 1024 * 1024); // 100MB cache

			// transaction
			environmentConfig.setTransactional(true);
			environmentConfig.setTxnMaxActive(20000);
			// teori: cachesize/pagesize ~ cache logBuffer
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
//			XmlManager.setLogLevel(XmlManager.LEVEL_ALL, true);
//			XmlManager.setLogCategory(XmlManager.CATEGORY_CONTAINER
//					| XmlManager.CATEGORY_MANAGER | XmlManager.CATEGORY_QUERY,
//					true);
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
			xmlContainerConfig.setIndexNodes(true);
			xmlContainerConfig.setNodeContainer(true);
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
		return openContainers.get(currentWorikingContainer);
	}

	public void setWorkingConnection(StorageType storage) throws Exception {
		openContainer(new File(storage.getConnection()));
	}

	public void openContainer(File file) throws Exception {
		if (getContainer(file.getName()) == null) {
			try {
				if (!file.exists()) {
					if (logSystem.isDebugEnabled()) {
						logSystem.debug("Creating dbxml container: "
								+ file.getAbsolutePath());
					}
					XmlContainer xmlContainer = xmlManager.createContainer(file
							.getName(), instance.getXmlContainerConfig());
					openContainers.put(file.getName(), xmlContainer);

					// create indices
					if (!file.getName().equals(
							PersistenceManager.RESOURCE_LIST_FILE)) {
						createIndices(xmlContainer);
						listIndices(xmlContainer);
					}
				} else {
					if (logSystem.isDebugEnabled()) {
						logSystem.debug("Opening dbxml container: "
								+ file.getAbsolutePath());
					}
					openContainers.put(file.getName(), xmlManager
							.openContainer(file.getName(), instance
									.getXmlContainerConfig()));
				}
			} catch (Exception e) {
				throw new DDIFtpException(
						"Error on create/ open xml conatiner: "
								+ file.getAbsolutePath(), e);
			}
		} else if (logSystem.isDebugEnabled()) {
			logSystem.debug("Is open dbxml container: "
					+ file.getAbsolutePath());
		}
		this.currentWorikingContainer = file.getName();
	}

	public void close() throws Exception {
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
			xmlContainer = getContainer(currentWorikingContainer);
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

	public void removeResource(String docName) throws Exception {
		List<String> documents = getResources();
		if (documents.contains(docName)) {
			if (logSystem.isInfoEnabled()) {
				logSystem.info("Remove doc: " + docName + " from container: "
						+ currentWorikingContainer);
			}
			getWorkingContainer().deleteDocument(getTransaction(), docName,
					getXmlUpdateContext());
		}
	}

	public List<String> getResources() throws Exception {
		List<String> documents = new ArrayList<String>();

		XmlResults xmlResults = getWorkingContainer().getAllDocuments(
				getXmlDocumentConfig());

		XmlDocument xmlDocument;
		while (xmlResults.hasNext()) {
			xmlDocument = xmlResults.next().asDocument();
			documents.add(xmlDocument.getName());
			xmlDocument.delete();
		}
		xmlResults.delete();

		return documents;
	}

	@Profiled(tag = "dbxml-query")
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

	@Profiled(tag = "dbxml-updatequery")
	public void updateQuery(String query) throws Exception {
		XmlResults rs = null;
		XmlQueryContext xmlQueryContext = xmlManager.createQueryContext(
				XmlQueryContext.LiveValues, XmlQueryContext.Lazy);
		XmlDocumentConfig xmlDocumentConfig = new XmlDocumentConfig();

		for (int i = 0; i < Ddi3NamespacePrefix.values().length; i++) {
			xmlQueryContext.setNamespace(Ddi3NamespacePrefix.values()[i]
					.getPrefix(), Ddi3NamespacePrefix.values()[i]
					.getNamespace());
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
			commitTransaction();
			return maintainableLabelQueryResult;
		}

		// populate result
		String localName;
		String localMaintainableName = DdiManager.getInstance()
				.getDdi3NamespaceHelper().getLocalSchemaName(
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
							maintainableLabelQueryResult.getResult().get(
									localName).addLast(element.toString());
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
		String localMaintainableName = DdiManager.getInstance()
				.getDdi3NamespaceHelper().getLocalSchemaName(
						maintainableLabelQuery.getMaintainableTarget());

		boolean end = false;
		String prevLocalName = "";
		LightXmlObjectType lightXmlObject = null;

		if (xmlValue.isNode()) {
			XmlEventReader reader = xmlValue.asEventReader();
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
						// insert
						// skip sub elements of maintainable target
						continue;
						// TODO include maintainable target light element!
					}

					// sub elements
					for (String queryLocalName : maintainableLightLabelQueryResult
							.getResult().keySet()) {
						if (queryLocalName.equals(localName)) {

							// init light xml object
							// lightXmlObject = LightXmlObjectType.Factory
							// .newInstance();
							// lightXmlObject
							// .setParentId(maintainableLightLabelQueryResult
							// .getId());
							// lightXmlObject
							// .setParentVersion(maintainableLightLabelQueryResult
							// .getVersion());
							// lightXmlObject.setElement(localName);

							// attributes
							// int attrs = reader.getAttributeCount();
							// for (int i = 0; i < attrs; i++) {
							// if (reader.getAttributeLocalName(i)
							// .equals("id")) {
							// lightXmlObject.setId(reader
							// .getAttributeValue(i));
							// }
							// if (reader.getAttributeLocalName(i).equals(
							// "version")) {
							// lightXmlObject.setVersion(reader
							// .getAttributeValue(i));
							// }
							// }

							// labels
							// setLabelsOnMaintainableLightSubelement(reader,
							// lightXmlObject);

							// insert
							lightXmlObject = extractLightXmlObject(localName,
									reader, maintainableLightLabelQueryResult);
							maintainableLightLabelQueryResult.getResult().get(
									localName).addLast(lightXmlObject);
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
		setLabelsOnMaintainableLightSubelement(reader, lightXmlObject);
		return lightXmlObject;
	}

	private void setLabelsOnMaintainableLightSubelement(XmlEventReader reader,
			LightXmlObjectType lightXmlObject) throws Exception {
		String localName;
		LabelType label = null;
		List<LabelType> tmpNameLabels = new ArrayList<LabelType>();
		String attrLang = "lang", labelPrecedence = "Name";
		
		// *Name takes precedence over *Label
		boolean isName = false;

		// scan xml
		while (reader.hasNext()) {
			int type = reader.next();
			if (type == XmlEventReader.StartElement) {
				localName = reader.getLocalName();
				for (Object localNameLabelName : DdiManager.getInstance()
						.getDdi3NamespaceHelper().getLocalNameLabelNames()) {
					if (localName.equals(localNameLabelName)) {
						if (localName.indexOf(labelPrecedence)>-1) {
							isName = true;
						}
						label = LabelType.Factory.newInstance();

						// set attributes
						for (int h = 0; h < reader.getAttributeCount(); h++) {
							if (reader.getAttributeLocalName(h)
									.equals(attrLang)) {
								label.setLang(reader.getAttributeValue(h));
							}
						}
					}
				}
			} else if (label != null && type == XmlEventReader.Characters) {
				// set text on light label
				XmlBeansUtil.setTextOnMixedElement(label, reader.getValue());

				// name
				if (isName) {
					lightXmlObject.getLabelList().add(label);
				}
				// label
				else {
					tmpNameLabels.add(label);
				}
				label = null;
				isName = false;
			} else if (type == XmlEventReader.EndElement) {
				if (reader.getLocalName().equals(lightXmlObject.getElement())) {
					break;
				}
			}
		}

		// enforce name over label rule
		if (lightXmlObject.getLabelList().isEmpty()) {
			lightXmlObject.getLabelList().addAll(tmpNameLabels);
		}
	}

	public void exportResource(String document, File file) throws Exception {
		// file
		if (file.exists()) {
			file.delete();
		}
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		FileChannel rafFc = raf.getChannel();

		// reader
		XmlEventReader reader = null;
		try {
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
				writeExportDocument(rafFc, reader.getValue());
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
				writeExportDocument(rafFc,
						"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
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
