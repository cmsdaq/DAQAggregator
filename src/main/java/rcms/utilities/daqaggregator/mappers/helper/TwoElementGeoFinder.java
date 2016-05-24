package rcms.utilities.daqaggregator.mappers.helper;

import rcms.utilities.daqaggregator.data.FED;

public interface TwoElementGeoFinder {
	public String getHostname(FED fed);

	public Integer getGeoslot(FED fed);
}
