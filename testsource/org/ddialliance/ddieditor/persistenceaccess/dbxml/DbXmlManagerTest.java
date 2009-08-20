package org.ddialliance.ddieditor.persistenceaccess.dbxml;

import java.io.File;

import org.ddialliance.ddi_3_0.xml.xmlbeans.instance.DDIInstanceDocument;
import org.ddialliance.ddieditor.DdieditorTestCase;
import org.ddialliance.ddieditor.model.DdiManager;
import org.ddialliance.ddieditor.persistenceaccess.PersistenceManager;
import org.junit.Assert;
import org.junit.Test;

import com.sleepycat.dbxml.XmlResults;

public class DbXmlManagerTest extends DdieditorTestCase {

	@Test
	public void query() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		StringBuilder query = new StringBuilder();
		query.append(PersistenceManager.getInstance().getResourcePath());
		query
				.append(DdiManager
						.getInstance()
						.getDdi3NamespaceHelper()
						.addFullyQualifiedNamespaceDeclarationToElements(
								"/DDIInstance/studyunit__StudyUnit[1]/logicalproduct__LogicalProduct/VariableScheme[1]"));
		XmlResults rs = DbXmlManager.getInstance().xQuery(query.toString());
		Assert.assertTrue(rs.hasNext());
		rs.delete();

		rs = DbXmlManager.getInstance().xQuery(query.toString());
		Assert.assertTrue(rs.hasNext());
		rs.delete();
	}

	@Test
	public void getResources() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				PersistenceManager.getInstance().RESOURCE_LIST_FILE);

		int size = DbXmlManager.getInstance().getResources().size();
		Assert.assertEquals(1, size);
	}

	@Test
	public void exportResource() throws Exception {
		PersistenceManager.getInstance().setWorkingResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC);
		File file = new File("export.xml");
		file.deleteOnExit();
		DbXmlManager.getInstance().exportResource(
				DdieditorTestCase.FULLY_DECLARED_NS_DOC, file);
		DDIInstanceDocument exportDoc = DDIInstanceDocument.Factory.parse(file);
		Assert.assertNotNull(exportDoc);
	}
}
