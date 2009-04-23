package org.ddialliance.ddieditor.model;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.ddialliance.ddieditor.DdieditorTestCase;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceManager;
import org.ddialliance.ddieditor.persistenceaccess.dbxml.DbXmlManager;
import org.ddialliance.ddieditor.persistenceaccess.filesystem.FilesystemManager;
import org.junit.Ignore;
import org.junit.Test;
import org.perf4j.LoggingStopWatch;
import org.perf4j.StopWatch;

public class PerformaceTest extends DdieditorTestCase {
	@Ignore
	public void questionItemLightOrg() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		String query = "declare namespace ddieditor= 'http://ddialliance.org/ddieditor/ns';declare function ddieditor:label_lang($element) {  for $z in $element/*[namespace-uri()='ddi:datacollection:3_0' and local-name()='QuestionText'] return if($z/@xml:lang/string()='') then <Label>{ddieditor:label_text($z)}</Label> else <Label lang='{$z/@xml:lang/string()}'>{ddieditor:label_text($z)}</Label> }; declare function ddieditor:label_text($element) { for $q in $element/*[namespace-uri()='ddi:datacollection:3_0' and local-name()='LiteralText']/*[namespace-uri()='ddi:datacollection:3_0' and local-name()='Text'] return $q/text() }; declare function ddieditor:root_by_id() { for $x in doc('dbxml:/big-doc.dbxml/big-doc.xml')//*[namespace-uri()='ddi:datacollection:3_0' and local-name()='QuestionScheme'] where $x/@id = 'qs_' return $x}; <dl:LightXmlObjectList xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='ddieditor-lightobject ddieditor-lightxmlobject.xsd' xmlns:dl='ddieditor-lightobject'>{ for $x in ddieditor:root_by_id() for $y in $x/*[namespace-uri()='ddi:datacollection:3_0' and local-name()='QuestionItem']  return <LightXmlObject element='QuestionItem' id='{$y/@id/string()}' version='{$y/@version/string()}' parentId='{$x/@id/string()}' parentVersion='{$x/@version/string()}'>{ddieditor:label_lang($y)}</LightXmlObject>}</dl:LightXmlObjectList>";
		StopWatch stopWatch = new LoggingStopWatch("questionItemLightOrg");
		List<String> result = DbXmlManager.getInstance().query(query);
		stopWatch.stop();
	}

	@Ignore
	public void questionItemLightOptimazied() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		String query = "declare namespace ddieditor= 'http://ddialliance.org/ddieditor/ns';declare function ddieditor:label_lang($element) {  for $z in $element/*[namespace-uri()='ddi:datacollection:3_0' and local-name()='QuestionText'] return if($z/@xml:lang/string()='') then <Label>{ddieditor:label_text($z)}</Label> else <Label lang='{$z/@xml:lang/string()}'>{ddieditor:label_text($z)}</Label> }; declare function ddieditor:label_text($element) { for $q in $element/*[namespace-uri()='ddi:datacollection:3_0' and local-name()='LiteralText']/*[namespace-uri()='ddi:datacollection:3_0' and local-name()='Text'] return $q/text() }; declare function ddieditor:root_by_id() { for $x in doc('dbxml:/big-doc.dbxml/big-doc.xml')//*[namespace-uri()='ddi:datacollection:3_0' and local-name()='QuestionScheme'] where $x/@id = 'qs_' return $x}; <dl:LightXmlObjectList xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='ddieditor-lightobject ddieditor-lightxmlobject.xsd' xmlns:dl='ddieditor-lightobject'>{ for $x in ddieditor:root_by_id() for $y in $x/*[namespace-uri()='ddi:datacollection:3_0' and local-name()='QuestionItem']  return <LightXmlObject element='QuestionItem' id='{$y/@id/string()}' version='{$y/@version/string()}' parentId='{$x/@id/string()}' parentVersion='{$x/@version/string()}'>{ddieditor:label_lang($y)}</LightXmlObject>}</dl:LightXmlObjectList>";
		StopWatch stopWatch = new LoggingStopWatch(
				"questionItemLightOptimazied");
		List<String> result = DbXmlManager.getInstance().query(query);
		stopWatch.stop();
	}

	@Ignore
	public void questionItemOrg() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		String query = "for $element in doc('dbxml:/big-doc.dbxml/big-doc.xml')//*[namespace-uri()='ddi:datacollection:3_0' and local-name()='QuestionScheme'] for $child in $element//*[namespace-uri()='ddi:datacollection:3_0' and local-name()='QuestionItem']  where $element/@id = 'qs_' and $child/@id = 'qi_1467' return $child";
		StopWatch stopWatch = new LoggingStopWatch("questionItemOrg");
		List<String> result = DbXmlManager.getInstance().query(query);
		stopWatch.stop();
		System.out.println("Result: " + result.size());
	}

	@Ignore
	public void questionItemOptimazied() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		String query = "for $element in doc('dbxml:/big-doc.dbxml/big-doc.xml')//*[namespace-uri()='ddi:datacollection:3_0' and local-name()='QuestionScheme'] for $child in $element//*[namespace-uri()='ddi:datacollection:3_0' and local-name()='QuestionItem']  where $element/@id = 'qs_' and $child/@id = 'qi_1467' return $child";
		StopWatch stopWatch = new LoggingStopWatch("questionItemOptimazied");
		List<String> result = DbXmlManager.getInstance().query(query);
		stopWatch.stop();
		System.out.println("Result: " + result.size());
	}

	@Test
	public void testname() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		// initial
		String query = " for $element in doc('dbxml:/big-doc.dbxml/big-doc.xml')//*[namespace-uri()='ddi:logicalproduct:3_0' and local-name()='LogicalProduct'] for $child in $element//*[namespace-uri()='ddi:logicalproduct:3_0' and local-name()='CategoryScheme']  where $element/@id = 'lp_1' and $child/@id = 'cats_7' and empty($child/@version) return $child";
		List<String> result = PersistenceManager.getInstance().query(query);
		for (String string : result) {
			System.err.println(string);
		}
		
		// update
		String update = "insert nodes <Label xmlns='ddi:reusable:3_0' xml:lang='en'>my new value</Label> as first into";
		PersistenceManager.getInstance().updateQuery(update+query );
		PersistenceManager.getInstance().commitWorkingResource();
		
		// check
		result = PersistenceManager.getInstance().query(query);
		for (String string : result) {
			System.err.println(string);
		}
		
		//DbXmlManager.getInstance().exportResource(FULLY_DECLARED_NS_DOC, new File("test.xml"));
	}
}
