package org.ddialliance.ddieditor.persistenceaccess.dbxml;

import java.util.ArrayList;
import java.util.List;

import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.log.Log;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;
import org.perf4j.aop.Profiled;

import com.sleepycat.db.TransactionConfig;
import com.sleepycat.dbxml.XmlDocumentConfig;
import com.sleepycat.dbxml.XmlException;
import com.sleepycat.dbxml.XmlManager;
import com.sleepycat.dbxml.XmlQueryContext;
import com.sleepycat.dbxml.XmlResults;
import com.sleepycat.dbxml.XmlTransaction;

public class DbXmlWorker {
	static Log logSystem = LogFactory.getLog(LogType.SYSTEM, DbXmlWorker.class);

	XmlManager xmlManager;
	XmlTransaction transaction = null;
	TransactionConfig tc;
	XmlQueryContext xmlQueryContext;
	XmlDocumentConfig xmlDocumentConfig;

	public DbXmlWorker(XmlManager xmlManager) {
		super();
		this.xmlManager = xmlManager;

		// document
		xmlDocumentConfig = new XmlDocumentConfig();
		// xmlDocumentConfig.setLazyDocs(false);
		xmlDocumentConfig.setWellFormedOnly(true);

		// context
		try {
			xmlQueryContext = xmlManager.createQueryContext(
					XmlQueryContext.LiveValues, XmlQueryContext.Lazy);
		} catch (XmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// transaction
		tc = new TransactionConfig();
		tc.setReadCommitted(true);
		// tc.setNoWait(true);
		// tc.setReadUncommitted(true);
	}

	protected synchronized XmlTransaction getTransaction() throws Exception {
		if (transaction == null) {
			transaction = xmlManager.createTransaction(null, tc);
		}
		return transaction;
	}

	@Profiled(tag = "commitTransaction")
	protected synchronized void commitTransaction() throws Exception {
		if (transaction != null) {
			if (System.getProperty("ddieditor.test") != null
					&& System.getProperty("ddieditor.test").equals("true")) {
				logSystem.debug("Testmode! Not commiting transaction, id: "
						+ transaction.getTransaction().getId());
			} else {
				transaction.commit();
				transaction.delete();
				transaction = null;
			}
		}
	}

	protected synchronized void rollbackTransaction() throws Exception {
		if (transaction != null) {
			transaction.abort();
			transaction.delete();
			transaction = null;
		}
	}

	public List<String> query(String query) throws Exception {
		XmlResults rs = xQuery(query);
		List<String> result = new ArrayList<String>();
		
		// quick fix - check for dead lock exception instead
		if (rs==null) {
			return result;
		}
		
		while (rs.hasNext()) {
			result.add(rs.next().asString());
		}
		rs.delete();
		commitTransaction();
		return result;
	}

	protected XmlResults xQuery(String query) throws Exception {
		XmlResults rs = null;
		try {
			rs = xmlManager.query(getTransaction(), query, xmlQueryContext,
					xmlDocumentConfig);
		} catch (XmlException xe) {
			// First, look for a deadlock and handle it
			// if that is the cause of the exception.
			if (xe.getDatabaseException() instanceof com.sleepycat.db.DeadlockException) {
				// TODO
			}
		} catch (Exception e) {
			rollbackTransaction();
			throw new DDIFtpException("Error on query execute of: " + query, e);
		}
		return rs;
	}
}
