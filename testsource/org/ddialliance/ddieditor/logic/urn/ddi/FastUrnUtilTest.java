package org.ddialliance.ddieditor.logic.urn.ddi;

import java.io.File;
import java.util.List;

import junit.framework.Assert;

import org.ddialliance.ddieditor.DdieditorTestCase;
import org.ddialliance.ddiftp.util.FileUtil;
import org.ddialliance.ddiftp.util.xml.Urn;
import org.junit.Test;

public class FastUrnUtilTest {
	@Test
	public void generateManintainablesUrns() throws Exception {
		FastUrnUtil urnUtil = new FastUrnUtil(FileUtil.readFile("resources"
				+ File.separator + DdieditorTestCase.FULLY_DECLARED_NS_DOC));
		List<Urn> urnList = urnUtil.generateManintainablesUrns(true);
		for (Urn urn : urnList) {
			System.out.println(urn.toUrnString());
		}
		Assert.assertTrue(urnList.size() == 5);
	}
}
