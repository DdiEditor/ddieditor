package org.ddialliance.ddieditor.util;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlObject;
import org.ddialliance.ddieditor.model.DdiManager;
import org.ddialliance.ddiftp.util.Config;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.ReflectionUtil;
import org.ddialliance.ddiftp.util.log.Log;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;
import org.ddialliance.ddiftp.util.xml.XmlBeansUtil;

public class XmlObjectUtil {
	private static Log log = LogFactory.getLog(LogType.SYSTEM,
			XmlObjectUtil.class);

	public static XmlObject createXmlObjectDocument(String localName)
			throws DDIFtpException {
		localName = DdiManager.getInstance().getDdi3NamespaceHelper()
				.getCleanedElementName(localName);

		// ddi3 package
		StringBuilder clazz = new StringBuilder(Config
				.get(Config.DDI3_XMLBEANS_BASEPACKAGE));
		// module package
		clazz.append(DdiManager.getInstance().getDdi3NamespaceHelper()
				.getModuleNameByElement(localName));
		// class name
		clazz.append(".");
		clazz.append(localName);
		clazz.append("Document");
		// factory
		clazz.append("$Factory");
		if (log.isDebugEnabled()) {
			log.debug(clazz.toString());
		}

		XmlObject result = null;
		try {
			result = (XmlObject) ReflectionUtil.invokeStaticMethod(clazz
					.toString(), "newInstance");
		} catch (Exception e) {
			DDIFtpException wrapedException = new DDIFtpException(
					"xmlbeanutil.open.error", localName);
			wrapedException.setRealThrowable(e);
			throw wrapedException;
		}
		return result;
	}

	public static XmlObject createXmlObjectType(String localName)
			throws DDIFtpException {

		XmlObject result = createXmlObjectDocument(localName);
		try {
			ReflectionUtil.invokeMethod(result, "addNew" + localName, false,
					null);

			result = (XmlObject) ReflectionUtil.invokeMethod(result, "get"
					+ localName, false, null);
		} catch (Exception e) {
			DDIFtpException wrapedException = new DDIFtpException(
					"xmlbeanutil.open.error", localName);
			wrapedException.setRealThrowable(e);
			throw wrapedException;
		}
		return result;
	}

	public static XmlObject addXmlObjectType(XmlObject xmlObject)
			throws DDIFtpException {
		XmlBeansUtil.instanceOfXmlBeanDocument(xmlObject, new Throwable());
		QName qName = xmlObject.schemaType().getDocumentElementName();
		String localName = qName.getLocalPart();
		XmlObject result;
		try {
			result = (XmlObject) ReflectionUtil.invokeMethod(xmlObject,
					"addNew" + localName, false, null);
		} catch (Exception e) {
			DDIFtpException wrapedException = new DDIFtpException(
					"xmlbeanutil.open.error", localName);
			wrapedException.setRealThrowable(e);
			throw wrapedException;
		}
		return result;
	}
}
