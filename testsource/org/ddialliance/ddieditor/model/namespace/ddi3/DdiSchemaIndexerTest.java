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
		DdiSchemaIndexer indexer = new DdiSchemaIndexer(new File(
				"resources/ddi/instance.xsd"));
		indexer.index();
		Properties properties = FileUtil
				.loadProperties(Ddi3NamespaceHelper.ELEMENT_NAMESPACE);
		// hist stats: 
		// ddi-3.0 - 512 elements
		// ddi-3.1 - 602 elements, something is cooking!!! 
		Assert.assertEquals(602, properties.size());
	}

	@Test
	public void indexUrnRelationship() throws Exception {
		DdiSchemaIndexer indexer = new DdiSchemaIndexer();
		indexer.indexDdiRelationship();
	}
}
