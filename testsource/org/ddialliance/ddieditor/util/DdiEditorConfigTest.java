package org.ddialliance.ddieditor.util;

import junit.framework.Assert;

import org.junit.Test;

public class DdiEditorConfigTest {
	@Test
	public void init() throws Exception {
		Assert.assertEquals(".", DdiEditorConfig.get(DdiEditorConfig.DBXML_ENVIROMENT_HOME));
	}
}
