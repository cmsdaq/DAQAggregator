package rcms.utilities.daqaggregator.mappers.helper;

import rcms.utilities.daqaggregator.data.FRL;

public class FRLGeoFinder extends TwoElementGeoMacher<FRL> {

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
