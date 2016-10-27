package rcms.utilities.daqaggregator.datasource;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

public class Flashlist {

	private FlashlistType flashlistType;

	private JsonNode rowsNode;

	private JsonNode definitionNode;

	private int sessionId;

	/** Date of flashlist retrieve */
	protected Date retrievalDate;

	/** Address from which the flashlist was retrieved */
	private String address;

	/** Name of flashlist used as parameter to API request */
	private String name;

	private static final Logger logger = Logger.getLogger(Flashlist.class);

	public Flashlist() {
	}

	public Flashlist(FlashlistType flashlistType) {
		this(flashlistType, 0);
	}

	public Flashlist(FlashlistType flashlistType, int sessionId) {
		super();
		this.flashlistType = flashlistType;
		this.sessionId = sessionId;
		this.name = "urn:xdaq-flashlist:" + flashlistType.getFlashlistName();
		this.address = flashlistType.getLas().getUrl() + "/retrieveCollection?flash=" + name + "&fmt=json";

		if (flashlistType.isSessionContext()) {
			if (sessionId != 0) {
				address = address + "&" + flashlistType.getSessionIdColumnName() + "=" + sessionId;
			} else {
				logger.warn("Attempt to downoad session context flashlist " + flashlistType
						+ " without passing the session nr");
			}
		}
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

	/**
	 * Initializes the flashlist
	 * 
	 * @param date
	 * @return request processing time
	 * @throws IOException
	 */
	public int download(Date date) throws IOException {

		/* Setting the retrieval date */
		retrievalDate = date;
		logger.debug("Reading flashlist from endpoint: " + address);

		long startTime = System.currentTimeMillis();
		download();
		long stopTime = System.currentTimeMillis();

		int timeResult = (int) (stopTime - startTime);

		/* Warning if there was no data retrieved */
		if (definitionNode.size() == 0 || rowsNode.size() == 0)
			logger.warn("Reading " + flashlistType + " finished in " + timeResult + "ms, fetched " + rowsNode.size()
					+ " rows and " + definitionNode.size() + " columns");

		return timeResult;
	}

	/**
	 * Downloads the data of flashlist
	 * 
	 * @throws IOException
	 */
	private void download() throws IOException {
		List<String> result = Connector.get().retrieveLines(address);

		com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
		JsonNode rootNode = mapper.readValue(result.get(0), JsonNode.class);

		definitionNode = rootNode.get("table").get("definition");
		rowsNode = rootNode.get("table").get("rows");
	}

	public FlashlistType getFlashlistType() {
		return flashlistType;
	}

	public Date getRetrievalDate() {
		return retrievalDate;
	}

	@Override
	public String toString() {
		return "Flashlist [sessionId=" + sessionId + ", rowsNode=" + rowsNode + ", definitionNode=" + definitionNode
				+ ", flashlistType=" + flashlistType + ", retrievalDate=" + retrievalDate + "]";
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
