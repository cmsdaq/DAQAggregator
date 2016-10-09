package rcms.utilities.daqaggregator.datasource;

import java.util.Date;
import java.util.Iterator;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.DAQException;
import rcms.utilities.daqaggregator.DAQExceptionCode;

/**
 * Retrieves session information from flashlist LEVEL_ZERO_STATIC
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class SessionRetriever {

	protected static final String FMURL_COLUMN_NAME = "FMURL";
	protected static final String TIMESTAMP_COLUMN_NAME = "timestamp";
	protected static final String SID_COLUMN_NAME = "SID";
	protected static final String HWKEY_COLUMN_NAME = "HWCFG_KEY";

	private final String filter1;
	private final String filter2;

	private static final Logger logger = Logger.getLogger(SessionRetriever.class);

	protected static final String EXCEPTION_MISSING_ROW_MESSAGE = "Could not find the appropriate row in flashist LEVEL_ZERO_STATIC to determine session";
	protected static final String EXCEPTION_OTHER_PROBLEM_MESSAGE = "Problem detecting the session";
	protected static final String EXCEPTION_PARSING_DATE_PROBLEM_MESSAGE = "Could not parse timestamp from flashlist data";
	protected static final String EXCEPTION_NO_DATA_MESSAGE = "Flashlist has no data";
	protected static final String EXCEPTION_FLASHLIST_NULL_MESSAGE = "Flashlist cannot be null";
	protected static final String EXCEPTION_WRONG_FLASHLIST_MESSAGE = "Wrong flashlist type supplied, expected level zero static";

	public SessionRetriever(String filter1, String filter2) {
		this.filter1 = filter1;
		this.filter2 = filter2;
	}

	/**
	 * Retrieve session information from flashlist
	 * 
	 * @param flashlist
	 * @return triple containing session id, hardware configuration key,
	 *         timestamp of last update
	 */
	public Triple<String, Integer, Long> retrieveSession(Flashlist flashlist) {

		if (flashlist == null)
			throw new RuntimeException(EXCEPTION_FLASHLIST_NULL_MESSAGE);
		if (flashlist.getFlashlistType() != FlashlistType.LEVEL_ZERO_FM_STATIC)
			throw new RuntimeException(EXCEPTION_WRONG_FLASHLIST_MESSAGE);
		if (flashlist.getRowsNode() == null || flashlist.getRowsNode().size() == 0)
			throw new DAQException(DAQExceptionCode.SessionCannotBeRetrieved, EXCEPTION_NO_DATA_MESSAGE + " @timestamp " + flashlist.getRetrievalDate());

		Iterator<JsonNode> rowIterator = flashlist.getRowsNode().iterator();
		Triple<String, Integer, Long> result = null;
		boolean foundRowSatisfyingFilters = false;

		while (rowIterator.hasNext()) {
			JsonNode row = rowIterator.next();

			String fmUrl = row.get(FMURL_COLUMN_NAME).asText();

			/* Retrieve data only from rows containing filters */
			if (fmUrl.contains(filter1) && fmUrl.contains(filter2)) {
				foundRowSatisfyingFilters = true;

				long timestamp = parseTimestamp(row.get(TIMESTAMP_COLUMN_NAME).asText());
				int sid = row.get(SID_COLUMN_NAME).asInt();
				String hwcfgKey = row.get(HWKEY_COLUMN_NAME).asText();

				logger.debug(timestamp + ", " + hwcfgKey + ", " + sid + ", " + fmUrl);

				String dpsetPath = hwcfgKey.split(":")[0];
				result = Triple.of(dpsetPath, sid, timestamp);
			}

		}

		if (!foundRowSatisfyingFilters) {
			throw new DAQException(DAQExceptionCode.SessionCannotBeRetrieved, EXCEPTION_MISSING_ROW_MESSAGE);
		}
		if (result != null) {
			logger.debug("Result of " + result);
		} else {
			throw new RuntimeException(EXCEPTION_OTHER_PROBLEM_MESSAGE);
		}
		return result;

	}

	private long parseTimestamp(String timestampString) {
		logger.debug("Parsing date from string: " + timestampString);
		Date date = DatatypeConverter.parseDateTime(timestampString).getTime();
		logger.debug("Parsed date: " + date + ", from string: " + timestampString);
		return date.getTime();
	}
}
