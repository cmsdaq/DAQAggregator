package rcms.utilities.daqaggregator.mappers.matcher;

import rcms.utilities.daqaggregator.data.TTCPartition;

public class TTCPartitionGeoFinder extends TwoElementGeoMatcher<TTCPartition> {

	public TTCPartitionGeoFinder(int sessionId) {
		super(sessionId);
	}

	@Override
	public String getHostname(TTCPartition ttcp) {
		try {
			return ttcp.getFmm().getFmmApplication().getHostname();
		} catch (NullPointerException e) {
			return null;
		}
	}

	@Override
	public Integer getGeoslot(TTCPartition ttcp) {
		try {
			return ttcp.getFmm().getGeoslot();
		} catch (NullPointerException e) {
			return null;
		}
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
