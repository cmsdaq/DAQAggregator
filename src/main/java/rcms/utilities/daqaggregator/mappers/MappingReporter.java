package rcms.utilities.daqaggregator.mappers;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class MappingReporter {

	private static MappingReporter instance;

	private Logger logger = Logger.getLogger(MappingReporter.class);

	private final Map<String, Integer> missingObjects;
	private final Map<String, Integer> totalObjects;

	public static MappingReporter get() {
		if (instance == null)
			instance = new MappingReporter();
		return instance;
	}

	public void clear() {
		missingObjects.clear();
		totalObjects.clear();
	}

	public MappingReporter() {
		missingObjects = new HashMap<>();
		totalObjects = new HashMap<>();
	}

	public void summarize() {
		int allMissingObjects = 0;
		int allTotalObjects = 0;
		for (Integer missing : missingObjects.values()) {
			allMissingObjects += missing;
		}
		for (Integer total : totalObjects.values()) {
			allTotalObjects += total;
		}
		logger.info("" + allMissingObjects + "/" + allTotalObjects
				+ " objects with warnings updating values from flashlists to hw structure");
	}

	public void detailedSummarize() {
		StringBuilder sb = new StringBuilder();
		for (String key : missingObjects.keySet()) {
			sb.append(key);
			sb.append(":");
			sb.append(missingObjects.get(key));
			sb.append("/");
			sb.append(totalObjects.get(key));
			sb.append(", ");
		}
		logger.info("Detailed flashlist-hw mapping report (warnings): " + sb.toString());
	}

	public void increaseMissing(String key, int number) {
		increaseMap(missingObjects, key, number);
	}

	public void increaseTotal(String key, int number) {
		increaseMap(totalObjects, key, number);
	}

	private void increaseMap(Map<String, Integer> map, String key, int number) {
		if (!map.containsKey(key)) {
			map.put(key, 0);
		}
		map.put(key, map.get(key) + number);
	}

}
