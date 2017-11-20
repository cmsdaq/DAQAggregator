package rcms.utilities.daqaggregator.datasource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.StorageManager;

/**
 * Retrieves data from Icinga
 */
public class IcingaDataRetriever {
	
	private static final Logger logger = Logger.getLogger(IcingaDataRetriever.class);
	
	private final ObjectMapper mapper;
	private final Connector connector;
	private final String smOccupancyUrl;
  
	private final Pattern occupancyPattern = Pattern.compile(
					"free space: /store/lustre .+ GB \\(([0-9\\.]+)% inode="
	);
	
	public IcingaDataRetriever(Connector connector, String smOccupancyUrl) {
  
		this.mapper = new ObjectMapper();
		this.connector = connector;
		this.smOccupancyUrl = smOccupancyUrl;
	}

	
	/** @return the fraction of occupied space (in the range 0..1) on the storage manager disk 
	    or null if there was a problem with parsing */
	protected Float parseOccupancy(JsonNode doc) {
		
		// assume we can always take the first element in results
		String text = doc.get("results").get(0).get("attrs").get("last_check_result").get("output").asText();
		
		Matcher mo = occupancyPattern.matcher(text);
		
		if (mo.find()) {
			// we cound a match
			// TODO: should we catch and convert IllegalNumberFormatException ?
			float fractionFree = Float.parseFloat(mo.group(1));

			// note that the text contains the percentage of free space while we return 
			// the fraction of occupied space
			return 1f - fractionFree / 100f;
			
		}	else {
			// no match found
			return null;
		}
	}
	
	/** @return the unix timestamp (in milliseconds) of the 'last check'
	 *  field of the response from Icinga.
	 */
	protected Long parseOccupancyLastCheck(JsonNode doc) {
		// note that the field in the json response is in seconds
		// while we return milliseconds (to be consistent with
		// other Java timestamps)
		// assume we can always take the first element in results
		double doubleValue = doc.get("results").get(0).get("attrs").get("last_check").asDouble();
		
		// convert to milliseconds and round
		return (long) (doubleValue * 1000 + 0.5);
		
	}
	
	/** retrieves information about the occupancy of the storage manager (SM)
	 *  and sets the corresponding values in the DAQ object.
	 */
	protected boolean dispatchSmOccupancy(DAQ daq) {
		
		Pair<Integer, List<String>> a = null;
		try {
			
			a = connector.retrieveLines(smOccupancyUrl);

			List<String> result = a.getRight();

			long count = result.size();
			if (count == 1) {
				JsonNode resultJson = mapper.readValue(result.get(0), JsonNode.class);

				try {
					
					Float occupancy = parseOccupancy(resultJson);
					Long lastUpdate = parseOccupancyLastCheck(resultJson);
					
					StorageManager sm = daq.getStorageManager();
									
					sm.setOccupancyFraction(occupancy);
					sm.setOccupancyLastUpdate(lastUpdate);
					
					// all worked fine
					return true;
					
				} catch (NoSuchElementException e) {
					logger.warn("Cannot retrieve storage manager occupancy (no such element) from response: " + result.get(0));
				} catch (NullPointerException e) {
					logger.warn("Cannot retrieve storage manager occupancy from response (NullPointerException): " + result.get(0));
				}
			}

		} catch (IOException e) {
			logger.warn("got an exception while trying to dispatch SM occupancy", e);
		}

		// there was a problem
		return false;
	}
	
}
