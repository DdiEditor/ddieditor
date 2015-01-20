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
	public static final String DDI_STYLE_SHEET_CODEBOOK_GUIDE_LINK = "ddi.stylesheet.codebookguidelink";
	public static final String DDI_STYLE_SHEET_CURATION_PROCESS_LINK = "ddi.stylesheet.curationprocesslink";
	public static final String SPPS_OMS_XML_NAMESPACE = "spss.oms.xml.namespace";
	public static final String SPSS_IMPORT_CHARSET = "spss.import.charset";
	public static final String SPSS_IMPORT_DECIMAL_SEPERATOR = "spss.import.decimalseparator";
	public static final String UTF8_ADD_BOM = "charset_utf8_add_bom";
	public static final String OSIRIS_TO_DDIL_TITLE_PARTH_SPLIT = "osiristoddil.title.pathsplit";
	public static final String OSIRIS_TO_DDIL_QBANKPREFIX = "osiristoddil.dbqbank.prefix";
	public static final String DDI_EDITOR_VERSION = "ddieditor.app.version";
	public static final String DDA_CONVERSION_STUDYINFO_DIR = "dda.conversion.studydir";

	public static void init() {
		LogFactory.getLog(LogType.SYSTEM, DdiEditorConfig.class).info(
				"DDI editor init config done ;- )");
	}
}
