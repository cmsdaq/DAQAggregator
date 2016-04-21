package rcms.utilities.daqaggregator;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqaggregator.data.DAQ;

/**
 * Persists DAQ structure in json format
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class StructurePersistor {

	private static final Logger logger = Logger.getLogger(StructurePersistor.class);

	public void persist(DAQ daq) throws JsonGenerationException, JsonMappingException, IOException {

		ObjectMapper mapper = new ObjectMapper();
		mapper.writerWithDefaultPrettyPrinter().writeValue(new File("daq.json"), daq);

		StringBuilder sb = new StringBuilder();
		sb.append("Persistor summary");
		sb.append("[bu size: " + daq.getBus().size() + "], ");
		sb.append("[ttcp size: " + daq.getTtcPartitions().size() + "]");
		logger.info(sb.toString());
	}

}
