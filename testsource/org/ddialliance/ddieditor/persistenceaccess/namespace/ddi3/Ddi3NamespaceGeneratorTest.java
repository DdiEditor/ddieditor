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

	@Test
	public void substitutePrefixesFromElements() throws Exception {
		Ddi3NamespaceGenerator gen = new Ddi3NamespaceGenerator();
		String query = "<l:Variable id=\"v5001\"><r:Label>SKEMATYPE1</r:Label>"
				+ "<l:ConceptReference><r:ID>cs_2</r:ID></l:ConceptReference>"
				+ "<l:QuestionReference><r:ID>qi_5</r:ID></l:QuestionReference></l:Variable>";

		query = gen.substitutePrefixesFromElements(query);
		Assert
				.assertEquals(
						"<Variable xmlns=\"ddi:logicalproduct:3_0\" id=\"v5001\"><Label xmlns=\"ddi:reusable:3_0\">SKEMATYPE1</Label><ConceptReference xmlns=\"ddi:datacollection:3_0\"><ID xmlns=\"ddi:reusable:3_0\">cs_2</ID></ConceptReference><QuestionReference xmlns=\"ddi:datacollection:3_0\"><ID xmlns=\"ddi:reusable:3_0\">qi_5</ID></QuestionReference></Variable>",
						query);
	}
}
