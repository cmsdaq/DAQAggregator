package rcms.utilities.daqaggregator.data;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.datasource.FlashlistType;
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
	public void clean() {
		this.status = "Unknown";

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((ttcPartitions == null) ? 0 : ttcPartitions.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SubSystem other = (SubSystem) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		if (ttcPartitions == null) {
			if (other.ttcPartitions != null)
				return false;
		} else if (!ttcPartitions.equals(other.ttcPartitions)) {
			System.out.println(ttcPartitions);
			System.out.println(other.ttcPartitions);
			ttcPartitions.equals(other.ttcPartitions);
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "SubSystem [name=" + name + ", status=" + status + ", ttcPartitions=" + ttcPartitions + "]";
	}

}
