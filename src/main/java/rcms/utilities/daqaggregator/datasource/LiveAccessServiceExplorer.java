package rcms.utilities.daqaggregator.datasource;

import java.io.IOException;
import java.rmi.server.ExportException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import rcms.utilities.daqaggregator.DAQAggregator;
import rcms.utilities.daqaggregator.DAQException;
import rcms.utilities.daqaggregator.DAQExceptionCode;

public class LiveAccessServiceExplorer {

	/**
	 * Based on order matched urls will be used or ignored
	 */
	private final List<String> urls;

	private final HashMap<String, String> flashlistToUrl;

	private final Connector connector;

	private final static Logger logger = Logger.getLogger(LiveAccessServiceExplorer.class);

	public LiveAccessServiceExplorer(List<String> urls) {
		this.urls = urls;
		this.flashlistToUrl = new HashMap<>();
		this.connector = new Connector();
	}

	public void exploreLiveAccessServices() {
		for (String url : urls) {
			try {
				logger.info("Exploring url: " + url);
				exploreLiveAccessService(url);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void exploreLiveAccessService(String url) throws IOException {
		Pair<Integer, List<String>> a = connector.retrieveLines(url + "/retrieveCatalog?fmt=json");

		if (a.getLeft() == 200) {

			com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
			JsonNode rootNode = mapper.readValue(a.getRight().get(0), JsonNode.class);

			JsonNode rowsNode = rootNode.get("table").get("rows");
			if (rowsNode.isArray()) {
				ArrayNode arrayNode = (ArrayNode) rowsNode;
				for (JsonNode b : arrayNode) {

					String name = b.get("Name").asText();
					if (name.startsWith("urn:xdaq-flashlist:")) {
						String shortName = name.substring(19);
						// System.out.println(shortName + ": " + b);

						if (flashlistToUrl.containsKey(shortName)) {
							logger.warn("Name colision across differend LAS for flashlist: " + shortName);
							logger.warn("LAS url for flashlist " + shortName
									+ " with be ignored, first match will be used");
							logger.warn(" - first match: " + flashlistToUrl.get(shortName));
							logger.warn(" - ignored:     " + url);

						} else {
							flashlistToUrl.put(shortName, url);
						}
					}
				}

				logger.info("Explored " + arrayNode.size() + " flashlists");

			} else {
				throw new DAQException(DAQExceptionCode.ProblemExploringLAS, "Problem exploring LAS with url: " + url);
			}

		} else {
			throw new DAQException(DAQExceptionCode.ProblemExploringLAS,
					"Request to LAS catalog returned with http status: " + a.getLeft());
		}
	}

	public String getFlashlistUrl(String flashlistName) {
		return flashlistToUrl.get(flashlistName);
	}

	public HashMap<String, String> getFlashlistToUrl() {
		return flashlistToUrl;
	}

}
