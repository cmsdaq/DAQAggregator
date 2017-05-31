package rcms.utilities.daqaggregator.mappers.matcher;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.datasource.Flashlist;
import rcms.utilities.daqaggregator.mappers.helper.ContextHelper;

/**
 * This is custom matcher for upgraded feds tcds state
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class TcdsTtsPiMatcher extends Matcher<FED> {

	private static final String HOSTNAME_PORT_KEY = "context";
	private static final String SERVICE_KEY = "service";

	private static final Logger logger = Logger.getLogger(TcdsTtsPiMatcher.class);

	public TcdsTtsPiMatcher() {
		super();
	}

	protected Map<String, Map<Integer, Map<String, Map<Integer, JsonNode>>>> prepareFlashlistMap(Flashlist flashlist) {
		Map<String, Map<Integer, Map<String, Map<Integer, JsonNode>>>> flashlistMap = new HashMap<>();

		for (JsonNode row : flashlist.getRowsNode()) {

			String context = row.get(HOSTNAME_PORT_KEY).asText();
			logger.info("Hostname plus port to split: " + context);

			String hostname = ContextHelper.getHostnameFromContext(context);
			Integer port = ContextHelper.getPortFromContext(context);
			logger.info("Result of splitting: " + hostname + ", port: " + port);

			hostname = ContextHelper.getHostnameFromContext(hostname);

			String serviceName = row.get(SERVICE_KEY).asText();

			if (!flashlistMap.containsKey(hostname)) {
				flashlistMap.put(hostname, new HashMap<Integer, Map<String, Map<Integer, JsonNode>>>());
			}

			if (!flashlistMap.get(hostname).containsKey(port)) {
				flashlistMap.get(hostname).put(port, new HashMap<String, Map<Integer, JsonNode>>());
			}

			if (!flashlistMap.get(hostname).get(port).containsKey(serviceName)) {
				flashlistMap.get(hostname).get(port).put(serviceName, new HashMap<Integer, JsonNode>());
			}

			flashlistMap.get(hostname).get(port).get(serviceName).put(1, row);
			flashlistMap.get(hostname).get(port).get(serviceName).put(2, row);
			flashlistMap.get(hostname).get(port).get(serviceName).put(3, row);
			flashlistMap.get(hostname).get(port).get(serviceName).put(4, row);
			flashlistMap.get(hostname).get(port).get(serviceName).put(5, row);
			flashlistMap.get(hostname).get(port).get(serviceName).put(6, row);
			flashlistMap.get(hostname).get(port).get(serviceName).put(7, row);
			flashlistMap.get(hostname).get(port).get(serviceName).put(8, row);
			flashlistMap.get(hostname).get(port).get(serviceName).put(9, row);
			flashlistMap.get(hostname).get(port).get(serviceName).put(10, row);

		}
		return flashlistMap;
	}

	@Override
	public Map<FED, JsonNode> match(Flashlist flashlist, Collection<FED> collection) {

		logger.info("Matching " + flashlist.getRowsNode().size() + " flashlist rows to " + collection.size()
				+ " FED objects");

		Map<FED, JsonNode> dispatchMap = new HashMap<>();

		Map<String, Map<Integer, Map<String, Map<Integer, JsonNode>>>> flashlistMap = this
				.prepareFlashlistMap(flashlist);

		for (FED findable : collection) {
			String hostname = findable.getFmm().getFmmApplication().getHostname();
			Integer port = findable.getFmm().getFmmApplication().getPort();
			String serviceName = findable.getFmm().getServiceName();
			Integer io = findable.getFmmIO();

			if (hostname != null && port != null && serviceName != null && io != null) {
				try {
					JsonNode matchedRow = flashlistMap.get(hostname).get(port).get(serviceName).get(io);

					if (matchedRow != null) {
						dispatchMap.put(findable, matchedRow);
						successful++;
					} else {
						failed++;
						logger.warn("IO mismatch. There is no data in flashlist for fed: " + findable.getSrcIdExpected()
								+ ", [hostname,port,service,io] = [" + hostname + "," + port + "," + serviceName + ","
								+ io + "]");
					}
				} catch (NullPointerException e) {
					logger.warn("General mismatch. There is no data in flashlist for fed: "
							+ findable.getSrcIdExpected() + ", [hostname,port,service,io] = [" + hostname + "," + port
							+ "," + serviceName + "," + io + "]");
					failed++;
				}
			} else {
				logger.warn("Coulcd not get geo information for fed: " + findable.getSrcIdExpected()
						+ ", [hostname,port,service,io] = [" + hostname + "," + port + "," + serviceName + "," + io
						+ "]");
				failed++;
			}
		}

		logger.info("Matching completed with " + successful + " and " + failed + " matches");

		return dispatchMap;
	}

}
