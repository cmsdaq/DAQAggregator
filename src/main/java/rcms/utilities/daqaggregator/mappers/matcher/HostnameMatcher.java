package rcms.utilities.daqaggregator.mappers.matcher;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.datasource.Flashlist;
import rcms.utilities.daqaggregator.mappers.helper.ContextHelper;

public abstract class HostnameMatcher<E> extends SessionFilteringMatcher<E> {

	private static final Logger logger = Logger.getLogger(HostnameMatcher.class);
	private final String flashlistKey;

	public abstract String getHostname(E e);

	public HostnameMatcher(int sessionId, String flashlistKey) {
		super(sessionId);
		this.flashlistKey = flashlistKey;
	}

	@Override
	public Map<E, JsonNode> match(Flashlist flashlist, Collection<E> collection) {

		Map<E, JsonNode> dispatchMap = new HashMap<>();

		Map<String, E> objectsByHostname = new HashMap<>();
		for (E object : collection) {
			String hostname = getHostname(object);
			objectsByHostname.put(hostname, object);
		}

		for (JsonNode row : getRowsFilteredBySessionId(flashlist.getRowsNode(), flashlist.getFlashlistType())) {

			String hostname = row.get(flashlistKey).asText();
			hostname = ContextHelper.getHostnameFromContext(hostname);
			if (objectsByHostname.containsKey(hostname)) {
				dispatchMap.put(objectsByHostname.get(hostname), row);
				successful++;
			} else {
				logger.debug("Cannot find object " + hostname + " by name in " + objectsByHostname.keySet());
				failed++;
			}

		}

		return dispatchMap;
	}

}
