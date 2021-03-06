package org.ddialliance.ddieditor.persistenceaccess.maintainablelabel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.xmlbeans.XmlObject;
import org.ddialliance.ddieditor.model.lightxmlobject.LabelType;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectListDocument;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.xml.XmlBeansUtil;

public class MaintainableLightLabelQueryResult {
	private String maintainableTarget;
	private String id, version, agency, parentId, parentVersion;
	private List<LabelType> labelList = new ArrayList<LabelType>();
	private Map<String, LinkedList<LightXmlObjectType>> result = new HashMap<String, LinkedList<LightXmlObjectType>>();

	/**
	 * Constructor for one light xml object
	 * @param maintainableTarget
	 * @param id
	 * @param version
	 * @param agency
	 * @param parentId
	 * @param parentVersion
	 */
	public MaintainableLightLabelQueryResult(String maintainableTarget,
			String id, String version, String agency, String parentId,
			String parentVersion) {
		super();
		this.maintainableTarget = maintainableTarget;
		this.id = id;
		this.version = version;
		this.agency = agency;
		this.parentId = parentId;
		this.parentVersion = parentVersion;
	}

	/**
	 * Constructor setting properties from query
	 * @param query query
	 */
	public MaintainableLightLabelQueryResult(MaintainableLabelQuery query) {
		this.setMaintainableTarget(query.getMaintainableTarget());
		this.setParentId(query.getParentId());
		this.setParentVersion(query.getParentVersion());
		this.setAgency(query.getAgency());
	}

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

	public LightXmlObjectType getMaintainableTargetAsLightXmlObject() {
		LightXmlObjectType result = LightXmlObjectListDocument.Factory
				.newInstance().addNewLightXmlObjectList()
				.addNewLightXmlObject();

		// TODO agency
		result.setElement(getMaintainableTarget());
		result.setId(getId());
		result.setVersion(getVersion());
		result.setLabelArray(labelList.toArray(new LabelType[] {}));

		result.setParentId(getParentId());
		result.setParentVersion(getParentVersion());

		return result;
	}

	public void setMaintainableTarget(String maintainableTarget) {
		this.maintainableTarget = maintainableTarget;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getParentVersion() {
		return parentVersion;
	}

	public void setParentVersion(String parentVersion) {
		this.parentVersion = parentVersion;
	}

	public List<LabelType> getLabelList() {
		return labelList;
	}

	public void setLabelList(List<LabelType> labelList) {
		this.labelList = labelList;
	}

	public String getTargetLabel() throws DDIFtpException{
		StringBuilder result = new StringBuilder();
		
		// target label
		if (!getLabelList().isEmpty()) {
			result.append(XmlBeansUtil
						.getTextOnMixedElement((XmlObject) XmlBeansUtil
								.getDefaultLangElement(getLabelList())));
		} else {
			// id label
			result.append(getMaintainableTarget());
			if (getId() != null) {
				result.append(": ");
				result.append(getId());
			}
		}
		return result.toString();
	}

	public String getSubElementLabels() {
		StringBuilder result = new StringBuilder();
		int count = 0;
		for (Entry<String, LinkedList<LightXmlObjectType>> entry : getResult().entrySet()) {
			result.append(entry.getKey());
			result.append(": ");
			result.append(entry.getValue().size());
			if (count<(getResult().entrySet().size())) {
				result.append(", ");
			}
			count++;
		}
		return result.toString();
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
		result.append(getResult().size());
		return result.toString();
	}

	@Override
	public int hashCode() {
		int hash = (1717 * (getId() == null ? "".hashCode() : getId()
				.hashCode()));
		return hash;
	}
}
