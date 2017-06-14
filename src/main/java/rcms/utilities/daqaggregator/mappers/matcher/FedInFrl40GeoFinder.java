package rcms.utilities.daqaggregator.mappers.matcher;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.datasource.Flashlist;

/**
 * FED geolocation finder in FRL 40 tree
 * 
 * 
 */
public class FedInFrl40GeoFinder extends ThreeElementGeoMatcher<FED> {


	public FedInFrl40GeoFinder(int sessionId) {
		super(sessionId);
	}

	@Override
	public String getHostname(FED fed) {
		try {
			return fed.getFrl().getFrlPc().getHostname();
		} catch (NullPointerException e) {
			return null;
		}
	}

	@Override
	public Integer getGeoslot(FED fed) {
		try {
			return fed.getFrl().getGeoSlot();
		} catch (NullPointerException e) {
			return null;
		}
	}

	@Override
	public Integer getIO(FED fed) {
		return fed.getFrlIO();
	}

	@Override
	public String getFlashlistHostnameKey() {
		return "context";
	}

	@Override
	public String getFlashlistGeoslotKey() {
		return "slotNumber";
	}

	@Override
	public String getFlashlistIoKey() {
		return "streamNumber";
	}

}
