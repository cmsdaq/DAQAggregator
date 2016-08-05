package rcms.utilities.daqaggregator.mappers.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.mappers.Flashlist;

/**
 * FED can be identified by its geolocation which consists of 3 elements:
 * hostname + geoslot + io
 * 
 * To find geolocation of FED one has to traverse snapshot structure (FED object
 * does not have fields necessary to define its location)
 * 
 * This is interface for fed geo finders (geolocation can be defined in fmm tree
 * or frl tree)
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public abstract class ThreeElementGeoMatcher<E> extends Matcher<E> {

	protected final Logger logger = Logger.getLogger(ThreeElementGeoMatcher.class);

	public abstract String getHostname(E fed);

	public abstract Integer getGeoslot(E fed);

	public abstract Integer getIO(E fed);

	public abstract String getFlashlistHostnameKey();

	public abstract String getFlashlistGeoslotKey();

	public abstract String getFlashlistIoKey();

	protected Map<String, Map<Integer, Map<Integer, JsonNode>>> prepareFlashlistMap(Flashlist flashlist) {
		Map<String, Map<Integer, Map<Integer, JsonNode>>> flashlistMap = new HashMap<>();

		for (JsonNode row : flashlist.getRowsNode()) {

			String hostname = row.get(this.getFlashlistHostnameKey()).asText();

			hostname = ContextHelper.getHostnameFromContext(hostname);
			Integer geoslot = row.get(this.getFlashlistGeoslotKey()).asInt();

			Integer io = row.get(this.getFlashlistIoKey()).asInt();

			if (!flashlistMap.containsKey(hostname)) {
				flashlistMap.put(hostname, new HashMap<Integer, Map<Integer, JsonNode>>());
			}

			if (!flashlistMap.get(hostname).containsKey(geoslot)) {
				flashlistMap.get(hostname).put(geoslot, new HashMap<Integer, JsonNode>());
			}

			flashlistMap.get(hostname).get(geoslot).put(io, row);

		}
		return flashlistMap;
	}

	@Override
	public Map<E, JsonNode> match(Flashlist flashlist, Collection<E> collection) {
		// 1 prepare map M of flashlists rows (hostname->geoslot->flashlit-row)
		// 2 traverse objects and get data from map M if available
		// 3 build map object->row

		Map<E, JsonNode> dispatchMap = new HashMap<>();

		Map<String, Map<Integer, Map<Integer, JsonNode>>> flashlistMap = this.prepareFlashlistMap(flashlist);

		int geoslots = 0;
		int ios = 0;
		for (Map<Integer, Map<Integer, JsonNode>> geoslotMap : flashlistMap.values()) {
			geoslots += geoslotMap.size();
			for (Map<Integer, JsonNode> ioMap : geoslotMap.values()) {
				ios += ioMap.size();
			}
		}
		logger.debug(flashlist.getRowsNode().size() + " flashlist rows mapped by hostname:geoslot:io "
				+ flashlistMap.size() + ":" + geoslots + ":" + ios);

		for (E findable : collection) {
			String hostname = this.getHostname(findable);
			Integer geoslot = this.getGeoslot(findable);
			Integer io = this.getIO(findable);

			if (hostname != null && geoslot != null && io != null) {
				try {
					JsonNode matchedRow = flashlistMap.get(hostname).get(geoslot).get(io);

					if (matchedRow != null) {
						dispatchMap.put(findable, matchedRow);
						successful++;
					} else {
						failed++;
						// row matched by hostname:geoslot but cannot match by
						// io
					}
				} catch (NullPointerException e) {
					// cannot match row by hostname and/or geoslot
					failed++;
				}
			} else {
				// object from hwdb cannot be identified by geolocation
				failed++;
			}
		}

		return dispatchMap;
	}
}
