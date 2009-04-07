package org.ddialliance.ddieditor.persistenceaccess.namespace.ddi3;

import org.ddialliance.ddieditor.model.namespace.ddi3.Ddi3NamespaceGenerator;
import org.ddialliance.ddieditor.model.namespace.ddi3.Ddi3NamespacePrefix;
import org.junit.Assert;
import org.junit.Test;

public class Ddi3NamespaceGeneratorTest {
	@Test
	public void namespacePrefix() throws Exception {
		Ddi3NamespaceGenerator gen = new Ddi3NamespaceGenerator();
		Ddi3NamespacePrefix prefix = gen.getNamespaceObjectByElement("DDIInstance");
		Assert.assertEquals(Ddi3NamespacePrefix.INSTANCE, prefix);
	}
}
