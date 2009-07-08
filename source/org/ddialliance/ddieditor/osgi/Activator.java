package org.ddialliance.ddieditor.osgi;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator, ServiceListener {
	private IDdiEditorCore service;
	private ServiceTracker serviceTracker;
	private BundleContext fContext;

	public void start(BundleContext context) throws Exception {
		System.out.println("Bundle ddieditor started");
		fContext = context;
		service = new DdiEditorCoreImpl();

		Hashtable props = new Hashtable();
		// register the service
		context.registerService(IDdiEditorCore.class.getName(), service, props);

		// create a tracker and track the service
		serviceTracker = new ServiceTracker(context, IDdiEditorCore.class
				.getName(), null);
		serviceTracker.open();

		// have a service listener to implement the whiteboard pattern
		fContext.addServiceListener(this, "(objectclass="
				+ IDdiEditorCore.class.getName() + ")");

		// grab the service
		service = (DdiEditorCoreImpl) serviceTracker.getService();
		service.getPersistenceManager();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		// close the service tracker
		serviceTracker.close();
		serviceTracker = null;

		service = null;
		fContext = null;
		System.out.println("Bundle ddieditor stopped");
	}

	public void serviceChanged(ServiceEvent ev) {
		ServiceReference sr = ev.getServiceReference();

	}
}
