package rcms.utilities.daqaggregator.mappers.matcher;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.datasource.Flashlist;

public class SubsystemMatcher extends SessionFilteringMatcher<SubSystem> {

	private static final String key = "SUBSYS";

	private static final Logger logger = Logger.getLogger(SubsystemMatcher.class);

	public SubsystemMatcher(int sessionId) {
		super(sessionId);
	}

	@Override
	public Map<SubSystem, JsonNode> match(Flashlist flashlist, Collection<SubSystem> collection) {

		Map<SubSystem, JsonNode> dispatchMap = new HashMap<>();

		Map<String, SubSystem> subsystemByName = new HashMap<>();

		for (SubSystem subsystem : collection) {
			subsystemByName.put(subsystem.getName(), subsystem);
		}

		for (JsonNode rowNode : getRowsFilteredBySessionId(flashlist.getRowsNode(), flashlist.getFlashlistType())) {

			if (rowNode.has(key)) {
				if (rowNode.get(key) != null) {
					String flashlistSubsystemName = rowNode.get(key).asText();

					if (subsystemByName.containsKey(flashlistSubsystemName)) {
						SubSystem matchedSubsystem = subsystemByName.get(flashlistSubsystemName);
						dispatchMap.put(matchedSubsystem, rowNode);
						successful++;
					} else {
						logger.warn("Subsystem " + flashlistSubsystemName
								+ " existing in flashlist could not be found in the structure built based on hardware database");
						failed++;
					}

				} else {
					logger.warn("Flashlist cell for: " + key + " is empty");
					failed++;
				}
			} else {
				logger.warn("Flashlist has no column: " + key);
				failed++;
			}
		}
		return dispatchMap;
	}

}
