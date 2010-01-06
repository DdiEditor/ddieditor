package org.ddialliance.ddieditor.model;

import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionItemType;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.QuestionSchemeDocument;
import org.ddialliance.ddieditor.DdieditorTestCase;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceManager;
import org.perf4j.aop.Profiled;

public class PerformanceRun {	
	public void setResource() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);	
	}
	
	@Profiled(tag = "queryQuestionSchemeGlobal")
	public String queryQuestionSchemeGlobal() throws Exception {
		return DdiManager.getInstance().queryElement(null, null, "QuestionScheme", null,
				null, "datacollection__DataCollection");
	}
	
	@Profiled(tag = "queryQuestionSchemeExact")
	public String queryQuestionSchemeExact() throws Exception {
		return DdiManager.getInstance().queryElement("qs2_", null, "QuestionScheme", "dd",
				null, "datacollection__DataCollection");
	}
	
	@Profiled(tag = "marshalQuestionScheme")
	public QuestionSchemeDocument marshalQuestionSchemeQuery(String text) throws Exception {
		return (text == "" ? null : QuestionSchemeDocument.Factory.parse(text));
	}
	
	@Profiled(tag = "queryQuestionItemGlobal")
	public String queryQuestionItemGlobal() throws Exception {
		return DdiManager.getInstance().queryElement("qi_35", null, "QuestionItem", null,
				null, "QuestionScheme");
	}
	
	@Profiled(tag = "queryQuestionItemExact")
	public String queryQuestionItemExact() throws Exception {
		return DdiManager.getInstance().queryElement("qi_35", null, "QuestionItem", "qs2_",
				null, "QuestionScheme");
	}
	
	@Profiled(tag = "marshalQuestionItem")
	public QuestionItemType marshalQuestionItem(String text) throws Exception {
		return (text == "" ? null : QuestionItemType.Factory.parse(text));
	}
		
	public QuestionSchemeDocument runQuestionSchemeGlobal() throws Exception {
		setResource();
		return marshalQuestionSchemeQuery(queryQuestionSchemeGlobal());
	}
	
	public QuestionSchemeDocument runQuestionSchemeExact() throws Exception {
		setResource();
		return marshalQuestionSchemeQuery(queryQuestionSchemeExact());
	}
	
	public QuestionItemType runQuestionItemGlobal() throws Exception {
		setResource();
		return marshalQuestionItem(queryQuestionItemGlobal());
	}
	
	public QuestionItemType runQuestionItemExact() throws Exception {
		setResource();
		return marshalQuestionItem(queryQuestionItemExact());
	}
}
