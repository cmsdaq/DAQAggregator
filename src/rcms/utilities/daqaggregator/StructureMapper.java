package rcms.utilities.daqaggregator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.BU;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FMM;
import rcms.utilities.daqaggregator.data.FRL;
import rcms.utilities.daqaggregator.data.FRLPc;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.hwcfg.dp.DAQPartition;
import rcms.utilities.hwcfg.dp.DAQPartitionSet;
import rcms.utilities.hwcfg.dp.DPGenericHost;
import rcms.utilities.hwcfg.eq.Host;

/**
 * This class is responsible for mapping data from hardware database to daq
 * structure. For better clarity and code maintainability the process is
 * performed in 2 stages:
 * 
 * <ul>
 * <li>Map hardware objects to DAQ structure objects</li>
 * <li>Map objects' relations</li>
 * </ul>
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class StructureMapper {

	private static final Logger logger = Logger.getLogger(StructureMapper.class);

	/** DAQ structure root object */
	private DAQ daq;

	/**
	 * Maps the structure of monitored data to DAQ structure (DAQ)
	 * 
	 * @see DAQ
	 * 
	 * @param daqPartition
	 *            object representing hardware configuration
	 */
	public void map(DAQPartition daqPartition) {

		daq = new DAQ();

		/* Building objects */
		List<BU> bus = mapBUs(daqPartition, daq);
		List<TTCPartition> ttcPartitions = mapTTCPartitions(daqPartition);
		HashMap<Integer, FMM> fmms = mapFMMs(daqPartition);
		HashMap<Integer, FED> feds = mapFEDs(daqPartition);
		HashMap<Integer, FRL> frls = mapFRLs(daqPartition);

		/* Building relations */
		Map<Integer, Set<Integer>> fmmToFed = mapRelationsFmmToFed(daqPartition);
		Map<Integer, Set<Integer>> frlToFed = mapRelationsFrlToFed(daqPartition);

		daq.setBus(bus);
		daq.setTtcPartitions(ttcPartitions);

		/* building FMM-FED */
		for (Entry<Integer, Set<Integer>> relation : fmmToFed.entrySet()) {
			FMM fmm = fmms.get(relation.getKey());
			for (int fedId : relation.getValue()) {
				FED fed = feds.get(fedId);
				fmm.getFeds().add(fed);
			}
		}

		/* building FRL-FED */
		for (Entry<Integer, Set<Integer>> relation : frlToFed.entrySet()) {
			FRL frl = frls.get(relation.getKey());
			for (int fedId : relation.getValue()) {
				FED fed = feds.get(fedId);
				// frl.getFeds().put(key, value) FIXME: what for this id?
			}
		}

	}

	/**
	 * Maps all BU objects
	 */
	private List<BU> mapBUs(DAQPartition dp, DAQ daq) {
		List<BU> result = new ArrayList<>();
		for (DPGenericHost host : dp.getGenericHosts()) {
			if (host.getRole().equals("BU")) {
				result.add(new BU(daq, host.getHostName()));
			} else {
				System.out.println("Host role: " + host.getRole());
			}
		}

		// TODO: what is the reason to sort the list?
		Collections.sort(result, new BU.HostNameComparator());
		return result;
	}

	/**
	 * Maps all TTCPartition objects
	 */
	private List<TTCPartition> mapTTCPartitions(DAQPartition daqPartition) {

		List<TTCPartition> result = new ArrayList<>();
		DAQPartitionSet daqPartitionSet = daqPartition.getDAQPartitionSet();
		for (rcms.utilities.hwcfg.eq.TTCPartition ttcPartition : daqPartitionSet.getEquipmentSet().getTTCPartitions()
				.values()) {
			String name = ttcPartition.getName();
			Boolean masked = false;// TODO: get masked info
			FMM fmm = null;// TODO: get FMM
			result.add(new TTCPartition());
		}
		return result;
	}

	/**
	 * Map all FMM objects. Note that objects are retrieved with identity
	 * information for further processing
	 * 
	 * @return map of all FMM identified by hardware object's hashCode
	 */
	private HashMap<Integer, FMM> mapFMMs(DAQPartition daqPartition) {

		HashMap<Integer, FMM> result = new HashMap<>();
		Map<Long, rcms.utilities.hwcfg.eq.FMM> fmms = daqPartition.getDAQPartitionSet().getEquipmentSet().getFMMs();
		for (rcms.utilities.hwcfg.eq.FMM hwfmm : fmms.values()) {

			// TODO: ttcPartition; fmmApplication;geoslot; url;
			FMM fmm = new FMM();
			result.put(hwfmm.hashCode(), fmm);
		}

		logger.info("Fmms retrieved: " + result.size());

		return result;
	}

	/**
	 * Map all FED objects. Note that objects are retrieved with identity
	 * information for further processing
	 * 
	 * @return map of all FED identified by hardware object's hashCode
	 */
	private HashMap<Integer, FED> mapFEDs(DAQPartition daqPartition) {

		HashMap<Integer, FED> result = new HashMap<>();
		Map<Long, rcms.utilities.hwcfg.eq.FED> feds = daqPartition.getDAQPartitionSet().getEquipmentSet().getFEDs();

		for (rcms.utilities.hwcfg.eq.FED hwfed : feds.values()) {

			Host a = hwfed.getFEDHost();
			String name = "-";
			if (a != null) {
				name = a.getName();
			}

			logger.info("fed host '" + a + "', fed host name '" + name + "'");
			logger.debug("debug");

			// TODO: frl frlIO fmm fmmIO srcIdExpected;
			FED fed = new FED();
			result.put(hwfed.hashCode(), fed);

		}

		logger.info("Feds retrieved: " + result.size());

		return result;
	}

	/**
	 * Map all FRL objects. Note that objects are retrieved with identity
	 * information for further processing
	 * 
	 * @return map of all FRL identified by hardware object's hashCode
	 */
	private HashMap<Integer, FRL> mapFRLs(DAQPartition daqPartition) {

		HashMap<Integer, FRL> result = new HashMap<>();
		Map<Long, rcms.utilities.hwcfg.eq.FRL> frls = daqPartition.getDAQPartitionSet().getEquipmentSet().getFRLs();
		for (rcms.utilities.hwcfg.eq.FRL hwfrl : frls.values()) {

			// TODO: SubFEDBuilder subFedbuilder, int geoSlot, String type
			FRL frl = new FRL();
			result.put(hwfrl.hashCode(), frl);
		}
		logger.info("FRLs retrieved: " + result.size());

		return result;
	}

	/**
	 * Map all FRLPc objects. Note that objects are retrieved with identity
	 * information for further processing
	 * 
	 * @return map of all FRLPc identified by hardware object's hashCode
	 */
	private HashMap<Integer, FRLPc> mapFrlPc(DAQPartition daqPartition) {

		HashMap<Integer, FRLPc> result = new HashMap<>();
		Map<Long, rcms.utilities.hwcfg.eq.FED> fmms = daqPartition.getDAQPartitionSet().getEquipmentSet().getFEDs();

		// TODO: retrieve FRLPc
		return result;
	}

	/**
	 * Retrieve FMM-FED relations
	 * 
	 * @return map representing FMM-FED one to many relation
	 */
	private Map<Integer, Set<Integer>> mapRelationsFmmToFed(DAQPartition daqPartition) {

		Map<Integer, Set<Integer>> result = new HashMap<>();
		Map<Long, rcms.utilities.hwcfg.eq.FMM> fmms = daqPartition.getDAQPartitionSet().getEquipmentSet().getFMMs();

		for (rcms.utilities.hwcfg.eq.FMM hwfmm : fmms.values()) {

			HashSet<Integer> children = new HashSet<>();
			result.put(hwfmm.hashCode(), children);

			for (rcms.utilities.hwcfg.eq.FED hwfed : hwfmm.getFEDs().values()) {
				children.add(hwfed.hashCode());
			}
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
		Map<Long, rcms.utilities.hwcfg.eq.FRL> frls = daqPartition.getDAQPartitionSet().getEquipmentSet().getFRLs();

		for (rcms.utilities.hwcfg.eq.FRL hwfrl : frls.values()) {

			HashSet<Integer> children = new HashSet<>();
			result.put(hwfrl.hashCode(), children);

			for (rcms.utilities.hwcfg.eq.FED hwfed : hwfrl.getFEDs().values()) {
				children.add(hwfed.hashCode());
			}
		}
		return result;
	}

	/** Get DAQ structure root object */
	public DAQ getDaq() {
		return daq;
	}

}
