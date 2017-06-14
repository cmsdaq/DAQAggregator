package rcms.utilities.daqaggregator.mappers.matcher;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.datasource.Flashlist;

public class FedInErrorMatcher extends SessionFilteringMatcher<FED> {

	private static final Logger logger = Logger.getLogger(FedInErrorMatcher.class);

	public FedInErrorMatcher(int sessionId) {
		super(sessionId);
	}

	@Override
	public Map<FED, JsonNode> match(Flashlist flashlist, Collection<FED> collection) {

		Map<FED, JsonNode> fedToFlashlistRow = new HashMap<>();

		Map<Integer, FED> fedsByExpectedId = new HashMap<>();
		for (FED object : collection) {
			fedsByExpectedId.put(object.getSrcIdExpected(), object);
		}

		for (JsonNode row : getRowsFilteredBySessionId(flashlist.getRowsNode(), flashlist.getFlashlistType())) {
			if (row.get("fedIdsWithErrors").isArray()) {
				for (JsonNode fedIdWithErrors : row.get("fedIdsWithErrors")) {
					int fedId = fedIdWithErrors.asInt();
					if (fedsByExpectedId.containsKey(fedId)) {
						FED fed = fedsByExpectedId.get(fedId);
						fedToFlashlistRow.put(fed, row);

					} else {
						logger.debug(
								"FED with problem indicated by flashlist RU.fedIdsWithErrors could not be found by id "
										+ fedId);
					}
				}
				for (JsonNode fedIdWithoutFragment : row.get("fedIdsWithoutFragments")) {
					int fedId = fedIdWithoutFragment.asInt();
					if (fedsByExpectedId.containsKey(fedId)) {
						FED fed = fedsByExpectedId.get(fedId);
						fedToFlashlistRow.put(fed, row);

					} else {
						logger.debug(
								"FED with problem indicated by flashlist RU.fedIdsWithoutFragments could not be found by id "
										+ fedId);
					}
				}

			}

		}
		logger.debug("There are " + fedToFlashlistRow.size() + " FEDs with problems (according to RU flashlist)");

		return fedToFlashlistRow;
	}

}
