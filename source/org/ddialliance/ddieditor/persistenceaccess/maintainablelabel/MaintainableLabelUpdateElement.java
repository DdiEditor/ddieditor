package org.ddialliance.ddieditor.persistenceaccess.maintainablelabel;

import org.apache.xmlbeans.XmlObject;

public class MaintainableLabelUpdateElement {
	public static final int NEW = 0;

	private String localName;
	private String value;
	private Integer crudValue;

	public MaintainableLabelUpdateElement() {
	}

	/**
	 * Constructor with arguments
	 * @param localName
	 * @param value
	 * @param crudValue
	 */
	public MaintainableLabelUpdateElement(String localName, String value,
			Integer crudValue) {
		super();
		this.localName = localName;
		this.value = value;
		this.crudValue = crudValue;
	}

	public MaintainableLabelUpdateElement(XmlObject element, Integer crudValue) {		
		this.localName = element.schemaType().getName().getLocalPart();
		this.value = element.xmlText();
		this.crudValue = crudValue;
	}

	public String getLocalName() {
		return localName;
	}

	public void setLocalName(String localName) {
		this.localName = localName;
	}

	public String getValue() {
		return value;
	}

	/**
	 * Value of xml tag
	 * 
	 * @param value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	public Integer getCrudValue() {
		return crudValue;
	}

	/**
	 * Create, update, delete value refers to the position of element when
	 * retrieved @see MaintainableLabelQueryResult 
	 * <br><br>zero ~ create
	 * <br>positive ~ update
	 * <br>negative ~ delete
	 * <p>
	 * Eg. QusteionSceme a has to Labels x and y. When requested
	 * MaintainableLabelQueryResult returns List<XmlObject[]> containing:
	 * list(xmlObject{x, y})<br>
	 * To update y: Create a new MaintainableLabelUpdateElement(y, 2) with
	 * crudValue corresponding the second element in the array of the query
	 * result
	 * </p>
	 * <p>
	 * Eg. to create a new Label z in QuestionScheme a: Create a new
	 * MaintainableLabelUpdateElement(z, 0) with crudValue of 0 or
	 * MaintainableLabelUpdateElement.NEW to indicate the creations of a new
	 * element
	 * </p>
	 * <p>
	 * Eg. to deletethe Label x in QuestionScheme a: Create a new
	 * MaintainableLabelUpdateElement(x, -1). The negative value crudValue 
	 * indicates deletion and its value indicates position.
	 * <p/>
	 * @param crudValue
	 */
	public void setCrudValue(Integer crudValue) {
		this.crudValue = crudValue;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("LocalName: ");
		result.append(localName);
		result.append(", crudValue: ");
		result.append(crudValue);
		result.append(", value: ");
		result.append(value);
		return result.toString();
	}
}
