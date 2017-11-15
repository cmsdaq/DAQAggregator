package rcms.utilities.daqaggregator.data;

/**
 * class to keep information about storage manager occupancy etc.
 */
public class StorageManager {
	
	/** fraction (0..1) of occupied disk space in the storage manager */
	private Float occupancyFraction;
	
	/** timestamp associated to the retrieved occupancy level */ 
	private Long occupancyLastUpdate;

	public Float getOccupancyFraction() {
		return occupancyFraction;
	}

	public void setOccupancyFraction(Float occupancy) {
		this.occupancyFraction = occupancy;
	}

	public Long getOccupancyLastUpdate() {
		return occupancyLastUpdate;
	}

	public void setOccupancyLastUpdate(Long occupancyLastUpdate) {
		this.occupancyLastUpdate = occupancyLastUpdate;
	}
	
}
