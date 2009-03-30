package org.ddialliance.ddieditor.persistenceaccess.dbxml;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.lf5.LogLevel;
import org.ddialliance.ddieditor.model.resource.StorageType;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceStorage;
import org.ddialliance.ddieditor.util.DdiEditorConfig;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.log.Log;
import org.ddialliance.ddiftp.util.log.Log4jLogOutputStream;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;
import org.perf4j.aop.Profiled;

import com.sleepycat.db.Environment;
import com.sleepycat.db.EnvironmentConfig;
import com.sleepycat.db.LockDetectMode;
import com.sleepycat.dbxml.XmlContainer;
import com.sleepycat.dbxml.XmlContainerConfig;
import com.sleepycat.dbxml.XmlDocument;
import com.sleepycat.dbxml.XmlDocumentConfig;
import com.sleepycat.dbxml.XmlEventReader;
import com.sleepycat.dbxml.XmlException;
import com.sleepycat.dbxml.XmlInputStream;
import com.sleepycat.dbxml.XmlManager;
import com.sleepycat.dbxml.XmlManagerConfig;
import com.sleepycat.dbxml.XmlQueryContext;
import com.sleepycat.dbxml.XmlQueryExpression;
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
			environmentConfig.setTxnMaxActive(20000); // teori: cachesize/
			// pagesize ~ cache/
			// logBuffer
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
//environmentConfig.setDsyncDatabases(true);
			// error stream
			// change to property definition of dbxml log level
			XmlManager.setLogLevel(XmlManager.LEVEL_ALL, true);
			XmlManager.setLogCategory(XmlManager.CATEGORY_CONTAINER|XmlManager.CATEGORY_MANAGER|XmlManager.CATEGORY_QUERY,
			true);
			// be aware of log level may affect execution to hang!
//			environmentConfig.setErrorStream(new Log4jLogOutputStream(
//			 logPersistence, LogLevel.INFO));
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
			/*
			 * xmlContainerConfig.setAllowValidation(false);
			 * xmlContainerConfig.setIndexNodes(true);
			 * xmlContainerConfig.setNodeContainer(true);
			 */
			xmlContainerConfig.setTransactional(true);
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
					openContainers.put(file.getName(), xmlManager
							.createContainer(file.getName(), instance
									.getXmlContainerConfig()));
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

	public XmlTransaction getTransaction() throws Exception {
		if (transaction.get() == null) {
			XmlTransaction t = xmlManager.createTransaction();
			logSystem.info("Transaction created, id: "
					+ t.getTransaction().getId());
			transaction.set(t);
		} else if (logSystem.isDebugEnabled()) {
			logSystem.debug("Reusing transaction, id: "
					+ transaction.get().getTransaction().getId());
		}
		return transaction.get();
	}

	public synchronized void commitTransaction() throws Exception {
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
			}
		}
	}

	public synchronized void rollbackTransaction() throws Exception {
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
		try {
			getContainer(currentWorikingContainer).putDocument(path.getName(),
					xmlInputStream, getXmlDocumentConfig());
		} catch (XmlException e) {
			if (e.getErrorCode() == XmlException.UNIQUE_ERROR) {
				logSystem.warn("Xml document: " + path.getName()
						+ " exists in container!");
			}
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

	@Profiled(tag="dbxml-query")
	public List<String> query(String query) throws Exception {
		XmlResults rs = xQuery(query);
		List<String> result = new ArrayList<String>();
		while (rs.hasNext()) {
			result.add(rs.next().asString());
		}
		rs.delete();
		return result;
	}

	@Profiled(tag="dbxml-updatequery")
	public void updateQuery(String query) throws Exception {
		XmlResults rs = xQuery(query);
		rs.delete();
	}

	@Profiled(tag = "xQuery")
	protected XmlResults xQuery(String query) throws Exception {
		xmlQueryContext = xmlManager.createQueryContext(//XmlQueryContext.Eager);
				XmlQueryContext.LiveValues, XmlQueryContext.Lazy);
		// for (int i = 0; i < Ddi3NamespacePrefix.values().length; i++) {
		// Ddi3NamespacePrefix prefix = Ddi3NamespacePrefix.values()[i];
		// xmlQueryContext.setNamespace(prefix.getPrefix(),
		// prefix.getNamespace());
		// }
		XmlQueryExpression xmlQueryExpression = null;
		try {
			//StopWatch stopWatch = new LoggingStopWatch("xmldbQueryPrepare");
			// aprox 5-24 mills
			xmlQueryExpression = xmlManager.prepare(getTransaction(), query,
					xmlQueryContext);
			//System.out.println(xmlQueryExpression.getQueryPlan());
			//stopWatch.stop();
		} catch (Exception e) {
			if (xmlQueryContext != null) {
				xmlQueryContext.delete();
			}
			throw new DDIFtpException("Error prepare query: " + query, e);
		}

		XmlResults rs = null;
		try {
			rs = xmlQueryExpression.execute(getTransaction(), xmlQueryContext);
		}
		// catch deadlock and implement retry
		catch (Exception e) {
			throw new DDIFtpException("Error on query execute of: " + query, e);
		} finally {
			if (xmlQueryContext != null) {
				xmlQueryContext.delete();
			}
			xmlQueryExpression.delete();
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
			e.printStackTrace();
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
				// name space

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
	}

	private void writeExportDocument(FileChannel rafFc, String value)
			throws Exception {
		rafFc.write(ByteBuffer.wrap(value.getBytes("utf-8")));
	}
}
