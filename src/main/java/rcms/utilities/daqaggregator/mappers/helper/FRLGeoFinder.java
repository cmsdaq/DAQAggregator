package rcms.utilities.daqaggregator.mappers.helper;

import rcms.utilities.daqaggregator.data.FRL;

public class FRLGeoFinder implements HostnameGeoslotFinder<FRL> {

	@Override
	public String getHostname(FRL object) {
		try {
			return object.getSubFedbuilder().getFrlPc().getHostname();
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
