package rcms.utilities.daqaggregator.datasource;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.DAQException;
import rcms.utilities.daqaggregator.DAQExceptionCode;

/**
 * Class to fetch (c|l)pm controller information (to be used at the beginning of
 * aggregation)
 *
 * @author Michail Vougioukas (michail.vougioukas@cern.ch)
 */

public class TCDSFMInfoRetriever {

	private static final Logger logger = Logger.getLogger(TCDSFMInfoRetriever.class);

	private final FlashlistRetriever flashlistRetriever;

	private String tcdsfm_fmUrl;
	private String tcdsfm_pmContext;
	private int tcdsfm_pmLid;
	private String tcdsfm_pmService;

	public TCDSFMInfoRetriever(FlashlistRetriever flashlistRetriever) {
		this.flashlistRetriever = flashlistRetriever;
	}

	/**
	 * Gets TCDS info from flashlists
	 */
	protected void aggregateInformation() {

		if (FlashlistType.TCDSFM.getUrl() != null) {

			Pair<Flashlist, String> tcdsFmRetrieveResult = flashlistRetriever.retrieveFlashlist(FlashlistType.TCDSFM);

			Flashlist tcdsFmFlashlist = tcdsFmRetrieveResult.getLeft();

			setTcdsFmFlashlistValues(tcdsFmFlashlist);

		} else {
			throw new DAQException(DAQExceptionCode.FlashlistNotFound, "The url of flashlist " + FlashlistType.TCDSFM
					+ " is unknown. Possibly because it was not discovered from LAS urls set.");
		}

	}

	/** Obtains TCDS function manager related quantities from the given
	 *  flashlist.
	 *
	 * This method is package private so we can use it in tests (but subclasses
	 * can not access it).
	 */
	void setTcdsFmFlashlistValues(Flashlist flashlist) {

		try {
			if (flashlist == null)
				throw new DAQException(DAQExceptionCode.FlashlistNull, "");
			if (flashlist.getFlashlistType() != FlashlistType.TCDSFM)
				throw new DAQException(DAQExceptionCode.WrongFlaslhist,
						"Wrong flashlist type: " + flashlist.getFlashlistType() + " (TCDFM expected)");
			if (flashlist.isUnknownAtLAS()) {
				throw new DAQException(DAQExceptionCode.EmptyFlashlistDetectingSession,
						"TCDSFM flashlist was not downloaded correctly, either due to a bad request or due to not having been found at LAS"
								+ flashlist.getRetrievalDate());
			}
			if (flashlist.getRowsNode() == null || flashlist.getRowsNode().size() == 0)
				throw new DAQException(DAQExceptionCode.EmptyFlashlistDetectingSession,
						"Empty TCDSFM flashlist at timestamp " + flashlist.getRetrievalDate());

			this.tcdsfm_fmUrl = flashlist.getRowsNode().get(0).get("FMURL").asText();
			this.tcdsfm_pmContext = flashlist.getRowsNode().get(0).get("pmContext").asText();
			this.tcdsfm_pmLid = flashlist.getRowsNode().get(0).get("pmLID").asInt();
			this.tcdsfm_pmService = flashlist.getRowsNode().get(0).get("pmService").asText();

		} catch (DAQException e) {
			logger.warn(e.getMessage());
		}
	}

	public String getTcdsfm_fmUrl() {
		return tcdsfm_fmUrl;
	}

	public String getTcdsfm_pmContext() {
		return tcdsfm_pmContext;
	}

	public int getTcdsfm_pmLid() {
		return tcdsfm_pmLid;
	}

	public String getTcdsfm_pmService() {
		return tcdsfm_pmService;
	}

	public boolean isInfoAvailable() {
		if (this.tcdsfm_fmUrl == null || this.tcdsfm_pmContext == null || this.tcdsfm_pmService == null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Works like the session detector but inverse logic: first store old
	 * values, then update object, then compare
	 */
	public boolean detectNewTrigger() {

		boolean detectedChange = false;
		long start = System.currentTimeMillis();
		try {

			/* current values of fields */
			// String tcdsfm_fmUrl_old = this.tcdsfm_fmUrl; //currently not used
			// to identify trigger
			String tcdsfm_pmContext_old = this.tcdsfm_pmContext;
			int tcdsfm_pmLid_old = this.tcdsfm_pmLid;
			String tcdsfm_pmService_old = this.tcdsfm_pmService;

			/* following line will overwrite fields */
			this.aggregateInformation(); // updates
											// fields on
											// this

			// There is no need to specifically check the first trigger, because
			// structure will already be updated by the first session
			if (tcdsfm_pmContext_old != null && tcdsfm_pmLid_old != 0 && tcdsfm_pmService_old != null) {

				if (this.tcdsfm_pmContext != null) {
					// comparison makes sense
					if (!this.tcdsfm_pmContext.equals(tcdsfm_pmContext_old)) {
						detectedChange = true;
					}
				} else {
					logger.warn(
							"Auto-detecting trigger change: a new tcdsfm value (PM Context) is null, no trigger change can be deduced");
				}

				if (this.tcdsfm_pmLid != 0) {
					// comparison makes sense
					if (this.tcdsfm_pmLid != tcdsfm_pmLid_old) {
						detectedChange = true;
					}
				} else {
					logger.warn(
							"Auto-detecting trigger change: a new tcdsfm value (PM Lid) is 0, no trigger change can be deduced");
				}

				if (this.tcdsfm_pmService != null) {
					// comparison makes sense
					if (!this.tcdsfm_pmService.equals(tcdsfm_pmService_old)) {
						detectedChange = true;
					}
				} else {
					logger.warn(
							"Auto-detecting trigger change: a new tcdsfm value (PM Service) is null, no trigger change can be deduced");
				}

			} else {
				/*
				 * this is usually the case at first iteration and will never
				 * occur after the first successful values setting from
				 * flashlist (which should happen in the first iteration,
				 * otherwise something is wrong with the flashlist)
				 */

				logger.debug(
						"Auto-detecting trigger change: an old tcdsfm value is null, no trigger change can be deduced");

				logger.info("N/A (expected in first iteration, check TCDSFM flashlist at LAS otherwise)");
			}

			/*
			 * logger.info("Auto-detecting trigger finished in " +
			 * timeToAutoDetect + " ms with detected change: " +
			 * detectedChange);
			 */
		} catch (DAQException exception) {

		}

		long end = System.currentTimeMillis();
		int timeToAutoDetect = (int) (end - start);

		if (timeToAutoDetect > 1000 || detectedChange) {
			logger.info("Auto-detecting trigger change finished in " + timeToAutoDetect + " ms with detected change: "
					+ detectedChange);
		}
		return detectedChange;
	}

}
