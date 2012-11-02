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
	public static final String DDI_AGENCY = "ddi.agency";
	public static final String DDI_AGENCY_NAME = "ddi.agency.name";
	public static final String DDI_AGENCY_DESCRIPTION = "ddi.agency.description";
	public static final String DDI_AGENCY_IDENTIFIER = "ddi.agency.identifier";
	public static final String DDI_AGENCY_HP = "ddi.agency.url";
	public static final String DDI_LANGUAGE = "ddi.lang";
	public static final String DDI_INSTRUMENT_PROGRAM_LANG = "ddi.inst.programlang";
	public static final String DDA_DDI_INSTRUMENT_PROGRAM_LANG = "dda.ddi.inst.programlang";
    public static final String CHARSET_ISO = "charset.iso";
    public static final String CHARSET_OEM = "charset.oem";
    public static final String CHARSET_UNICODE = "charset.unicode";
    public static final String CODEBOOKSTYLESHEETNAME = "ddi.codebookstylesheetname";
	public static final String DO_HOUSE_KEEPING_COUNT = "dbxml.doHouseKeeping";
	
	public static void init() {
		LogFactory.getLog(LogType.SYSTEM, DdiEditorConfig.class).info(
				"DDI editor init config done ;- )");
	}
}
