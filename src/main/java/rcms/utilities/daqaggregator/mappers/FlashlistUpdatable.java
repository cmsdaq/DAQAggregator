package rcms.utilities.daqaggregator.mappers;

import com.fasterxml.jackson.databind.JsonNode;

public interface FlashlistUpdatable {

	/**
	 * Update field(s) of object from given row of flashlist
	 * @param flashlistType type of flashlist so that object knows what data to expect
	 * @param flashlistRow row of flashlist
	 */
	public void updateFromFlashlist(FlashlistType flashlistType, JsonNode flashlistRow);
}
