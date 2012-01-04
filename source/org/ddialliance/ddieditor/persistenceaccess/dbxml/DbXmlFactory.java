package org.ddialliance.ddieditor.persistenceaccess.dbxml;

import org.apache.commons.pool.PoolableObjectFactory;

import com.sleepycat.dbxml.XmlManager;

public class DbXmlFactory implements PoolableObjectFactory {

	XmlManager xmlManager;

	public DbXmlFactory(XmlManager xmlManager) {
		super();
		this.xmlManager = xmlManager;
	}

	@Override
	public void activateObject(Object arg0) throws Exception {
		// is invoked on every instance that has been passivated before it is
		// borrowed from the pool
		// do nothing here
	}

	@Override
	public void destroyObject(Object arg0) throws Exception {
		// is invoked on every instance when it is being "dropped" from the pool
		// do nothing
	}

	@Override
	public Object makeObject() throws Exception {
		// is called whenever a new instance is needed
		return new DbXmlWorker(xmlManager);
	}

	@Override
	public void passivateObject(Object arg0) throws Exception {
		// is invoked on every instance when it is returned to the pool
		// do nothing here
	}

	@Override
	public boolean validateObject(Object arg0) {
		// is invoked on activated instances to make sure they can be borrowed
		// from the pool. validateObject may also be used to test an instance
		// being returned to the pool before it is passivated. It will only be
		// invoked on an activated instance
		return true;
	}
}
