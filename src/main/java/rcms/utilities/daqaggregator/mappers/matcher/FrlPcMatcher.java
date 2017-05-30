package rcms.utilities.daqaggregator.mappers.matcher;

import rcms.utilities.daqaggregator.data.FRLPc;

public class FrlPcMatcher extends HostnameMatcher<FRLPc> {

	public FrlPcMatcher(int sessionId, String flashlistKey) {
		super(sessionId, flashlistKey);
	}

	@Override
	public String getHostname(FRLPc e) {
		return e.getHostname();
	}

}
