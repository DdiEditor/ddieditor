package org.ddialliance.ddieditor.logic.urn.ddi;

import java.util.ArrayList;
import java.util.List;

import org.ddialliance.ddieditor.model.DdiManager;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.log.Log;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;
import org.ddialliance.ddiftp.util.xml.Urn;

import com.ximpleware.AutoPilot;
import com.ximpleware.NodeRecorder;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

/**
 * Generation util for top URNs based on XML byte input
 */
public class FastUrnUtil {
	private static Log log = LogFactory.getLog(LogType.SYSTEM,
			FastUrnUtil.class);
	private VTDGen vg = new VTDGen();
	private VTDNav vn = null;
	private AutoPilot ap = null;
	private NodeRecorder nr = null;

	/**
	 * Initialize constructor
	 * 
	 * @param bytes
	 *            to scan for top URNs
	 * @throws Exception
	 */
	public FastUrnUtil(byte[] bytes) throws Exception {
		vg.setDoc(bytes);
		vg.parse(true);
		vn = vg.getNav();
		ap = new AutoPilot(vn);
		nr = new NodeRecorder();
		nr.bind(vn);
	}

	/**
	 * Generate manintainables URNs
	 * 
	 * @param validUrnsOnly
	 *            generate valid URNs only
	 * @return list of manintainables URNs
	 * @throws Exception
	 */
	public List<Urn> generateManintainablesUrns(boolean validUrnsOnly)
			throws Exception {
		// generate urns
		List<Urn> tmpUrnList = null;
		List<Urn> urnList = new ArrayList<Urn>();
		for (String maintainable : DdiManager.getInstance()
				.getDdi3NamespaceHelper().getMaintainableElementsList()) {
			tmpUrnList = executeMantainableQuery(maintainable, validUrnsOnly,
					true);
			if (!tmpUrnList.isEmpty()) {
				urnList.addAll(tmpUrnList);
			}
		}
		return urnList;
	}

	private List<Urn> executeMantainableQuery(String localName,
			boolean validUrnsOnly, boolean clearNodeRecord) throws Exception {
		// build query
		String query = "/"
				+ DdiManager.getInstance().getDdi3NamespaceHelper()
						.addFullyQualifiedNamespaceDeclarationToElements(
								localName);

		// execute query
		ap.selectXPath(query.toString());
		while ((ap.evalXPath()) != -1) {
			nr.record();
		}
		ap.resetXPath(); // a good practice
		nr.resetPointer(); // get into nr's read mode

		// generate urn
		Urn urn = null;
		List<Urn> urnList = new ArrayList<Urn>();
		int attrIndex = -1;
		while ((nr.iterate()) != -1) {
			urn = new Urn();
			urn.setSchemaVersion("3.0");
			urn.setMaintainableElement(localName);

			// id
			attrIndex = vn.getAttrVal("id");
			if (attrIndex > -1) {
				urn.setMaintainableId(vn.toNormalizedString(attrIndex));
			}

			// version
			attrIndex = vn.getAttrVal("version");
			if (attrIndex > -1) {
				urn.setMaintainableVersion(vn.toNormalizedString(attrIndex));
			}

			// agency
			attrIndex = vn.getAttrVal("agency");
			if (attrIndex > -1) {
				urn.setIdentifingAgency(vn.toNormalizedString(attrIndex));
			} else if (urn.getMaintainableVersion() != null) {
				// retrieve parent agency
				retrieveParentAgency(vn, urn);
			}

			if (urn.getMaintainableVersion() != null || !validUrnsOnly) {
				urnList.add(urn);
			}
		}

		if (clearNodeRecord) {
			nr.clear();
		}
		return urnList;
	}

	private Urn retrieveParentAgency(VTDNav vn, Urn urn) throws Exception {
		// get parent
		if (!vn.toElement(VTDNav.PARENT)) {
			throw new DDIFtpException("No defined agency found for element: "
					+ urn.getContainedElement() + " with id: "
					+ urn.getMaintainableId());
		}

		// agency
		int attrIndex = vn.getAttrVal("agency");
		if (attrIndex > -1) {
			urn.setIdentifingAgency(vn.toNormalizedString(attrIndex));
			return urn;
		} else {
			return retrieveParentAgency(vn, urn);
		}
	}
}
