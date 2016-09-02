package rcms.utilities.daqaggregator.datasource;

import java.util.Map;

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
	public Map<FlashlistType, Flashlist> retrieveAllFlashlists();

	/**
	 * Retrieve specific flashlist
	 * 
	 * @param flashlistType
	 *            flashlist type
	 * @return retrieved flashlist
	 */
	public Flashlist retrieveFlashlist(FlashlistType flashlistType);

	
	public void retrieveAvailableFlashlists(int sessionId);
}
