package rcms.utilities.daqaggregator.mappers.matcher;

import rcms.utilities.daqaggregator.data.FRLPc;

public class FrlPcMatcher extends HostnameMatcher<FRLPc> {

	public FrlPcMatcher(int sessionId, String flashlistKey) {
		this(sessionId, flashlistKey, false);
	}

	public FrlPcMatcher(int sessionId, String flashlistKey, boolean ignoreFiltering) {
		super(sessionId, flashlistKey, ignoreFiltering);
	}

	@Override
	public String getHostname(FRLPc e) {
		return e.getHostname();
	}

}
