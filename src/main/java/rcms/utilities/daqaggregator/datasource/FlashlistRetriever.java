package rcms.utilities.daqaggregator.datasource;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Flashlist retriever flashlists
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public interface FlashlistRetriever {

	/**
	 * Retrieve all flashlists
	 * 
	 * @return set of retrieved flashlists
	 */
	public Map<FlashlistType, Flashlist> retrieveAllFlashlists(int sessionId);

	/**
	 * Retrieve specific flashlist
	 * 
	 * @param flashlistType
	 *            flashlist type
	 * @return retrieved flashlist and time to retrieve
	 */
	public Pair<Flashlist, String> retrieveFlashlist(FlashlistType flashlistType);

}
