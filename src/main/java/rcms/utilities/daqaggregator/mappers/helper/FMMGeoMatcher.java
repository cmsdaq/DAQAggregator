package rcms.utilities.daqaggregator.mappers.helper;

import rcms.utilities.daqaggregator.data.FMM;

public class FMMGeoMatcher extends TwoElementGeoMatcher<FMM> {

	@Override
	public String getHostname(FMM fmm) {
		try {
			return fmm.getFmmApplication().getHostname();
		} catch (NullPointerException e) {
			return null;
		}
	}

	@Override
	public Integer getGeoslot(FMM fmm) {
		return fmm.getGeoslot();
	}

	@Override
	public String getFlashlistHostnameKey() {
		return "context";
	}

	@Override
	public String getFlashlistGeoslotKey() {
		return "geoslot";
	}

	

}
