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
			updateObjectFromRow(flashlist, structureMapper.getObjectMapper().rusById);
			break;
		case BU:
			updateObjectFromRow(flashlist, structureMapper.getObjectMapper().busById);
			break;
		case FEROL_INPUT_STREAM:
			updateObjectFromRow(flashlist, structureMapper.getObjectMapper().fedsById);
			break;
		case FMM_INPUT:
			updateObjectFromRow(flashlist, structureMapper.getObjectMapper().fedsById);
			break;

		case FEROL_STATUS:
			updateObjectFromRow(flashlist, structureMapper.getObjectMapper().ttcpartitionsById);
			break;
		default:
			break;
		}

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
	public <T extends FlashlistUpdatable> void updateObjectFromRow(Flashlist flashlist, Map<Integer, T> objectsById) {

		logger.info("Updating " + flashlist.getRowsNode().size() + " of " + flashlist.getFlashlistType() + " objects ("
				+ objectsById.size() + " in the structure)");

		for (JsonNode rowNode : flashlist.getRowsNode()) {
			try {
				int objectId = Integer.parseInt(rowNode.get(INSTANCE).asText());
				if (objectsById.containsKey(objectId)) {

					T flashlistUpdatableObject = objectsById.get(objectId);
					flashlistUpdatableObject.updateFromFlashlist(flashlist.getFlashlistType(), rowNode);

					logger.debug("Updated ru: " + flashlistUpdatableObject);

				} else {
					logger.warn("No DAQ object " + flashlist.getFlashlistType() + " with flashlist id " + objectId + ", ignoring..");
				}
			} catch (NumberFormatException e) {
				logger.warn("Instance number can not be parsed " + rowNode.get(INSTANCE));
			}
		}
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
