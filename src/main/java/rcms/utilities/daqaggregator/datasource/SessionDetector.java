package rcms.utilities.daqaggregator.datasource;

import java.io.IOException;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.RunMode;

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

		long start = System.currentTimeMillis();
		boolean detectedChange = false;

		Flashlist levelZeroStaticFlashist = flashlistRetriever.retrieveFlashlist(FlashlistType.LEVEL_ZERO_FM_STATIC);

		Triple<String, Integer, Long> result = sessionRetriever.retrieveSession(levelZeroStaticFlashist);

		if (lastResult == null) {
			logger.info("Detected first session " + result);
			detectedChange = true;
		} else {
			if (lastResult.getMiddle() != result.getMiddle()) {
				logger.info("Detected new session");
				detectedChange = true;
			} else {
				logger.info("Session has not changed");
			}
			if (!lastResult.getLeft().equals(result.getLeft())) {
				logger.info("Detected new HWCFG_KEY");
				detectedChange = true;
			} else {
				logger.info("HWCFG_KEY has not changed");
			}
		}

		lastResult = result;
		long end = System.currentTimeMillis();
		int timeToAutoDetect = (int) (end - start);
		logger.info("Auto-detecting session finished in " + timeToAutoDetect + " ms");

		return detectedChange;
	}

}
