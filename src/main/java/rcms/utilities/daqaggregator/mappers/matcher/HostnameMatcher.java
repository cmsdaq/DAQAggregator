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
		super(sessionId, false);
		this.flashlistKey = flashlistKey;
	}
	
	public HostnameMatcher(int sessionId, String flashlistKey, boolean ignoreFiltering) {
		super(sessionId, ignoreFiltering);
		this.flashlistKey = flashlistKey;
	}

	private static final String CMS_DOMAIN = ".cms";
	private static final String CMS904_DOMAIN = ".cms904";

	private String hostStrip(String fullHostname){

		if(fullHostname.toLowerCase().endsWith(CMS_DOMAIN)){
			fullHostname = fullHostname.substring(0, fullHostname.length() - CMS_DOMAIN.length());
		}

		if(fullHostname.toLowerCase().endsWith(CMS904_DOMAIN)){
			fullHostname = fullHostname.substring(0, fullHostname.length() - CMS904_DOMAIN.length());
		}

		return fullHostname;
	}

	@Override
	public Map<E, JsonNode> match(Flashlist flashlist, Collection<E> collection) {

		Map<E, JsonNode> dispatchMap = new HashMap<>();

		Map<String, E> objectsByHostname = new HashMap<>();
		for (E object : collection) {
			String hostname = getHostname(object);
			hostname = hostStrip(hostname);
			objectsByHostname.put(hostname, object);
		}

		for (JsonNode row : getRowsFilteredBySessionId(flashlist.getRowsNode(), flashlist.getFlashlistType())) {

			String hostname = row.get(flashlistKey).asText();
			hostname = ContextHelper.getHostnameFromContext(hostname);
			hostname = hostStrip(hostname);
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
