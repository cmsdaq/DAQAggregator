package rcms.utilities.daqaggregator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.data.FMM;
import rcms.utilities.daqaggregator.data.FMMApplication;
import rcms.utilities.daqaggregator.data.FRL;
import rcms.utilities.daqaggregator.data.FRLPc;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.data.SubFEDBuilder;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.hwcfg.dp.DAQPartition;

/**
 * This class performs mapping of hardware objects' relations into {@link DAQ}
 * objects'relations
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class RelationMapper implements Serializable {

	private final ObjectMapper objectMapper;

	public Map<Integer, Integer> subFedBuilderToFrlPc;
	public Map<Integer, Integer> subFedBuilderToTTCP;
	public Map<Integer, Integer> subFMMToTTCP;
	public Map<Integer, Integer> ruToFedBuilder;
	public Map<Integer, Set<Integer>> fedBuilderToSubFedBuilder;
	public Map<Integer, Set<Integer>> subFedBuilderToFrl;
	public Map<Integer, Set<Integer>> fmmToFed;
	public Map<Integer, Set<Integer>> frlToFed;
	public Map<Integer, Set<Integer>> fmmApplicationToFmm;

	public RelationMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	private void fetchRelations(DAQPartition daqPartition) {
		fmmToFed = mapRelationsFmmToFed(daqPartition);
		frlToFed = mapRelationsFrlToFed(daqPartition);
		fmmApplicationToFmm = mapRelationsFmmApplicationToFmm(daqPartition);
		subFMMToTTCP = mapRelationsFmmToTTCP(daqPartition);
		ruToFedBuilder = mapRelationsRuToFedBuilder(daqPartition);
	}

	private void buildRelations() {
		objectMapper.daq.setBus(new ArrayList<>(objectMapper.bus.values()));
		objectMapper.daq.setTtcPartitions(new ArrayList<TTCPartition>(objectMapper.ttcPartitions.values()));
		objectMapper.daq.setFrlPcs(new ArrayList<FRLPc>(objectMapper.frlPcs.values()));
		objectMapper.daq.setFmmApplications(new ArrayList<FMMApplication>(objectMapper.fmmApplications.values()));
		objectMapper.daq.getFedBuilders().addAll(objectMapper.fedBuilders.values());

		/* building FMM-FED */
		for (Entry<Integer, Set<Integer>> relation : fmmToFed.entrySet()) {
			FMM fmm = objectMapper.fmms.get(relation.getKey());
			for (int fedId : relation.getValue()) {
				FED fed = objectMapper.feds.get(fedId);
				fmm.getFeds().add(fed);
				fed.setFmm(fmm);
			}
		}

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
		for (Entry<Integer, Integer> relation : subFMMToTTCP.entrySet()) {
			FMM fmm = objectMapper.fmms.get(relation.getKey());
			TTCPartition ttcPartition = objectMapper.ttcPartitions.get(relation.getValue());
			fmm.setTtcPartition(ttcPartition);
			ttcPartition.setFmm(fmm);
		}

		/* building RU - FEDBuilder */
		for (Entry<Integer, Integer> relation : ruToFedBuilder.entrySet()) {
			RU ru = objectMapper.rus.get(relation.getKey());
			FEDBuilder fedBuilder = objectMapper.fedBuilders.get(relation.getValue());
			ru.setFedBuilder(fedBuilder);
			fedBuilder.setRu(ru);
		}

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
	 * Retrieve FMM-TTCP relations
	 * 
	 * @return map representing FMM-TTCP one to one relation
	 */
	private Map<Integer, Integer> mapRelationsFmmToTTCP(DAQPartition daqPartition) {

		Map<Integer, Integer> result = new HashMap<>();
		Map<Long, rcms.utilities.hwcfg.eq.FMM> fmms = daqPartition.getDAQPartitionSet().getEquipmentSet().getFMMs();

		for (rcms.utilities.hwcfg.eq.FMM hwfmm : fmms.values()) {

			for (rcms.utilities.hwcfg.eq.FED hwfed : hwfmm.getFEDs().values()) {
				rcms.utilities.hwcfg.eq.TTCPartition hwttcPartition = hwfed.getTTCPartition();
				result.put(hwfmm.hashCode(), hwttcPartition.hashCode());
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

	/**
	 * Retrieve FMMApplication-FMM relations
	 * 
	 * @return map representing FMMApplication-FMM one to many relation
	 */
	private Map<Integer, Set<Integer>> mapRelationsFmmApplicationToFmm(DAQPartition daqPartition) {

		Map<Integer, Set<Integer>> result = new HashMap<>();

		Map<Long, rcms.utilities.hwcfg.eq.FMM> fmms = daqPartition.getDAQPartitionSet().getEquipmentSet().getFMMs();
		for (rcms.utilities.hwcfg.eq.FMM hwfmm : fmms.values()) {
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

}
