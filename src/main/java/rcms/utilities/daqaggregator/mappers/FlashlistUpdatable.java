package rcms.utilities.daqaggregator.mappers;

import com.fasterxml.jackson.databind.JsonNode;

public interface FlashlistUpdatable {

	public void updateFromFlashlist(FlashlistType flashlistType, JsonNode flashlistRow);
}
