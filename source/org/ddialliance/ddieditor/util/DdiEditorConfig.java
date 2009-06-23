package org.ddialliance.ddieditor.util;

import org.ddialliance.ddiftp.util.Config;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;

public class DdiEditorConfig extends Config {
	// dbxml
	public static final String DBXML_ENVIROMENT_HOME = "dbxml.enviroment.home";
	public static final String DBXML_IMPORT_VALIDATE = "dbxml.import.validate";
	public static final String DBXML_DDI_INDEX = "dbxml.index.ddi";
	public static final String DDI_ELEMENTS_RELATIONSHIP_LIST = "ddi.elements.relationshiplist";
	
	public static void init() {
		LogFactory.getLog(LogType.SYSTEM, DdiEditorConfig.class).info(
				"DDI editor init config done ;- )");
	}
}
