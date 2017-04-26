package rcms.utilities.daqaggregator.mappers.helper;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.datasource.Flashlist;

/**
 * FED geolocation finder in FRL tree
 * 
 * For more information see {@link ThreeElementGeoMatcher} documentation
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class FedInFrlGeoFinder extends ThreeElementGeoMatcher<FED> {
	
	private final String ioKey;
	
	public FedInFrlGeoFinder(String ioKey){
		this.ioKey = ioKey;
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
		return ioKey;
	}

	@Override
	protected Map<String, Map<Integer, Map<Integer, JsonNode>>> prepareFlashlistMap(Flashlist flashlist) {
		Map<String, Map<Integer, Map<Integer, JsonNode>>> flashlistMap = new HashMap<>();

		for (JsonNode row : flashlist.getRowsNode()) {

			String hostname = row.get(this.getFlashlistHostnameKey()).asText();

			hostname = ContextHelper.getHostnameFromContext(hostname);
			Integer geoslot = row.get(this.getFlashlistGeoslotKey()).asInt();

			if (!flashlistMap.containsKey(hostname)) {
				flashlistMap.put(hostname, new HashMap<Integer, Map<Integer, JsonNode>>());
			}

			if (!flashlistMap.get(hostname).containsKey(geoslot)) {
				flashlistMap.get(hostname).put(geoslot, new HashMap<Integer, JsonNode>());
			}

			flashlistMap.get(hostname).get(geoslot).put(0, row);
			flashlistMap.get(hostname).get(geoslot).put(1, row);

		}
		return flashlistMap;
	}

}
