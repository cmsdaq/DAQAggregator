package rcms.utilities.daqaggregator.mappers;

import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

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
			updateObjectFromRowByInstanceId(flashlist, structureMapper.getObjectMapper().fedsById);
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

		logger.warn("Flash matching report for " + flashlist.getName() + ", found " + found + ", not found " + failed);
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
					found ++;

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
		

		logger.warn("Flash matching report for " + flashlist.getName() + ", found " + found + ", not found " + failed);
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
