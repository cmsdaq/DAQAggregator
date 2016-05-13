package rcms.utilities.daqaggregator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.reasoning.base.CheckManager;

/**
 * This class loads data from files
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
public class PersistorManager {

	private final int recentFiles = 1000000;

	// TODO: from properties
	private final String persistenceDir = "/tmp/mgladki/persistence-small"; // TODO: put to configuration file
	private static final Logger logger = Logger.getLogger(PersistorManager.class);

	/**
	 * Get available file names from persistence folder
	 * 
	 * @return available file names from persistence folder
	 * @throws IOException
	 */
	private List<File> getFiles() throws IOException {
		List<File> result = new ArrayList<>();

		File folder = new File(persistenceDir);
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

	public void loadRange() {

	}

	/**
	 * This method walks through all files but does not keep them in memory. It
	 * runs analysis modules and saves the results.
	 * 
	 * @throws IOException
	 */
	public void walkAll() throws IOException {

		List<File> fileList = getFiles();
		Collections.sort(fileList, FileComparator);

		StructurePersistor structurePersistor = new StructurePersistor();
		CheckManager checkManager = new CheckManager();
		DAQ daq;
		logger.info("Processing files...");
		for (File path : fileList) {
			logger.debug(path.getName().toString());

			/* read from .ser */
			daq = structurePersistor.deserialize(path.getAbsolutePath().toString());

			// TODO: move this before serializing, data needs to be available in
			// structure, not only in filename
			String isoDate = path.getName().toString().substring(0, path.getName().toString().length() - 4); // 5
																												// when
																												// json
			logger.debug("Iso date 	" + isoDate);
			Calendar c = javax.xml.bind.DatatypeConverter.parseDateTime(isoDate);
			daq.setLastUpdate(c.getTimeInMillis());

			checkManager.runCheckers(daq);
			TaskManager.get().rawData.add(new DummyDAQ(daq));
			daq = null;

			// TODO: after this comes persisting the reasons.

		}
		logger.info("processed " + fileList.size() + " objects");
	}

	/**
	 * Loads most recent files
	 * 
	 * @throws IOException
	 */
	public void loadRecent() throws IOException {

		List<File> fileList = getFiles();
		Collections.sort(fileList, FileComparator);

		logger.info("Available files: " + fileList.size() + ", oldest: " + fileList.get(0).getName() + ", newest: "
				+ fileList.get(fileList.size() - 1).getName().toString());

		CircularFifoQueue<File> buf = new CircularFifoQueue<>(recentFiles);
		for (File path : fileList) {
			buf.add(path);
		}

		StructurePersistor structurePersistor = new StructurePersistor();
		logger.info("Loading files...");
		for (File path : buf) {
			logger.debug(path.getName().toString());

			/* read from .ser */
			DAQ daq = structurePersistor.deserialize(path.getAbsolutePath().toString());

			// TODO: move this before serializing, data needs to be available in
			// structure, not only in filename
			String isoDate = path.getName().toString().substring(0, path.getName().toString().length() - 4); // 5
																												// when
																												// json
			logger.debug("Iso date 	" + isoDate);
			Calendar c = javax.xml.bind.DatatypeConverter.parseDateTime(isoDate);
			daq.setLastUpdate(c.getTimeInMillis());
			TaskManager.get().buf.add(daq);
		}
		logger.info("deserialized " + TaskManager.get().buf.size() + " objects");

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

	/**
	 * TODO: deleteme, only for quick testing
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		PersistorManager persistorManager = new PersistorManager();
		try {
			persistorManager.loadRecent();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
