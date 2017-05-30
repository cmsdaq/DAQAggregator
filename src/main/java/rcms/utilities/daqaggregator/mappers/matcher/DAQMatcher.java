package rcms.utilities.daqaggregator.mappers.matcher;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.datasource.Flashlist;

public class DAQMatcher extends SessionFilteringMatcher<DAQ> {

	private static final Logger logger = Logger.getLogger(DAQMatcher.class);

	public DAQMatcher(int sessionId) {
		super(sessionId);
	}

	@Override
	public Map<DAQ, JsonNode> match(Flashlist flashlist, Collection<DAQ> collection) {

		Map<DAQ, JsonNode> dispatchMap = new HashMap<>();

		/* there is always one daq in the structure */
		DAQ daq = collection.iterator().next();

		List<JsonNode> filteredRows = getRowsFilteredBySessionId(flashlist.getRowsNode(), flashlist.getFlashlistType());

		if (filteredRows.size() != 1) {
			logger.warn("More than one row filtered based on session id in flashlist " + flashlist.getFlashlistType()
					+ ": " + filteredRows.size() + " , expected 1");
		}

		for (JsonNode rowNode : filteredRows) {
			dispatchMap.put(daq, rowNode);
		}

		return dispatchMap;
	}

}
