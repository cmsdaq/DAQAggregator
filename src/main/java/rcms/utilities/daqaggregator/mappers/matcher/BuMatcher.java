package rcms.utilities.daqaggregator.mappers.matcher;

import rcms.utilities.daqaggregator.data.BU;

public class BuMatcher extends HostnameMatcher<BU> {

	public BuMatcher(int sessionId, String flashlistKey) {
		super(sessionId, flashlistKey);
	}

	@Override
	public String getHostname(BU e) {
		return e.getHostname();
	}

}
