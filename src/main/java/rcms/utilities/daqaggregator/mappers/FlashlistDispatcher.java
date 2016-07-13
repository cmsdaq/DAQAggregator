package rcms.utilities.daqaggregator.mappers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqaggregator.mappers.helper.ContextHelper;
import rcms.utilities.daqaggregator.mappers.helper.FMMGeoFinder;
import rcms.utilities.daqaggregator.mappers.helper.FRLGeoFinder;
import rcms.utilities.daqaggregator.mappers.helper.FedGeoFinder;
import rcms.utilities.daqaggregator.mappers.helper.FedInFmmGeoFinder;
import rcms.utilities.daqaggregator.mappers.helper.FedInFrlGeoFinder;
import rcms.utilities.daqaggregator.mappers.helper.HostnameGeoslotFinder;
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
			dispatchRowsByInstanceId(flashlist, mappingManager.getObjectMapper().fedsById);
			//TODO: 3geo
			break;
		case FMM_INPUT:
			dispatchRowsByThreeElementGeo(flashlist, mappingManager.getObjectMapper().fedsById.values(),
					new FedInFmmGeoFinder());
			break;
		case FEROL_STATUS:
			dispatchRowsByTwoElementGeo(flashlist, mappingManager.getObjectMapper().frls.values(), new FRLGeoFinder());
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
			//TODO: dispatch by context (in the future) (multiple context by hostname)
			dispatchRowsByHostname(flashlist, mappingManager.getObjectMapper().frlPcByHostname, "hostname");
			dispatchRowsByHostname(flashlist, mappingManager.getObjectMapper().fmmApplicationByHostname, "hostname");
			//TODO: should go to RU, BU
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
			//TODO: future - use frlpc.context for mapping
			dispatchRowsByHostname(flashlist, mappingManager.getObjectMapper().frlPcByHostname, "context");
			dispatchRowsByThreeElementGeo(flashlist, mappingManager.getObjectMapper().fedsById.values(),
					new FedInFrlGeoFinder());
			break;
		case FMM_STATUS:
			dispatchRowsByTwoElementGeo(flashlist, mappingManager.getObjectMapper().fmms.values(), new FMMGeoFinder());
			dispatchRowsByTwoElementGeo(flashlist, mappingManager.getObjectMapper().ttcPartitions.values(),
					new TTCPartitionGeoFinder());
			break;
		default:
			break;
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

	public <T extends FlashlistUpdatable> void dispatchRowsByTwoElementGeo(Flashlist flashlist,
			Collection<T> collection, HostnameGeoslotFinder<T> finder) {

		int failed = 0;
		int total = 0;
		Map<String, Map<Integer, JsonNode>> hostnameGeoslotMap = new HashMap<>();
		Set<T> findablesToProcess = new HashSet<>();
		for (T findable : collection) {
			if (finder.getHostname(findable) == null || finder.getGeoslot(findable) == null) {
				continue;
			}
			findablesToProcess.add(findable);
			String hostname = finder.getHostname(findable);
			// prepare HOSTNAME
			if (!hostnameGeoslotMap.containsKey(hostname)) {
				hostnameGeoslotMap.put(hostname, new HashMap<Integer, JsonNode>());
			}
		}

		/* prepare data from flashlist */
		int rows = 0;
		for (JsonNode row : flashlist.getRowsNode()) {
			rows++;

			String hostname = row.get(finder.getFlashlistHostnameKey()).asText();

			hostname = ContextHelper.getHostnameFromContext(hostname);
			Integer geoslot = row.get(finder.getFlashlistGeoslotKey()).asInt();
			if (hostnameGeoslotMap.containsKey(hostname)) {
				hostnameGeoslotMap.get(hostname).put(geoslot, row);
			}
		}

		/* dispatch right flashlist rows to corresponding FRLs */
		for (T findable : findablesToProcess) {

			String hostname = finder.getHostname(findable);
			Integer geoslot = finder.getGeoslot(findable);
			JsonNode row = hostnameGeoslotMap.get(hostname).get(geoslot);
			total++;
			if (row != null)
				findable.updateFromFlashlist(flashlist.getFlashlistType(), row);
			else {
				failed++;
			}
		}

		
		MappingReporter.get().increaseMissing(flashlist.getFlashlistType().name(), failed);
		MappingReporter.get().increaseTotal(flashlist.getFlashlistType().name(), total);

	}

	/**
	 * Dispatch rows of a flashlist to appropriate objects by geolocation
	 * 
	 * @param flashlist
	 * @param objects
	 */
	public void dispatchRowsByThreeElementGeo(Flashlist flashlist, Collection<FED> objects, FedGeoFinder finder) {

		int failed = 0;
		int total = 0;

		/*
		 * There may be FED without FMM/FRL or FMMApplication/FRLPc - either way
		 * we cannot map them by hostname (comes from FMMApplication) and
		 * geoslot (comes from FMM), we will process only these ones:
		 */
		Set<FED> fedsToProcess = new HashSet<>();

		Map<String, Map<Integer, Map<Integer, JsonNode>>> hostnameGeoslotMap = new HashMap<>();
		for (FED t : objects) {
			// total++;
			if (finder.getHostname(t) == null || finder.getGeoslot(t) == null) {
				// failed++;
				continue;
			}
			fedsToProcess.add(t);

			Integer geoslot = finder.getGeoslot(t);
			String hostname = finder.getHostname(t);

			// prepare HOSTNAME
			if (!hostnameGeoslotMap.containsKey(hostname)) {
				hostnameGeoslotMap.put(hostname, new HashMap<Integer, Map<Integer, JsonNode>>());
			}

			// prepare GEOSLOT
			if (!hostnameGeoslotMap.get(hostname).containsKey(geoslot)) {
				hostnameGeoslotMap.get(hostname).put(geoslot, new HashMap<Integer, JsonNode>());
			}

		}

		/* prepare data from flashlist */
		for (JsonNode row : flashlist.getRowsNode()) {

			/* preparing data if FMM_INPUT flashlist */
			if (flashlist.getFlashlistType() == FlashlistType.FMM_INPUT) {
				String hostname = row.get("hostname").asText();
				Integer geoslot = row.get("geoslot").asInt();
				Integer io = row.get("io").asInt();
				if (hostnameGeoslotMap.containsKey(hostname) && hostnameGeoslotMap.get(hostname).containsKey(geoslot)) {
					hostnameGeoslotMap.get(hostname).get(geoslot).put(io, row);
				}
			}

			/* preparing data if FEROL_CONFIGURATION flashlist */
			else if (flashlist.getFlashlistType() == FlashlistType.FEROL_CONFIGURATION) {
				String hostname = row.get("context").asText();

				hostname = ContextHelper.getHostnameFromContext(hostname);
				Integer geoslot = row.get("slotNumber").asInt();
				Integer io0 = 0;
				Integer io1 = 1;
				if (hostnameGeoslotMap.containsKey(hostname) && hostnameGeoslotMap.get(hostname).containsKey(geoslot)) {
					hostnameGeoslotMap.get(hostname).get(geoslot).put(io0, row);
					hostnameGeoslotMap.get(hostname).get(geoslot).put(io1, row);
				}
			}
		}

		/* dispatch right flashlist rows to corresponding FEDs */
		for (FED fed : fedsToProcess) {

			String hostname = finder.getHostname(fed);
			Integer geoslot = finder.getGeoslot(fed);
			Integer io = finder.getIO(fed);
			JsonNode row = hostnameGeoslotMap.get(hostname).get(geoslot).get(io);
			total++;
			if (row != null)
				fed.updateFromFlashlist(flashlist.getFlashlistType(), row);
			else {
				failed++;
			}
		}

		MappingReporter.get().increaseMissing(flashlist.getFlashlistType().name(), failed);
		MappingReporter.get().increaseTotal(flashlist.getFlashlistType().name(), total);

	}

	/**
	 * Dispatch rows of a flashlist to appropriate objects by hostname
	 * 
	 * @param flashlist
	 * @param objectsByHostname
	 * @param flashlistKey
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

					logger.debug("Updated ru: " + flashlistUpdatableObject);
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

	// TODO: verify data
	private void verifyRUFlashlist() {
		/*
		 * {"key":"eventCount","type":"unsigned int 64"},
		 * {"key":"eventRate","type":"unsigned int 32"},
		 * {"key":"eventsInRU","type":"unsigned int 32"},
		 * {"key":"fragmentCount","type":"unsigned int 64"},
		 * {"key":"superFragmentSize","type":"unsigned int 32"},
		 * {"key":"superFragmentSizeStdDev","type":"unsigned int 32"},
		 */
	}

}
