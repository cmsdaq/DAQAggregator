package rcms.utilities.daqaggregator.mappers;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubFEDBuilder;
import rcms.utilities.hwcfg.HardwareConfigurationException;
import rcms.utilities.hwcfg.dp.DAQPartition;
import rcms.utilities.hwcfg.fb.FBI;

/**
 * This class is responsible for mapping data from hardware database to daq
 * structure. For better clarity and code maintainability the process is
 * performed in 2 stages:
 * 
 * <ul>
 * <li>Map hardware objects to DAQ structure objects - performed by
 * {@link ObjectMapper}</li>
 * <li>Map objects' relations - performed by {@link RelationMapper}</li>
 * </ul>
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class MappingManager implements Serializable {

	private static final Logger logger = Logger.getLogger(MappingManager.class);

	private final ObjectMapper objectMapper;

	private final RelationMapper relationMapper;

	private final transient DAQPartition daqPartition;

	/**
	 * 
	 * @param daqPartition
	 *            object representing hardware configuration
	 */
	public MappingManager(DAQPartition daqPartition) {
		objectMapper = new ObjectMapper();
		relationMapper = new RelationMapper(objectMapper);
		this.daqPartition = daqPartition;
	}

	/**
	 * Maps the structure of monitored data retrieved from hardware database to
	 * {@link DAQ} structure
	 * 
	 * @return DAQ structure
	 */
	public DAQ map() {

		// this needs to be refactored
		relationMapper.fedBuilderToSubFedBuilder = new HashMap<>();
		relationMapper.subFedBuilderToFrl = new HashMap<>();
		relationMapper.subFedBuilderToFrlPc = new HashMap<>();
		relationMapper.subFedBuilderToTTCP = new HashMap<>();

		objectMapper.subFedBuilders = mapSubFEDBuilders(daqPartition, relationMapper.fedBuilderToSubFedBuilder,
				relationMapper.subFedBuilderToFrl, relationMapper.subFedBuilderToFrlPc,
				relationMapper.subFedBuilderToTTCP);

		objectMapper.mapAllObjects(daqPartition);
		relationMapper.mapAllRelations(daqPartition);

		return objectMapper.daq;
	}

	/**
	 * This function maps subFedBuilders and all relations to subFedBuilders.
	 * Needs refactoring (ObjectMapper & RelationMapper).
	 * 
	 * @return
	 */
	private Map<Integer, SubFEDBuilder> mapSubFEDBuilders(DAQPartition daqPartition,
			Map<Integer, Set<Integer>> fedBuilderToSubFedBuilder, Map<Integer, Set<Integer>> subFedBuilderToFrl,
			Map<Integer, Integer> subFedBuilderToFrlPc, Map<Integer, Integer> subFedBuilderToTTCP) {

		Map<Integer, SubFEDBuilder> subFedBuilders = new HashMap<>();

		/* loop over fed builders */
		for (rcms.utilities.hwcfg.fb.FEDBuilder fb : objectMapper.getHardwareFedBuilders(daqPartition)) {

			Map<String, Set<String>> ttcPartitionToFrlPCs = new HashMap<String, Set<String>>();

			// loop over fedbuilder inputs of given fedbuilder
			for (FBI fbi : fb.getFBIs().values()) {

				rcms.utilities.hwcfg.eq.FRL frl;

				try {

					frl = daqPartition.getDAQPartitionSet().getEquipmentSet().getFRL(fbi.getFRLId());

					// loop over feds of given fbi
					for (rcms.utilities.hwcfg.eq.FED fed : frl.getFEDs().values()) {

						String ttcpName = fed.getTTCPartition().getName();
						String frlPc = frl.getFRLCrate().getHostName();
						
						String fedBuilderName = fb.getName();
						
						String sfbMappingId = new String(String.valueOf(ttcpName)+"$"+String.valueOf(frlPc)+"$"+String.valueOf(fedBuilderName));
						int sfbId = sfbMappingId.hashCode(); //replaces use of empty object hashcode as key

						/* a new TTC partition in this fedbuilder */
						if (!ttcPartitionToFrlPCs.containsKey(ttcpName)) {
							ttcPartitionToFrlPCs.put(ttcpName, new HashSet<String>());
						}

						/*
						 * this TTC partition does not have this frlpc in this
						 * fedbuilder yet, create a new subfedbuilder for this
						 */
						if (!ttcPartitionToFrlPCs.get(ttcpName).contains(frlPc)) {
							ttcPartitionToFrlPCs.get(ttcpName).add(frlPc);
							SubFEDBuilder subFedBuilder = new SubFEDBuilder();
							//int id = subFedBuilder.hashCode()+(new java.util.Random()).nextInt(1000);
							
							subFedBuilders.put(sfbId, subFedBuilder);

							/* FEDBuilder - SubFEDBuilder */
							if (!fedBuilderToSubFedBuilder.containsKey(fb.hashCode())) {
								fedBuilderToSubFedBuilder.put(fb.hashCode(), new HashSet<Integer>());
							}
							fedBuilderToSubFedBuilder.get(fb.hashCode()).add(sfbId);

							/* SubFEDBuilder - FRL */
							if (!subFedBuilderToFrl.containsKey(sfbId)) {
								subFedBuilderToFrl.put(sfbId, new HashSet<Integer>());
							}
							
							/* SubFedBuilder - TTCPartition */
							subFedBuilderToTTCP.put(sfbId, fed.getTTCPartition().hashCode());

							/* SubFedBuilder - FRLPc */
							subFedBuilderToFrlPc.put(sfbId, frlPc.hashCode());

						}
						
						/*If no new subFEDBuilder is created, attach this frl to the one existing
						 */
						subFedBuilderToFrl.get(sfbId).add(frl.hashCode());
						
						
					}

				} catch (HardwareConfigurationException e) {
					e.printStackTrace();
				}
			}
		}
		logger.debug("Sub FED builders retrieved: " + subFedBuilders.size());

		return subFedBuilders;
	}

	public DAQPartition getDaqPartition() {
		return daqPartition;
	}

	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public RelationMapper getRelationMapper() {
		return relationMapper;
	}

}
