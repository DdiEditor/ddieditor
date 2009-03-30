package org.ddialliance.ddieditor.logic.validation;

import org.ddialliance.ddieditor.DdieditorTestCase;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceManager;
import org.junit.Test;

public class Ddi3ValidatorTest extends DdieditorTestCase {
	@Test
	public void validateDatacollection() throws Exception {
		Ddi3Validator.validateDatacollection("dd", "dda-4755");
	}
}
