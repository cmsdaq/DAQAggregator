package rcms.utilities.daqaggregator.mappers.helper;

import rcms.utilities.daqaggregator.data.FED;

public class FedInFmmGeoFinder implements FedGeoFinder{

	@Override
	public String getHostname(FED fed) {
		try {
			return fed.getFmm().getFmmApplication().getHostname();
		} catch (NullPointerException e) {
			return null;
		}
	}

	@Override
	public Integer getGeoslot(FED fed) {
		try {
			return fed.getFmm().getGeoslot();
		} catch (NullPointerException e) {
			return null;
		}
	}

	@Override
	public Integer getIO(FED fed) {
		return fed.getFmmIO();
	}

}
