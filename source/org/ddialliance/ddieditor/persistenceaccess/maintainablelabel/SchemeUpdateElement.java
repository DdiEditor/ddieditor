package org.ddialliance.ddieditor.persistenceaccess.maintainablelabel;

public class SchemeUpdateElement {
	public static final int NEW = 0;
	
	private String localName;
	private String value;
	private Integer updateValue;

	public SchemeUpdateElement() {}
	
	public SchemeUpdateElement(String localName, String value,
			Integer updateValue) {
		super();
		this.localName = localName;
		this.value = value;
		this.updateValue = updateValue;
	}

	public String getLocalName() {
		return localName;
	}

	/**
	 * Local name of element
	 * 
	 * @param localName
	 */
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

	public Integer getUpdateValue() {
		return updateValue;
	}

	/**
	 * Value of position: <br>
	 * -2 ~ new <br>
	 * -1 ~ delete <br>
	 * >0 ~ existing
	 * 
	 * @return value
	 */
	public void setUpdateValue(Integer updateValue) {
		this.updateValue = updateValue;
	}

}
