package org.ddialliance.ddieditor.util;

import java.util.Arrays;
import java.util.List;

import org.ddialliance.ddieditor.model.lightxmlobject.CustomListType;
import org.ddialliance.ddieditor.model.lightxmlobject.CustomType;
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

	public static List<CustomType> getCustomListbyType(
			LightXmlObjectType lightXmlObject, String type) {
		for (CustomListType cuslist : lightXmlObject.getCustomListList()) {
			if (cuslist.getType().equals(type)) {
				return cuslist.getCustomList();
			}
		}
		return Arrays.asList(CustomType.Factory.newInstance());
	}

	public static LightXmlObjectType createLightXmlObject(String parentId,
			String parentVersion, String id, String version, String elementType) {
		LightXmlObjectType lightXmlObject = LightXmlObjectType.Factory
				.newInstance();
		lightXmlObject.setParentId(parentId);
		lightXmlObject.setParentVersion(parentVersion);
		lightXmlObject.setId(id);
		lightXmlObject.setVersion(version);
		lightXmlObject.setElement(elementType);
		return lightXmlObject;
	}
}
