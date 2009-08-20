package org.ddialliance.ddieditor.model.namespace.ddi3;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Properties;

import junit.framework.Assert;

import org.ddialliance.ddiftp.util.FileUtil;
import org.junit.Ignore;
import org.junit.Test;

public class DdiSchemaIndexerTest {
	@Test
	public void indexIdentifiables() throws Exception {
		DdiSchemaIndexer indexer = new DdiSchemaIndexer(
		// new
		// URL("http://www.icpsr.umich.edu/DDI/schema/ddi3.0/instance.xsd"));
				new File(
						"/home/ddajvj/app/ddi3/3.0-20080428/XMLSchema/instance.xsd"));
		indexer.index();
		Properties properties = FileUtil.loadProperties(Ddi3NamespaceHelper.ELEMENT_NAMESPACE);
		Assert.assertEquals(512, properties.size());
	}
	
	@Test
	public void indexUrnRelationship() throws Exception {
		DdiSchemaIndexer indexer = new DdiSchemaIndexer();
		indexer.indexDdiRelationship();
	}
}
