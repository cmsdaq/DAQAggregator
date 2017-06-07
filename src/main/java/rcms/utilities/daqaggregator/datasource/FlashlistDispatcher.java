package rcms.utilities.daqaggregator.datasource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.data.GlobalTTSState;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqaggregator.mappers.FlashlistUpdatable;
import rcms.utilities.daqaggregator.mappers.MappingManager;
import rcms.utilities.daqaggregator.mappers.MappingReporter;
import rcms.utilities.daqaggregator.mappers.helper.TCDSFlashlistHelpers;
import rcms.utilities.daqaggregator.mappers.matcher.BuMatcher;
import rcms.utilities.daqaggregator.mappers.matcher.DAQMatcher;
import rcms.utilities.daqaggregator.mappers.matcher.FMMGeoMatcher;
import rcms.utilities.daqaggregator.mappers.matcher.FRLGeoFinder;
import rcms.utilities.daqaggregator.mappers.matcher.FedFromFerolInputStreamGeoFinder;
import rcms.utilities.daqaggregator.mappers.matcher.FedInErrorMatcher;
import rcms.utilities.daqaggregator.mappers.matcher.FedInFmmGeoFinder;
import rcms.utilities.daqaggregator.mappers.matcher.FedInFrl40GeoFinder;
import rcms.utilities.daqaggregator.mappers.matcher.FedInFrlGeoFinder;
import rcms.utilities.daqaggregator.mappers.matcher.FmmApplicationMatcher;
import rcms.utilities.daqaggregator.mappers.matcher.FrlPcMatcher;
import rcms.utilities.daqaggregator.mappers.matcher.Matcher;
import rcms.utilities.daqaggregator.mappers.matcher.RuMatcher;
import rcms.utilities.daqaggregator.mappers.matcher.SubsystemMatcher;
import rcms.utilities.daqaggregator.mappers.matcher.TTCPartitionGeoFinder;
import rcms.utilities.daqaggregator.mappers.matcher.TcdsTtsPiMatcher;

/**
 * This class dispatches the flashlist content (monitoring data that changes
 * over time) to appropriate objects (that are built based on hardware database)
 * 
 * Note that this object is NOT performing the actual matching of the data. The
 * matching is done by matcher classes located in
 * rcms.utilities.daqaggregator.mappers.matcher package.
 * 
 * Dispatcher takes the retrieved flashlist data and passes it to appropriate
 * matcher based on the type of flaslhist - this is the only responsibility of
 * this class
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 * 
 *         TODO: most of the matching code has been moved to matcher package,
 *         but there are still some code related to matching ferol40 and tcds
 *
 */
public class FlashlistDispatcher {

	private static final String subsystemKey = "SUBSYS";

	// Value to filter out values from TCDS flashlists
	// This will only work for CDAQ. Need to figure out how to know what
	// PM (CPM or LPM) is used for a given run, instead of hardcoding
	// private String serviceField;

	private static final Logger logger = Logger.getLogger(Flashlist.class);

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
			dispatchRowsUsingMatcher(flashlist, mappingManager.getObjectMapper().rus.values(),
					new RuMatcher(sessionId, "context"));

			dispatchRowsUsingMatcher(flashlist, mappingManager.getObjectMapper().feds.values(),
					new FedInErrorMatcher(sessionId));
			break;
		case BU:
			dispatchRowsUsingMatcher(flashlist, mappingManager.getObjectMapper().bus.values(),
					new BuMatcher(sessionId, "context"));
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

				for (JsonNode row : getRowsFilteredBySessionId(flashlist.getRowsNode(), flashlist.getFlashlistType(),
						sessionId)) {
					for (RU ru : mappingManager.getObjectMapper().rus.values()) {
						if (ru.isEVM())
							ru.updateFromFlashlist(flashlist.getFlashlistType(), row);
					}
				}

			} else {
				logger.error("run-number problem while dispatching EVM flashlist, here is EVM flashlist row: "
						+ flashlist.getRowsNode());
			}

			break;
		case LEVEL_ZERO_FM_STATIC:
			break;
		case JOB_CONTROL:

			dispatchRowsUsingMatcher(flashlist, mappingManager.getObjectMapper().frlPcs.values(),
					new FrlPcMatcher(sessionId, "context", true));
			dispatchRowsUsingMatcher(flashlist, mappingManager.getObjectMapper().fmmApplications.values(),
					new FmmApplicationMatcher(sessionId, "context", true));
			dispatchRowsUsingMatcher(flashlist, mappingManager.getObjectMapper().rus.values(),
					new RuMatcher(sessionId, "context", true));
			dispatchRowsUsingMatcher(flashlist, mappingManager.getObjectMapper().bus.values(),
					new BuMatcher(sessionId, "context", true));
			break;

		case LEVEL_ZERO_FM_SUBSYS: {

			dispatchRowsUsingMatcher(flashlist, mappingManager.getObjectMapper().subSystems.values(),
					new SubsystemMatcher(sessionId));

			for (JsonNode rowNode : getRowsFilteredBySessionId(flashlist.getRowsNode(), flashlist.getFlashlistType(),
					sessionId)) {
				if (rowNode.get(subsystemKey) != null) {
					String subsystemName = rowNode.get(subsystemKey).textValue();
					if (subsystemName.equals("DAQ")) {
						mappingManager.getObjectMapper().daq.updateFromFlashlist(flashlist.getFlashlistType(), rowNode);
					}
				} else {
					logger.warn("Flashlist cell for: " + subsystemKey + " is empty");
				}
			}
		}
			break;

		case LEVEL_ZERO_FM_DYNAMIC:

			dispatchRowsUsingMatcher(flashlist, Arrays.asList(mappingManager.getObjectMapper().daq),
					new DAQMatcher(sessionId));
			break;

		case FEROL_CONFIGURATION:

			dispatchRowsUsingMatcher(flashlist, mappingManager.getObjectMapper().frlPcs.values(),
					new FrlPcMatcher(sessionId, "context"));
			dispatchRowsUsingMatcher(flashlist, mappingManager.getObjectMapper().fedsById.values(),
					new FedInFrlGeoFinder("io", sessionId));
			break;
		case FRL_MONITORING:
			// TODO: future - use frlpc.context for mapping
			dispatchRowsUsingMatcher(flashlist, mappingManager.getObjectMapper().frlPcs.values(),
					new FrlPcMatcher(sessionId, "context"));
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
			dispatchRowsUsingMatcher(flashlist, mappingManager.getObjectMapper().frlPcs.values(),
					new FrlPcMatcher(sessionId, "context"));
			break;
		case TCDS_PI_TTS_SUMMARY:
			dispatchRowsUsingMatcher(flashlist, mappingManager.getObjectMapper().fedsById.values(),
					new TcdsTtsPiMatcher());
			break;
		default:
			break;
		}
	}

	/**
	 * TODO: this method is coppied from SessionFilteringMatcher, refactor this
	 * so that it's only in one place
	 * 
	 * @param rowsToFilter
	 * @param flashlistType
	 * @param sessionId
	 * @return
	 */
	protected List<JsonNode> getRowsFilteredBySessionId(JsonNode rowsToFilter, FlashlistType flashlistType,
			int sessionId) {
		List<JsonNode> result = new ArrayList<>();
		logger.debug("Before the sid filter: " + rowsToFilter.size());

		for (JsonNode rowNode : rowsToFilter) {

			if (flashlistType.isSessionContext()) {
				if (flashlistType.getSessionIdColumnName() != null) {
					if (rowNode.has(flashlistType.getSessionIdColumnName())) {
						try {
							int rowSessionContext = rowNode.get(flashlistType.getSessionIdColumnName()).asInt();
							if (rowSessionContext == sessionId) {
								result.add(rowNode);
							} else {
								logger.debug("Ignoring row of " + flashlistType + " with SID " + rowSessionContext
										+ ", expecting " + sessionId);
							}
						} catch (NumberFormatException e) {
							logger.info(
									"Could not parse SID from: " + rowNode.get(flashlistType.getSessionIdColumnName()));
						}
					} else {
						logger.warn("Flashlist " + flashlistType + " has no such column "
								+ flashlistType.getSessionIdColumnName());
					}
				} else {
					logger.warn("Flashlist " + flashlistType
							+ " is defined as having session context but no session id column is defined");
				}
			} else {
				result.add(rowNode);
			}
		}
		logger.debug("After the sid filter: " + result.size());

		return result;
	}

}
