package rcms.utilities.daqaggregator.datasource;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.GlobalTTSState;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqaggregator.mappers.FlashlistUpdatable;
import rcms.utilities.daqaggregator.mappers.MappingManager;
import rcms.utilities.daqaggregator.mappers.MappingReporter;
import rcms.utilities.daqaggregator.mappers.helper.ContextHelper;
import rcms.utilities.daqaggregator.mappers.helper.FEDEnableMaskParser;
import rcms.utilities.daqaggregator.mappers.helper.FMMGeoMatcher;
import rcms.utilities.daqaggregator.mappers.helper.FRLGeoFinder;
import rcms.utilities.daqaggregator.mappers.helper.FedFromFerolInputStreamGeoFinder;
import rcms.utilities.daqaggregator.mappers.helper.FedInFmmGeoFinder;
import rcms.utilities.daqaggregator.mappers.helper.FedInFrlGeoFinder;
import rcms.utilities.daqaggregator.mappers.helper.Matcher;
import rcms.utilities.daqaggregator.mappers.helper.TCDSFlashlistHelpers;
import rcms.utilities.daqaggregator.mappers.helper.TTCPartitionGeoFinder;

public class FlashlistDispatcher {

	final String INSTANCE = "instance";

	// Value to filter out values from TCDS flashlists
	// This will only work for CDAQ. Need to figure out how to know what
	// PM (CPM or LPM) is used for a given run, instead of hardcoding
	//private String serviceField;


	private static final Logger logger = Logger.getLogger(Flashlist.class);

	/**
	 * Dispatch flashlist rows to appropriate objects from DAQ structure. Note
	 * that a flashlist must be already initialized, for initialization see
	 * {@link FlashlistManager}
	 * 
	 * @param flashlist
	 * @param mappingManager
	 */
	public void dispatch(Flashlist flashlist, MappingManager mappingManager) {

		/**TCDS service name*/
		String tcds_serviceField = mappingManager.getTcdsFmInfoRetriever().getTcdsfm_pmService();
		String tcds_url = mappingManager.getTcdsFmInfoRetriever().getTcdsfm_pmContext();


		logger.debug("Received "+tcds_serviceField+" TCDS PM service name");

		if (flashlist.isUnknownAtLAS()){
			logger.debug("Flashlist dispatcher received and will ignore "+flashlist.getName()+" because it was not successfully downloaded from LAS");
			return;
		}


		FlashlistType type = flashlist.getFlashlistType();

		switch (type) {
		case RU:
			dispatchRowsByHostname(flashlist, mappingManager.getObjectMapper().rusByHostname, "context");
			dispatchRowsByFedIdsWithErrors(flashlist, mappingManager.getObjectMapper().fedsByExpectedId);
			break;
		case BU:
			dispatchRowsByHostname(flashlist, mappingManager.getObjectMapper().busByHostname, "context");
			break;
		case FEROL_INPUT_STREAM:
			dispatchRowsByGeo(flashlist, mappingManager.getObjectMapper().fedsById.values(),
					new FedFromFerolInputStreamGeoFinder("streamNumber"));
			break;
		case FMM_INPUT:
			dispatchRowsByGeo(flashlist, mappingManager.getObjectMapper().fedsById.values(), new FedInFmmGeoFinder());

			break;
		case FEROL_STATUS:
			dispatchRowsByGeo(flashlist, mappingManager.getObjectMapper().frls.values(), new FRLGeoFinder());
			break;
		case EVM:
			if (flashlist.getRowsNode().isArray() && flashlist.getRowsNode().size() > 0) {

				int runNumber = flashlist.getRowsNode().get(0).get("runNumber").asInt();
				mappingManager.getObjectMapper().daq.setRunNumber(runNumber);
				logger.debug("Successfully got runnumber: " + runNumber);

				for (RU ru : mappingManager.getObjectMapper().rus.values()) {
					if (ru.isEVM())
						ru.updateFromFlashlist(flashlist.getFlashlistType(), flashlist.getRowsNode().get(0));
					// additional check hostname

				}

			} else {
				logger.error("run-number problem while dispatching EVM flashlist, here is EVM flashlist row: "
						+ flashlist.getRowsNode());
			}


			break;
		case LEVEL_ZERO_FM_STATIC:
			if (flashlist.getRowsNode().isArray() && flashlist.getRowsNode().size() > 0) {
				String fedEnMask = "";
				//concatenates fed_enabled_mask from all rows
				for (int i = 0; i < flashlist.getRowsNode().size() ; i++){
					fedEnMask += flashlist.getRowsNode().get(i).get("FED_ENABLE_MASK").asText();
				}
				
				FEDEnableMaskParser parser = new FEDEnableMaskParser(fedEnMask);
				Map<Integer, String> maskedFlagsByFed = parser.getFedByExpectedIdToMaskingFlags();

				int notFound = 0;
				int total = 0;

				for (Entry<Integer, FED> fedEntry : mappingManager.getObjectMapper().fedsByExpectedId.entrySet()) {
					total++;
					if (maskedFlagsByFed.containsKey(fedEntry.getKey())) {
						String[] maskingFlags = maskedFlagsByFed.get(fedEntry.getKey()).split("-");

						if (fedEntry.getKey() == 890){
							System.out.println(maskingFlags[0]+" , "+maskingFlags[1]);
						}
						fedEntry.getValue().setFmmMasked(Boolean.parseBoolean(maskingFlags[0]));
						fedEntry.getValue().setFrlMasked(Boolean.parseBoolean(maskingFlags[1]));

					} else {
						notFound++;
					}
				}
				logger.debug("Could not find " + notFound + " out of " + total
						+ " FEDs in the FED_ENABLE_MASK and mask flags were not set");
				logger.debug("Successfully got FED_ENABLE_MASK info for " + (total - notFound) + " FEDs");
			} else {
				logger.error("FED_ENABLE_MASK problem " + flashlist.getRowsNode());
			}
			break;
		case JOB_CONTROL:
			// TODO: dispatch by context (in the future) (multiple context by
			// hostname)
			dispatchRowsByHostname(flashlist, mappingManager.getObjectMapper().frlPcByHostname, "hostname");
			dispatchRowsByHostname(flashlist, mappingManager.getObjectMapper().fmmApplicationByHostname, "hostname");
			dispatchRowsByHostname(flashlist, mappingManager.getObjectMapper().rusByHostname, "hostname");
			dispatchRowsByHostname(flashlist, mappingManager.getObjectMapper().busByHostname, "hostname");
			// TODO: should go to RU, BU
			break;

		case LEVEL_ZERO_FM_SUBSYS: // TODO: SID column
			for (JsonNode rowNode : flashlist.getRowsNode()) {

				String subsystemName = rowNode.get("SUBSYS").asText();

				if (subsystemName.equals("DAQ") && rowNode.get("FMURL").asText().contains("toppro")) {
					mappingManager.getObjectMapper().daq.updateFromFlashlist(flashlist.getFlashlistType(), rowNode);
				}

				if (mappingManager.getObjectMapper().subsystemByName.containsKey(subsystemName)) {
					mappingManager.getObjectMapper().subsystemByName.get(subsystemName)
					.updateFromFlashlist(flashlist.getFlashlistType(), rowNode);
				}

			}
			break;
		case LEVEL_ZERO_FM_DYNAMIC:
			for (JsonNode rowNode : flashlist.getRowsNode()) {
				if (rowNode.get("FMURL").asText().contains("toppro")) {
					mappingManager.getObjectMapper().daq.updateFromFlashlist(flashlist.getFlashlistType(), rowNode);
				}
			}
			break;
		case FEROL_CONFIGURATION:
			// TODO: future - use frlpc.context for mapping
			dispatchRowsByHostname(flashlist, mappingManager.getObjectMapper().frlPcByHostname, "context");
			dispatchRowsByGeo(flashlist, mappingManager.getObjectMapper().fedsById.values(),
					new FedInFrlGeoFinder("io"));
			break;
		case FRL_MONITORING:
			// TODO: future - use frlpc.context for mapping
			dispatchRowsByHostname(flashlist, mappingManager.getObjectMapper().frlPcByHostname, "context");
			dispatchRowsByGeo(flashlist, mappingManager.getObjectMapper().fedsById.values(),
					new FedInFrlGeoFinder("io"));
			break;
		case FMM_STATUS:
			dispatchRowsByGeo(flashlist, mappingManager.getObjectMapper().fmms.values(), new FMMGeoMatcher());
			dispatchRowsByGeo(flashlist, mappingManager.getObjectMapper().ttcPartitions.values(),
					new TTCPartitionGeoFinder());
			break;
		case TCDS_PM_TTS_CHANNEL:

			String typeField1 = "tts_ici";
			String typeField2 = "tts_apve";
			
			if (tcds_serviceField == null || tcds_url == null){
				return;
			}

			//stpi-indexed flashlist
			Map<String, Map<String, Map<Integer, Map<Integer, Map<String, String>>>>> stpiDataFromFlashlist = TCDSFlashlistHelpers
					.getTreeFromFlashlist(flashlist);


			//setting TTC partition states for types tts_ici and tts_apve (where applicable)

			// . iterate over all ttcpartitions and set tcds_pm_ttsState
			// according to the code in 'value' flash column
			// .. retrieve this value from map above, specifying pmNr, iciNr
			// info, which are already stored in the ttcpartitions, in field
			// topFMMInfo
			// .. decode the value from map using
			// rcms.utilities.daqaggregator.mappers.helper.TTSStateDecoder.decodeTCDSTTSState(int
			// tts_value)
			// .. set ttcpartition.tcds_pm_ttsState
			for (Entry<Integer, TTCPartition> ttcpEntry : mappingManager.getObjectMapper().ttcPartitions.entrySet()) {
				TTCPartition ttcp = ttcpEntry.getValue(); // ref to ttcp object

				/*this happens if the fmm was null but there is further information on the reason*/
				if (ttcp.getTopFMMInfo().getNullCause() != null) {
					// topFMM was null for this ttcp and ici/pi info were not
					// filled, therefore there are no keys to get tcds_tts_state
					// from flashlist data
					// in this case, the nullCause String field of associated
					// FMMInfo is not null and contains more information on why
					// this ttcp's topFMM was null

					ttcp.setTcds_pm_ttsState(ttcp.getTopFMMInfo().getNullCause());
					ttcp.setTcds_apv_pm_ttsState(ttcp.getTopFMMInfo().getNullCause());

					continue;
				}

				if (stpiDataFromFlashlist.containsKey(tcds_serviceField)  &&
						stpiDataFromFlashlist.get(tcds_serviceField).containsKey(typeField1) &&
						stpiDataFromFlashlist.get(tcds_serviceField).get(typeField1).containsKey( ttcp.getTopFMMInfo().getPMNr() ) && 
						stpiDataFromFlashlist.get(tcds_serviceField).get(typeField1).get( ttcp.getTopFMMInfo().getPMNr() ).containsKey( ttcp.getTopFMMInfo().getICINr() ) )  {

					int stateCode = Integer.parseInt(stpiDataFromFlashlist.get(tcds_serviceField).get(typeField1)
							.get(ttcp.getTopFMMInfo().getPMNr()).get(ttcp.getTopFMMInfo().getICINr()).get("value"));

					ttcp.setTcds_pm_ttsState(TCDSFlashlistHelpers.decodeTCDSTTSState(stateCode));
				}


				if (stpiDataFromFlashlist.containsKey(tcds_serviceField)  &&
						stpiDataFromFlashlist.get(tcds_serviceField).containsKey(typeField2) &&
						stpiDataFromFlashlist.get(tcds_serviceField).get(typeField2).containsKey( ttcp.getTopFMMInfo().getPMNr() ) && 
						stpiDataFromFlashlist.get(tcds_serviceField).get(typeField2).get( ttcp.getTopFMMInfo().getPMNr() ).containsKey( ttcp.getTopFMMInfo().getICINr() ) )  {



					String label = stpiDataFromFlashlist.get(tcds_serviceField).get(typeField2)
							.get(ttcp.getTopFMMInfo().getPMNr()).get(ttcp.getTopFMMInfo().getICINr()).get("label");

					if (label.equalsIgnoreCase("Unused")){
						ttcp.setTcds_apv_pm_ttsState("x");
					}else{
						int stateCode = Integer.parseInt(stpiDataFromFlashlist.get(tcds_serviceField).get(typeField2)
								.get(ttcp.getTopFMMInfo().getPMNr()).get(ttcp.getTopFMMInfo().getICINr()).get("value"));

						ttcp.setTcds_apv_pm_ttsState(TCDSFlashlistHelpers.decodeTCDSTTSState(stateCode));
					}

				}
			}


			//setting global TTS states for all other types detected in flashlist

			//.detect types other than tts_ici, tts_apve
			Set<String> types = new HashSet<String>();

			types.addAll(stpiDataFromFlashlist.get(tcds_serviceField).keySet());
			types.remove("tts_ici");
			types.remove("tts_apve");


			//.foreach type, decode state value and %B/%W value (if existing) and set corresponding value in model's daq
			GlobalTTSState globalTtsState;
			for (String typeName: types){
				logger.debug("Global TTS state detected for this service:"+typeName);

				globalTtsState = new GlobalTTSState();

				int stateCode = Integer.parseInt(stpiDataFromFlashlist.get(tcds_serviceField).get(typeName)
						.get(0).get(0).get("value"));
				globalTtsState.setState(TCDSFlashlistHelpers.decodeTCDSTTSState(stateCode));

				//percentage keys should be reviewed when the flashlist column name for these attributes is defined
				String busyKey = "outputFractionBusy";
				String warningKey = "outputFractionWarning";

				if (stpiDataFromFlashlist.get(tcds_serviceField).get(typeName).get(0).get(0).containsKey(busyKey)){
					globalTtsState.setPercentBusy(Float.parseFloat(stpiDataFromFlashlist.get(tcds_serviceField).get(typeName).get(0).get(0).get(busyKey)));
				}

				if (stpiDataFromFlashlist.get(tcds_serviceField).get(typeName).get(0).get(0).containsKey(warningKey)){
					globalTtsState.setPercentWarning(Float.parseFloat(stpiDataFromFlashlist.get(tcds_serviceField).get(typeName).get(0).get(0).get(warningKey)));
				}

				mappingManager.getObjectMapper().daq.getTcdsGlobalInfo().getGlobalTtsStates().put(typeName, globalTtsState);
			}

			mappingManager.getObjectMapper().daq.getTcdsGlobalInfo().setTcdsControllerServiceName(tcds_serviceField);
			mappingManager.getObjectMapper().daq.getTcdsGlobalInfo().setTcdsControllerContext(tcds_url);

			break;


		case TCDS_CPM_COUNTS:
			if (tcds_serviceField == null || tcds_url == null){
				return;
			}
			for (JsonNode rowNode : flashlist.getRowsNode()) {

				//get flashlist row corresponding to service
				if (rowNode.get("service").asText().equalsIgnoreCase(tcds_serviceField)){
					mappingManager.getObjectMapper().daq.getTcdsGlobalInfo().updateFromFlashlist(flashlist.getFlashlistType(), rowNode);
					break;
				}
			}

			break;

		case TCDS_CPM_DEADTIMES:
			if (tcds_serviceField == null || tcds_url == null){
				return;
			}
			for (JsonNode rowNode : flashlist.getRowsNode()) {

				//get flashlist row corresponding to service
				if (rowNode.get("service").asText().equalsIgnoreCase(tcds_serviceField)){
					mappingManager.getObjectMapper().daq.getTcdsGlobalInfo().updateFromFlashlist(flashlist.getFlashlistType(), rowNode);
					break;
				}
			}

			break;

		case TCDS_CPM_RATES:
			if (tcds_serviceField == null || tcds_url == null){
				return;
			}
			for (JsonNode rowNode : flashlist.getRowsNode()) {

				//get flashlist row corresponding to service
				if (rowNode.get("service").asText().equalsIgnoreCase(tcds_serviceField)){
					mappingManager.getObjectMapper().daq.getTcdsGlobalInfo().updateFromFlashlist(flashlist.getFlashlistType(), rowNode);
					break;
				}  
			}

			break;

		case TCDS_PM_ACTION_COUNTS:
			if (tcds_serviceField == null || tcds_url == null){
				return;
			}
			for (JsonNode rowNode : flashlist.getRowsNode()) {

				//get flashlist row corresponding to service
				if (rowNode.get("service").asText().equalsIgnoreCase(tcds_serviceField)){
					mappingManager.getObjectMapper().daq.getTcdsGlobalInfo().updateFromFlashlist(flashlist.getFlashlistType(), rowNode);
					break;
				}
			}

			break;
		default:
			break;
		}
	}

	private void printFlashListTypeInfo(Flashlist flashlist) {
		com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
		try {
			logger.debug("Flashlist schema:  " + om.writeValueAsString(flashlist.getDefinitionNode()));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void dispatchRowsByFedIdsWithErrors(Flashlist flashlist, Map<Integer, FED> fedsByExpectedId) {

		HashMap<FED, JsonNode> fedToFlashlistRow = new HashMap<>();

		for (JsonNode row : flashlist.getRowsNode()) {
			if (row.get("fedIdsWithErrors").isArray()) {
				for (JsonNode fedIdWithErrors : row.get("fedIdsWithErrors")) {
					int fedId = fedIdWithErrors.asInt();
					if (fedsByExpectedId.containsKey(fedId)) {
						FED fed = fedsByExpectedId.get(fedId);
						fedToFlashlistRow.put(fed, row);

					} else {
						logger.debug(
								"FED with problem indicated by flashlist RU.fedIdsWithErrors could not be found by id "
										+ fedId);
					}
				}
				for (JsonNode fedIdWithoutFragment : row.get("fedIdsWithoutFragments")) {
					int fedId = fedIdWithoutFragment.asInt();
					if (fedsByExpectedId.containsKey(fedId)) {
						FED fed = fedsByExpectedId.get(fedId);
						fedToFlashlistRow.put(fed, row);

					} else {
						logger.debug(
								"FED with problem indicated by flashlist RU.fedIdsWithoutFragments could not be found by id "
										+ fedId);
					}
				}

			}

		}
		logger.debug("There are " + fedToFlashlistRow.size() + " FEDs with problems (according to RU flashlist)");
		for (Entry<FED, JsonNode> entry : fedToFlashlistRow.entrySet()) {
			FED fed = entry.getKey();
			fed.updateFromFlashlist(flashlist.getFlashlistType(), entry.getValue());
		}

	}

	/**
	 * 
	 * Dispatch rows of a flashlist to appropriate objects using 2 elements geo
	 * matcher
	 *
	 * @param flashlist
	 *            flashlist to dispatch
	 * @param collection
	 *            objects to dispatch flashlist data to
	 * @param matcher
	 *            object responsible for matching row - object
	 */
	public <T extends FlashlistUpdatable> void dispatchRowsByGeo(Flashlist flashlist, Collection<T> collection,
			Matcher<T> matcher) {

		FlashlistType flashlistType = flashlist.getFlashlistType();

		/* Object T will receive row JsonNode */
		Map<T, JsonNode> dispatchMap = matcher.match(flashlist, collection);
		logger.debug("Elements matched by geolocation: " + dispatchMap.size() + "/" + collection.size());

		for (Entry<T, JsonNode> match : dispatchMap.entrySet()) {
			match.getKey().updateFromFlashlist(flashlistType, match.getValue());
		}

		int failed = matcher.getFailded();
		int all = matcher.getFailded() + matcher.getSuccessful();

		MappingReporter.get().increaseMissing(flashlistType.name(), failed);
		MappingReporter.get().increaseTotal(flashlistType.name(), all);

	}

	/**
	 * Dispatch rows of a flashlist to appropriate objects
	 * 
	 * @param flashlist
	 *            flashlit to dispatch
	 * @param objectsByHostname
	 *            map of objects by hostname
	 * @param flashlistKey
	 *            key to find flashlist column with hostname
	 */
	public <T extends FlashlistUpdatable> void dispatchRowsByHostname(Flashlist flashlist,
			Map<String, T> objectsByHostname, String flashlistKey) {

		logger.debug("Updating " + flashlist.getRowsNode().size() + " of " + flashlist.getFlashlistType() + " objects ("
				+ objectsByHostname.size() + " in the structure)");

		int found = 0;
		int failed = 0;

		for (JsonNode rowNode : flashlist.getRowsNode()) {
			String hostname = rowNode.get(flashlistKey).asText();
			hostname = ContextHelper.getHostnameFromContext(hostname);
			if (objectsByHostname.containsKey(hostname)) {
				T flashlistUpdatableObject = objectsByHostname.get(hostname);
				flashlistUpdatableObject.updateFromFlashlist(flashlist.getFlashlistType(), rowNode);
				found++;
			} else {
				logger.debug("Cannot find object " + hostname + " by name in " + objectsByHostname.keySet());
				failed++;
			}
		}

		// TODO: better report this warnings
		MappingReporter.get().increaseMissing(flashlist.getFlashlistType().name(), failed);
		MappingReporter.get().increaseTotal(flashlist.getFlashlistType().name(), failed + found);
	}

	/**
	 * 
	 * Dispatch rows of a flashlist to appropriate objects by instance id
	 * 
	 * @param flashlist
	 *            Flashlist object with data retrieved from LAS
	 * @param objectsById
	 *            objects to update
	 */
	public <T extends FlashlistUpdatable> void dispatchRowsByInstanceId(Flashlist flashlist,
			Map<Integer, T> objectsById) {

		logger.debug("Updating " + flashlist.getRowsNode().size() + " of " + flashlist.getFlashlistType() + " objects ("
				+ objectsById.size() + " in the structure)");
		int found = 0;
		int failed = 0;

		for (JsonNode rowNode : flashlist.getRowsNode()) {
			try {
				int objectId = Integer.parseInt(rowNode.get(INSTANCE).asText());

				if (objectsById.containsKey(objectId)) {

					T flashlistUpdatableObject = objectsById.get(objectId);
					flashlistUpdatableObject.updateFromFlashlist(flashlist.getFlashlistType(), rowNode);

					logger.debug("Updated objects: " + flashlistUpdatableObject);
					found++;

				} else {
					logger.debug("No DAQ object " + flashlist.getFlashlistType() + " with flashlist id " + objectId
							+ ", ignoring "); // TODO: print class name of
					// object being ignored
					failed++;
				}
			} catch (NumberFormatException e) {
				logger.warn("Instance number can not be parsed " + rowNode.get(INSTANCE));
			}
		}

		MappingReporter.get().increaseMissing(flashlist.getFlashlistType().name(), failed);
		MappingReporter.get().increaseTotal(flashlist.getFlashlistType().name(), failed + found);
	}

}
