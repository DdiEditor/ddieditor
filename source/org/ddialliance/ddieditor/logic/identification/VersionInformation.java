package org.ddialliance.ddieditor.logic.identification;

import java.util.List;

import org.ddialliance.ddi3.xml.xmlbeans.reusable.InternationalStringType;

/**
 * Transfer object class wrapping version responsibility and a list of version rationales   
 */
public class VersionInformation {
	public String versionResponsibility;
	public List<InternationalStringType> versionRationaleList;
}
