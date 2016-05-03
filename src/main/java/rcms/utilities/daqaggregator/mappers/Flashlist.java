package rcms.utilities.daqaggregator.mappers;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.Connector;

public class Flashlist {

	private final int sessionId;

	private final String address;

	private final String name;

	private JsonNode rowsNode;

	private JsonNode definitionNode;

	private final FlashlistType flashlistType;

	private static final Logger logger = Logger.getLogger(Flashlist.class);

	public Flashlist(String address, String name, int sessionId) {
		super();
		this.address = address;
		this.name = name;
		this.sessionId = sessionId;
		this.flashlistType = FlashlistType.inferTypeByName(name);
	}

	public String getAddress() {
		return address;
	}

	public String getName() {
		return name;
	}

	public JsonNode getRowsNode() {
		return rowsNode;
	}

	public void setRowsNode(JsonNode rowsNode) {
		this.rowsNode = rowsNode;
	}

	public JsonNode getDefinitionNode() {
		return definitionNode;
	}

	public void setDefinitionNode(JsonNode definitionNode) {
		this.definitionNode = definitionNode;
	}

	public void initialize() throws IOException {

		int timeResult;
		long startTime = System.currentTimeMillis();

		List<String> result = Connector.get()
				.retrieveLines(address + "/retrieveCollection?flash=" + name + "&fmt=json&sessionid=" + sessionId);

		com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
		JsonNode rootNode = mapper.readValue(result.get(0), JsonNode.class);

		definitionNode = rootNode.get("table").get("definition");
		rowsNode = rootNode.get("table").get("rows");

		long stopTime = System.currentTimeMillis();
		timeResult = (int) (stopTime - startTime);
		logger.info("Reading " + name + " finished in " + timeResult + "ms, fetched " + rowsNode.size() + " rows and "
				+ definitionNode.size() + " columns");
	}

	public void preetyPrint() {
		String format = "|%-20.20s|%n";
		System.out.format(format, "a");

	}

	public FlashlistType getFlashlistType() {
		return flashlistType;
	}

}
