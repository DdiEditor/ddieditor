package org.ddialliance.ddieditor.logic.identification;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlbeans.XmlOptions;
import org.ddialliance.ddi3.xml.xmlbeans.logicalproduct.CodeSchemeReferenceDocument;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.AbstractIdentifiableType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.AbstractMaintainableType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.AbstractVersionableType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.InternationalStringType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.ReferenceType;
import org.ddialliance.ddieditor.model.DdiManager;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;
import org.ddialliance.ddiftp.util.Config;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.ReflectionUtil;
import org.ddialliance.ddiftp.util.Translator;
import org.ddialliance.ddiftp.util.log.Log;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;
import org.ddialliance.ddiftp.util.xml.XmlBeansUtil;

public class IdentificationManager {
	private static Log log = LogFactory.getLog(LogType.SYSTEM,
			IdentificationManager.class);
	private static IdentificationManager instance;
	private String deleimiter = ".";
	private IdentificationGenerator idGenerator = null;
	private ReferenceGenerator refGenerator = null;
	private Map<String, String> properties;

	enum PolicyType {
		ID("id"), VERSION("version"), AGENCY("agency");

		String artifact;

		private PolicyType(String artifact) {
			this.artifact = artifact;
		}
	}

	enum PolicyActionType {
		MINIMAL("minimal"), ALLWAYS_ADD("allways_add"), NEVER("never");
		String rule;

		private PolicyActionType(String rule) {
			this.rule = rule;
		}
	}

	private IdentificationManager() {
		properties = new HashMap<String, String>();
		this.properties.put("agency", "dk.dda");
		this.reloadPolicy(new HashMap<String, String>());
	}

	/**
	 * Access singleton instance
	 * 
	 * @return identification manager
	 */
	public static synchronized IdentificationManager getInstance() {
		if (instance == null) {
			instance = new IdentificationManager();
			log.info("IdentificationManager created");
		}
		return instance;
	}

	/**
	 * Initialize the identification manager with policy
	 * 
	 * identification.implementation.id = class-name-of-id-implementation<br>
	 * * identification.implementation.ref =
	 * class-name-of-reference-implementation<br>
	 * identification.agency identification.policy.id = allways_add<br>
	 * identification.policy.version = minimal<br>
	 * identification.policy.agency = never ...
	 */
	public void reloadPolicy(Map<String, String> properties) {
		// dynamically class load implementations
		idGenerator = new DefaultIdGenerator();
		refGenerator = new DefaultReferenceGenereator();

		// setup rules and preferences for agency etc
	}

	private void checkPolicy() throws DDIFtpException {
		if (idGenerator == null) {
			throw new DDIFtpException(
					"Identification implementaion not initialized",
					new Throwable());
		}
		if (refGenerator == null) {
			throw new DDIFtpException(
					"Reference implementaion not initialized", new Throwable());
		}

		// TODO apply check on rules too
	}

	/**
	 * Add ID information
	 * 
	 * @param abstractIdentifiable
	 *            to apply ID on
	 * @param prefix
	 *            id prefix
	 * @param postfix
	 *            id postfix
	 * @throws DDIFtpException
	 */
	public void addIdentification(
			AbstractIdentifiableType abstractIdentifiable, String prefix,
			String postfix) throws DDIFtpException {
		abstractIdentifiable.setId(idGenerator.generateId(prefix, postfix));

		// agency
		if (abstractIdentifiable instanceof AbstractMaintainableType) {
			addAgency((AbstractMaintainableType) abstractIdentifiable);
		}
	}

	/**
	 * Add version information
	 * 
	 * @param abstractVersionable
	 *            to apply version information on
	 * @param versionChange
	 *            degree of change
	 * @param versionRationale
	 *            version rationale in human readable text
	 * @throws DDIFtpException
	 */
	public void addVersionInformation(
			AbstractVersionableType abstractVersionable,
			VersionChangeType versionChange, String versionRationale)
			throws DDIFtpException {
		// version change
		String oldVersion = abstractVersionable.getVersion();
		String version = generateVersion(oldVersion, versionChange);
		abstractVersionable.setVersion(version);

		// user id
		abstractVersionable.setVersionResponsibility(idGenerator.getUserId());

		// date
		String time = Translator.formatIso8601DateTime(System
				.currentTimeMillis());
		abstractVersionable.setVersionDate(time);

		// versionRationale
		InternationalStringType str = abstractVersionable
				.addNewVersionRationale();
		XmlBeansUtil.addTranslationAttributes(str, Translator
				.getLocaleLanguage(), false, true);

		// change info, ddi currently does not bind, version rationale to a
		// specific version change
		StringBuilder changeInfo = new StringBuilder("Version: ");
		changeInfo.append(version);
		changeInfo.append(", date: ");
		changeInfo.append(time);

		// user specified change info
		if (versionRationale != null) {
			changeInfo.append(", rationale: ");
			changeInfo.append(versionRationale);
		}
		str.setStringValue(changeInfo.toString());

		// agency
		if (abstractVersionable instanceof AbstractMaintainableType) {
			addAgency((AbstractMaintainableType) abstractVersionable);
		}
	}

	public enum VersionChangeType {
		MAJOR, MINIOR, MINOR_MINOR;
	}

	private String generateVersion(String oldVeresion,
			VersionChangeType versionChange) throws DDIFtpException {
		checkPolicy();
		// TODO apply version rules

		// guard
		if (oldVeresion == null) {
			return "1.0.0";
		}

		// add change
		String[] versionSplit = oldVeresion.split("\\.");
		int version = Integer.parseInt(versionSplit[versionChange.ordinal()]);
		versionSplit[versionChange.ordinal()] = "" + ++version;

		// result
		StringBuilder result = new StringBuilder(versionSplit[0]);
		result.append(deleimiter);
		result.append(versionSplit[1]);
		result.append(deleimiter);
		result.append(versionSplit[2]);

		return result.toString();
	}

	private void addAgency(AbstractMaintainableType abstractMaintainable) {
		// TODO apply agency rules
		abstractMaintainable.setAgency(properties.get("agency"));
	}

	/**
	 * Add reference information
	 * 
	 * @param reference
	 *            reference to add to, if NULL a new reference is created
	 * @param lightXmlObject
	 *            reference to add from
	 * @return changed reference
	 * @throws DDIFtpException
	 */
	public ReferenceType addReferenceInformation(ReferenceType reference,
			LightXmlObjectType lightXmlObject) throws DDIFtpException {
		if (reference == null) {
			reference = createReferenceByReflection(lightXmlObject);
		}

		// TODO apply reference rules
		return refGenerator.addReferenceInformation(reference, lightXmlObject,
				null);
	}

	private ReferenceType createReferenceByReflection(
			LightXmlObjectType lightXmlObject) throws DDIFtpException {
		StringBuilder clazz = new StringBuilder(Config
				.get(Config.DDI3_XMLBEANS_BASEPACKAGE));
		clazz.append(DdiManager.getInstance().getDdi3NamespaceHelper()
				.getModuleNameByElement(lightXmlObject.getElement()));
		clazz.append(".");
		clazz.append(lightXmlObject.getElement());
		clazz.append("ReferenceDocument");
		clazz.append("$Factory");

		if (log.isDebugEnabled()) {
			log.debug("Classname: " + clazz.toString());
		}

		Object result = null;
		try {
			Object obj = ReflectionUtil.invokeStaticMethod(clazz.toString(),
					"newInstance");
			ReflectionUtil.invokeMethod(obj, "addNew"
					+ lightXmlObject.getElement() + "Reference", false, null);
			
			result =  ReflectionUtil.invokeMethod(obj, "get"
					+ lightXmlObject.getElement() + "Reference", false, null);
		} catch (Exception e) {
			DDIFtpException wrapedException = new DDIFtpException(
					"xmlbeanutil.open.error", lightXmlObject);
			wrapedException.setRealThrowable(e);
			throw wrapedException;
		}
		return (ReferenceType) result;
	}
}
