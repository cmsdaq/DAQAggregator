package rcms.utilities.daqaggregator.mappers.matcher;

import rcms.utilities.daqaggregator.data.RU;

public class RuMatcher extends HostnameMatcher<RU> {

	public RuMatcher(int sessionId, String flashlistKey) {
		this(sessionId, flashlistKey, false);
	}

	public RuMatcher(int sessionId, String flashlistKey, boolean ignoreFiltering) {
		super(sessionId, flashlistKey, ignoreFiltering);
	}

	@Override
	public String getHostname(RU e) {
		return e.getHostname();
	}

}
