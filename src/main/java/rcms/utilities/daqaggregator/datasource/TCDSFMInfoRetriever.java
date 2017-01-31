package rcms.utilities.daqaggregator.datasource;


import org.apache.commons.lang3.tuple.Pair;

import rcms.utilities.daqaggregator.DAQException;
import rcms.utilities.daqaggregator.DAQExceptionCode;

/**
 * Class to fetch (c|l)pm controller information (to be used at the beginning of aggregation)
 *
 * @author Michail Vougioukas (michail.vougioukas@cern.ch)
 */

public class TCDSFMInfoRetriever {

	private final FlashlistRetriever flashlistRetriever;
	
	private String tcdsfm_fmUrl;
	private String tcdsfm_pmContext = "http://tcds-control-cpm.cms:2050";
	private int tcdsfm_pmLid = 100;
	private String tcdsfm_pmService = "cpm-pri";

	public TCDSFMInfoRetriever(FlashlistRetriever flashlistRetriever) {
		this.flashlistRetriever = flashlistRetriever;
	}
	
	/**
	 * Gets TCDS info from flashlists
	 */
	protected void aggregateInformation() {

		Pair<Flashlist, Integer> tcdsFmRetrieveResult = flashlistRetriever
				.retrieveFlashlist(FlashlistType.TCDSFM);

		Flashlist tcdsFmFlashlist = tcdsFmRetrieveResult.getLeft();

		setTcdsFmFlashlistValues(tcdsFmFlashlist);
		  
	}
	
	private void setTcdsFmFlashlistValues(Flashlist flashlist) {
		if (flashlist == null)
			throw new DAQException(DAQExceptionCode.FlashlistNull, "");
		if (flashlist.getFlashlistType() != FlashlistType.TCDSFM)
			throw new DAQException(DAQExceptionCode.WrongFlaslhist,
					"Wrong flashlist type: " + flashlist.getFlashlistType()+" (TCDFM expected)");
		if (flashlist.isUnknownAtLAS()){
			throw new DAQException(DAQExceptionCode.EmptyFlashlistDetectingSession,
					"TCDSFM flashlist was not downloaded correctly, either due to a bad request or due to not having been found at LAS" + flashlist.getRetrievalDate());
		}
		if (flashlist.getRowsNode() == null || flashlist.getRowsNode().size() == 0)
			throw new DAQException(DAQExceptionCode.EmptyFlashlistDetectingSession,
					"Empty TCDSFM flashlist at timestamp " + flashlist.getRetrievalDate());
		
		this.tcdsfm_fmUrl = flashlist.getRowsNode().get(0).get("FMURL").asText();
		this.tcdsfm_pmContext = flashlist.getRowsNode().get(0).get("pmContext").asText();
		this.tcdsfm_pmLid = flashlist.getRowsNode().get(0).get("pmLID").asInt();
		this.tcdsfm_pmService = flashlist.getRowsNode().get(0).get("pmService").asText();
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
}
