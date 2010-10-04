package org.ddialliance.ddieditor.util;

import java.lang.reflect.Method;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlObject;
import org.ddialliance.ddieditor.model.DdiManager;
import org.ddialliance.ddieditor.model.namespace.ddi3.Ddi3NamespaceHelper;
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
		StringBuilder clazz = createXmlObjectClassName(localName);
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

	public static StringBuilder createXmlObjectClassName(String localName)
			throws DDIFtpException {
		localName = Ddi3NamespaceHelper.getCleanedElementName(localName);

		// ddi3 package
		StringBuilder clazz = new StringBuilder(Config
				.get(Config.DDI3_XMLBEANS_BASEPACKAGE));
		// module package
		clazz.append(DdiManager.getInstance().getDdi3NamespaceHelper()
				.getModuleNameByElement(localName));
		// class name
		clazz.append(".");
		clazz.append(localName);
		return clazz;
	}

	public static StringBuilder createXmlObjectClassName(String localName,
			String namespace) throws DDIFtpException {
		localName = Ddi3NamespaceHelper.getCleanedElementName(localName);

		// ddi3 package
		StringBuilder clazz = new StringBuilder(Config
				.get(Config.DDI3_XMLBEANS_BASEPACKAGE));
		// module package
		String[] result = namespace.split(":");
		clazz.append(result[1]);
		// class name
		clazz.append(".");
		clazz.append(localName);
		return clazz;
	}

	public static Method[] getXmlObjectMethods(String localName,
			String namespace) throws DDIFtpException {
		StringBuilder clazz = createXmlObjectClassName(localName, namespace);
		clazz.append("Type");
		Method[] methods = new Method[] {};
		Class c = null;
		try {
			c = Class.forName(clazz.toString());
		} catch (ClassNotFoundException e) {
			try {
				clazz.delete(clazz.length() - 4, clazz.length());
				clazz.append("Document");				
				c = Class.forName(clazz
						.toString());
			} catch (ClassNotFoundException otherE) {
				throw new DDIFtpException(otherE);
			}
		}

		try {
			methods = c.getMethods();
		} catch (Exception e) {
			throw new DDIFtpException(e);
		}
		return methods;
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
