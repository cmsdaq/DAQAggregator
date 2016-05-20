package rcms.utilities.daqaggregator.mappers;

import java.io.Serializable;
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
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.data.SubFEDBuilder;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.hwcfg.HardwareConfigurationException;
import rcms.utilities.hwcfg.dp.DAQPartition;
import rcms.utilities.hwcfg.dp.DAQPartitionSet;
import rcms.utilities.hwcfg.dp.DPGenericHost;
import rcms.utilities.hwcfg.fb.FBI;

/**
 * This class performs mapping of hardware objects into {@link DAQ} objects
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class ObjectMapper implements Serializable {

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

	public Map<Integer, RU> rusById;
	public Map<Integer, BU> busById;
	public Map<Integer, FED> fedsById;
	public Map<Integer, FED> ttcpById;
	public Map<Integer, TTCPartition> ttcpartitionsById;
	
	public Map<String, FRLPc> frlPcByHostname;
	public Map<String, FMMApplication> fmmApplicationByHostname;

	public void mapAllObjects(DAQPartition daqPartition) {

		daq = new DAQ();


		// TODO: this is for flashlist mapping, refactor me
		rusById = new HashMap<>();
		busById = new HashMap<>();
		fedsById = new HashMap<>();
		ttcpById = new HashMap<>();
		ttcpartitionsById = new HashMap<>();
		frlPcByHostname = new HashMap<>();
		fmmApplicationByHostname = new HashMap<>();

		/* Building objects */
		bus = mapBUs(daqPartition);
		rus = mapRUs(daqPartition);
		frlPcs = mapFrlPcs(daqPartition);
		ttcPartitions = mapTTCPartitions(daqPartition);

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
			feds.put(hwfed.hashCode(), fed);
		}

		/* map FMMs */
		fmms = new HashMap<>();
		for (rcms.utilities.hwcfg.eq.FMM hwfmm : getHardwareFmms(daqPartition)) {
			FMM fmm = new FMM();
			fmm.setGeoslot(hwfmm.getGeoSlot());
			// fmm.setUrl(hwfmm.); TODO: where is url?
			fmms.put(hwfmm.hashCode(), fmm);
		}

		/* map FMMApplications */
		fmmApplications = new HashMap<>();
		Set<String> fmmPcs = new HashSet<>();
		for (rcms.utilities.hwcfg.eq.FMM hwfmm : getHardwareFmms(daqPartition)) {
			String fmmPc = hwfmm.getFMMCrate().getHostName();
			if (!fmmPcs.contains(fmmPc))
				fmmPcs.add(fmmPc);
		}
		for (String fmmPc : fmmPcs) {
			FMMApplication fmmApplication = new FMMApplication();
			fmmApplication.setHostname(fmmPc);
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
		
		logger.info("Retrieval summary " + this.toString());

	}

	@Override
	public String toString() {
		return "ObjectMapper [subFedBuilders=" + subFedBuilders.size() + ", fedBuilders=" + fedBuilders.size()
				+ ", frls=" + frls.size() + ", ttcPartitions=" + ttcPartitions.size() + ", frlPcs=" + frlPcs.size()
				+ ", bus=" + bus.size() + ", rus=" + rus.size() + ", fmms=" + fmms.size() + ", feds=" + feds.size()
				+ ", fmmApplications=" + fmmApplications.size() + "]";
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
			rusById.put(ru.getInstance(), ru);
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
		DAQPartitionSet daqPartitionSet = daqPartition.getDAQPartitionSet();
		for (rcms.utilities.hwcfg.eq.TTCPartition hwttcPartition : daqPartitionSet.getEquipmentSet().getTTCPartitions()
				.values()) {
			TTCPartition ttcPartition = new TTCPartition();
			ttcPartition.setName(hwttcPartition.getName());
			ttcpartitionsById.put((int) hwttcPartition.getId(), ttcPartition);
			// ttcpById.put(hwttcPartition.getId(), value)
			// TODO: get masked info

			result.put(hwttcPartition.hashCode(), ttcPartition);

		}

		return result;
	}

}
