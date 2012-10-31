package org.ddialliance.ddieditor.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.ddialliance.ddieditor.model.lightxmlobject.CustomListType;
import org.ddialliance.ddieditor.model.lightxmlobject.CustomType;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectListDocument;
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

	public static List<LightXmlObjectType> getXmlObjectsByCustomListType(
			LightXmlObjectListDocument lightXmlObjectListDoc,
			String customElementName) {
		List<LightXmlObjectType> result = new ArrayList<LightXmlObjectType>();

		if (lightXmlObjectListDoc == null
				|| lightXmlObjectListDoc.getLightXmlObjectList() == null
				|| lightXmlObjectListDoc.getLightXmlObjectList() // guard
						.getLightXmlObjectList() == null) {
			return result;
		}

		for (LightXmlObjectType lightXmlObject : lightXmlObjectListDoc
				.getLightXmlObjectList().getLightXmlObjectList()) {

			LightXmlObjectType test = createLightXmlObject(
					getCustomListbyType(lightXmlObject, customElementName),
					customElementName);
			if (test != null) {
				result.add(test);
			}
		}
		return result;
	}

	public static LightXmlObjectType createLightXmlObject(
			List<CustomType> list, String customElementName) {
		String[] attr = { "id", "version", "parentId", "parentVersion", "label" };
		String[] result = new String[4];
		HashMap<String, String> labels = new HashMap<String, String>();

		for (CustomType custom : list) {
			for (int i = 0; i < attr.length; i++) {
				if (custom.getOption() != null
						&& custom.getOption().equals(attr[4])) {
					labels.put(XmlBeansUtil.getTextOnMixedElement(custom),
							custom.getValue());
					break;
				}
				if (custom.getOption() != null
						&& custom.getOption().equals(attr[i])) {
					result[i] = custom.getValue();
					// System.out.println(i + ": " + custom.getOption() + ": "
					// + custom.getValue());
					break;
				}
			}
		}
		if (result[0] == null) { // guard
			return null;
		}
		return createLightXmlObject(result[2], result[3], result[0], result[1],
				customElementName, labels);
	}

	public static LightXmlObjectType createLightXmlObject(String parentId,
			String parentVersion, String id, String version,
			String elementType, HashMap<String, String> labels) {
		LightXmlObjectType lightXmlObject = createLightXmlObject(parentId,
				parentVersion, id, version, elementType);

		// add labels
		for (Entry<String, String> entry : labels.entrySet()) {
			org.ddialliance.ddieditor.model.lightxmlobject.LabelType label = lightXmlObject
					.addNewLabel();
			label.setLang(entry.getValue());
			XmlBeansUtil.setTextOnMixedElement(label, entry.getKey());
		}

		return lightXmlObject;
	}

	/**
	 * Use create with agency instead
	 * 
	 * @deprecated
	 * @see org.ddialliance.ddieditor.util.createLightXmlObject(String agency,
	 *      String parentId, String parentVersion, String id, String version,
	 *      String elementType)
	 */
	public static LightXmlObjectType createLightXmlObject(String parentId,
			String parentVersion, String id, String version, String elementType) {
		return createLightXmlObject(null, parentId, parentVersion, id, version,
				elementType);
	}

	/**
	 * Create a light xml object without labels to use for reference
	 * 
	 * @param agency
	 *            identifying agency
	 * @param parentId
	 *            id of containing element
	 * @param parentVersion
	 *            version of containing element
	 * @param id
	 *            id
	 * @param version
	 *            version
	 * @param elementType
	 *            local name
	 * @return light xml object
	 */
	public static LightXmlObjectType createLightXmlObject(String agency,
			String parentId, String parentVersion, String id, String version,
			String elementType) {
		LightXmlObjectType lightXmlObject = LightXmlObjectType.Factory
				.newInstance();
		lightXmlObject.setAgency(agency == null ? "" : agency);
		lightXmlObject.setParentId(parentId);
		lightXmlObject.setParentVersion(parentVersion);
		lightXmlObject.setId(id);
		lightXmlObject.setVersion(version == null ? "" : version);
		lightXmlObject.setElement(elementType);
		return lightXmlObject;
	}
}
