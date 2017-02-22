package rcms.utilities.daqaggregator.mappers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.data.FMM;
import rcms.utilities.daqaggregator.data.FMMApplication;
import rcms.utilities.daqaggregator.data.FMMInfo;
import rcms.utilities.daqaggregator.data.FRL;
import rcms.utilities.daqaggregator.data.FRLPc;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.data.SubFEDBuilder;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqaggregator.datasource.TCDSFMInfoRetriever;
import rcms.utilities.hwcfg.HardwareConfigurationException;
import rcms.utilities.hwcfg.dp.DAQPartition;
import rcms.utilities.hwcfg.dp.DAQPartitionStructureExtractor.HalfFMM;
import rcms.utilities.hwcfg.eq.FMMFMMLink;
import rcms.utilities.hwcfg.eq.FMMTriggerLink;
import rcms.utilities.hwcfg.eq.TCDSPartitionManager;
import rcms.utilities.hwcfg.eq.TCDSiCI;
import rcms.utilities.hwcfg.eq.Trigger;
import rcms.utilities.hwcfg.fb.FBI;

/**
 * This class performs mapping of hardware objects' relations into {@link DAQ}
 * objects'relations
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class RelationMapper implements Serializable {

	private final static Logger logger = Logger.getLogger(RelationMapper.class);

	private final transient TCDSFMInfoRetriever tcdsFmInfoRetriever;

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
	public Map<Integer, Set<Integer>> pseudoFedsToMainFeds;

	public RelationMapper(ObjectMapper objectMapper, TCDSFMInfoRetriever tcdsFmInfoRetriever) {
		this.objectMapper = objectMapper;
		this.tcdsFmInfoRetriever = tcdsFmInfoRetriever;
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
		pseudoFedsToMainFeds = mapRelationsPseudoFedsToMainFeds(daqPartition);
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
		/* building FMM - TTCP (only builds relation to topFMM) */ 
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
		for (Entry<Integer, Set<Integer>> relation : frlPcToFrl.entrySet()) {
			FRLPc frlPc = objectMapper.frlPcs.get(relation.getKey());
			for (int frlId : relation.getValue()) {
				FRL frl = objectMapper.frls.get(frlId);
				frlPc.getFrls().add(frl);
				frl.setFrlPc(frlPc);
			}
		}

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

		/* building FEDs - pseudoFEDs */
		for (Entry<Integer, FED> fedEntry : objectMapper.fedsByExpectedId.entrySet()){
			//case this fedEntry is a pseudofed, exposed through another fed (mainFed/parent)
			if (pseudoFedsToMainFeds.containsKey(fedEntry.getKey())){
				for (Integer mainFedSrcId : pseudoFedsToMainFeds.get(fedEntry.getKey())){
					objectMapper.fedsByExpectedId.get(mainFedSrcId).getDependentFeds().add(fedEntry.getValue());
				}
			}
		}

		/* building SubFEDBuilder - FED (only for pseudofeds) */
		for (Entry<Integer, Set<Integer>> relation : fedBuilderToSubFedBuilder.entrySet()) {
			FEDBuilder fedBuilder = objectMapper.fedBuilders.get(relation.getKey());
			for (int subFedBuilderId : relation.getValue()) {
				SubFEDBuilder subFedBuilder = objectMapper.subFedBuilders.get(subFedBuilderId);
				List<FRL> frls = subFedBuilder.getFrls();

				/*Multiple references to the same pseudofed may be found across multiple feds,
				 * but should be processed only once in this fedbuilder-subfedbuilder context*/
				Set<Integer> encounteredPseudofeds = new HashSet<Integer>();

				for (FRL frl : frls){
					Map<Integer, FED> feds = frl.getFeds();

					for (FED fed : feds.values()){
						//loop over dependent feds, if available
						for (FED pseudofed : fed.getDependentFeds()){

							if (encounteredPseudofeds.contains(pseudofed.getSrcIdExpected())){
								continue;
							}else{
								encounteredPseudofeds.add(pseudofed.getSrcIdExpected());
							}
							if (pseudofed.getTtcp().getName().equals(fed.getTtcp().getName())){
								objectMapper.subFedBuilders.get(subFedBuilderId).getFeds().add(pseudofed);
							} else if (isExistTtcpCompatibleSubfedbuilder(fedBuilder, pseudofed.getTtcp())){
								int sfbId = getTtcpCompatibleSubfedbuilderId(fedBuilder, pseudofed.getTtcp());
								objectMapper.subFedBuilders.get(sfbId).getFeds().add(pseudofed);
							}else{
								//no frl in this case
								String frlPc = "nullFrlPc";
								String sfbMappingId = new String(String.valueOf(pseudofed.getTtcp().getName())+"$"+String.valueOf(frlPc)+"$"+String.valueOf(fedBuilder.getName()));
								int sfbId = sfbMappingId.hashCode();

								SubFEDBuilder newSubFedBuilder = new SubFEDBuilder();

								newSubFedBuilder.getFeds().add(pseudofed);
								newSubFedBuilder.setFedBuilder(fedBuilder);
								newSubFedBuilder.setTtcPartition(pseudofed.getTtcp());

								fedBuilder.getSubFedbuilders().add(newSubFedBuilder);

								objectMapper.subFedBuilders.put(sfbId, newSubFedBuilder);
							}
						}
					}
				}
			}
		}


	}


	private int getTtcpCompatibleSubfedbuilderId(FEDBuilder fedBuilder, TTCPartition ttcp) {
		for (SubFEDBuilder sfb : fedBuilder.getSubFedbuilders()){
			if (sfb.getTtcPartition().getName().equals(ttcp.getName())){
				String frlPc = (sfb.getFrlPc()==null) ? "nullFrlPc" : sfb.getFrlPc().getHostname();
				String sfbMappingId = new String(String.valueOf(ttcp.getName())+"$"+String.valueOf(frlPc)+"$"+String.valueOf(fedBuilder.getName()));
				int sfbId = sfbMappingId.hashCode();
				return sfbId;
			}
		}
		return -1; //if called in pair wth isExistTtcpCompatibleSubfedbuilder(args), this should never be reached
	}

	private boolean isExistTtcpCompatibleSubfedbuilder(FEDBuilder fedBuilder, TTCPartition ttcp) {
		for (SubFEDBuilder sfb : fedBuilder.getSubFedbuilders()){
			if (sfb.getTtcPartition().getName().equals(ttcp.getName())){
				return true;
			}
		}
		return false;
	}

	public void mapAllRelations(DAQPartition daqPartition) {

		fetchRelations(daqPartition);
		buildRelations();

	}

	/**
	 * Retrieve FED-mainFED relations
	 * 
	 * @return map representing pseudoFED-mainFED one to many relation
	 */
	private Map<Integer, Set<Integer>> mapRelationsPseudoFedsToMainFeds(DAQPartition daqPartition) {
		Map<Integer, Set<Integer>> result = new HashMap<>();
		for (rcms.utilities.hwcfg.eq.FED hwfed : objectMapper.getHardwareFeds(daqPartition)) {
			if (hwfed.getDependentFEDs() == null || hwfed.getDependentFEDs().size() == 0)
				continue;
			else {
				for (rcms.utilities.hwcfg.eq.FED dependent : hwfed.getDependentFEDs()) {

					//stores links between pseudofeds and their parent feds to be used in relation mapping
					int expectedSrcId = dependent.getSrcId();
					if (!result.containsKey(expectedSrcId)){
						Set<Integer> mainFeds = new HashSet<Integer>();
						mainFeds.add(hwfed.getSrcId());
						result.put(expectedSrcId, mainFeds);
					}else{
						result.get(expectedSrcId).add(hwfed.getSrcId());
					}
				}
			}
		}
		return result;
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
				/*
				 * Only add links to FEDs
				 * which have actually
				 * been included in the model
				 * (as an FMM of a FED may point
				 * to other FEDs as well)
				 */
				if (objectMapper.feds.containsKey(hwfed.hashCode()))
					children.add(hwfed.hashCode());
			}
		}
		return result;
	}


	private FMMInfo getFMMInfo(DAQPartition dp, String ttcpName) {

		FMMInfo fmmInfo = new FMMInfo(); //creates info wrapper in all cases


		if (ttcpName.toLowerCase().startsWith("cpm")||
				ttcpName.toLowerCase().startsWith("lpm")
				||ttcpName.toLowerCase().startsWith("dvcpm")||
				ttcpName.toLowerCase().startsWith("dvlpm")) {

			fmmInfo.setNullCause("-");
			return fmmInfo;
		}

		String triggerName = "GTPe"; //default value which will cause a noTRG label

		if(tcdsFmInfoRetriever.isInfoAvailable()){
			String pmUrl = tcdsFmInfoRetriever.getTcdsfm_pmContext().toLowerCase();
			pmUrl = pmUrl.split("//|.cms")[1];
			
			if (!pmUrl.endsWith("-pri")){
				//hack to work before the suffix integration in tcdsfm
				pmUrl = pmUrl + "-pri";
			}
			
			int pmLid = tcdsFmInfoRetriever.getTcdsfm_pmLid();
			String pmService = tcdsFmInfoRetriever.getTcdsfm_pmService().toLowerCase();


			for (Entry<Long, Trigger> eTrig: dp.getDAQPartitionSet().getEquipmentSet().getTriggers().entrySet()){

				for (Entry<Integer, TCDSPartitionManager> ePm: eTrig.getValue().getPMs().entrySet()){

					String hwcfg_PmHostname = ePm.getValue().getHostName().toLowerCase();
					hwcfg_PmHostname = hwcfg_PmHostname.split(".cms")[0];
					String hwcfg_pmService = ePm.getValue().getServiceName().toLowerCase();

					if (pmUrl.equalsIgnoreCase(hwcfg_PmHostname)&&pmService.equalsIgnoreCase(hwcfg_pmService)){

						triggerName = eTrig.getValue().getName();
						logger.debug("Resolved trigger: "+triggerName+" for ttcp: "+ttcpName);
					}
				}
			}


			Trigger trigger;
			try {
				trigger = dp.getDAQPartitionSet().getEquipmentSet().getTriggerByName(triggerName);
			} catch (HardwareConfigurationException e) {
				logger.warn("Could not resolve trigger: "+triggerName+" in hardware database.");
				fmmInfo.setNullCause("noTRG");

				return fmmInfo;
			}

			// Find the ICI
			TCDSiCI ici;
			try {
				ici = trigger.getICIByTTCPName(ttcpName);
			} catch (HardwareConfigurationException e) {
				logger.warn("Could not find ici for TTC partition: "+ttcpName+" in hardware database.");

				fmmInfo.setNullCause("noICI");


				return fmmInfo;

				// for LPM/LTC partitions it is normal that some of them will have
				// no fmm. They are unused in the CDAQ config. You will find no FEDs
				// in these partitions =.
				// FIXME: encode in the data model, that this partition has no FMM
				// (and also no PI and no CI).
			}

			rcms.utilities.hwcfg.eq.FMM pi = null;
			// Find the PI connected to this partition
			for (FMMTriggerLink ftl : dp.getDAQPartitionSet().getEquipmentSet().getFMMTriggerLinks())
				if (ftl.getTriggerId() == trigger.getId() && ftl.getLPMNr() == ici.getPMNr()
				&& ftl.getiCINr() == ici.getICINr()) {
					pi = dp.getDAQPartitionSet().getEquipmentSet().getFMMs().get(ftl.getFMMId());
				}

			if (pi == null) {
				fmmInfo.setNullCause("noPI");

				return fmmInfo;
			}

			// Find the FMM connected to the PI
			for (FMMFMMLink fmmfmm : dp.getDAQPartitionSet().getEquipmentSet().getFMMFMMLinks()) {
				if (fmmfmm.getTargetFMMId() == pi.getId()) {

					//rcms.utilities.hwcfg.eq.FMM fmm = dp.getDAQPartitionSet().getEquipmentSet().getFMMs().get(fmmfmm.getSourceFMMId());

					fmmInfo.setAb((fmmfmm.getSourceFMMIO() == 20 || fmmfmm.getSourceFMMIO() == 21) ? "a" : "b");

					fmmInfo.setPMNr(ici.getPMNr());
					fmmInfo.setICINr(ici.getICINr());

					//do not set nullCause if fmm connected is found!

					return fmmInfo;
				}
			}

			fmmInfo.setAb("");
			fmmInfo.setPMNr(ici.getPMNr());
			fmmInfo.setICINr(ici.getICINr());

			return fmmInfo;

		}else{
			fmmInfo.setNullCause("noTRG");
			return fmmInfo;
		}
	}

	/**
	 * Retrieve FMM-TTCP relations
	 * 
	 * @return map representing FMM-TTCP one to one relation
	 */
	private Map<Integer, Integer> mapRelationsFmmToTTCP(DAQPartition daqPartition) {

		Map<Integer, Integer> result = new HashMap<>();

		Set<rcms.utilities.hwcfg.eq.TTCPartition> hwTtcPartitions = new HashSet<rcms.utilities.hwcfg.eq.TTCPartition>();

		rcms.utilities.hwcfg.dp.DAQPartitionStructureExtractor dpStructExt = new rcms.utilities.hwcfg.dp.DAQPartitionStructureExtractor(daqPartition);
		Map<String, rcms.utilities.hwcfg.dp.DAQPartitionStructureExtractor.HalfFMM> ttcpToHalFmm;
		try {
			ttcpToHalFmm = dpStructExt.getTTCPNameHalfFMMMapping();

			//fill ttcp partitions
			for (rcms.utilities.hwcfg.eq.FED hwfed : objectMapper.getHardwareFeds(daqPartition)) {

				hwTtcPartitions.add(hwfed.getTTCPartition());

			}

			for (rcms.utilities.hwcfg.eq.TTCPartition hwTtcPartition : hwTtcPartitions){
				if(ttcpToHalFmm.containsKey(hwTtcPartition.getName())){
					rcms.utilities.hwcfg.eq.FMM hwfmm = ttcpToHalFmm.get(hwTtcPartition.getName()).fmm;

					if (hwfmm != null)
						result.put(hwfmm.hashCode(), hwTtcPartition.hashCode());

				}else{
					logger.debug("Could not find ttcp: "+hwTtcPartition.getName()+" in ttcp to halffmm mapping. There will be no top FMM for this ttcp.");
				}

				//Discover trigger and ICI for all partitions, including those without an FMM
				FMMInfo fmmInfo = getFMMInfo(daqPartition, hwTtcPartition.getName());
				objectMapper.ttcPartitions.get(hwTtcPartition.hashCode()).setTopFMMInfo(fmmInfo);
			}

		} catch (HardwareConfigurationException e) {
			logger.warn("Could not fetch ttcp to halffmm mapping from hardware database");
			//e.printStackTrace();
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


		/* loop over fed builders */
		for (rcms.utilities.hwcfg.fb.FEDBuilder hwfb : objectMapper.getHardwareFedBuilders(daqPartition)) {

			// loop over fedbuilder inputs of given fedbuilder
			for (FBI hwfbi : hwfb.getFBIs().values()) {
				try {
					rcms.utilities.hwcfg.eq.FRL hwfrl;
					hwfrl = daqPartition.getDAQPartitionSet().getEquipmentSet().getFRL(hwfbi.getFRLId());

					HashSet<Integer> children = new HashSet<>();
					result.put(hwfrl.hashCode(), children);

					for (Integer frlIO : hwfrl.getFEDs().keySet() ) {
						if ( hwfbi.getFRLInputEnableMask() == null || 
								((hwfbi.getFRLInputEnableMask() & (1<<frlIO)) == (1<<frlIO) ) ) {							
							rcms.utilities.hwcfg.eq.FED hwfed = hwfrl.getFEDs().get(frlIO);

							children.add(hwfed.hashCode());

						}
					}

				} catch (HardwareConfigurationException e) {
					logger.warn("Cannot get FRL by id, source error: " + e.getMessage());
				}
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
			if (!result.containsKey(fmmPc.hashCode())) {
				result.put(fmmPc.hashCode(), new HashSet<Integer>());
			}
			result.get(fmmPc.hashCode()).add(hwfmm.hashCode());
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

		Set<rcms.utilities.hwcfg.eq.SubSystem> subsystems = new HashSet<rcms.utilities.hwcfg.eq.SubSystem>();


		for (rcms.utilities.hwcfg.eq.FED hwfed : objectMapper.getHardwareFeds(daqPartition)) {

			rcms.utilities.hwcfg.eq.TTCPartition hwttcPartition = hwfed.getTTCPartition();

			rcms.utilities.hwcfg.eq.SubSystem hwsubsystem = hwttcPartition.getSubSystem();

			subsystems.add(hwsubsystem);

		}

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

		for (rcms.utilities.hwcfg.eq.FED hwfed : objectMapper.getHardwareFeds(daqPartition)) {

			rcms.utilities.hwcfg.eq.TTCPartition hwttcPartition = hwfed.getTTCPartition();

			if (result.containsKey(hwttcPartition.hashCode())){
				result.get(hwttcPartition.hashCode()).add(hwfed.hashCode());
			}else{
				HashSet<Integer> children = new HashSet<>();
				children.add(hwfed.hashCode());
				result.put(hwttcPartition.hashCode(), children);
			}
		}

		return result;
	}
}
