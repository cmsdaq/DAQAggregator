package rcms.utilities.daqaggregator.data;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.mappers.FlashlistType;
import rcms.utilities.daqaggregator.mappers.FlashlistUpdatable;

public class SubSystem implements FlashlistUpdatable {

	private String name;

	private String status;

	private Set<TTCPartition> ttcPartitions = new HashSet<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Set<TTCPartition> getTtcPartitions() {
		return ttcPartitions;
	}

	public void setTtcPartitions(Set<TTCPartition> ttcPartitions) {
		this.ttcPartitions = ttcPartitions;
	}

	@Override
	public void updateFromFlashlist(FlashlistType flashlistType, JsonNode flashlistRow) {

		if (flashlistType == FlashlistType.LEVEL_ZERO_FM_SUBSYS) {
			this.status = flashlistRow.get("STATE").asText();
		}
	}

	@Override
	public String toString() {
		return name;
	}

}
