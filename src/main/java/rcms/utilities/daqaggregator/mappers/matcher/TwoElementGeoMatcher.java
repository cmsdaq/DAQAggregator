package rcms.utilities.daqaggregator.mappers.matcher;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.datasource.Flashlist;
import rcms.utilities.daqaggregator.mappers.helper.ContextHelper;

/**
 * Some objects in DAQ snapshot structure can be identified by 2 elements
 * (geolocation): hostname + geoslot.
 * 
 * To map this objects with data in flashlist geoslot must match. This is class
 * for all object-flashlist row matching.
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 * @param <E>
 *            Any object that can be located with 2 elements geolocation
 */
public abstract class TwoElementGeoMatcher<E> extends SessionFilteringMatcher<E> {

	public TwoElementGeoMatcher(int sessionId) {
		super(sessionId);
		// TODO Auto-generated constructor stub
	}

	private final Logger logger = Logger.getLogger(TwoElementGeoMatcher.class);

	public abstract String getHostname(E e);

	public abstract Integer getGeoslot(E e);

	public abstract String getFlashlistHostnameKey();

	public abstract String getFlashlistGeoslotKey();

	/**
	 * Match rows from flashlist to objects from collection
	 * 
	 * @param flashlist
	 * @param collection
	 * @return matched pairs object-flashlist_row
	 */
	// public abstract Map<E, JsonNode> match(Flashlist flashlist, Collection<E>
	// collection);

	

	@Override
	public Map<E, JsonNode> match(Flashlist flashlist, Collection<E> collection) {

		// 1 prepare map M of flashlists rows (hostname->geoslot->flashlit-row)
		// 2 traverse objects and get data from map M if available
		// 3 build map object->row

		Map<E, JsonNode> dispatchMap = new HashMap<>();

		Map<String, Map<Integer, JsonNode>> flashlistMap = new HashMap<>();

		for (JsonNode row : flashlist.getRowsNode()) {

			String hostname = row.get(this.getFlashlistHostnameKey()).asText();

			hostname = ContextHelper.getHostnameFromContext(hostname);
			Integer geoslot = row.get(this.getFlashlistGeoslotKey()).asInt();

			if (!flashlistMap.containsKey(hostname)) {
				flashlistMap.put(hostname, new HashMap<Integer, JsonNode>());
			}

			flashlistMap.get(hostname).put(geoslot, row);

		}
		int geoslots = 0;
		for (Map<Integer, JsonNode> geoslotMap : flashlistMap.values()) {
			geoslots += geoslotMap.size();
		}
		logger.debug(flashlist.getRowsNode().size() + " flashlist rows mapped by hostname:geoslot "
				+ flashlistMap.size() + ":" + geoslots);

		for (E findable : collection) {
			String hostname = this.getHostname(findable);
			Integer geoslot = this.getGeoslot(findable);

			if (hostname != null && geoslot != null) {
				try {
					JsonNode matchedRow = flashlistMap.get(hostname).get(geoslot);

					if (matchedRow != null) {
						dispatchMap.put(findable, matchedRow);
						successful++;
					} else {
						failed++;
						// row matched by hostname but cannot match by geoslot
					}
				} catch (NullPointerException e) {
					// cannot match row by hostname
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
