package rcms.utilities.daqaggregator.mappers.matcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FMM;
import rcms.utilities.daqaggregator.data.helper.FEDHelper;
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
			logger.debug("Hostname plus port to split: " + context);

			String hostname = ContextHelper.getHostnameFromContext(context);
			Integer port = ContextHelper.getPortFromContext(context);
			logger.debug("Result of splitting: " + hostname + ", port: " + port);

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

	/* deleteme */
	Collection<FED> all;

	@Override
	public Map<FED, JsonNode> match(Flashlist flashlist, Collection<FED> collection) {

		all = collection;
		logger.info("Matching " + flashlist.getRowsNode().size() + " flashlist rows to " + collection.size()
				+ " FED objects");

		List<String> report = new ArrayList<>();
		int all = 0, ignored = 0;
		for (JsonNode a : flashlist.getRowsNode()) {
			for (int i = 1; i <= 10; i++) {
				int b = a.get("tts_slot" + i).asInt();
				all++;
				if (b == 152) {
					ignored++;
				}
			}
		}
		logger.info("There are " + all + " TTS states in this flashlist. " + ignored + " of them has a ignored value");

		Map<FED, JsonNode> dispatchMap = new HashMap<>();

		Map<String, Map<Integer, Map<String, Map<Integer, JsonNode>>>> flashlistMap = this
				.prepareFlashlistMap(flashlist);

		for (FED findable : collection) {

			Pair<Integer, FMM> result = FEDHelper.getFMM(findable);
			FMM fmm = result.getRight();
			Integer fmmIO = result.getLeft();

			if (fmm != null) {
				String hostname = fmm.getFmmApplication().getHostname();
				Integer port = fmm.getFmmApplication().getPort();
				String serviceName = fmm.getServiceName();
				Integer io = fmmIO; // TODO;

				if (hostname != null && port != null && serviceName != null && io != null) {

					logger.debug("Dispatching fed: " + hostname + ":" + port + ":" + serviceName + ":" + io);
					try {
						JsonNode matchedRow = flashlistMap.get(hostname).get(port).get(serviceName).get(io);

						if (matchedRow != null) {
							report.add(formatFedToRaport("matched", findable, hostname, port.toString(), serviceName,
									io.toString()));

							dispatchMap.put(findable, matchedRow);
							successful++;
						} else {
							failed++;
							report.add(formatFedToRaport("n/m (missing io)", findable, hostname, port.toString(),
									serviceName, io.toString()));
							logger.warn("IO mismatch. There is no data in flashlist for fed: "
									+ findable.getSrcIdExpected() + ", [hostname,port,service,io] = [" + hostname + ","
									+ port + "," + serviceName + "," + io + "]");
						}
					} catch (NullPointerException e) {
						logger.warn("General mismatch. There is no data in flashlist for fed: "
								+ findable.getSrcIdExpected() + ", [hostname,port,service,io] = [" + hostname + ","
								+ port + "," + serviceName + "," + io + "]");
						failed++;
						report.add(formatFedToRaport("n/m (a, not matched)", findable, hostname, port.toString(),
								serviceName, io.toString()));
					}
				} else {
					logger.debug("Could not get geo information for fed: " + findable.getSrcIdExpected()
							+ ", [hostname,port,service,io] = [" + hostname + "," + port + "," + serviceName + "," + io
							+ "]");
					// failed++;
					report.add(formatFedToRaport("n/m (incomplete)", findable, hostname, port.toString(), serviceName,
							io.toString()));
				}
			} else {
				// nothign to do: FED has no FMM
				report.add(formatFedToRaport("n/m (b, no FMM)", findable, "-", "-", "-", findable.getFmmIO() + ""));
			}
		}
		logger.info("Matching completed with " + successful + " successful and " + failed + " failed matches");
		
		/*
		Collections.sort(report);

		for (String reportFed : report) {
			System.out.println(reportFed);
		}*/

		return dispatchMap;
	}

	/**
	 * Debug method
	 */
	private String formatFedToRaport(String status, FED fed, String hostname, String port, String service, String io) {

		List<String> deps = new ArrayList<>();

		for (FED f : fed.getDependentFeds()) {
			deps.add("FED " + f.getSrcIdExpected());
		}

		List<String> deps2 = new ArrayList<>();
		for (FED f : all) {
			if (f.getSrcIdExpected() != fed.getSrcIdExpected()) {
				for (FED d : f.getDependentFeds()) {
					if (d.getSrcIdExpected() == fed.getSrcIdExpected()) {
						deps2.add("FED " + f.getSrcIdExpected());
					}
				}
			}
		}

		Collections.sort(deps);

		Collections.sort(deps2);

		String report = String.format("%-32s", status) + ";"//
				+ String.format("%-34s", hostname) + ";" //
				+ String.format("%-5s", port) + ";"//
				+ String.format("%-22s", service) + ";" + String.format("%-4s", io) + ";" //
				+ "FED " + String.format("%-8s", fed.getSrcIdExpected()) + ";"//
				+ String.format("%-8s", FEDHelper.getFEDType(fed)) + ";" //
				+ String.format("%-15s", deps) + ";"//
				+ String.format("%-15s", deps2);

		return report;
	}

}
