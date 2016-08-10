package rcms.utilities.daqaggregator.mappers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.mappers.helper.ContextHelper;
import rcms.utilities.daqaggregator.mappers.helper.FMMGeoMatcher;
import rcms.utilities.daqaggregator.mappers.helper.FRLGeoFinder;
import rcms.utilities.daqaggregator.mappers.helper.FedFromFerolInputStreamGeoFinder;
import rcms.utilities.daqaggregator.mappers.helper.FedInFmmGeoFinder;
import rcms.utilities.daqaggregator.mappers.helper.FedInFrlGeoFinder;
import rcms.utilities.daqaggregator.mappers.helper.Matcher;
import rcms.utilities.daqaggregator.mappers.helper.TTCPartitionGeoFinder;

public class FlashlistDispatcher {

	final String INSTANCE = "instance";

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
			dispatchRowsByGeo(flashlist, mappingManager.getObjectMapper().fedsById.values(), new FedFromFerolInputStreamGeoFinder("streamNumber"));
			break;
		case FMM_INPUT:
			dispatchRowsByGeo(flashlist, mappingManager.getObjectMapper().fedsById.values(), new FedInFmmGeoFinder());
			break;
		case FEROL_STATUS:
			dispatchRowsByGeo(flashlist, mappingManager.getObjectMapper().frls.values(), new FRLGeoFinder());
			break;
		case EVM:
			if (flashlist.getRowsNode().isArray()) {
				int runNumber = flashlist.getRowsNode().get(0).get("runNumber").asInt();
				mappingManager.getObjectMapper().daq.setRunNumber(runNumber);
				logger.debug("Successfully got runnumber: " + runNumber);
			} else {
				logger.error("runnumber problem " + flashlist.getRowsNode());
			}

			for (RU ru : mappingManager.getObjectMapper().rus.values()) {
				if (ru.isEVM())
					ru.updateFromFlashlist(flashlist.getFlashlistType(), flashlist.getRowsNode().get(0));
				// additional check hostname

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
				mappingManager.getObjectMapper().daq.updateFromFlashlist(flashlist.getFlashlistType(), rowNode);
			}
			break;
		case FEROL_CONFIGURATION:
			// TODO: future - use frlpc.context for mapping
			dispatchRowsByHostname(flashlist, mappingManager.getObjectMapper().frlPcByHostname, "context");
			dispatchRowsByGeo(flashlist, mappingManager.getObjectMapper().fedsById.values(), new FedInFrlGeoFinder("io"));
			break;
		case FRL_MONITORING:
			// TODO: future - use frlpc.context for mapping
			dispatchRowsByHostname(flashlist, mappingManager.getObjectMapper().frlPcByHostname, "context");
			dispatchRowsByGeo(flashlist, mappingManager.getObjectMapper().fedsById.values(), new FedInFrlGeoFinder("io"));
			break;
		case FMM_STATUS:
			dispatchRowsByGeo(flashlist, mappingManager.getObjectMapper().fmms.values(), new FMMGeoMatcher());
			dispatchRowsByGeo(flashlist, mappingManager.getObjectMapper().ttcPartitions.values(),
					new TTCPartitionGeoFinder());
			break;
		default:
			break;
		}

	}

	private void printFlashListTypeInfo(Flashlist flashlist) {
		com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
		try {
			logger.info("Flashlist schema:  "+om.writeValueAsString(flashlist.getDefinitionNode()));
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
						logger.info(
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
						logger.info(
								"FED with problem indicated by flashlist RU.fedIdsWithoutFragments could not be found by id "
										+ fedId);
					}
				}

			}

		}
		logger.info("There are " + fedToFlashlistRow.size() + " FEDs with problems (according to RU flashlist)");
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
		logger.info("Elements matched by geolocation: " + dispatchMap.size() + "/" + collection.size());

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
