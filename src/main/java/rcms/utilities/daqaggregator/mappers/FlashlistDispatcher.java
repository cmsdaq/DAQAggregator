package rcms.utilities.daqaggregator.mappers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.mappers.helper.ContextHelper;
import rcms.utilities.daqaggregator.mappers.helper.FedGeoFinder;
import rcms.utilities.daqaggregator.mappers.helper.FedInFmmGeoFinder;
import rcms.utilities.daqaggregator.mappers.helper.FedInFrlGeoFinder;

public class FlashlistDispatcher {

	final String INSTANCE = "instance";

	private static final Logger logger = Logger.getLogger(Flashlist.class);

	/**
	 * Dispatch flashlist rows to appropriate objects from DAQ structure. Note
	 * that a flashlist must be already initialized, for initialization see
	 * {@link FlashlistManager}
	 * 
	 * @param flashlist
	 * @param structureMapper
	 */
	public void dispatch(Flashlist flashlist, StructureMapper structureMapper) {
		FlashlistType type = flashlist.getFlashlistType();
		switch (type) {
		case RU:
			dispatchRowsByInstanceId(flashlist, structureMapper.getObjectMapper().rusById);
			break;
		case BU:
			dispatchRowsByInstanceId(flashlist, structureMapper.getObjectMapper().busById);
			break;
		case FEROL_INPUT_STREAM:
			dispatchRowsByInstanceId(flashlist, structureMapper.getObjectMapper().fedsById);
			break;
		case FMM_INPUT:
			dispatchRowsByGeo(flashlist, structureMapper.getObjectMapper().fedsById.values(), new FedInFmmGeoFinder());
			break;
		case FEROL_STATUS:
			dispatchRowsByInstanceId(flashlist, structureMapper.getObjectMapper().ttcpartitionsById);
			break;
		case EVM:
			if (flashlist.getRowsNode().isArray()) {
				int runNumber = flashlist.getRowsNode().get(0).get("runNumber").asInt();
				structureMapper.getObjectMapper().daq.setRunNumber(runNumber);
				logger.debug("Successfully got runnumber: " + runNumber);
			} else {
				logger.error("runnumber problem " + flashlist.getRowsNode());
			}
			break;

		case JOB_CONTROL:
			dispatchRowsByHostname(flashlist, structureMapper.getObjectMapper().frlPcByHostname, "hostname");
			dispatchRowsByHostname(flashlist, structureMapper.getObjectMapper().fmmApplicationByHostname, "hostname");
			break;

		case LEVEL_ZERO_FM_SUBSYS: // TODO: SID column
			for (JsonNode rowNode : flashlist.getRowsNode()) {
				if (rowNode.get("SUBSYS").asText().equals("DAQ") && rowNode.get("FMURL").asText().contains("toppro")) {
					structureMapper.getObjectMapper().daq.updateFromFlashlist(flashlist.getFlashlistType(), rowNode);
				}
			}
			break;
		case LEVEL_ZERO_FM_DYNAMIC:
			for (JsonNode rowNode : flashlist.getRowsNode()) {
				structureMapper.getObjectMapper().daq.updateFromFlashlist(flashlist.getFlashlistType(), rowNode);
			}
			break;
		case FEROL_CONFIGURATION:
			dispatchRowsByHostname(flashlist, structureMapper.getObjectMapper().frlPcByHostname, "context");
			dispatchRowsByGeo(flashlist, structureMapper.getObjectMapper().fedsById.values(), new FedInFrlGeoFinder());
			break;
		default:
			break;
		}

	}

	/**
	 * Dispatch rows of a flashlist to appropriate objects by geolocation
	 * 
	 * @param flashlist
	 * @param objects
	 */
	public void dispatchRowsByGeo(Flashlist flashlist, Collection<FED> objects, FedGeoFinder finder) {

		int failed = 0;
		int total = 0;

		/*
		 * There may be FED without FMM/FRL or FMMApplication/FRLPc - either way we cannot
		 * map them by hostname (comes from FMMApplication) and geoslot (comes
		 * from FMM), we will process only these ones:
		 */
		Set<FED> fedsToProcess = new HashSet<FED>();

		Map<String, Map<Integer, Map<Integer, JsonNode>>> hostnameGeoslotMap = new HashMap<>();
		for (FED t : objects) {
			//total++;
			if (finder.getHostname(t) == null || finder.getGeoslot(t) == null) {
				//failed++;
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

		/* pass right flashlist rows to corresponding FEDs */
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
