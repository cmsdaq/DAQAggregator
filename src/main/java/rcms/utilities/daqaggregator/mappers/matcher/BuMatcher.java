package rcms.utilities.daqaggregator.mappers.matcher;

import rcms.utilities.daqaggregator.data.BU;

public class BuMatcher extends HostnameMatcher<BU> {

	public BuMatcher(int sessionId, String flashlistKey) {
		this(sessionId, flashlistKey, false);
	}

	public BuMatcher(int sessionId, String flashlistKey, boolean ignoreFiltering) {
		super(sessionId, flashlistKey, ignoreFiltering);
	}

	@Override
	public String getHostname(BU e) {
		return e.getHostname();
	}

}
