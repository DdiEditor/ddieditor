package org.ddialliance.ddieditor.util;

import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;
import org.ddialliance.ddiftp.util.xml.XmlBeansUtil;

public class LightXmlObjectUtil {
	/**
	 * Get label of Light XML Object. If no label found the id is return as
	 * label.
	 * 
	 * @param lightXmlObject
	 * @return
	 */
	public static String getLabel(LightXmlObjectType lightXmlObject) {
		if (lightXmlObject == null) {
			return null;
		}

		if (lightXmlObject.getLabelList().size() != 0) {
			// TODO implement get locale to get i18n, instead of of first label

			// TODO implement preferences setting to get other label if get
			// locale lang not present in light xml object :: label, e.g. id or
			// other lang
			return XmlBeansUtil.getTextOnMixedElement(lightXmlObject
					.getLabelList().get(0));
		} else {
			return lightXmlObject.getId();
		}
	}
}
