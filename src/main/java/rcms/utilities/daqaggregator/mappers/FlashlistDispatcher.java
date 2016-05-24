package rcms.utilities.daqaggregator.mappers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.data.FED;

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
			dispatchRowsByGeo(flashlist, structureMapper.getObjectMapper().fedsById.values());
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

		case LEVEL_ZERO_FM_SUBSYS:
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
	public void dispatchRowsByGeo(Flashlist flashlist, Collection<FED> objects) {

		int failed = 0;
		int total = 0;

		/*
		 * There may be FED without FMM or FMMApplication - either way we cannot
		 * map them by hostname (comes from FMMApplication) and geoslot (comes
		 * from FMM), we will process only these ones:
		 */
		Set<FED> fedsToProcess = new HashSet<FED>();

		Map<String, Map<Integer, Map<Integer, JsonNode>>> hostnameGeoslotMap = new HashMap<>();
		for (FED t : objects) {
			total++;
			if (t.getFmm() == null || t.getFmm().getFmmApplication() == null) {
				failed++;
				continue;
			}
			fedsToProcess.add(t);

			Integer geoslot = t.getFmm().getGeoslot();
			String hostname = t.getFmm().getFmmApplication().getHostname();

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
			String hostname = row.get("hostname").asText();
			Integer geoslot = row.get("geoslot").asInt();
			Integer io = row.get("io").asInt();
			if (hostnameGeoslotMap.containsKey(hostname) && hostnameGeoslotMap.get(hostname).containsKey(geoslot)) {
				hostnameGeoslotMap.get(hostname).get(geoslot).put(io, row);
			}
		}

		/* pass right flashlist rows to corresponding FEDs */
		for (FED t : fedsToProcess) {
			String hostname = t.getFmm().getFmmApplication().getHostname();
			Integer geoslot = t.getFmm().getGeoslot();
			Integer io = t.getFmmIO();
			JsonNode row = hostnameGeoslotMap.get(hostname).get(geoslot).get(io);
			total++;
			if (row != null)
				t.updateFromFlashlist(flashlist.getFlashlistType(), row);
			else {
				failed++;
			}
		}

		MappingReporter.get().increaseMissing(flashlist.getName(), failed);
		MappingReporter.get().increaseTotal(flashlist.getName(), total);

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
			// remove protocol
			if (hostname.startsWith("http://")) {
				hostname = hostname.substring(7);
			}
			// remove port
			if (hostname.contains(":")) {
				hostname = hostname.substring(0, hostname.indexOf(":"));
			}
			if (!hostname.endsWith(".cms")) {
				hostname = hostname + ".cms";
			}
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
		MappingReporter.get().increaseMissing(flashlist.getName(), failed);
		MappingReporter.get().increaseTotal(flashlist.getName(), failed + found);
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

		MappingReporter.get().increaseMissing(flashlist.getName(), failed);
		MappingReporter.get().increaseTotal(flashlist.getName(), failed + found);
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
