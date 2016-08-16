package rcms.utilities.daqaggregator.persistence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqaggregator.data.DAQ;

/**
 * This class manages persistence
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
public class PersistorManager {

	/** Logger for this class */
	private static final Logger logger = Logger.getLogger(PersistorManager.class);

	/** Persistence directory to work with */
	protected final String persistenceDir;

	private ObjectMapper objectMapper = new ObjectMapper();

	private SnapshotFormat format;

	/** Constructor */
	public PersistorManager(String persistenceDir, SnapshotFormat format) {
		this.persistenceDir = persistenceDir;
		this.format = format;
		instance = this;
	}

	private static PersistorManager instance;

	public static PersistorManager get() {
		if (instance == null)
			throw new RuntimeException("Persister manager not initialized");
		return instance;
	}

	public String persistSnapshot(DAQ daq) {

		try {

			String isoDate = objectMapper.writeValueAsString(new Date(daq.getLastUpdate()));

			StructureSerializer persistor = new StructureSerializer();
			String filename = null;
			switch (format) {
			case SMILE:
				filename = persistor.serializeToSmile(daq, isoDate, persistenceDir);
				break;
			case JSON:
				filename = persistor.serializeToJSON(daq, isoDate, persistenceDir);
				break;
			case JSONREFPREFIXED:
				filename = persistor.serializeToRefJSON(daq, isoDate, persistenceDir);
				break;
			case JSONUGLY:
				filename = persistor.serializeToJSONUgly(daq, isoDate, persistenceDir);
				break;
			case JSONREFPREFIXEDUGLY:
				filename = persistor.serializeToRefJSONUgly(daq, isoDate, persistenceDir);
				break;
			default:
				logger.warn("Format of snapshot not available");
			}
			logger.info("Successfully persisted in " + persistenceDir + " as file " + filename);
			return filename;
		} catch (IOException e) {
			logger.warn("Problem persisting " + e.getMessage());
			return null;
		}
	}

	/**
	 * Converts files from one format to another
	 */
	public void convertSnapshots(String targetDirectory) throws IOException {

		List<File> fileList = getFiles(persistenceDir);
		Collections.sort(fileList, FileComparator);

		StructureSerializer structurePersistor = new StructureSerializer();
		DAQ daq;
		logger.info("Converting files...");
		for (File path : fileList) {
			logger.debug(path.getName().toString());

			daq = structurePersistor.deserializeFromSmile(path.getAbsolutePath().toString());
			if (daq != null)
				structurePersistor.serializeToSmile(daq, daq.getLastUpdate() + ".smile", targetDirectory);
		}
	}

	/**
	 * Get available file names from persistence folder
	 * 
	 * @return available file names from persistence folder
	 * @throws IOException
	 */
	protected List<File> getFiles(String file) throws IOException {
		List<File> result = new ArrayList<>();

		File folder = new File(file);

		if (folder.exists() && folder.isDirectory()) {

			File[] listOfFiles = folder.listFiles();

			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {

					result.add(listOfFiles[i]);
				} else if (listOfFiles[i].isDirectory()) {
					System.out.println("Directory " + listOfFiles[i].getName());
				}
			}

			return result;
		} else {
			throw new FileNotFoundException("Folder does not exist " + folder.getAbsolutePath());
		}
	}

	/**
	 * Filename comparator
	 */
	public static Comparator<File> FileComparator = new Comparator<File>() {
		public int compare(File path1, File path2) {
			String filename1 = path1.getName().toString().toUpperCase();
			String filename2 = path2.getName().toString().toUpperCase();
			return filename1.compareTo(filename2);
		}
	};

}
