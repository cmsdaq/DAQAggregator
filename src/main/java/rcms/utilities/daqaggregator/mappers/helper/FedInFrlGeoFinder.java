package rcms.utilities.daqaggregator.mappers.helper;

import rcms.utilities.daqaggregator.data.FED;

public class FedInFrlGeoFinder implements FedGeoFinder {

	@Override
	public String getHostname(FED fed) {
		try {
			return fed.getFrl().getSubFedbuilder().getFrlPc().getHostname();
		} catch (NullPointerException e) {
			return null;
		}
	}

	@Override
	public Integer getGeoslot(FED fed) {
		try {
			return fed.getFrl().getGeoSlot();
		} catch (NullPointerException e) {
			return null;
		}
	}

	@Override
	public Integer getIO(FED fed) {
		return fed.getFrlIO();
	}

}
