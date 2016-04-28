package rcms.utilities.daqaggregator;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.BU;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.data.FMM;
import rcms.utilities.daqaggregator.data.FMMApplication;
import rcms.utilities.daqaggregator.data.FRL;
import rcms.utilities.daqaggregator.data.FRLPc;
import rcms.utilities.daqaggregator.data.FRLType;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.data.SubFEDBuilder;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.hwcfg.dp.DAQPartition;
import rcms.utilities.hwcfg.dp.DAQPartitionSet;
import rcms.utilities.hwcfg.dp.DPGenericHost;

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

	public void mapAllObjects(DAQPartition daqPartition) {

		daq = new DAQ();
		/* Building objects */
		bus = mapBUs(daqPartition);
		rus = mapRUs(daqPartition);
		frlPcs = mapFrlPcs(daqPartition);
		ttcPartitions = mapTTCPartitions(daqPartition);
		fmms = mapFMMs(daqPartition);
		feds = mapFEDs(daqPartition);
		frls = mapFRLs(daqPartition);
		fmmApplications = mapFMMApplications(daqPartition);
		fedBuilders = mapFEDBuilders(daqPartition);

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
			ru.setHostname(hwru.getHostName());
			result.put(hwru.hashCode(), ru);
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
			// TODO: get masked info

			result.put(hwttcPartition.hashCode(), ttcPartition);

		}

		return result;
	}

	/**
	 * Map all FMM objects. Note that objects are retrieved with identity
	 * information for further processing
	 * 
	 * @return map of all FMM identified by hardware object's hashCode
	 */
	public HashMap<Integer, FMM> mapFMMs(DAQPartition daqPartition) {

		HashMap<Integer, FMM> result = new HashMap<>();
		Map<Long, rcms.utilities.hwcfg.eq.FMM> fmms = daqPartition.getDAQPartitionSet().getEquipmentSet().getFMMs();
		for (rcms.utilities.hwcfg.eq.FMM hwfmm : fmms.values()) {

			FMM fmm = new FMM();
			fmm.setGeoslot(hwfmm.getGeoSlot());
			// fmm.setUrl(hwfmm.); TODO: where is url?
			result.put(hwfmm.hashCode(), fmm);
		}

		return result;
	}

	/**
	 * Map all FED objects. Note that objects are retrieved with identity
	 * information for further processing
	 * 
	 * @return map of all FED identified by hardware object's hashCode
	 */
	public HashMap<Integer, FED> mapFEDs(DAQPartition daqPartition) {

		HashMap<Integer, FED> result = new HashMap<>();
		Map<Long, rcms.utilities.hwcfg.eq.FED> feds = daqPartition.getDAQPartitionSet().getEquipmentSet().getFEDs();

		for (rcms.utilities.hwcfg.eq.FED hwfed : feds.values()) {

			FED fed = new FED();
			fed.setId((int) hwfed.getId());
			fed.setFmmIO(hwfed.getFMMIO());
			fed.setFrlIO(hwfed.getFRLIO());
			fed.setSrcIdExpected(hwfed.getSrcId());
			result.put(hwfed.hashCode(), fed);

		}

		return result;
	}

	/**
	 * Map all FEDBuilder objects. Note that objects are retrieved with identity
	 * information for further processing
	 * 
	 * @return map of all FEDBuilder identified by hardware object's hashCode
	 */
	public HashMap<Integer, FEDBuilder> mapFEDBuilders(DAQPartition daqPartition) {

		HashMap<Integer, FEDBuilder> result = new HashMap<>();
		Collection<rcms.utilities.hwcfg.fb.FEDBuilder> fedbuilders = daqPartition.getDAQPartitionSet()
				.getFEDBuilderSet().getFBs().values();

		for (rcms.utilities.hwcfg.fb.FEDBuilder hwfedBuilder : fedbuilders) {

			System.out.println("FB id: " + hwfedBuilder.getId());
			FEDBuilder fedbuilder = new FEDBuilder();
			fedbuilder.setName(hwfedBuilder.getName());
			result.put(hwfedBuilder.hashCode(), fedbuilder);
		}

		return result;
	}

	/**
	 * Map all FRL objects. Note that objects are retrieved with identity
	 * information for further processing
	 * 
	 * @return map of all FRLPcs identified by hardware object's hashCode
	 */
	public HashMap<Integer, FRL> mapFRLs(DAQPartition daqPartition) {

		HashMap<Integer, FRL> result = new HashMap<>();
		Map<Long, rcms.utilities.hwcfg.eq.FRL> frls = daqPartition.getDAQPartitionSet().getEquipmentSet().getFRLs();
		for (rcms.utilities.hwcfg.eq.FRL hwfrl : frls.values()) {

			FRL frl = new FRL();
			frl.setGeoSlot(hwfrl.getGeoSlot());
			frl.setType(FRLType.getByName(hwfrl.getFRLMode()));
			result.put(hwfrl.hashCode(), frl);
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
		}

		return result;
	}

	public Map<Integer, FMMApplication> mapFMMApplications(DAQPartition daqPartition) {

		Map<Integer, FMMApplication> result = new HashMap<>();
		Set<String> fmmPcs = new HashSet<String>();
		Map<Long, rcms.utilities.hwcfg.eq.FMM> fmms = daqPartition.getDAQPartitionSet().getEquipmentSet().getFMMs();
		for (rcms.utilities.hwcfg.eq.FMM hwfmm : fmms.values()) {
			fmmPcs.add(hwfmm.getFMMCrate().getHostName());
		}

		for (String fmmPc : fmmPcs) {
			// TODO: url how to get URL
			FMMApplication fmmApplication = new FMMApplication();
			fmmApplication.setHostname(fmmPc);
			result.put(fmmPc.hashCode(), fmmApplication);
		}

		return result;
	}

}
