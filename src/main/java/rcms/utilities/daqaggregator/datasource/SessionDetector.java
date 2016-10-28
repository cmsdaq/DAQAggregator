package rcms.utilities.daqaggregator.datasource;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.log4j.Logger;

/**
 * Detects changes in session
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class SessionDetector {

	private final SessionRetriever sessionRetriever;

	private final FlashlistRetriever flashlistRetriever;

	public SessionDetector(SessionRetriever sessionRetriever, FlashlistRetriever flashlistRetriever) {
		this.sessionRetriever = sessionRetriever;
		this.flashlistRetriever = flashlistRetriever;
	}

	private Triple<String, Integer, Long> lastResult;

	private static final Logger logger = Logger.getLogger(SessionDetector.class);

	/**
	 * Detect new session
	 * 
	 * @return true if new session or hardware configuration detected, false
	 *         otherwise
	 */
	public boolean detectNewSession() {

		boolean detectedChange = false;

		Pair<Flashlist, Integer> levelZeroRetrieveResult = flashlistRetriever
				.retrieveFlashlist(FlashlistType.LEVEL_ZERO_FM_STATIC);

		Flashlist levelZeroStaticFlashist = levelZeroRetrieveResult.getLeft();
		int timeToAutoDetect = levelZeroRetrieveResult.getRight();

		Triple<String, Integer, Long> result = sessionRetriever.retrieveSession(levelZeroStaticFlashist);

		if (lastResult == null) {
			logger.info("Detected first session " + result);
			detectedChange = true;
		} else {
			if (!lastResult.getMiddle().equals(result.getMiddle())) {
				logger.info("Detected new session: " + result.getMiddle());
				detectedChange = true;
			} else {
				logger.trace("Session has not changed");
			}
			if (!lastResult.getLeft().equals(result.getLeft())) {
				logger.info("Detected new HWCFG_KEY: " + result.getLeft());
				detectedChange = true;
			} else {
				logger.trace("HWCFG_KEY has not changed");
			}
		}

		lastResult = result;
		logger.info("Auto-detecting session finished in " + timeToAutoDetect + " ms with detected change: "
				+ detectedChange);

		return detectedChange;
	}

	public Triple<String, Integer, Long> getResult() {
		return lastResult;
	}

}
