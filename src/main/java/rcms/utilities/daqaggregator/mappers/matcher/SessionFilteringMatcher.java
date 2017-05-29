package rcms.utilities.daqaggregator.mappers.matcher;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.datasource.FlashlistType;

public abstract class SessionFilteringMatcher<E> extends Matcher<E> {

	private static final Logger logger = Logger.getLogger(SessionFilteringMatcher.class);

	private final int sessionId;
	
	public SessionFilteringMatcher(int sessionId){
		super();
		this.sessionId = sessionId;
	}

	protected List<JsonNode> getRowsFilteredBySessionId(JsonNode rowsToFilter, FlashlistType flashlistType,
			int expectedSession) {
		List<JsonNode> result = new ArrayList<>();
		logger.info("Before the sid filter: " + rowsToFilter.size());

		for (JsonNode rowNode : rowsToFilter) {

			if (flashlistType.isSessionContext()) {
				if (flashlistType.getSessionIdColumnName() != null) {
					if (rowNode.has(flashlistType.getSessionIdColumnName())) {
						try {
							int rowSessionContext = rowNode.get(flashlistType.getSessionIdColumnName()).asInt();
							if (rowSessionContext == expectedSession) {
								result.add(rowNode);
							} else {
								logger.info("Ignoring row of " + flashlistType + " with SID " + rowSessionContext
										+ ", expecting " + expectedSession);
							}
						} catch (NumberFormatException e) {
							logger.info(
									"Could not parse SID from: " + rowNode.get(flashlistType.getSessionIdColumnName()));
						}
					} else {
						logger.warn("Flashlist " + flashlistType + " has no such column "
								+ flashlistType.getSessionIdColumnName());
					}
				} else {
					logger.warn("Flashlist " + flashlistType
							+ " is defined as having session context but no session id column is defined");
				}
			} else {
				result.add(rowNode);
			}
		}
		logger.info("After the sid filter: " + result.size());

		return result;
	}

	public int getSessionId() {
		return sessionId;
	}

}
