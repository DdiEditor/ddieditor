package org.ddialliance.ddieditor;

import junit.framework.Assert;

import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionItemType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionSchemeDocument;
import org.ddialliance.ddieditor.model.PerformanceRun;
import org.ddialliance.ddiftp.util.log.Log;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;
import org.junit.Test;

public class PerformanceTest extends DdieditorTestCase {
	@Test
	public void questionScheme() throws Exception {
		Log bug = LogFactory.getLog(LogType.BUG, PerformanceTest.class);
		QuestionSchemeDocument doc = null;
		PerformanceRun run = new PerformanceRun();
		for (int i = 0; i < 3; i++) {
			doc = run.runQuestionSchemeGlobal();
			Assert.assertTrue("Not found!", !doc.getQuestionScheme()
					.getQuestionItemList().isEmpty());

			bug.info(doc);
			bug.info("questionItems: "
					+ doc.getQuestionScheme().getQuestionItemList().size());
		}
		 Thread.sleep(sleepTime);

		for (int i = 0; i < 3; i++) {
			doc = run.runQuestionSchemeExact();
			Assert.assertTrue("Not found!", !doc.getQuestionScheme()
					.getQuestionItemList().isEmpty());

			bug.info(doc);
			bug.info("questionItems: "
					+ doc.getQuestionScheme().getQuestionItemList().size());
		}
		 Thread.sleep(sleepTime);
	}

	@Test
	public void questionItem() throws Exception {
		Log bug = LogFactory.getLog(LogType.BUG, PerformanceTest.class);
		QuestionItemType doc = null;
		PerformanceRun run = new PerformanceRun();
		for (int i = 0; i < 3; i++) {
			doc = run.runQuestionItemGlobal();
			Assert.assertNotNull("Not found1!", doc);
			bug.info(doc);
		}
		 Thread.sleep(sleepTime);

		for (int i = 0; i < 3; i++) {
			doc = run.runQuestionItemExact();
			Assert.assertNotNull("Not found2!", doc);
			bug.info(doc);
		}
	}

	public static long sleepTime = 4000L;
}
