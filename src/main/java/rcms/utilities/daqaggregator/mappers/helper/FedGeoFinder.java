package rcms.utilities.daqaggregator.mappers.helper;

import rcms.utilities.daqaggregator.data.FED;

public interface FedGeoFinder {

	public String getHostname(FED fed);

	public Integer getGeoslot(FED fed);

	public Integer getIO(FED fed);
	
}
