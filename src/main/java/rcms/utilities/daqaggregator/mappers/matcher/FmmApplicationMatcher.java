package rcms.utilities.daqaggregator.mappers.matcher;

import rcms.utilities.daqaggregator.data.FMMApplication;

public class FmmApplicationMatcher extends HostnameMatcher<FMMApplication> {

	public FmmApplicationMatcher(int sessionId, String flashlistKey, boolean ignoreFiltering) {
		super(sessionId, flashlistKey, ignoreFiltering);
	}

	@Override
	public String getHostname(FMMApplication e) {
		return e.getHostname();
	}

}
