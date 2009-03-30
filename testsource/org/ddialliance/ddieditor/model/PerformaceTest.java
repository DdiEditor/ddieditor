package org.ddialliance.ddieditor.model;

import java.util.List;

import org.ddialliance.ddieditor.DdieditorTestCase;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceManager;
import org.ddialliance.ddieditor.persistenceaccess.dbxml.DbXmlManager;
import org.junit.Test;
import org.perf4j.LoggingStopWatch;
import org.perf4j.StopWatch;

public class PerformaceTest extends DdieditorTestCase {
	@Test
	public void questionItemLightOrg() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		String query= "declare namespace ddieditor= 'http://ddialliance.org/ddieditor/ns';declare function ddieditor:label_lang($element) {  for $z in $element/*[namespace-uri()='ddi:datacollection:3_0' and local-name()='QuestionText'] return if($z/@xml:lang/string()='') then <Label>{ddieditor:label_text($z)}</Label> else <Label lang='{$z/@xml:lang/string()}'>{ddieditor:label_text($z)}</Label> }; declare function ddieditor:label_text($element) { for $q in $element/*[namespace-uri()='ddi:datacollection:3_0' and local-name()='LiteralText']/*[namespace-uri()='ddi:datacollection:3_0' and local-name()='Text'] return $q/text() }; declare function ddieditor:root_by_id() { for $x in doc('dbxml:/big-doc.dbxml/big-doc.xml')//*[namespace-uri()='ddi:datacollection:3_0' and local-name()='QuestionScheme'] where $x/@id = 'qs_' return $x}; <dl:LightXmlObjectList xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='ddieditor-lightobject ddieditor-lightxmlobject.xsd' xmlns:dl='ddieditor-lightobject'>{ for $x in ddieditor:root_by_id() for $y in $x/*[namespace-uri()='ddi:datacollection:3_0' and local-name()='QuestionItem']  return <LightXmlObject element='QuestionItem' id='{$y/@id/string()}' version='{$y/@version/string()}' parentId='{$x/@id/string()}' parentVersion='{$x/@version/string()}'>{ddieditor:label_lang($y)}</LightXmlObject>}</dl:LightXmlObjectList>";
		StopWatch stopWatch = new LoggingStopWatch("questionItemLightOrg");
		DbXmlManager.getInstance().query(query);
		stopWatch.stop();
	}
	
	@Test
	public void questionItemLightOptimazied() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		String query= "declare namespace ddieditor= 'http://ddialliance.org/ddieditor/ns';declare function ddieditor:label_lang($element) {  for $z in $element/*[namespace-uri()='ddi:datacollection:3_0' and local-name()='QuestionText'] return if($z/@xml:lang/string()='') then <Label>{ddieditor:label_text($z)}</Label> else <Label lang='{$z/@xml:lang/string()}'>{ddieditor:label_text($z)}</Label> }; declare function ddieditor:label_text($element) { for $q in $element/*[namespace-uri()='ddi:datacollection:3_0' and local-name()='LiteralText']/*[namespace-uri()='ddi:datacollection:3_0' and local-name()='Text'] return $q/text() }; declare function ddieditor:root_by_id() { for $x in doc('dbxml:/big-doc.dbxml/big-doc.xml')//*[namespace-uri()='ddi:datacollection:3_0' and local-name()='QuestionScheme'] where $x/@id = 'qs_' return $x}; <dl:LightXmlObjectList xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='ddieditor-lightobject ddieditor-lightxmlobject.xsd' xmlns:dl='ddieditor-lightobject'>{ for $x in ddieditor:root_by_id() for $y in $x/*[namespace-uri()='ddi:datacollection:3_0' and local-name()='QuestionItem']  return <LightXmlObject element='QuestionItem' id='{$y/@id/string()}' version='{$y/@version/string()}' parentId='{$x/@id/string()}' parentVersion='{$x/@version/string()}'>{ddieditor:label_lang($y)}</LightXmlObject>}</dl:LightXmlObjectList>";
		StopWatch stopWatch = new LoggingStopWatch("questionItemLightOptimazied");
		DbXmlManager.getInstance().query(query);
		stopWatch.stop();
	}
	
	@Test
	public void questionItemOrg() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		String query= "for $element in doc('dbxml:/big-doc.dbxml/big-doc.xml')//*[namespace-uri()='ddi:datacollection:3_0' and local-name()='QuestionScheme'] for $child in $element//*[namespace-uri()='ddi:datacollection:3_0' and local-name()='QuestionItem']  where $element/@id = 'qs_' and $child/@id = 'qi_1467' return $child";
		StopWatch stopWatch = new LoggingStopWatch("questionItemOrg");
		List<String> result = DbXmlManager.getInstance().query(query);
		stopWatch.stop();
		System.out.println("Result: "+result.size());
	}
	
	@Test
	public void questionItemOptimazied() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		
		String query= "for $element in doc('dbxml:/big-doc.dbxml/big-doc.xml')//*/d:QuestionScheme for $child in $element/d:QuestionItem  where $element/@id = 'qs_' and $child/@id = 'qi_1467' return $child";
		
		StopWatch stopWatch = new LoggingStopWatch("questionItemOptimazied");
		List<String> result = DbXmlManager.getInstance().query(query);
		stopWatch.stop();
		System.out.println("Result: "+result.size());
	}
}
