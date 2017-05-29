package rcms.utilities.daqaggregator.datasource;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.GlobalTTSState;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqaggregator.mappers.FlashlistUpdatable;
import rcms.utilities.daqaggregator.mappers.MappingManager;
import rcms.utilities.daqaggregator.mappers.MappingReporter;
import rcms.utilities.daqaggregator.mappers.helper.ContextHelper;
import rcms.utilities.daqaggregator.mappers.helper.TCDSFlashlistHelpers;
import rcms.utilities.daqaggregator.mappers.matcher.FMMGeoMatcher;
import rcms.utilities.daqaggregator.mappers.matcher.FRLGeoFinder;
import rcms.utilities.daqaggregator.mappers.matcher.FedFromFerolInputStreamGeoFinder;
import rcms.utilities.daqaggregator.mappers.matcher.FedInFmmGeoFinder;
import rcms.utilities.daqaggregator.mappers.matcher.FedInFrl40GeoFinder;
import rcms.utilities.daqaggregator.mappers.matcher.FedInFrlGeoFinder;
import rcms.utilities.daqaggregator.mappers.matcher.Matcher;
import rcms.utilities.daqaggregator.mappers.matcher.TTCPartitionGeoFinder;

/**
 * This class dispatches the flashlist content (monitoring data that changes
 * over time) to appropriate objects (that are built based on hardware database)
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class FlashlistDispatcher {

	final String INSTANCE = "instance";

	// Value to filter out values from TCDS flashlists
	// This will only work for CDAQ. Need to figure out how to know what
	// PM (CPM or LPM) is used for a given run, instead of hardcoding
	// private String serviceField;

	private static final Logger logger = Logger.getLogger(Flashlist.class);

	/** string for filtering by FM URL */
	private final String filter1;

	public FlashlistDispatcher(String filter1) {
		this.filter1 = filter1;
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
	 * 
	 */
	public <T extends FlashlistUpdatable> void dispatchRowsUsingMatcher(Flashlist flashlist, Collection<T> collection,
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
	 * Dispatch flashlist rows to appropriate objects from DAQ structure. Note
	 * that a flashlist must be already initialized, for initialization see
	 * {@link FlashlistManager}
	 * 
	 * @param flashlist
	 * @param mappingManager
	 */
	public void dispatch(Flashlist flashlist, MappingManager mappingManager) {

		/** TCDS service name */
		String tcds_serviceField = mappingManager.getTcdsFmInfoRetriever().getTcdsfm_pmService();
		String tcds_url = mappingManager.getTcdsFmInfoRetriever().getTcdsfm_pmContext();

		int sessionId = mappingManager.getObjectMapper().daq.getSessionId();

		logger.debug("Received " + tcds_serviceField + " TCDS PM service name");

		if (flashlist.isUnknownAtLAS()) {
			logger.debug("Flashlist dispatcher received and will ignore " + flashlist.getName()
					+ " because it was not successfully downloaded from LAS");
			return;
		}

		FlashlistType type = flashlist.getFlashlistType();

		switch (type) {
		case RU:
			dispatchRowsByHostname(flashlist, mappingManager.getObjectMapper().rusByHostname, "context", sessionId);
			dispatchRowsByFedIdsWithErrors(flashlist, mappingManager.getObjectMapper().fedsByExpectedId, sessionId);
			break;
		case BU:
			dispatchRowsByHostname(flashlist, mappingManager.getObjectMapper().busByHostname, "context", sessionId);
			break;
		case FEROL_INPUT_STREAM:
			dispatchRowsUsingMatcher(flashlist, mappingManager.getObjectMapper().fedsById.values(),
					new FedFromFerolInputStreamGeoFinder("streamNumber", sessionId));
			break;
		case FMM_INPUT:
			dispatchRowsUsingMatcher(flashlist, mappingManager.getObjectMapper().fedsById.values(),
					new FedInFmmGeoFinder(sessionId));

			break;
		case FEROL_STATUS:
			dispatchRowsUsingMatcher(flashlist, mappingManager.getObjectMapper().frls.values(),
					new FRLGeoFinder(sessionId));
			break;
		case EVM:
			if (flashlist.getRowsNode().isArray() && flashlist.getRowsNode().size() > 0) {

				for (RU ru : mappingManager.getObjectMapper().rus.values()) {
					if (ru.isEVM())
						ru.updateFromFlashlist(flashlist.getFlashlistType(), flashlist.getRowsNode().get(0));
				}

			} else {
				logger.error("run-number problem while dispatching EVM flashlist, here is EVM flashlist row: "
						+ flashlist.getRowsNode());
			}

			break;
		case LEVEL_ZERO_FM_STATIC:
			break;
		case JOB_CONTROL:
			dispatchRowsByHostname(flashlist, mappingManager.getObjectMapper().frlPcByHostname, "context", sessionId);
			dispatchRowsByHostname(flashlist, mappingManager.getObjectMapper().fmmApplicationByHostname, "context",
					sessionId);
			dispatchRowsByHostname(flashlist, mappingManager.getObjectMapper().rusByHostname, "context", sessionId);
			dispatchRowsByHostname(flashlist, mappingManager.getObjectMapper().busByHostname, "context", sessionId);
			break;

		case LEVEL_ZERO_FM_SUBSYS: {

			Integer daqSid = this.getDAQsid(flashlist);
			logger.debug("DAQ session id: " + daqSid);

			for (JsonNode rowNode : flashlist.getRowsNode()) {

				Integer sid = null;
				try {
					sid = Integer.parseInt(rowNode.get("SID").asText());
				} catch (Exception ex) {

					logger.error("Unexpected exception caught when trying to parse subsystem session id", ex);
				}

				if (sid != null && daqSid != null && sid.equals(daqSid)) {

					logger.debug("Successfully matched session id: " + daqSid);

					String subsystemName = rowNode.get("SUBSYS").asText();

					if (subsystemName.equals("DAQ") && rowNode.get("FMURL").asText().contains(filter1)) {
						mappingManager.getObjectMapper().daq.updateFromFlashlist(flashlist.getFlashlistType(), rowNode);
					}

					if (mappingManager.getObjectMapper().subsystemByName.containsKey(subsystemName)) {
						mappingManager.getObjectMapper().subsystemByName.get(subsystemName)
								.updateFromFlashlist(flashlist.getFlashlistType(), rowNode);
					}
				} else {
					logger.debug("Ignoring unrelated session ids: " + rowNode.get("SID").asText());
				}

			}
		}
			break;

		case LEVEL_ZERO_FM_DYNAMIC:

			for (JsonNode rowNode : flashlist.getRowsNode()) {
				if (rowNode.get("FMURL").asText().contains(filter1)) {
					mappingManager.getObjectMapper().daq.updateFromFlashlist(flashlist.getFlashlistType(), rowNode);
				}
			}
			break;

		case FEROL_CONFIGURATION:
			dispatchRowsByHostname(flashlist, mappingManager.getObjectMapper().frlPcByHostname, "context", sessionId);
			dispatchRowsUsingMatcher(flashlist, mappingManager.getObjectMapper().fedsById.values(),
					new FedInFrlGeoFinder("io", sessionId));
			break;
		case FRL_MONITORING:
			// TODO: future - use frlpc.context for mapping
			dispatchRowsByHostname(flashlist, mappingManager.getObjectMapper().frlPcByHostname, "context", sessionId);
			dispatchRowsUsingMatcher(flashlist, mappingManager.getObjectMapper().fedsById.values(),
					new FedInFrlGeoFinder("io", sessionId));
			break;
		case FMM_STATUS:
			dispatchRowsUsingMatcher(flashlist, mappingManager.getObjectMapper().fmms.values(),
					new FMMGeoMatcher(sessionId));
			dispatchRowsUsingMatcher(flashlist, mappingManager.getObjectMapper().ttcPartitions.values(),
					new TTCPartitionGeoFinder(sessionId));
			break;
		case TCDS_PM_TTS_CHANNEL:

			String typeField1 = "tts_ici";
			String typeField2 = "tts_apve";

			if (tcds_serviceField == null || tcds_url == null) {
				return;
			}

			// stpi-indexed flashlist
			Map<String, Map<String, Map<Integer, Map<Integer, Map<String, String>>>>> stpiDataFromFlashlist = TCDSFlashlistHelpers
					.getTreeFromFlashlist(flashlist);

			// setting TTC partition states for types tts_ici and tts_apve
			// (where applicable)

			// . iterate over all ttcpartitions and set tcds_pm_ttsState
			// according to the code in 'value' flash column
			// .. retrieve this value from map above, specifying pmNr, iciNr
			// info, which are already stored in the ttcpartitions, in field
			// tcdsPartitionInfo
			// .. decode the value from map using
			// rcms.utilities.daqaggregator.mappers.helper.TTSStateDecoder.decodeTCDSTTSState(int
			// tts_value)
			// .. set ttcpartition.tcds_pm_ttsState
			for (Entry<Integer, TTCPartition> ttcpEntry : mappingManager.getObjectMapper().ttcPartitions.entrySet()) {
				TTCPartition ttcp = ttcpEntry.getValue(); // ref to ttcp object

				/* if no tcds ici/pi information could be found */
				if (ttcp.getTcdsPartitionInfo().getNullCause() != null) {
					ttcp.setTcds_pm_ttsState(ttcp.getTcdsPartitionInfo().getNullCause());
					ttcp.setTcds_apv_pm_ttsState(ttcp.getTcdsPartitionInfo().getNullCause());

					continue;
				}

				if (stpiDataFromFlashlist.containsKey(tcds_serviceField)
						&& stpiDataFromFlashlist.get(tcds_serviceField).containsKey(typeField1)
						&& stpiDataFromFlashlist.get(tcds_serviceField).get(typeField1)
								.containsKey(ttcp.getTcdsPartitionInfo().getPMNr())
						&& stpiDataFromFlashlist.get(tcds_serviceField).get(typeField1)
								.get(ttcp.getTcdsPartitionInfo().getPMNr())
								.containsKey(ttcp.getTcdsPartitionInfo().getICINr())) {

					int stateCode = Integer.parseInt(stpiDataFromFlashlist.get(tcds_serviceField).get(typeField1)
							.get(ttcp.getTcdsPartitionInfo().getPMNr()).get(ttcp.getTcdsPartitionInfo().getICINr())
							.get("value"));

					ttcp.setTcds_pm_ttsState(TCDSFlashlistHelpers.decodeTCDSTTSState(stateCode));
				}

				if (stpiDataFromFlashlist.containsKey(tcds_serviceField)
						&& stpiDataFromFlashlist.get(tcds_serviceField).containsKey(typeField2)
						&& stpiDataFromFlashlist.get(tcds_serviceField).get(typeField2)
								.containsKey(ttcp.getTcdsPartitionInfo().getPMNr())
						&& stpiDataFromFlashlist.get(tcds_serviceField).get(typeField2)
								.get(ttcp.getTcdsPartitionInfo().getPMNr())
								.containsKey(ttcp.getTcdsPartitionInfo().getICINr())) {

					String label = stpiDataFromFlashlist.get(tcds_serviceField).get(typeField2)
							.get(ttcp.getTcdsPartitionInfo().getPMNr()).get(ttcp.getTcdsPartitionInfo().getICINr())
							.get("label");

					if (label.equalsIgnoreCase("Unused")) {
						ttcp.setTcds_apv_pm_ttsState("x");
					} else {
						int stateCode = Integer.parseInt(stpiDataFromFlashlist.get(tcds_serviceField).get(typeField2)
								.get(ttcp.getTcdsPartitionInfo().getPMNr()).get(ttcp.getTcdsPartitionInfo().getICINr())
								.get("value"));

						ttcp.setTcds_apv_pm_ttsState(TCDSFlashlistHelpers.decodeTCDSTTSState(stateCode));
					}

				}
			}

			// setting global TTS states for all other types detected in
			// flashlist

			// .detect types other than tts_ici, tts_apve
			Set<String> types = new HashSet<String>();

			types.addAll(stpiDataFromFlashlist.get(tcds_serviceField).keySet());
			types.remove("tts_ici");
			types.remove("tts_apve");

			// .foreach type, decode state value and %B/%W value (if existing)
			// and set corresponding value in model's daq
			GlobalTTSState globalTtsState;
			for (String typeName : types) {
				logger.debug("Global TTS state detected for this service:" + typeName);

				globalTtsState = new GlobalTTSState();

				int stateCode = Integer.parseInt(
						stpiDataFromFlashlist.get(tcds_serviceField).get(typeName).get(0).get(0).get("value"));
				globalTtsState.setState(TCDSFlashlistHelpers.decodeTCDSTTSState(stateCode));

				// percentage keys should be reviewed when the flashlist column
				// name for these attributes is defined
				String busyKey = "outputFractionBusy";
				String warningKey = "outputFractionWarning";

				if (stpiDataFromFlashlist.get(tcds_serviceField).get(typeName).get(0).get(0).containsKey(busyKey)) {
					globalTtsState.setPercentBusy(Float.parseFloat(
							stpiDataFromFlashlist.get(tcds_serviceField).get(typeName).get(0).get(0).get(busyKey)));
				}

				if (stpiDataFromFlashlist.get(tcds_serviceField).get(typeName).get(0).get(0).containsKey(warningKey)) {
					globalTtsState.setPercentWarning(Float.parseFloat(
							stpiDataFromFlashlist.get(tcds_serviceField).get(typeName).get(0).get(0).get(warningKey)));
				}

				mappingManager.getObjectMapper().daq.getTcdsGlobalInfo().getGlobalTtsStates().put(typeName,
						globalTtsState);
			}

			mappingManager.getObjectMapper().daq.getTcdsGlobalInfo().setTcdsControllerServiceName(tcds_serviceField);
			mappingManager.getObjectMapper().daq.getTcdsGlobalInfo().setTcdsControllerContext(tcds_url);

			break;

		case TCDS_CPM_COUNTS:
			if (tcds_serviceField == null || tcds_url == null) {
				return;
			}
			for (JsonNode rowNode : flashlist.getRowsNode()) {

				// get flashlist row corresponding to service
				if (rowNode.get("service").asText().equalsIgnoreCase(tcds_serviceField)) {
					mappingManager.getObjectMapper().daq.getTcdsGlobalInfo()
							.updateFromFlashlist(flashlist.getFlashlistType(), rowNode);
					break;
				}
			}

			break;

		case TCDS_CPM_DEADTIMES:
			if (tcds_serviceField == null || tcds_url == null) {
				return;
			}
			for (JsonNode rowNode : flashlist.getRowsNode()) {

				// get flashlist row corresponding to service
				if (rowNode.get("service").asText().equalsIgnoreCase(tcds_serviceField)) {
					mappingManager.getObjectMapper().daq.getTcdsGlobalInfo()
							.updateFromFlashlist(flashlist.getFlashlistType(), rowNode);
					break;
				}
			}

			break;

		case TCDS_CPM_RATES:
			if (tcds_serviceField == null || tcds_url == null) {
				return;
			}
			for (JsonNode rowNode : flashlist.getRowsNode()) {

				// get flashlist row corresponding to service
				if (rowNode.get("service").asText().equalsIgnoreCase(tcds_serviceField)) {
					mappingManager.getObjectMapper().daq.getTcdsGlobalInfo()
							.updateFromFlashlist(flashlist.getFlashlistType(), rowNode);
					break;
				}
			}

			break;

		case TCDS_PM_ACTION_COUNTS:
			if (tcds_serviceField == null || tcds_url == null) {
				return;
			}
			for (JsonNode rowNode : flashlist.getRowsNode()) {

				// get flashlist row corresponding to service
				if (rowNode.get("service").asText().equalsIgnoreCase(tcds_serviceField)) {
					mappingManager.getObjectMapper().daq.getTcdsGlobalInfo()
							.updateFromFlashlist(flashlist.getFlashlistType(), rowNode);
					break;
				}
			}

			break;
		case FEROL40_STREAM_CONFIGURATION:
			dispatchRowsUsingMatcher(flashlist, mappingManager.getObjectMapper().fedsById.values(),
					new FedInFrl40GeoFinder(sessionId));
			break;
		case FEROL40_INPUT_STREAM:
			dispatchRowsUsingMatcher(flashlist, mappingManager.getObjectMapper().fedsById.values(),
					new FedInFrl40GeoFinder(sessionId));
			break;
		case FEROL40_STATUS:
			dispatchRowsUsingMatcher(flashlist, mappingManager.getObjectMapper().frls.values(),
					new FRLGeoFinder(sessionId));
			break;
		case FEROL40_CONFIGURATION:
			dispatchRowsByHostname(flashlist, mappingManager.getObjectMapper().frlPcByHostname, "context", sessionId);
			break;
		default:
			break;
		}
	}

	/**
	 * @deprecated
	 * @param flashlist
	 * @param fedsByExpectedId
	 * @param sessionId
	 */
	private void dispatchRowsByFedIdsWithErrors(Flashlist flashlist, Map<Integer, FED> fedsByExpectedId,
			int sessionId) {

		HashMap<FED, JsonNode> fedToFlashlistRow = new HashMap<>();

		for (JsonNode row : getRowsFilteredBySessionId(flashlist.getRowsNode(), flashlist.getFlashlistType(),
				sessionId)) {
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
	 * Dispatch rows of a flashlist to appropriate objects
	 * 
	 * @param flashlist
	 *            flashlit to dispatch
	 * @param objectsByHostname
	 *            map of objects by hostname
	 * @param flashlistKey
	 *            key to find flashlist column with hostname
	 * @param sessionId
	 *            used to filter the results by session id
	 */
	@Deprecated
	public <T extends FlashlistUpdatable> void dispatchRowsByHostname(Flashlist flashlist,
			Map<String, T> objectsByHostname, String flashlistKey, int sessionId) {

		logger.debug("Updating " + flashlist.getRowsNode().size() + " of " + flashlist.getFlashlistType() + " objects ("
				+ objectsByHostname.size() + " in the structure)");

		int found = 0;
		int failed = 0;

		for (JsonNode rowNode : getRowsFilteredBySessionId(flashlist.getRowsNode(), flashlist.getFlashlistType(),
				sessionId)) {
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
	@Deprecated
	private <T extends FlashlistUpdatable> void dispatchRowsByInstanceId(Flashlist flashlist,
			Map<Integer, T> objectsById, int sessionId) {

		logger.debug("Updating " + flashlist.getRowsNode().size() + " of " + flashlist.getFlashlistType() + " objects ("
				+ objectsById.size() + " in the structure)");
		int found = 0;
		int failed = 0;

		for (JsonNode rowNode : getRowsFilteredBySessionId(flashlist.getRowsNode(), flashlist.getFlashlistType(),
				sessionId)) {
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

	@Deprecated
	private JsonNode getRowsFilteredBySessionId(JsonNode rowsNode, FlashlistType flashlistType, int sessionId) {
		return rowsNode;
	}

	/**
	 * helper function for dispatch(..): returns the session id (SID) of the DAQ
	 * subsystem or null if not found
	 */
	@Deprecated
	private Integer getDAQsid(Flashlist flashlist) {

		try {
			for (JsonNode rowNode : flashlist.getRowsNode()) {

				String subsystemName = rowNode.get("SUBSYS").asText();

				if (subsystemName.equals("DAQ") && rowNode.get("FMURL").asText().contains(filter1)) {
					return Integer.parseInt(rowNode.get("SID").asText());
				}
			} // loop over rows of the flashlist
		} catch (Exception ex) {

			logger.error("Unexpected exception caught when trying to determine DAQ session id", ex);
		}

		// not found or there was a problem
		return null;
	}

}
