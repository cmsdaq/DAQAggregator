package rcms.utilities.daqaggregator.datasource;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

	public final static String PHYSICS_STREAM_NAME = "Physics";
	
	/** @param events if true, fills event rates otherwise fills bandwidths */
	private void fillHLTInfo(int runNumber, HLToutputInfo hltOutputInfo, boolean events) throws IOException {
		
		final String unit;
		if (events)
			unit = "events";
		else
			unit = "bytes";
		
		Pair<Integer, List<String>> a = connector.retrieveLines(
				"http://es-cdaq.cms/sc/php/stream_summary_last.php?setup=cdaq&run=" + runNumber + "&unit=" + unit);

		List<String> result = a.getRight();

		long count = result.size();
		if (count == 1) {
			JsonNode resultJson = mapper.readValue(result.get(0), JsonNode.class);

			logger.debug(resultJson);
			try {

				// node corresponding to the latest lumi section
				JsonNode node = resultJson.elements().next();
				
				// loop over streams
				for (Iterator<Map.Entry<String, JsonNode>> it = node.fields();
						 it.hasNext(); ) {
					
					Map.Entry<String, JsonNode> entry = it.next();
					String streamName = entry.getKey();
					double rate = entry.getValue().asDouble();
					
					if (events)
						hltOutputInfo.setEventRate(streamName, rate);
					else
						hltOutputInfo.setBandwidth(streamName, rate);
					
				}
			} catch (NoSuchElementException e) {

				logger.warn("Cannot retrieve hlt rate (no such element) from response: " + result.get(0));
			}

			catch (NullPointerException e) {
				logger.warn("Cannot retrieve hlt rate from response: " + result.get(0));
			}
		} else {
			logger.warn("Expected 1 node as a response but was " + count);
		}
	}
	
	public HLToutputInfo getHLToutputInfo(int runNumber) throws IOException {
		HLToutputInfo info = new HLToutputInfo();
		
		// fill event rates
		this.fillHLTInfo(runNumber, info, true);

		// fill bandwidths
		this.fillHLTInfo(runNumber, info, false);
		
		return info;
	}
	
	public Double getHLTInfo(int runNumber) throws IOException {
		Pair<Integer, List<String>> a = connector.retrieveLines(
				"http://es-cdaq.cms/sc/php/stream_summary_last.php?setup=cdaq&run=" + runNumber + "&unit=events");

		List<String> result = a.getRight();

		long count = result.size();
		if (count == 1) {
			JsonNode resultJson = mapper.readValue(result.get(0), JsonNode.class);

			logger.debug(resultJson);
			try {
				return resultJson.elements().next().get(PHYSICS_STREAM_NAME).asDouble();
			} catch (NoSuchElementException e) {

				logger.warn("Cannot retrieve hlt rate (no such element) from response: " + result.get(0));
				return null;
			}

			catch (NullPointerException e) {
				logger.warn("Cannot retrieve hlt rate from response: " + result.get(0));
				return null;
			}
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

		long count = result.size();
		if (count == 1) {
			JsonNode resultJson = mapper.readValue(result.get(0), JsonNode.class);

			try {
				DiskInfo diskInfo = new DiskInfo();
				diskInfo.setOutputOccupancyFraction(resultJson.get("output_occ").asDouble());
				diskInfo.setOutputTotal(resultJson.get("output_tot").asInt());
				diskInfo.setRamdiskOccupancyFraction(resultJson.get("ramdisk_occ").asDouble());
				diskInfo.setRamdiskTotal(resultJson.get("ramdisk_tot").asInt());
				return diskInfo;
			} catch (NoSuchElementException e) {
				logger.warn("Cannot retrieve disk info (no such element) from response: " + result.get(0));
				return null;
			} catch (NullPointerException e) {
				logger.warn("Cannot retrieve disk info from response: " + result.get(0));
				return null;
			}
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

	/** contains information about the HLT output, retrieved in one go
	    from the F3 monitoring */
	public class HLToutputInfo {
		
		/** HLT output event rates (events per second) by stream name */
		private final Map<String, Double> eventRates = new HashMap<String, Double>();
		
		/** HLT output bandwidth (bytes per second) by stream name */
		private final Map<String, Double> bandwidths = new HashMap<String, Double>();

		private void setEventRate(String streamName, double rate) {
			eventRates.put(streamName, rate);
		}

		/** @return the event rate (events per second) for the given stream
		 *  or null if not known
		 */
		public Double getEventRate(String streamName) {
			return eventRates.get(streamName);
		}
		
		private void setBandwidth(String streamName, double bandwidth) {
			bandwidths.put(streamName, bandwidth);
		}
		
		/** @return the bandwidth (bytes per second) for the given stream
		 *  or null if not known
		 */
		public Double getBandwidth(String streamName) {
			return bandwidths.get(streamName);
		}
		
	}
	
	
	public void dispatch(DAQ daq) {

		long start = System.currentTimeMillis();

		boolean diskSuccessful = dispatchDisk(daq);
		boolean hltSuccessful = dispatchHLT(daq);

		long end = System.currentTimeMillis();

		if (diskSuccessful && hltSuccessful)
			logger.info("F3 data successfully retrieved and mapped in: " + (end - start) + "ms");
		else {
			logger.warn("Problem retrieving F3 data [disk successful,hlt successful]=[" + diskSuccessful + ","
					+ hltSuccessful + "]");
		}
	}

	/** @return true if retrieving HLT output information from F3mon
	 *  was successful, false otherwise
	 */
	public boolean dispatchHLT(DAQ daq) {

		try {
			Double d = getHLTInfo(daq.getRunNumber());
			if (d != null) {
				daq.setHltRate(d);
				return true;
			} else {
				daq.setHltRate(null);
			}

		} catch (JsonMappingException e) {
			logger.warn("Could not retrieve F3 HLT rate,  json mapping exception: ", e);
		} catch (IOException e) {
			logger.warn("Could not retrieve F3 HLT rate, IO exception: ", e);
		}
		return false;

	}

	public boolean dispatchDisk(DAQ daq) {

		try {
			DiskInfo d = getDiskInfo();
			if (d != null) {
				daq.getBuSummary().setOutputDiskTotal(d.getOutputTotal());
				daq.getBuSummary().setOutputDiskUsage(d.getOutputOccupancyFraction());
				return true;
			} else {
				daq.getBuSummary().setOutputDiskTotal(null);
				daq.getBuSummary().setOutputDiskUsage(null);
			}

		} catch (JsonMappingException e) {
			logger.warn("Could not retrieve F3 disk info,  json mapping exception: ", e);
		} catch (IOException e) {
			logger.warn("Could not retrieve F3 disk info, IO exception: ", e);
		}
		return false;

	}

	/**
	 * Quick test F3
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		F3DataRetriever f3dr = new F3DataRetriever(new Connector(false));
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
