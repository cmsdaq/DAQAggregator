package rcms.utilities.daqaggregator.mappers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.data.FED;

public class ObjectUpdater {

	final String INSTANCE = "instance";

	private static final Logger logger = Logger.getLogger(Flashlist.class);

	public void update(Flashlist flashlist, StructureMapper structureMapper) {
		FlashlistType type = flashlist.getFlashlistType();
		switch (type) {
		case RU:
			updateObjectFromRowByInstanceId(flashlist, structureMapper.getObjectMapper().rusById);
			break;
		case BU:
			updateObjectFromRowByInstanceId(flashlist, structureMapper.getObjectMapper().busById);
			break;
		case FEROL_INPUT_STREAM:
			updateObjectFromRowByInstanceId(flashlist, structureMapper.getObjectMapper().fedsById);
			break;
		case FMM_INPUT:
			// updateObjectFromRowByInstanceId(flashlist,
			// structureMapper.getObjectMapper().fedsById);
			updateObjectFromRowByGeo(flashlist, structureMapper.getObjectMapper().fedsById.values());
			break;
		case FEROL_STATUS:
			updateObjectFromRowByInstanceId(flashlist, structureMapper.getObjectMapper().ttcpartitionsById);
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
			updateObjectFromRowByHostname(flashlist, structureMapper.getObjectMapper().frlPcByHostname);
			updateObjectFromRowByHostname(flashlist, structureMapper.getObjectMapper().fmmApplicationByHostname);
			break;

		case LEVEL_ZERO_FM_SUBSYS:
			for (JsonNode rowNode : flashlist.getRowsNode()) {
				if (rowNode.get("SUBSYS").asText().equals("DAQ") && rowNode.get("FMURL").asText().contains("toppro")) {
					logger.debug("Found DAQ status: " + rowNode.get("STATE") + ", url: " + rowNode.get("FMURL"));
					structureMapper.getObjectMapper().daq.setDaqState(rowNode.get("STATE").asText());
				}
			}
			break;
		default:
			break;
		}

	}

	public void updateObjectFromRowByGeo(Flashlist flashlist, Collection<FED> objects) {

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

	public <T extends FlashlistUpdatable> void updateObjectFromRowByHostname(Flashlist flashlist,
			Map<String, T> objectsByHostname) {

		logger.debug("Updating " + flashlist.getRowsNode().size() + " of " + flashlist.getFlashlistType() + " objects ("
				+ objectsByHostname.size() + " in the structure)");

		int found = 0;
		int failed = 0;

		for (JsonNode rowNode : flashlist.getRowsNode()) {
			String hostname = rowNode.get("hostname").asText() + ".cms";
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
		//MappingReporter.get().increaseMissing(flashlist.getName(), failed);
		//MappingReporter.get().increaseTotal(flashlist.getName(), failed + found);
	}

	/**
	 * 
	 * Update objects with data from given flashlist
	 * 
	 * @param flashlist
	 *            Flashlist object with data retrieved from LAS
	 * @param objectsById
	 *            objects to update
	 */
	public <T extends FlashlistUpdatable> void updateObjectFromRowByInstanceId(Flashlist flashlist,
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
