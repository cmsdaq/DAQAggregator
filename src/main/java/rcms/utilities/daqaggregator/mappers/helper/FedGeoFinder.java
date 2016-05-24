package rcms.utilities.daqaggregator.mappers.helper;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.data.FED;

public interface FedGeoFinder {

	public String getHostname(FED fed);

	public Integer getGeoslot(FED fed);

	public Integer getIO(FED fed);
	
}
