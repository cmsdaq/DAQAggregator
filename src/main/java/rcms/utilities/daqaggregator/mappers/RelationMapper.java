package rcms.utilities.daqaggregator.mappers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.data.FMM;
import rcms.utilities.daqaggregator.data.FMMApplication;
import rcms.utilities.daqaggregator.data.FRL;
import rcms.utilities.daqaggregator.data.FRLPc;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.data.SubFEDBuilder;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.hwcfg.HardwareConfigurationException;
import rcms.utilities.hwcfg.dp.DAQPartition;
import rcms.utilities.hwcfg.eq.FMMFMMLink;
import rcms.utilities.hwcfg.eq.FMMTriggerLink;
import rcms.utilities.hwcfg.eq.TCDSiCI;
import rcms.utilities.hwcfg.eq.Trigger;

/**
 * This class performs mapping of hardware objects' relations into {@link DAQ}
 * objects'relations
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class RelationMapper implements Serializable {

	private final static Logger logger = Logger.getLogger(RelationMapper.class);
	private final ObjectMapper objectMapper;

	public Map<Integer, Integer> subFedBuilderToFrlPc;
	public Map<Integer, Integer> subFedBuilderToTTCP;
	public Map<Integer, Integer> FMMToTTCP;
	public Map<Integer, Integer> ruToFedBuilder;
	public Map<Integer, Set<Integer>> fedBuilderToSubFedBuilder;
	public Map<Integer, Set<Integer>> subFedBuilderToFrl;
	public Map<Integer, Set<Integer>> fmmToFed;
	public Map<Integer, Set<Integer>> frlToFed;
	public Map<Integer, Set<Integer>> ttcpToFed;
	public Map<Integer, Set<Integer>> fmmApplicationToFmm;
	public Map<Integer, Set<Integer>> frlPcToFrl;
	public Map<Integer, Set<Integer>> subsystemToTTCP;

	public RelationMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	private void fetchRelations(DAQPartition daqPartition) {
		fmmToFed = mapRelationsFmmToFed(daqPartition);
		frlToFed = mapRelationsFrlToFed(daqPartition);
		ttcpToFed = mapRelationsTTCPToFed(daqPartition);
		fmmApplicationToFmm = mapRelationsFmmApplicationToFmm(daqPartition);
		FMMToTTCP = mapRelationsFmmToTTCP(daqPartition);
		ruToFedBuilder = mapRelationsRuToFedBuilder(daqPartition);
		frlPcToFrl = mapRelationsFrlPcToFrl(daqPartition);
		subsystemToTTCP = mapRelationsSubsystemToTTCP(daqPartition);
	}

	private void buildRelations() {
		objectMapper.daq.setBus(new ArrayList<>(objectMapper.bus.values()));
		objectMapper.daq.setSubSystems(new ArrayList<>(objectMapper.subSystems.values()));
		objectMapper.daq.setFrlPcs(new ArrayList<FRLPc>(objectMapper.frlPcs.values()));
		objectMapper.daq.setFmmApplications(new ArrayList<FMMApplication>(objectMapper.fmmApplications.values()));
		objectMapper.daq.setFedBuilders(new ArrayList<>(objectMapper.fedBuilders.values()));

		/* building FMM-FED */
		int ignoredFeds = 0;
		for (Entry<Integer, Set<Integer>> relation : fmmToFed.entrySet()) {
			FMM fmm = objectMapper.fmms.get(relation.getKey());
			for (int fedId : relation.getValue()) {
				if (objectMapper.feds.containsKey(fedId)) {
					FED fed = objectMapper.feds.get(fedId);
					fmm.getFeds().add(fed);
					fed.setFmm(fmm);
				} else {
					ignoredFeds++;
				}
			}
		}
		if (ignoredFeds > 0)
			logger.warn("There are " + ignoredFeds + " warnings/problems mapping FED-FMM relations");

		/* building FRL-FED */
		for (Entry<Integer, Set<Integer>> relation : frlToFed.entrySet()) {
			FRL frl = objectMapper.frls.get(relation.getKey());
			for (int fedId : relation.getValue()) {
				FED fed = objectMapper.feds.get(fedId);
				frl.getFeds().put(fed.getFrlIO(), fed); // TODO: check if
														// correct
				fed.setFrl(frl);
			}
		}

		/* building TTCP-FED */
		int all = 0, success = 0;
		for (Entry<Integer, Set<Integer>> relation : ttcpToFed.entrySet()) {
			TTCPartition ttcp = objectMapper.ttcPartitions.get(relation.getKey());
			for (int fedId : relation.getValue()) {
				FED fed = objectMapper.feds.get(fedId);
				if (fed != null) {
					ttcp.getFeds().add(fed);
					fed.setTtcp(ttcp);
					success++;
				}
				all++;

			}
		}
		if (all - success > 0) {
			logger.warn("All FEDs: " + all + ", mapped " + success);
		}

		/* building FMMApplication - FMM */
		for (Entry<Integer, Set<Integer>> relation : fmmApplicationToFmm.entrySet()) {
			FMMApplication fmmApplication = objectMapper.fmmApplications.get(relation.getKey());
			for (int fmmId : relation.getValue()) {
				FMM fmm = objectMapper.fmms.get(fmmId);
				fmmApplication.getFmms().add(fmm);
				fmm.setFmmApplication(fmmApplication);
				fmmApplication.setDaq(objectMapper.daq);
			}
		}

		/* building FEDBuilder - SubFEDBuilder */
		for (Entry<Integer, Set<Integer>> relation : fedBuilderToSubFedBuilder.entrySet()) {
			FEDBuilder fedBuilder = objectMapper.fedBuilders.get(relation.getKey());
			for (int subFedBuilderId : relation.getValue()) {
				SubFEDBuilder subFedBuilder = objectMapper.subFedBuilders.get(subFedBuilderId);
				fedBuilder.getSubFedbuilders().add(subFedBuilder);
				subFedBuilder.setFedBuilder(fedBuilder);
			}
		}

		/* building SubFEDBuilder - FRL */
		for (Entry<Integer, Set<Integer>> relation : subFedBuilderToFrl.entrySet()) {
			SubFEDBuilder subFedBuilder = objectMapper.subFedBuilders.get(relation.getKey());
			for (int frlId : relation.getValue()) {
				FRL frl = objectMapper.frls.get(frlId);
				subFedBuilder.getFrls().add(frl);
				frl.setSubFedbuilder(subFedBuilder);
			}
		}

		/* building SubFEDBuilder - FRLPc */
		for (Entry<Integer, Integer> relation : subFedBuilderToFrlPc.entrySet()) {
			SubFEDBuilder subFedBuilder = objectMapper.subFedBuilders.get(relation.getKey());
			FRLPc frlPc = objectMapper.frlPcs.get(relation.getValue());
			subFedBuilder.setFrlPc(frlPc);
		}

		/* building SubFEDBuilder - TTCP */
		for (Entry<Integer, Integer> relation : subFedBuilderToTTCP.entrySet()) {
			SubFEDBuilder subFedBuilder = objectMapper.subFedBuilders.get(relation.getKey());
			TTCPartition ttcPartition = objectMapper.ttcPartitions.get(relation.getValue());
			subFedBuilder.setTtcPartition(ttcPartition);
		}
		/* building FMM - TTCP */
		int ignoredttcp = 0;
		for (Entry<Integer, Integer> relation : FMMToTTCP.entrySet()) {
			try {
				FMM fmm = objectMapper.fmms.get(relation.getKey());
				TTCPartition ttcPartition = objectMapper.ttcPartitions.get(relation.getValue());
				fmm.setTtcPartition(ttcPartition);
				ttcPartition.setFmm(fmm);
			} catch (NullPointerException e) {
				ignoredttcp++;
			}
		}
		if (ignoredttcp > 0)
			logger.warn("Problems when FMM-TTCP mapping:" + ignoredttcp);

		/* building RU - FEDBuilder */
		for (Entry<Integer, Integer> relation : ruToFedBuilder.entrySet()) {
			RU ru = objectMapper.rus.get(relation.getKey());
			FEDBuilder fedBuilder = objectMapper.fedBuilders.get(relation.getValue());
			ru.setFedBuilder(fedBuilder);
			fedBuilder.setRu(ru);
		}

		/* building FRLPc - FRL */
		// for (Entry<Integer, Set<Integer>> relation : frlPcToFrl.entrySet()) {
		// FRLPc frlPc = objectMapper.frlPcs.get(relation.getKey());
		// for (int frlId : relation.getValue()) {
		// FRL frl = objectMapper.frls.get(frlId);
		// frlPc.getFrls().add(frl);
		// frl.setFrlPc(frlPc);
		// }
		// }

		/* building Subsystsem - TTCP */
		int ignoredTTCP = 0;
		for (Entry<Integer, Set<Integer>> relation : subsystemToTTCP.entrySet()) {
			SubSystem subsystem = objectMapper.subSystems.get(relation.getKey());
			for (int ttcpId : relation.getValue()) {
				try {
					TTCPartition ttcp = objectMapper.ttcPartitions.get(ttcpId);
					ttcp.setSubsystem(subsystem);
					subsystem.getTtcPartitions().add(ttcp);
				} catch (NullPointerException e) {
					ignoredTTCP++;
				}
			}
		}
		logger.warn("Ignored ttcp in subsystems " + ignoredTTCP);

	}

	public void mapAllRelations(DAQPartition daqPartition) {

		fetchRelations(daqPartition);
		buildRelations();

	}

	/**
	 * Retrieve FMM-FED relations
	 * 
	 * @return map representing FMM-FED one to many relation
	 */
	private Map<Integer, Set<Integer>> mapRelationsFmmToFed(DAQPartition daqPartition) {

		Map<Integer, Set<Integer>> result = new HashMap<>();

		for (rcms.utilities.hwcfg.eq.FMM hwfmm : objectMapper.getHardwareFmms(daqPartition)) {

			HashSet<Integer> children = new HashSet<>();
			result.put(hwfmm.hashCode(), children);

			for (rcms.utilities.hwcfg.eq.FED hwfed : hwfmm.getFEDs().values()) {
				children.add(hwfed.hashCode());
			}
		}
		return result;
	}

	private rcms.utilities.hwcfg.eq.FMM getTopFMMForPartition(DAQPartition dp, String ttcpName) {

		if ("CPM-PRI".equals(ttcpName) || "CPM-SEC".equals(ttcpName)) {
			return null;
		}

		String triggerName = "TCDS-PRI";

		Trigger trigger;
		try {
			trigger = dp.getDAQPartitionSet().getEquipmentSet().getTriggerByName(triggerName);
		} catch (HardwareConfigurationException e) {
			e.printStackTrace();
			return null;
		}

		// Find the ICI
		TCDSiCI ici;
		try {
			ici = trigger.getICIByTTCPName(ttcpName);
		} catch (HardwareConfigurationException e) {
			// for LPM/LTC partitions it is normal that some of them will have
			// no fmm. They are unused in the CDAQ config. You will find no FEDs
			// in these partitions =.
			// FIXME: encode in the data model, that this partition has no FMM
			// (and also no PI and no CI).
			return null;
		}

		rcms.utilities.hwcfg.eq.FMM pi = null;
		// Find the PI connected to this partition
		for (FMMTriggerLink ftl : dp.getDAQPartitionSet().getEquipmentSet().getFMMTriggerLinks())
			if (ftl.getTriggerId() == trigger.getId() && ftl.getLPMNr() == ici.getPMNr()
					&& ftl.getiCINr() == ici.getICINr()) {
				pi = dp.getDAQPartitionSet().getEquipmentSet().getFMMs().get(ftl.getFMMId());
			}

		if (pi == null) {
			return null;
		}

		// Find the FMM connected to the PI
		for (FMMFMMLink fmmfmm : dp.getDAQPartitionSet().getEquipmentSet().getFMMFMMLinks()) {
			if (fmmfmm.getTargetFMMId() == pi.getId()) {

				rcms.utilities.hwcfg.eq.FMM fmm = dp.getDAQPartitionSet().getEquipmentSet().getFMMs()
						.get(fmmfmm.getSourceFMMId());
				return fmm;
			}
		}

		return null;
	}

	/**
	 * Retrieve FMM-TTCP relations
	 * 
	 * @return map representing FMM-TTCP one to one relation
	 */
	private Map<Integer, Integer> mapRelationsFmmToTTCP(DAQPartition daqPartition) {

		Map<Integer, Integer> result = new HashMap<>();

		for (rcms.utilities.hwcfg.eq.TTCPartition hwttcPartition : daqPartition.getDAQPartitionSet().getEquipmentSet()
				.getTTCPartitions().values()) {

			rcms.utilities.hwcfg.eq.FMM hwfmm = getTopFMMForPartition(daqPartition, hwttcPartition.getName());
			if (hwfmm != null)
				result.put(hwfmm.hashCode(), hwttcPartition.hashCode());

		}
		return result;
	}

	/**
	 * Retrieve FRL-FED relations
	 * 
	 * @return map representing FRL-FED one to many relation
	 */
	private Map<Integer, Set<Integer>> mapRelationsFrlToFed(DAQPartition daqPartition) {

		Map<Integer, Set<Integer>> result = new HashMap<>();
		Set<rcms.utilities.hwcfg.eq.FRL> frls = objectMapper.getHardwareFrls(daqPartition);
		for (rcms.utilities.hwcfg.eq.FRL hwfrl : frls) {

			HashSet<Integer> children = new HashSet<>();
			result.put(hwfrl.hashCode(), children);

			for (rcms.utilities.hwcfg.eq.FED hwfed : hwfrl.getFEDs().values()) {
				children.add(hwfed.hashCode());
			}
		}
		return result;
	}

	/**
	 * Retrieve FMMApplication-FMM relations
	 * 
	 * @return map representing FMMApplication-FMM one to many relation
	 */
	private Map<Integer, Set<Integer>> mapRelationsFmmApplicationToFmm(DAQPartition daqPartition) {

		Map<Integer, Set<Integer>> result = new HashMap<>();

		Set<rcms.utilities.hwcfg.eq.FMM> fmms = objectMapper.getHardwareFmms(daqPartition);
		for (rcms.utilities.hwcfg.eq.FMM hwfmm : fmms) {
			String fmmPc = hwfmm.getFMMCrate().getHostName();

			if (result.containsKey(fmmPc.hashCode())) {
				result.get(fmmPc.hashCode()).add(hwfmm.hashCode());
			} else {
				result.put(fmmPc.hashCode(), new HashSet<Integer>());
			}
		}

		return result;
	}

	/**
	 * Retrieve RU-FEDBuilder relations
	 */
	private Map<Integer, Integer> mapRelationsRuToFedBuilder(DAQPartition daqPartition) {
		Map<Integer, Integer> result = new HashMap<>();
		for (rcms.utilities.hwcfg.dp.RU hwru : daqPartition.getRUs().values()) {
			rcms.utilities.hwcfg.fb.FEDBuilder hwFedBuilder = daqPartition.getDAQPartitionSet().getFEDBuilderSet()
					.getFBs().get(hwru.getFBId());
			result.put(hwru.hashCode(), hwFedBuilder.hashCode());
		}
		return result;
	}

	/**
	 * Retrieve FRLPc-FRL relations
	 * 
	 * @return map representing FRLPc-FRL one to many relation
	 */
	private Map<Integer, Set<Integer>> mapRelationsFrlPcToFrl(DAQPartition daqPartition) {

		Map<Integer, Set<Integer>> result = new HashMap<>();

		Set<rcms.utilities.hwcfg.eq.FRL> frls = objectMapper.getHardwareFrls(daqPartition);
		for (rcms.utilities.hwcfg.eq.FRL hwfrl : frls) {

			String frlPc = hwfrl.getFRLCrate().getHostName();

			if (!result.containsKey(frlPc.hashCode())) {
				HashSet<Integer> children = new HashSet<>();
				result.put(frlPc.hashCode(), children);
			}
			result.get(frlPc.hashCode()).add(hwfrl.hashCode());

		}
		return result;
	}

	/**
	 * Retrieve Subsystem-TTCP relations
	 * 
	 * @return map representing Subsystem-TTCP one to many relation
	 */
	private Map<Integer, Set<Integer>> mapRelationsSubsystemToTTCP(DAQPartition daqPartition) {

		Map<Integer, Set<Integer>> result = new HashMap<>();

		Collection<rcms.utilities.hwcfg.eq.SubSystem> subsystems = daqPartition.getDAQPartitionSet().getEquipmentSet()
				.getSubsystems().values();
		for (rcms.utilities.hwcfg.eq.SubSystem hwsubsystem : subsystems) {

			Collection<rcms.utilities.hwcfg.eq.TTCPartition> ttcPartitions = hwsubsystem.getTTCPartitions().values();

			HashSet<Integer> children = new HashSet<>();
			result.put(hwsubsystem.hashCode(), children);

			for (rcms.utilities.hwcfg.eq.TTCPartition ttcPartition : ttcPartitions) {

				result.get(hwsubsystem.hashCode()).add(ttcPartition.hashCode());
			}

		}
		return result;
	}

	/**
	 * Retrieve TTCP-FED relations
	 * 
	 * @return map representing TTCP-FED one to many relation
	 */
	private Map<Integer, Set<Integer>> mapRelationsTTCPToFed(DAQPartition daqPartition) {

		Map<Integer, Set<Integer>> result = new HashMap<>();

		for (rcms.utilities.hwcfg.eq.TTCPartition hwttcp : daqPartition.getDAQPartitionSet().getEquipmentSet()
				.getTTCPartitions().values()) {

			HashSet<Integer> children = new HashSet<>();
			result.put(hwttcp.hashCode(), children);

			for (rcms.utilities.hwcfg.eq.FED hwfed : hwttcp.getFEDs().values()) {
				children.add(hwfed.hashCode());
			}
		}
		return result;
	}
}
