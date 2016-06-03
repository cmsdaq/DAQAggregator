package rcms.utilities.daqaggregator.persistence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqaggregator.DummyDAQ;
import rcms.utilities.daqaggregator.TaskManager;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.reasoning.base.CheckManager;
import rcms.utilities.daqaggregator.reasoning.base.EventProducer;

/**
 * This class manages persistence
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
public class PersistorManager {

	/** Logger for this class */
	private static final Logger logger = Logger.getLogger(PersistorManager.class);

	/** Persistence directory to work with */
	private final String persistenceDir;

	private final String updatedDir;

	private ObjectMapper objectMapper = new ObjectMapper();

	/** Constructor */
	public PersistorManager(String persistenceDir) {
		this.persistenceDir = persistenceDir;
		this.updatedDir = "/tmp/mgladki/snapshots/";
		instance = this;
	}

	private static PersistorManager instance;

	public static PersistorManager get() {
		if (instance == null)
			throw new RuntimeException("Persister manager not initialized");
		return instance;
	}

	public void persistSnapshot(DAQ daq) {

		try {

			String isoDate = objectMapper.writeValueAsString(new Date(daq.getLastUpdate()));

			StructureSerializer persistor = new StructureSerializer();
			// persistor.serializeToJSON(daq, isoDate, persistenceDir);
			// persistor.serializeToJava(daq, isoDate, persistenceDir);
			// persistor.serializeToBSON(daq, isoDate, persistenceDir);
			persistor.serializeToSmile(daq, isoDate, persistenceDir);

			logger.info("Successfully persisted in " + persistenceDir + " as file " + isoDate);
		} catch (IOException e) {
			logger.warn("Problem persisting " + e.getMessage());
		}
	}

	/**
	 * This method walks through all files but does not keep them in memory. It
	 * runs analysis modules and saves the results.
	 * 
	 * @throws IOException
	 */
	public void walkAll() throws IOException {

		Date earliestSnapshotDate = null, latestSnapshotDate;
		List<File> fileList = getFiles(persistenceDir);
		if (fileList.size() == 0) {
			logger.error("No files to process");
			return;
		}
		Collections.sort(fileList, FileComparator);

		StructureSerializer structurePersistor = new StructureSerializer();
		CheckManager checkManager = new CheckManager();
		DAQ daq = null;
		logger.info("Processing files from " + persistenceDir + "...");

		long start = System.currentTimeMillis();
		for (File path : fileList) {
			logger.debug(path.getName().toString());

			daq = structurePersistor.deserializeFromSmile(path.getAbsolutePath().toString());

			if (earliestSnapshotDate == null)
				earliestSnapshotDate = new Date(daq.getLastUpdate());
			// test logic modules
			checkManager.runCheckers(daq);
			TaskManager.get().rawData.add(new DummyDAQ(daq));

		}
		EventProducer.get().finish(new Date(daq.getLastUpdate()));
		latestSnapshotDate = new Date(daq.getLastUpdate());
		long diff = latestSnapshotDate.getTime() - earliestSnapshotDate.getTime();

		long end = System.currentTimeMillis();
		int result = (int) (end - start);
		long hours = TimeUnit.HOURS.convert(diff, TimeUnit.MILLISECONDS);
		if (hours != 0)
			logger.info("Deserializing and running analysis modules on " + hours + " hours data (" + fileList.size()
					+ " snapshots) finished in " + result + "ms. (1h of data processed in " + result / hours + "ms)");
		logger.debug("Current producer state: " + EventProducer.get().toString());
	}

	public DAQ findSnapshot(Date date) {
		StructureSerializer structurePersistor = new StructureSerializer();
		try {
			List<File> fileList = getFiles(persistenceDir);
			List<File> updatedList = getFiles(updatedDir);
			fileList.addAll(updatedList);
			if (fileList.size() == 0) {
				logger.error("No files to process");
				return null;
			}
			Collections.sort(fileList, FileComparator);

			long diff = Integer.MAX_VALUE;
			String bestFile = null;
			DAQ best = null;
			for (File path : fileList) {

				String currentName = path.getAbsolutePath().toString();
				String dateFromFileName = path.getName();
				if (dateFromFileName.contains(".")) {
					int indexOfDot = dateFromFileName.indexOf(".");
					dateFromFileName = dateFromFileName.substring(0, indexOfDot);
				}
				Date currentDate;
				currentDate = objectMapper.readValue(dateFromFileName, Date.class);

				logger.trace("Current file: " + currentName);

				if (bestFile == null) {
					bestFile = currentName;
					continue;
				}

				long currDiff = date.getTime() - currentDate.getTime();

				if (Math.abs(currDiff) < diff) {
					bestFile = currentName;
					diff = Math.abs(currDiff);
				}
			}

			logger.info("Best file found: " + bestFile + " with time diff: " + diff + "ms.");
			best = structurePersistor.deserializeFromSmile(bestFile);
			return best;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;

	}

	/**
	 * Loads most recent files
	 * 
	 * @throws IOException
	 */
	public void loadRecent() throws IOException {

		/* dont read more files than */
		int maxFiles = 1000000;

		List<File> fileList = getFiles(persistenceDir);
		Collections.sort(fileList, FileComparator);

		logger.info("Available files: " + fileList.size() + ", oldest: " + fileList.get(0).getName() + ", newest: "
				+ fileList.get(fileList.size() - 1).getName().toString());

		CircularFifoQueue<File> buf = new CircularFifoQueue<>(maxFiles);
		for (File path : fileList) {
			buf.add(path);
		}

		StructureSerializer structurePersistor = new StructureSerializer();
		logger.info("Loading files...");
		for (File path : buf) {
			logger.debug(path.getName().toString());

			DAQ daq = structurePersistor.deserializeFromJava(path.getAbsolutePath().toString());
			TaskManager.get().buf.add(daq);
		}
		logger.info("deserialized " + TaskManager.get().buf.size() + " objects");

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

	public void getUnprocessedSnapshots(Map<String, File> processed, CheckManager checkManager) throws IOException {

		List<File> fileList = getFiles(updatedDir);
		Collections.sort(fileList, FileComparator);

		StructureSerializer structurePersistor = new StructureSerializer();
		DAQ daq = null;
		logger.debug("Processing files from " + updatedDir + "...");

		for (File path : fileList) {
			if (!processed.containsKey(path.getName())) {

				daq = structurePersistor.deserializeFromSmile(path.getAbsolutePath().toString());
				checkManager.runCheckers(daq);
				TaskManager.get().rawData.add(new DummyDAQ(daq));
				processed.put(path.getName(), path);
			}
		}

		 //temporarly finish
		 if (daq != null)
		 EventProducer.get().finish(new Date(daq.getLastUpdate()));

	}

	/**
	 * Get available file names from persistence folder
	 * 
	 * @return available file names from persistence folder
	 * @throws IOException
	 */
	private List<File> getFiles(String file) throws IOException {
		List<File> result = new ArrayList<>();

		File folder = new File(file);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {

				result.add(listOfFiles[i]);
			} else if (listOfFiles[i].isDirectory()) {
				System.out.println("Directory " + listOfFiles[i].getName());
			}
		}

		return result;
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
