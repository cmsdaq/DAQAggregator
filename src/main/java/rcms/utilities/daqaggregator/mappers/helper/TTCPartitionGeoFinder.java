package rcms.utilities.daqaggregator.mappers.helper;

import rcms.utilities.daqaggregator.data.TTCPartition;

public class TTCPartitionGeoFinder extends TwoElementGeoMacher<TTCPartition> {

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
