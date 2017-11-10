package rcms.utilities.daqaggregator.mappers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;


import rcms.utilities.daqaggregator.data.*;
import rcms.utilities.hwcfg.HardwareConfigurationException;
import rcms.utilities.daqaggregator.datasource.TCDSFMInfoRetriever;
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

	private final transient TCDSFMInfoRetriever tcdsFmInfoRetriever;
	
	/** DAQ structure root object */
	public DAQ daq;

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
	
	public ObjectMapper(TCDSFMInfoRetriever tcdsFmInfoRetriever) {
		this.tcdsFmInfoRetriever = tcdsFmInfoRetriever;
	}

	public void mapAllObjects(DAQPartition daqPartition) {

		daq = new DAQ();
		try {
			String execPath = URLDecoder.decode(DAQ.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");
			String [] tokens = execPath.split("/");
			daq.setDaqAggregatorProducer(tokens[tokens.length-1]);
		} catch (UnsupportedEncodingException e2) {}

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
			fedbuilder.setDaq(daq);
			fedBuilders.put(hwfedBuilder.hashCode(), fedbuilder);
		}

		/* map FRL from hw */
		frls = new HashMap<>();
		for (rcms.utilities.hwcfg.eq.FRL hwfrl : getHardwareFrls(daqPartition)) {
			FRL frl = new FRL();
			frl.setId(String.valueOf(hwfrl.getId()));
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
			fmm.setId(String.valueOf(hwfmm.getId()));
			fmm.setGeoslot(hwfmm.getGeoSlot());
			fmm.setFmmType(FMMType.valueOf(hwfmm.getFMMType().name()));
			fmm.setServiceName(hwfmm.getServiceName());
			if (hwfmm.getDual()) {
				FMMFMMLink fmmLink = fmmMap.get(hwfmm.getId());
				if (fmmLink != null && daqPartition.getDAQPartitionSet().getEquipmentSet().getFMMs()
						.get(fmmLink.getTargetFMMId()).getFMMType().equals(rcms.utilities.hwcfg.eq.FMM.FMMType.pi)) {
					try {
						int fmmIO = fmmLink.getSourceFMMIO();
						if (fmmIO == 22 || fmmIO == 23)
							fmm.setTakeB(true);
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

		/* create object with global tcds information */
		TCDSGlobalInfo tcdsGlobalInfo = new TCDSGlobalInfo();
		daq.setTcdsGlobalInfo(tcdsGlobalInfo);


		daq.setTtcPartitions(new ArrayList<>(ttcpartitionsById.values()));
		daq.setFmms(new ArrayList<>(fmms.values()));
		daq.setRus(new ArrayList<>(rus.values()));
		daq.setFrls(new ArrayList<>(frls.values()));
		daq.setFeds(new ArrayList<>(feds.values()));
		daq.setSubFEDBuilders(new ArrayList<>(subFedBuilders.values()));
		daq.setHltInfo(new HltInfo());

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
			if (fmm.isTakeB())
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
				bu.setDaq(daq);
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

				//also retrieve second-level FMMs linked to this FMM
				for (rcms.utilities.hwcfg.eq.FMMFMMLink ffl: daqPartition.getDAQPartitionSet().getEquipmentSet().getFMMFMMLinks()){
					if (ffl.getSourceFMMId() == hwfed.getFMM().getId()){
						rcms.utilities.hwcfg.eq.FMM targetFMM = daqPartition.getDAQPartitionSet().getEquipmentSet().getFMMs().get(ffl.getTargetFMMId());
						if (targetFMM.getFMMType().equals(rcms.utilities.hwcfg.eq.FMM.FMMType.fmm)){
							result.add(targetFMM);
						}
					}
				}
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
		try {

			for (rcms.utilities.hwcfg.dp.RU hwru : daqPartition.getRUs().values()){
				rcms.utilities.hwcfg.fb.FEDBuilder hwfb = daqPartition.getDAQPartitionSet().getFEDBuilderSet().getFBs().get(hwru.getFBId());

				for (FBI hwfbi : hwfb.getFBIs().values()){
					Long frlId = hwfbi.getFRLId();
					rcms.utilities.hwcfg.eq.FRL frl = daqPartition.getDAQPartitionSet().getEquipmentSet().getFRL(frlId);

					for (Integer frlIO : frl.getFEDs().keySet() ) {
						if ( hwfbi.getFRLInputEnableMask() == null || 
								((hwfbi.getFRLInputEnableMask() & (1<<frlIO)) == (1<<frlIO) ) ) {							
							rcms.utilities.hwcfg.eq.FED fed = frl.getFEDs().get(frlIO);

							result.add(fed);

							Set<rcms.utilities.hwcfg.eq.FED> dependents = getDependentFeds(fed);

							result.addAll(dependents);
						}
					}
				}
			}
		}catch (HardwareConfigurationException e){
			e.printStackTrace();
		}

		return result;
	}

	public Set<rcms.utilities.hwcfg.eq.FED> getDependentFeds(rcms.utilities.hwcfg.eq.FED fed) {
		Set<rcms.utilities.hwcfg.eq.FED> result = new HashSet<>();

		if (fed.getDependentFEDs() == null || fed.getDependentFEDs().size() == 0)
			return result;
		else {
			for (rcms.utilities.hwcfg.eq.FED dependent : fed.getDependentFEDs()) {

				result.add(dependent);
				result.addAll(getDependentFeds(dependent));
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
		
		Set<rcms.utilities.hwcfg.eq.FRL> frls = getHardwareFrls(daqPartition);
		for (rcms.utilities.hwcfg.eq.FRL hwfrl : frls){

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
			ttcPartition.setTtcpNr(hwttcPartition.getTTCPNr());
			ttcpartitionsById.put((int) hwttcPartition.getId(), ttcPartition);
			// ttcpById.put(hwttcPartition.getId(), value)
			// TODO: get masked info

			result.put(hwttcPartition.hashCode(), ttcPartition);

		}

		return result;
	}

	public Map<Integer, SubSystem> mapSubSystem(DAQPartition daqPartition) {

		Map<Integer, SubSystem> result = new HashMap<>();




		for (rcms.utilities.hwcfg.eq.FED hwfed : getHardwareFeds(daqPartition)) {

			rcms.utilities.hwcfg.eq.TTCPartition hwttcPartition = hwfed.getTTCPartition();

			rcms.utilities.hwcfg.eq.SubSystem hwsubsystem = hwttcPartition.getSubSystem();

			SubSystem subSystem = new SubSystem();
			subSystem.setName(hwsubsystem.getName());
			result.put(hwsubsystem.hashCode(), subSystem);
			subsystemByName.put(hwsubsystem.getName(), subSystem);
		}

		return result;

	}
}
