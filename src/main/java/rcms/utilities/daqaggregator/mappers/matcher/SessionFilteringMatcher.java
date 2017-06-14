package rcms.utilities.daqaggregator.mappers.matcher;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.datasource.FlashlistType;

public abstract class SessionFilteringMatcher<E> extends Matcher<E> {

	private static final Logger logger = Logger.getLogger(SessionFilteringMatcher.class);

	private final int sessionId;

	private final boolean ignoreFiltering;

	private int filtered;

	public SessionFilteringMatcher(int sessionId) {
		this(sessionId, false);
	}

	public SessionFilteringMatcher(int sessionId, boolean ignoreFiltering) {
		super();
		this.sessionId = sessionId;
		this.filtered = 0;
		this.ignoreFiltering = ignoreFiltering;
	}

	protected List<JsonNode> getRowsFilteredBySessionId(JsonNode rowsToFilter, FlashlistType flashlistType) {
		List<JsonNode> result = new ArrayList<>();
		logger.debug("Before the sid filter: " + rowsToFilter.size());
		filtered = 0;

		for (JsonNode rowNode : rowsToFilter) {

			if (flashlistType.isSessionContext() && !ignoreFiltering) {
				if (flashlistType.getSessionIdColumnName() != null) {
					if (rowNode.has(flashlistType.getSessionIdColumnName())) {
						try {
							int rowSessionContext = rowNode.get(flashlistType.getSessionIdColumnName()).asInt();
							if (rowSessionContext == sessionId) {
								result.add(rowNode);
							} else {
								filtered++;
								logger.debug("Ignoring row of " + flashlistType + " with SID " + rowSessionContext
										+ ", expecting " + sessionId);
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
		logger.debug("After the sid filter: " + result.size());

		return result;
	}

	public int getSessionId() {
		return sessionId;
	}

	public int getFiltered() {
		return filtered;
	}

}
