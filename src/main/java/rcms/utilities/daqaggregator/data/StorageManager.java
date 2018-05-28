package rcms.utilities.daqaggregator.data;

import java.util.Objects;

/**
 * class to keep information about storage manager occupancy (and more in the future).
 */
public class StorageManager {

	/** fraction (0..1) of occupied disk space in the storage manager */
	private Float occupancyFraction;

	/** @return the fraction (0..1) of occupied disk space in the storage manager */
	public Float getOccupancyFraction() {
		return occupancyFraction;
	}

	public void setOccupancyFraction(Float occupancy) {
		this.occupancyFraction = occupancy;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		StorageManager that = (StorageManager) o;
		return Objects.equals(occupancyFraction, that.occupancyFraction);
	}

	@Override
	public int hashCode() {

		return Objects.hash(occupancyFraction);
	}
}
