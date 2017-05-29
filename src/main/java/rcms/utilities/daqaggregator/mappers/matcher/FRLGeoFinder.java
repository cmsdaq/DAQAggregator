package rcms.utilities.daqaggregator.mappers.matcher;

import rcms.utilities.daqaggregator.data.FRL;

public class FRLGeoFinder extends TwoElementGeoMatcher<FRL> {

	public FRLGeoFinder(int sessionId) {
		super(sessionId);
	}

	@Override
	public String getHostname(FRL object) {
		try {
			return object.getFrlPc().getHostname();
		} catch (NullPointerException e) {
			return null;
		}
	}

	@Override
	public Integer getGeoslot(FRL object) {
		return object.getGeoSlot();
	}

	@Override
	public String getFlashlistHostnameKey() {
		return "context";
	}

	@Override
	public String getFlashlistGeoslotKey() {
		return "slotNumber";
	}

}
