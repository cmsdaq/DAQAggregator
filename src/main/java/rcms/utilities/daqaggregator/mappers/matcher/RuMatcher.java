package rcms.utilities.daqaggregator.mappers.matcher;

import rcms.utilities.daqaggregator.data.RU;

public class RuMatcher extends HostnameMatcher<RU> {

	public RuMatcher(int sessionId, String flashlistKey) {
		super(sessionId, flashlistKey);
	}

	@Override
	public String getHostname(RU e) {
		return e.getHostname();
	}

}
