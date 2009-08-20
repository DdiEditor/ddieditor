package org.ddialliance.ddieditor.logic.validation;

import org.ddialliance.ddieditor.model.DdiManager;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceManager;
import org.ddialliance.ddiftp.util.log.Log;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;

public class Ddi3Validator {
	static Log log = LogFactory.getLog(LogType.SYSTEM, Ddi3Validator.class
			.getName());

	public static void validate(String id, String elementType, String parentId,
			String parentType) throws Exception {
		// id uniqueness - query to see if there are one than one instance with
		// the same id
		boolean result = checkIdUniqeness(id, elementType, parentId, parentType);

		StringBuffer query = new StringBuffer();

		// check if element is valid -> lookup parent if maintainable and
		// version and id are valid
		// urn:ddi:[ddi_schemaversion]:[class]=
		// [agency_id]:[maintainable_container_id]([version]).[contained_item_id]([version])

	}

	private static boolean checkIdUniqeness(String id, String elementType,
			String parentId, String parentType) throws Exception {
		StringBuffer query = new StringBuffer();
		query.append("for $validation in ");
		query.append(PersistenceManager.getInstance().getResourcePath());
		query.append("/");
		query.append(DdiManager.getInstance().getDdi3NamespaceHelper()
				.addFullyQualifiedNamespaceDeclarationToElements(elementType));
		query
				.append(" where some $exact in $validation satisfies (matches($validation/@id/string(), '");
		query.append(id);
		query.append("')) return $validation//parent::node()/@id/string() = '");
		query.append(parentId);
		query.append("'");

		boolean idTest = PersistenceManager.getInstance().querySingleBoolean(
				query.toString());
		if (log.isDebugEnabled()) {
			log.debug("Result: " + idTest);
		}
		return idTest;
	}

	public static void validateDatacollection(String id, String parentId)
			throws Exception {

		validate(id, "DataCollection", parentId, "StudyUnit");
		// DataCollectionDocument dataCollection = DDI3Parser
		// .getDataCollectionById(id, parentId);

	}
}
