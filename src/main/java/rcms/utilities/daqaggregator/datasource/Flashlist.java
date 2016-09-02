package rcms.utilities.daqaggregator.datasource;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

public class Flashlist {

	// TODO: final
	private int sessionId;

	// TODO: final
	private String address;

	// TODO: final
	private String name;

	// TODO: final
	private String sessionIdColumnName;

	// TODO: final
	private FlashlistType flashlistType;

	private JsonNode rowsNode;

	private JsonNode definitionNode;

	protected Date retrievalDate;

	private static final Logger logger = Logger.getLogger(Flashlist.class);

	public Flashlist() {
	}

	public Flashlist(FlashlistType flashlistType) {
		this.flashlistType = flashlistType;
	}

	public Flashlist(String address, String name, int sessionId) {
		super();
		this.address = address;
		this.name = name;
		this.sessionId = sessionId;
		inferType();
	}

	public void inferType() {
		this.flashlistType = FlashlistType.inferTypeByName(name);
		this.sessionIdColumnName = this.flashlistType.getSessionIdColumnName();
	}

	public String getName() {
		return name;
	}

	public String getSessionIdColumnName() {
		return sessionIdColumnName;
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

	public int initialize(Date date) throws IOException {

		retrievalDate = date;
		int timeResult;
		String requestAddress = address + "/retrieveCollection?flash=" + name + "&fmt=json";

		if (flashlistType.isSessionContext()) {
			// requestAddress = requestAddress + "&sessionid=" + sessionId;
			requestAddress = requestAddress + "&" + sessionIdColumnName + "=" + sessionId;
		}
		logger.debug("Reading flashlist from endpoint: " + requestAddress);

		long startTime = System.currentTimeMillis();
		List<String> result = Connector.get().retrieveLines(requestAddress);

		com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
		JsonNode rootNode = mapper.readValue(result.get(0), JsonNode.class);

		definitionNode = rootNode.get("table").get("definition");
		rowsNode = rootNode.get("table").get("rows");

		long stopTime = System.currentTimeMillis();
		timeResult = (int) (stopTime - startTime);
		if (definitionNode.size() == 0 || rowsNode.size() == 0)
			logger.warn("Reading " + name + " finished in " + timeResult + "ms, fetched " + rowsNode.size()
					+ " rows and " + definitionNode.size() + " columns");
		return timeResult;
	}

	public FlashlistType getFlashlistType() {
		return flashlistType;
	}

	public Date getRetrievalDate() {
		return retrievalDate;
	}

	@Override
	public String toString() {
		return "Flashlist [sessionId=" + sessionId + ", address=" + address + ", name=" + name
				+ ", sessionIdColumnName=" + sessionIdColumnName + ", rowsNode=" + rowsNode + ", definitionNode="
				+ definitionNode + ", flashlistType=" + flashlistType + ", retrievalDate=" + retrievalDate + "]";
	}

	public String getAddress() {
		return address;
	}

}
