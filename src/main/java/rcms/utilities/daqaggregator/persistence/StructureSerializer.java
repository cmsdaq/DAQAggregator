package rcms.utilities.daqaggregator.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;

import rcms.utilities.daqaggregator.data.DAQ;

/**
 * Persists DAQ structure in multiple formats format
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class StructureSerializer {

	private static final Logger logger = Logger.getLogger(StructureSerializer.class);

	public String serializeToSmile(DAQ daq, String name, String folder)
			throws JsonGenerationException, JsonMappingException, IOException {
		File file = new File(folder + name + ".smile");
		ObjectMapper mapper = new ObjectMapper(new SmileFactory());
		mapper.writeValue(file, daq);
		return file.getAbsolutePath();
	}

	public void serializeToJSON(DAQ daq, String name, String folder)
			throws JsonGenerationException, JsonMappingException, IOException {
		File file = new File(folder + name + ".json");
		ObjectMapper mapper = new ObjectMapper();
		mapper.writerWithDefaultPrettyPrinter().writeValue(file, daq);
	}

	public DAQ deserializeFromSmile(String filepath) {
		return mapperDeserialiser(filepath, new SmileFactory());
	}

	public DAQ deserializeFromJSON(String filepath) {
		return mapperDeserialiser(filepath, new JsonFactory());
	}

	private DAQ mapperDeserialiser(String filepath, JsonFactory factory) {

		DAQ daq = null;
		/* read from smile */
		ObjectMapper mapper = new ObjectMapper(factory);

		ObjectInputStream in = null;
		FileInputStream fileIn = null;
		try {
			daq = mapper.readValue(new File(filepath), DAQ.class);
			return daq;
		} catch (IOException i) {
			logger.error("File incompatible: " + filepath);
			i.printStackTrace();
			return null;
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e1) {
				}
			if (fileIn != null)
				try {
					fileIn.close();
				} catch (IOException e1) {
				}
		}
	}
}
