package rcms.utilities.daqaggregator.mappers.matcher;

import rcms.utilities.daqaggregator.data.FMMApplication;

public class FmmApplicationMatcher extends HostnameMatcher<FMMApplication> {

	public FmmApplicationMatcher(int sessionId, String flashlistKey) {
		super(sessionId, flashlistKey);
	}

	@Override
	public String getHostname(FMMApplication e) {
		return e.getHostname();
	}

}
