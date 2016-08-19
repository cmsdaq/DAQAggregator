package rcms.utilities.daqaggregator.persistence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

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

	private final SnapshotFormat format;

	protected final StructureSerializer persistor;

	/** Constructor */
	public PersistorManager(String persistenceDir, SnapshotFormat format) {

		this.persistenceDir = persistenceDir;
		this.format = format;
		this.persistor = new StructureSerializer();
		instance = this;
	}

	private static PersistorManager instance;

	public static PersistorManager get() {
		if (instance == null)
			throw new RuntimeException("Persister manager not initialized");
		return instance;
	}

	/**
	 * Persist single DAQ snapshot. It will be persisted in time-based directory
	 * structure based on timestamp from daq snapshot.
	 * 
	 * For instance snapshot with timestamp 1470234844087 (Wed Aug 03 16:34:04
	 * CEST 2016) will be stored in /2016/8/3/16/ directory
	 * 
	 * 
	 * @param daq
	 *            snapshot to be persisted
	 * @return absolute path to file where snapshot was persisted or null when
	 *         persistence was not successfull
	 */
	public String persistSnapshot(DAQ daq) {

		try {

			Date current = new Date(daq.getLastUpdate());
			createTimeDirs(persistenceDir, current);
			String extension = format.getExtension();

			String snapshotFilename = current.getTime() + extension;
			File file = new File(getTimeDir(persistenceDir, current) + snapshotFilename);

			FileOutputStream fos = new FileOutputStream(file);

			persistor.serialize(daq, fos, format);
			String filename = file.getAbsolutePath();

			logger.info("Successfully persisted in " + persistenceDir + " as file " + filename);
			return filename;
		} catch (IOException e) {
			logger.warn("Problem persisting " + e.getMessage());
			return null;
		}
	}

	/**
	 * Create time-base directory structure for given date
	 * 
	 * @param baseDir
	 *            base directory where time-based directory will be situated
	 * @param date
	 *            timestamp for which time directory will be created
	 */
	private void createTimeDirs(String baseDir, Date date) {

		File files = new File(getTimeDir(baseDir, date));
		if (!files.exists()) {
			if (files.mkdirs()) {
				logger.info("Time-based directories created successfully");
			} else {
				throw new RuntimeException("Failed to create following dir: " + files.getAbsolutePath());
			}
		}
	}

	/**
	 * Get the directory based on given date
	 * 
	 * @param baseDir
	 *            base directory where time-based directory will be situated
	 * @param date
	 *            timestamp for which time directory will be returned
	 * @return absolute path to time-based directory
	 */
	public String getTimeDir(String baseDir, Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hour = cal.get(Calendar.HOUR_OF_DAY);

		logger.trace("Date: " + date);
		logger.trace(year + ", " + month + ", " + day + ", " + hour);

		String result = baseDir + year + "/" + month + "/" + day + "/" + hour + "/";
		return result;
	}

}
