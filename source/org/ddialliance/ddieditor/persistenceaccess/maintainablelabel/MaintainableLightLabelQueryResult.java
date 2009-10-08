package org.ddialliance.ddieditor.persistenceaccess.maintainablelabel;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;
import org.ddialliance.ddiftp.util.DDIFtpException;

public class MaintainableLightLabelQueryResult {
	private String maintainableTarget;
	private String id;
	private String version;
	private String agency;
	private Map<String, LinkedList<LightXmlObjectType>> result = new HashMap<String, LinkedList<LightXmlObjectType>>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getAgency() {
		return agency;
	}

	public void setAgency(String agency) {
		this.agency = agency;
	}

	public String getMaintainableTarget() {
		return maintainableTarget;
	}

	public void setMaintainableTarget(String maintainableTarget) {
		this.maintainableTarget = maintainableTarget;
	}

	/**
	 * Retrieve a sub elements as light XmlObjects
	 * 
	 * @param elementName
	 *            sub element to retrieve
	 * @return array of XmlObjects
	 * @throws DDIFtpException
	 */
	public LightXmlObjectType[] getLightSubElement(String elementName)
			throws DDIFtpException {
		if (result.get(elementName).isEmpty()) {
			return new LightXmlObjectType[] {};
		} else {
			LightXmlObjectType[] lightResult = {};
			return result.get(elementName).toArray(lightResult);
		}
	}

	/**
	 * Retrieve result of query
	 * 
	 * @return map with key sub element name, list of sub elements as XML
	 */
	public Map<String, LinkedList<LightXmlObjectType>> getResult() {
		return result;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("Map: ");
		result.append(result);
		return result.toString();
	}
}
