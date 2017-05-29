package rcms.utilities.daqaggregator.mappers.matcher;

import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.datasource.Flashlist;

public abstract class Matcher<E> {

	/**
	 * Match flashlist row to appropriate object
	 * 
	 * @param flashlist
	 *            flashlist with rows to match
	 * @param collection
	 *            collection of objects to match
	 * @return map of object-row matched
	 */
	public abstract Map<E, JsonNode> match(Flashlist flashlist, Collection<E> collection);

	protected int failed = 0;
	protected int successful = 0;

	public int getSuccessful() {
		return successful;
	}

	public int getFailded() {
		return failed;
	}
}
