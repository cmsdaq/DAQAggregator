package rcms.utilities.daqaggregator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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

	private String persistenceFolder = "persistence/";

	private static final Logger logger = Logger.getLogger(StructurePersistor.class);

	public void persist(DAQ daq, String filename) throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filename), daq);

		logger.debug("Persisted structure in " + filename);
	}

	public void persist(DAQ daq) throws JsonGenerationException, JsonMappingException, IOException {
		persist(daq, persistenceFolder + "daq.json");
	}

	public void serialize(Object object) {
		try {
			String fileName = persistenceFolder + object.getClass().getCanonicalName() + ".ser";
			FileOutputStream fileOut = new FileOutputStream(fileName);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(object);
			out.close();
			fileOut.close();
			logger.debug("Serialized object in " + fileName);
		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	public DAQ deserialize() {
		DAQ e = null;
		try {
			FileInputStream fileIn = new FileInputStream("/tmp/daq.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			e = (DAQ) in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException i) {
			i.printStackTrace();
			return null;
		} catch (ClassNotFoundException c) {
			System.out.println("Employee class not found");
			c.printStackTrace();
			return null;
		}
		System.out.println("Deserialized Employee...");
		return e;
	}
}
