package rcms.utilities.daqaggregator.mappers.helper;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.datasource.Flashlist;

/**
 * FED geolocation finder in FMM tree
 * 
 * For more information see {@link ThreeElementGeoMatcher} documentation
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class FedInFmmGeoFinder extends ThreeElementGeoMatcher<FED> {

	@Override
	public String getHostname(FED fed) {
		try {
			return fed.getFmm().getFmmApplication().getHostname();
		} catch (NullPointerException e) {
			return null;
		}
	}

	@Override
	public Integer getGeoslot(FED fed) {
		try {
			return fed.getFmm().getGeoslot();
		} catch (NullPointerException e) {
			return null;
		}
	}

	@Override
	public Integer getIO(FED fed) {
		return fed.getFmmIO();
	}

	@Override
	public String getFlashlistHostnameKey() {
		return "hostname";
	}

	@Override
	public String getFlashlistGeoslotKey() {
		return "geoslot";
	}

	@Override
	public String getFlashlistIoKey() {
		return "io";
	}

}
