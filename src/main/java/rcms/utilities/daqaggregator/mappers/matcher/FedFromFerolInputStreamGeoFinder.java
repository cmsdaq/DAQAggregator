package rcms.utilities.daqaggregator.mappers.matcher;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.datasource.Flashlist;
import rcms.utilities.daqaggregator.mappers.helper.ContextHelper;

/**
 * FED geolocation finder to be used with the FEROL INPUT STREAM flashlist
 * 
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class FedFromFerolInputStreamGeoFinder extends ThreeElementGeoMatcher<FED> {
	
	private final String ioKey;
	
	public FedFromFerolInputStreamGeoFinder(String ioKey, int sessionId){
		super(sessionId);
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
			Integer ioKey = row.get(this.getFlashlistIoKey()).asInt();

			if (!flashlistMap.containsKey(hostname)) {
				flashlistMap.put(hostname, new HashMap<Integer, Map<Integer, JsonNode>>());
			}

			if (!flashlistMap.get(hostname).containsKey(geoslot)) {
				flashlistMap.get(hostname).put(geoslot, new HashMap<Integer, JsonNode>());
			}

			flashlistMap.get(hostname).get(geoslot).put(ioKey, row);

		}
		return flashlistMap;
	}

}
