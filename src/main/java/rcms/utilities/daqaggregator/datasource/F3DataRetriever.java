package rcms.utilities.daqaggregator.datasource;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqaggregator.Application;
import rcms.utilities.daqaggregator.ProxyManager;
import rcms.utilities.daqaggregator.data.DAQ;

/**
 * Retrieves date from F3
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
public class F3DataRetriever {
	private static final Logger logger = Logger.getLogger(F3DataRetriever.class);

	private final Connector connector;
	private final ObjectMapper mapper;

	public F3DataRetriever(Connector connector) {
		this.mapper = new ObjectMapper();
		this.connector = connector;
	}

	public Double getHLTInfo(int runNumber) throws IOException {
		Pair<Integer, List<String>> a = connector.retrieveLines(
				"http://es-cdaq.cms/sc/php/stream_summary_last.php?setup=cdaq&run=" + runNumber + "&unit=events");

		List<String> result = a.getRight();

		long count = result.stream().count();
		if (count == 1) {
			JsonNode resultJson = mapper.readValue(result.get(0), JsonNode.class);

			logger.debug(resultJson);
			logger.debug("Accessing physics hlt rate: " + resultJson.elements().next().get("Physics"));
			return resultJson.elements().next().get("Physics").asDouble();
		} else {
			logger.warn("Expected 1 node as a response but was " + count);
			return null;
		}
	}

	/**
	 * Gets ramdisk and output disk occupancy levels. It's summary of all cdaq
	 * BU's
	 * 
	 * @return
	 * 
	 * @throws IOException
	 * @throws JsonMappingException
	 */
	public DiskInfo getDiskInfo() throws IOException, JsonMappingException {

		Pair<Integer, List<String>> a = connector.retrieveLines("http://es-cdaq.cms/sc/php/summarydisks.php");
		List<String> result = a.getRight();

		long count = result.stream().count();
		if (count == 1) {
			JsonNode resultJson = mapper.readValue(result.get(0), JsonNode.class);

			DiskInfo diskInfo = new DiskInfo();
			diskInfo.setOutputOccupancyFraction(resultJson.get("output_occ").asDouble());
			diskInfo.setOutputTotal(resultJson.get("output_tot").asInt());
			diskInfo.setRamdiskOccupancyFraction(resultJson.get("ramdisk_occ").asDouble());
			diskInfo.setRamdiskTotal(resultJson.get("ramdisk_tot").asInt());
			return diskInfo;
		} else {
			logger.warn("Expected 1 node as a response but was " + count);
			return null;
		}

	}

	public class DiskInfo {
		private Double ramdiskOccupancyFraction;
		private Integer ramdiskTotal;
		private Double outputOccupancyFraction;
		private Integer outputTotal;

		public Double getRamdiskOccupancyFraction() {
			return ramdiskOccupancyFraction;
		}

		public void setRamdiskOccupancyFraction(Double ramdiskOccupancyFraction) {
			this.ramdiskOccupancyFraction = ramdiskOccupancyFraction;
		}

		public Integer getRamdiskTotal() {
			return ramdiskTotal;
		}

		public void setRamdiskTotal(Integer ramdiskTotal) {
			this.ramdiskTotal = ramdiskTotal;
		}

		public Double getOutputOccupancyFraction() {
			return outputOccupancyFraction;
		}

		public void setOutputOccupancyFraction(Double outputOccupancyFraction) {
			this.outputOccupancyFraction = outputOccupancyFraction;
		}

		public Integer getOutputTotal() {
			return outputTotal;
		}

		public void setOutputTotal(Integer outputTotal) {
			this.outputTotal = outputTotal;
		}

		@Override
		public String toString() {
			return "DiskInfo [ramdiskOccupancyFraction=" + ramdiskOccupancyFraction + ", ramdiskTotal=" + ramdiskTotal
					+ ", outputOccupancyFraction=" + outputOccupancyFraction + ", outputTotal=" + outputTotal + "]";
		}

	}

	public void dispatch(DAQ daq) {

		long start = System.currentTimeMillis();
		long end = 0;

		try {
			DiskInfo d = getDiskInfo();
			daq.getBuSummary().setOutputDiskTotal(d.getOutputTotal());
			daq.getBuSummary().setOutputDiskUsage(d.getOutputOccupancyFraction());

			end = System.currentTimeMillis();
		} catch (JsonMappingException e) {
			logger.warn("Could not retrieve F3 disk info,  json mapping exception: ", e);
		} catch (IOException e) {
			logger.warn("Could not retrieve F3 disk info, IO exception: ", e);
		}
		if (end != 0)
			logger.info("F3 data retrieved and mapped in: " + (end - start) + "ms");

	}

	/**
	 * Quick test F3
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		F3DataRetriever f3dr = new F3DataRetriever(new Connector());
		try {
			Application.initialize("DAQAggregator.properties");
			ProxyManager.get().startProxy();
			DiskInfo d = f3dr.getDiskInfo();
			Double h = f3dr.getHLTInfo(288498);

			logger.info(d);
			logger.info(h);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
