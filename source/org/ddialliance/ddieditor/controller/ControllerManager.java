package org.ddialliance.ddieditor.controller;

/**
 * Manager to control the UI
 */
public class ControllerManager {
	private String defaultLanguage = "en";

	/**
	 * Construct the controller manager with a default language
	 * 
	 * @param defaultLanguage
	 */
	public ControllerManager(String defaultLanguage) {
		super();
		this.defaultLanguage = defaultLanguage;
	}

	public String getDefaultLanguage() {
		return defaultLanguage;
	}

	public void setDefaultLanguage(String defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}
}
