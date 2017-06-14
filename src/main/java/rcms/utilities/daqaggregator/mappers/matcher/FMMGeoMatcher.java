package rcms.utilities.daqaggregator.mappers.matcher;

import rcms.utilities.daqaggregator.data.FMM;

public class FMMGeoMatcher extends TwoElementGeoMatcher<FMM> {

	public FMMGeoMatcher(int sessionId) {
		super(sessionId);
	}

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
