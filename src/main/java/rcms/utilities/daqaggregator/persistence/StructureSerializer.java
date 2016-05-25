package rcms.utilities.daqaggregator.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;

import de.undercouch.bson4jackson.BsonFactory;
import rcms.utilities.daqaggregator.data.DAQ;

/**
 * Persists DAQ structure in multiple formats format
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class StructureSerializer {

	private static final Logger logger = Logger.getLogger(StructureSerializer.class);

	public void serializeToBSON(DAQ daq, String name, String folder)
			throws JsonGenerationException, JsonMappingException, IOException {
		File file = new File(folder + name + ".bson");
		ObjectMapper mapper = new ObjectMapper(new BsonFactory());
		mapper.writeValue(file, daq);
	}

	public void serializeToSmile(DAQ daq, String name, String folder)
			throws JsonGenerationException, JsonMappingException, IOException {
		File file = new File(folder + name + ".smile");
		ObjectMapper mapper = new ObjectMapper(new SmileFactory());
		mapper.writeValue(file, daq);
	}

	public void serializeToJSON(DAQ daq, String name, String folder)
			throws JsonGenerationException, JsonMappingException, IOException {
		File file = new File(folder + name + ".json");
		ObjectMapper mapper = new ObjectMapper();
		mapper.writerWithDefaultPrettyPrinter().writeValue(file, daq);
	}

	public void serializeToJava(DAQ daq, String name, String folder) {
		try {

			String fileName = folder + name + ".ser";
			FileOutputStream fileOut = new FileOutputStream(fileName);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(daq);
			out.close();
			fileOut.close();
			logger.debug("Serialized object in " + fileName);
		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	public DAQ deserializeFromSmile(String filepath) {
		return mapperDeserialiser(filepath, new SmileFactory());
	}

	public DAQ deserializeFromJSON(String filepath) {
		return mapperDeserialiser(filepath, new JsonFactory());
	}

	public DAQ deserializeFromBSON(String filepath) {
		return mapperDeserialiser(filepath, new BsonFactory());
	}

	public DAQ deserializeFromJava(String filepath) {
		DAQ e = null;
		ObjectInputStream in = null;
		FileInputStream fileIn = null;
		try {
			fileIn = new FileInputStream(filepath);
			in = new ObjectInputStream(fileIn);
			e = (DAQ) in.readObject();
		} catch (IOException i) {
			logger.error("File incompatible: " + filepath);
			i.printStackTrace();
			return null;
		} catch (ClassNotFoundException c) {
			logger.error("Cannot find class: " + filepath);
			c.printStackTrace();
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
		return e;
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
