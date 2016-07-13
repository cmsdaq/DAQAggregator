package rcms.utilities.daqaggregator.mappers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.BU;
import rcms.utilities.daqaggregator.data.BUSummary;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.data.FEDBuilderSummary;
import rcms.utilities.daqaggregator.data.FMM;
import rcms.utilities.daqaggregator.data.FMMApplication;
import rcms.utilities.daqaggregator.data.FRL;
import rcms.utilities.daqaggregator.data.FRLPc;
import rcms.utilities.daqaggregator.data.FRLType;
import rcms.utilities.daqaggregator.data.FMMType;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.data.SubFEDBuilder;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.hwcfg.HardwareConfigurationException;
import rcms.utilities.hwcfg.dp.DAQPartition;
import rcms.utilities.hwcfg.dp.DPGenericHost;
import rcms.utilities.hwcfg.eq.FMMFMMLink;
import rcms.utilities.hwcfg.eq.FMMTriggerLink;
import rcms.utilities.hwcfg.fb.FBI;

/**
 * This class performs mapping of hardware objects into {@link DAQ} objects
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class ObjectMapper {

	private static final Logger logger = Logger.getLogger(ObjectMapper.class);

	/** DAQ structure root object */
	protected DAQ daq;

	public Map<Integer, SubFEDBuilder> subFedBuilders;
	public Map<Integer, FEDBuilder> fedBuilders;
	public Map<Integer, FRL> frls;
	public Map<Integer, TTCPartition> ttcPartitions;
	public Map<Integer, FRLPc> frlPcs;
	public Map<Integer, BU> bus;
	public Map<Integer, RU> rus;
	public Map<Integer, FMM> fmms;
	public Map<Integer, FED> feds;
	public Map<Integer, FMMApplication> fmmApplications;
	public Map<Integer, SubSystem> subSystems;

	public Map<Integer, RU> rusById;
	public Map<Integer, BU> busById;
	public Map<Integer, FED> fedsById;
	public Map<Integer, FED> fedsByExpectedId;
	public Map<Integer, FED> ttcpById;
	public Map<Integer, TTCPartition> ttcpartitionsById;

	public Map<String, FRLPc> frlPcByHostname;
	public Map<String, FMMApplication> fmmApplicationByHostname;
	public Map<String, RU> rusByHostname;
	public Map<String, BU> busByHostname;
	public Map<String, SubSystem> subsystemByName;

	public void mapAllObjects(DAQPartition daqPartition) {

		daq = new DAQ();

		// TODO: this is for flashlist mapping, refactor me
		rusById = new HashMap<>();
		busById = new HashMap<>();
		fedsById = new HashMap<>();
		fedsByExpectedId = new HashMap<>();
		ttcpById = new HashMap<>();
		ttcpartitionsById = new HashMap<>();
		frlPcByHostname = new HashMap<>();
		fmmApplicationByHostname = new HashMap<>();
		subsystemByName = new HashMap<>();
		rusByHostname = new HashMap<>();
		busByHostname = new HashMap<>();

		/* Building objects */
		bus = mapBUs(daqPartition);
		rus = mapRUs(daqPartition);
		frlPcs = mapFrlPcs(daqPartition);
		ttcPartitions = mapTTCPartitions(daqPartition);
		subSystems = mapSubSystem(daqPartition);

		/* map FEDBuilders from hardware structure */
		fedBuilders = new HashMap<>();
		for (rcms.utilities.hwcfg.fb.FEDBuilder hwfedBuilder : getHardwareFedBuilders(daqPartition)) {
			FEDBuilder fedbuilder = new FEDBuilder();
			fedbuilder.setName(hwfedBuilder.getName());
			fedBuilders.put(hwfedBuilder.hashCode(), fedbuilder);
		}

		/* map FRL from hw */
		frls = new HashMap<>();
		for (rcms.utilities.hwcfg.eq.FRL hwfrl : getHardwareFrls(daqPartition)) {
			FRL frl = new FRL();
			frl.setGeoSlot(hwfrl.getGeoSlot());
			frl.setType(FRLType.getByName(hwfrl.getFRLMode()));
			frls.put(hwfrl.hashCode(), frl);
		}

		/* map FEDs */
		feds = new HashMap<>();
		for (rcms.utilities.hwcfg.eq.FED hwfed : getHardwareFeds(daqPartition)) {
			FED fed = new FED();
			fed.setId((int) hwfed.getId());
			fedsById.put(fed.getId(), fed);
			fed.setFmmIO(hwfed.getFMMIO());
			fed.setFrlIO(hwfed.getFRLIO());
			fed.setSrcIdExpected(hwfed.getSrcId());
			fedsByExpectedId.put(fed.getSrcIdExpected(), fed);
			fed.setHasSLINK(hwfed.hasSLINK());
			fed.setHasTTS(hwfed.hasTTS());
			feds.put(hwfed.hashCode(), fed);
		}

		/* map FMMs */
		Set<FMMFMMLink> fmmLinks = daqPartition.getDAQPartitionSet().getEquipmentSet().getFMMFMMLinks();
		HashMap<Long, FMMFMMLink> fmmMap = new HashMap<>();
		for (FMMFMMLink fmmLink : fmmLinks) {
			fmmMap.put(fmmLink.getSourceFMMId(), fmmLink);
		}
		fmms = new HashMap<>();
		for (rcms.utilities.hwcfg.eq.FMM hwfmm : getHardwareFmms(daqPartition)) {
			FMM fmm = new FMM();
			fmm.setGeoslot(hwfmm.getGeoSlot());
			fmm.setFmmType(FMMType.valueOf( hwfmm.getFMMType().name() ));			
			fmm.setServiceName(hwfmm.getServiceName());
			if (hwfmm.getDual()) {
				FMMFMMLink fmmLink = fmmMap.get(hwfmm.getId());
				if (fmmLink != null &&
					daqPartition.getDAQPartitionSet().getEquipmentSet().getFMMs().get(fmmLink.getTargetFMMId()).getFMMType().equals( rcms.utilities.hwcfg.eq.FMM.FMMType.pi )) {
					try {
						int fmmIO = fmmLink.getSourceFMMIO();
						if (fmmIO == 22 || fmmIO == 23)
							fmm.takeB = true;
					} catch (NullPointerException e) {
						logger.warn("Dual FMM has no link: ");
						logger.warn("Problem fmm :" + hwfmm.getGeoSlot() + hwfmm.getFMMCrate().getHostName());
					}
				} 
			}
			fmms.put(hwfmm.hashCode(), fmm);
		}

		/* map FMMApplications */
		fmmApplications = new HashMap<>();
		Map<String, Integer> fmmPcs = new HashMap<>();
		for (rcms.utilities.hwcfg.eq.FMM hwfmm : getHardwareFmms(daqPartition)) {
			String fmmPc = hwfmm.getFMMCrate().getHostName();
			if (!fmmPcs.containsKey(fmmPc))
				fmmPcs.put(fmmPc, hwfmm.getFMMCrate().getPort());
		}
		for (String fmmPc : fmmPcs.keySet()) {
			FMMApplication fmmApplication = new FMMApplication();
			fmmApplication.setHostname(fmmPc);
			fmmApplication.setPort(fmmPcs.get(fmmPc));
			fmmApplications.put(fmmPc.hashCode(), fmmApplication);
			fmmApplicationByHostname.put(fmmApplication.getHostname(), fmmApplication);
		}

		/* create summary */
		BUSummary buSummary = new BUSummary();
		daq.setBuSummary(buSummary);
		buSummary.setDaq(daq);

		/* create summary */
		FEDBuilderSummary fedBuilderSummary = new FEDBuilderSummary();
		daq.setFedBuilderSummary(fedBuilderSummary);
		fedBuilderSummary.setDaq(daq);
		daq.setAllFeds(new HashSet<FED>(feds.values()));

		logger.info("Retrieval summary " + this.toString());
		logger.info("Subsystem summary " + subSystems.values());

	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();

		int objectsMapped = subSystems.size() + rus.size() + bus.size() + feds.size() + frls.size() + fmms.size()
				+ ttcPartitions.size() + fedBuilders.size() + subFedBuilders.size() + fmmApplications.size()
				+ frlPcs.size();

		int bFmms = 0;
		for (FMM fmm : fmms.values()) {
			if (fmm.takeB)
				bFmms++;
		}

		sb.append("Object mapping raport: " + objectsMapped + " objects mapped: ");
		sb.append("[Subsystems:" + subSystems.size() + "],");
		sb.append("[RUs:" + rus.size() + "],");
		sb.append("[BUs:" + bus.size() + "],");
		sb.append("[FEDs:" + feds.size() + "],");
		sb.append("[FRLs:" + frls.size() + "],");
		sb.append("[FMMs:" + fmms.size() + "(" + bFmms + " dual with io 22|23)],");
		sb.append("[TTCPs:" + ttcPartitions.size() + "],");
		sb.append("[FBs:" + fedBuilders.size() + "],");
		sb.append("[sFBs:" + subFedBuilders.size() + "],");
		sb.append("[FMMApps:" + fmmApplications.size() + "],");
		sb.append("[FRLPs:" + frlPcs.size() + "],");

		return sb.toString();

	}

	/**
	 * Maps all BU objects
	 */
	public Map<Integer, BU> mapBUs(DAQPartition dp) {
		Map<Integer, BU> result = new HashMap<>();
		for (DPGenericHost host : dp.getGenericHosts()) {
			if (host.getRole().equals("BU")) {
				BU bu = new BU();
				busById.put((int) host.getId(), bu);
				busByHostname.put(host.getHostName(), bu);
				bu.setHostname(host.getHostName());
				result.put(host.hashCode(), bu);
			}
		}
		return result;
	}

	/**
	 * Maps all RU objects
	 */
	public Map<Integer, RU> mapRUs(DAQPartition dp) {
		Map<Integer, RU> result = new HashMap<>();
		for (rcms.utilities.hwcfg.dp.RU hwru : dp.getRUs().values()) {
			RU ru = new RU();
			ru.setEVM(hwru.isEVM());
			ru.setInstance((int) hwru.getId());
			ru.setHostname(hwru.getHostName());
			ru.setStateName("unknown");
			rusById.put(ru.getInstance(), ru);
			rusByHostname.put(ru.getHostname(), ru);
			result.put(hwru.hashCode(), ru);
		}
		return result;
	}

	/**
	 * Get FMM objects. Note that result may be subset of EquipmentSet.
	 * 
	 * @return set of hardware FMM objects.
	 */
	public Set<rcms.utilities.hwcfg.eq.FMM> getHardwareFmms(DAQPartition daqPartition) {

		Set<rcms.utilities.hwcfg.eq.FMM> result = new HashSet<>();

		for (rcms.utilities.hwcfg.eq.FED hwfed : getHardwareFeds(daqPartition)) {
			if (hwfed.getFMM() != null) {
				result.add(hwfed.getFMM());
			}
		}

		return result;
	}

	/**
	 * Get FED objects. Note that result may be subset of EquipmentSet.
	 * 
	 * @return set of hardware FED objects.
	 */
	public Set<rcms.utilities.hwcfg.eq.FED> getHardwareFeds(DAQPartition daqPartition) {

		Set<rcms.utilities.hwcfg.eq.FED> result = new HashSet<>();
		for (rcms.utilities.hwcfg.eq.FRL hwfrl : getHardwareFrls(daqPartition)) {
			for (rcms.utilities.hwcfg.eq.FED hwfed : hwfrl.getFEDs().values())
				//
				if (hwfed != null) {
					result.add(hwfed);
					Set<rcms.utilities.hwcfg.eq.FED> dependants = getDependantFeds(hwfed);
					result.addAll(dependants);
				}
		}
		return result;
	}

	public Set<rcms.utilities.hwcfg.eq.FED> getDependantFeds(rcms.utilities.hwcfg.eq.FED fed) {
		Set<rcms.utilities.hwcfg.eq.FED> result = new HashSet<>();

		if (fed.getDependentFEDs() == null || fed.getDependentFEDs().size() == 0)
			return result;
		else {
			for (rcms.utilities.hwcfg.eq.FED dependent : fed.getDependentFEDs()) {
				result.add(dependent);
				result.addAll(getDependantFeds(dependent));
			}

			logger.debug("Found " + result.size() + " dependent feds");
			return result;
		}
	}

	/**
	 * Get FEDBuilder objects. Note that result may be subset of EquipmentSet.
	 * 
	 * @return set of hardware FEDBuilders objects.
	 */
	public Set<rcms.utilities.hwcfg.fb.FEDBuilder> getHardwareFedBuilders(DAQPartition daqPartition) {

		Set<rcms.utilities.hwcfg.fb.FEDBuilder> result = new HashSet<>();

		/* start with a rus */
		Collection<rcms.utilities.hwcfg.dp.RU> hwrus = daqPartition.getRUs().values();

		/* get all fedbuilders map */
		Map<Long, rcms.utilities.hwcfg.fb.FEDBuilder> allFedbuilders = daqPartition.getDAQPartitionSet()
				.getFEDBuilderSet().getFBs();

		/* get only rus corresponding fedBuidlers */
		for (rcms.utilities.hwcfg.dp.RU hwru : hwrus) {
			Long fedBuilderId = hwru.getFBId();
			rcms.utilities.hwcfg.fb.FEDBuilder hwfedBuilder = allFedbuilders.get(fedBuilderId);
			result.add(hwfedBuilder);
		}

		return result;
	}

	/**
	 * Get FRL objects. Note that result may be subset of EquipmentSet.
	 * 
	 * @return set of hardware FRL objects.
	 */
	public Set<rcms.utilities.hwcfg.eq.FRL> getHardwareFrls(DAQPartition daqPartition) {

		Set<rcms.utilities.hwcfg.eq.FRL> result = new HashSet<>();

		/* loop over fed builders */
		for (rcms.utilities.hwcfg.fb.FEDBuilder fb : getHardwareFedBuilders(daqPartition)) {

			// loop over fedbuilder inputs of given fedbuilder
			for (FBI fbi : fb.getFBIs().values()) {
				try {
					rcms.utilities.hwcfg.eq.FRL frl;
					frl = daqPartition.getDAQPartitionSet().getEquipmentSet().getFRL(fbi.getFRLId());
					result.add(frl);
				} catch (HardwareConfigurationException e) {
					logger.warn("cannot get FRL by id, source error: " + e.getMessage());
				}
			}
		}

		return result;
	}

	/**
	 * Map all FRLPc objects.
	 * 
	 * @return map of all FRLPc
	 */
	public Map<Integer, FRLPc> mapFrlPcs(DAQPartition daqPartition) {

		Map<Integer, FRLPc> result = new HashMap<>();

		Set<String> frlPcs = new HashSet<String>();

		Map<Long, rcms.utilities.hwcfg.eq.FRL> frls = daqPartition.getDAQPartitionSet().getEquipmentSet().getFRLs();
		for (rcms.utilities.hwcfg.eq.FRL hwfrl : frls.values()) {

			String frlPc = hwfrl.getFRLCrate().getHostName();
			frlPcs.add(frlPc);
		}

		for (String hwfrlPc : frlPcs) {
			FRLPc frlPc = new FRLPc();
			frlPc.setHostname(hwfrlPc);
			result.put(hwfrlPc.hashCode(), frlPc);
			frlPcByHostname.put(frlPc.getHostname(), frlPc);
		}

		return result;
	}

	/**
	 * Maps all TTCPartition objects
	 */
	public Map<Integer, TTCPartition> mapTTCPartitions(DAQPartition daqPartition) {

		Map<Integer, TTCPartition> result = new HashMap<>();
		for (rcms.utilities.hwcfg.eq.FED hwfed : getHardwareFeds(daqPartition)) {

			rcms.utilities.hwcfg.eq.TTCPartition hwttcPartition = hwfed.getTTCPartition();

			TTCPartition ttcPartition = new TTCPartition();
			ttcPartition.setName(hwttcPartition.getName());
			ttcpartitionsById.put((int) hwttcPartition.getId(), ttcPartition);
			// ttcpById.put(hwttcPartition.getId(), value)
			// TODO: get masked info

			result.put(hwttcPartition.hashCode(), ttcPartition);

		}

		return result;
	}

	public Map<Integer, SubSystem> mapSubSystem(DAQPartition daqPartition) {

		Map<Integer, SubSystem> result = new HashMap<>();

		for (rcms.utilities.hwcfg.eq.SubSystem hwsubsystem : daqPartition.getDAQPartitionSet().getEquipmentSet()
				.getSubsystems().values()) {

			SubSystem subSystem = new SubSystem();
			subSystem.setName(hwsubsystem.getName());
			result.put(hwsubsystem.hashCode(), subSystem);
			subsystemByName.put(hwsubsystem.getName(), subSystem);
		}

		return result;

	}
}
