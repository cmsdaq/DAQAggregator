package rcms.utilities.daqaggregator.persistence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.mappers.Flashlist;
import rcms.utilities.daqaggregator.mappers.FlashlistManager;

/**
 * This class manages persistence
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
public class PersistorManager {

	/** Logger for this class */
	private static final Logger logger = Logger.getLogger(PersistorManager.class);

	/** Persistence directory to work with */
	protected final String snapshotPersistenceDir;
	private final String flashlistPersistenceDir;

	private final PersistenceFormat snapshotFormat;
	private final PersistenceFormat flashlistFormat;

	protected final StructureSerializer persistor;

	/** Constructor */
	public PersistorManager(String snapshotPersistenceDir, String flashlistPersistenceDir,
			PersistenceFormat snapshotFormat, PersistenceFormat flashlistFormat) {

		this.snapshotPersistenceDir = snapshotPersistenceDir;
		this.flashlistPersistenceDir = flashlistPersistenceDir;
		this.snapshotFormat = snapshotFormat;
		this.flashlistFormat = flashlistFormat;
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
	 *         persistence was not successful, null when persistence faile
	 */
	public String persistSnapshot(DAQ daq) {

		try {

			Date current = new Date(daq.getLastUpdate());
			createTimeDirs(snapshotPersistenceDir, current);
			String extension = snapshotFormat.getExtension();

			String snapshotFilename = current.getTime() + extension;
			File file = new File(getTimeDir(snapshotPersistenceDir, current) + snapshotFilename);

			FileOutputStream fos = new FileOutputStream(file);

			persistor.serialize(daq, fos, snapshotFormat);
			String filename = file.getAbsolutePath();

			logger.info("Successfully persisted in " + snapshotPersistenceDir + " as file " + filename);
			return filename;
		} catch (IOException e) {
			logger.warn("Problem persisting " + e.getMessage());
			return null;
		}
	}

	/**
	 * 
	 * Persist given flashlist. It will be persisted in time-based directory
	 * structure based on retrieval timestamp from flashlist object.
	 * 
	 * For instance flashlist with retrieval timestamp 1470234844087 (Wed Aug 03
	 * 16:34:04 CEST 2016) will be stored in /2016/8/3/16/ directory
	 * 
	 * @param flashlist
	 *            flashlist to be persisted
	 * @param base
	 *            base directory for persistence
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 */
	public String persistFlashlist(Flashlist flashlist, String base)
			throws JsonGenerationException, JsonMappingException, IOException {
		String flashlistBase = base + flashlist.getFlashlistType().name() + "/";
		logger.debug("Persisting flashlist " + flashlist.getFlashlistType().name() + " retrieved on "
				+ flashlist.getRetrievalDate());
		createTimeDirs(flashlistBase, flashlist.getRetrievalDate());

		String flashlistFilename = flashlist.getRetrievalDate().getTime() + getFlashlistFormat().getExtension();
		File file = new File(getTimeDir(flashlistBase, flashlist.getRetrievalDate()) + flashlistFilename);

		ObjectMapper mapper = getFlashlistFormat().getMapper();

		// mapper.addMixIn(Flashlist.class,
		// rcms.utilities.daqaggregator.FlashlistMixin.class);

		FileOutputStream fos = new FileOutputStream(file);
		mapper.writerWithDefaultPrettyPrinter().writeValue(fos, flashlist);
		return file.getAbsolutePath();
	}

	/**
	 * Persist all flashlists. There will be one separate directory created for
	 * each flashlist
	 * 
	 * @param flashlistManager
	 *            manager of flashlists
	 */
	public void persistFlashlists(FlashlistManager flashlistManager) {

		int success = 0, fail = 0;

		for (Flashlist flashlist : flashlistManager.getFlashlists()) {

			try {
				persistFlashlist(flashlist, getFlashlistPersistenceDir());
				success++;
			} catch (IOException e) {
				fail++;
				e.printStackTrace();
			}

		}
		logger.info("Persisted " + success + " flashlists sucessfully, " + fail + " failures");
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

	/**
	 * Explore time-based directory structure. Only chunk of data will be
	 * returned.
	 * 
	 * @param startTimestamp
	 *            explore after this date
	 * @param directory
	 *            base directory when exploration will take place
	 * @return tuple with chunk of explored files and timestamp of most recent
	 *         one
	 * @throws IOException
	 */
	public Entry<Long, List<File>> explore(Long startTimestamp, String directory) throws IOException {
		return explore(startTimestamp, Long.MAX_VALUE, directory, 2000);
	}

	/**
	 * Explore time-based directory structure. Only chunk of data will be
	 * returned.
	 * 
	 * @param startTimestamp
	 *            explore after this date
	 * @param endTimestamp
	 *            explore before this date
	 * @param directory
	 *            base directory when exploration will take place
	 * @return tuple with chunk of explored files and timestamp of most recent
	 *         one
	 * @throws IOException
	 */
	public Entry<Long, List<File>> explore(Long startTimestamp, Long endTimestamp, String directory)
			throws IOException {
		return explore(startTimestamp, endTimestamp, directory, 2000);
	}

	/**
	 * Explore time-based directory structure. Only chunk of data will be
	 * returned.
	 * 
	 * @param startTimestamp
	 *            explore after this date
	 * @param endTimestamp
	 *            explore before this date
	 * @param chunkSize
	 *            chunk size to limit the returned data
	 * @param directory
	 *            base directory when exploration will take place
	 * @return tuple with chunk of explored files and timestamp of most recent
	 *         one
	 * @throws IOException
	 */
	public Entry<Long, List<File>> explore(Long startTimestamp, Long endTimestamp, String dir, int chunkSize)
			throws IOException {

		Long tmpLast = startTimestamp;
		Long startTime = System.currentTimeMillis();
		Long snapshotCount = 0L;

		List<File> result = new ArrayList<>();

		List<File> yearDirs = getDirs(dir);

		for (File dirYear : yearDirs) {
			List<File> monthDirs = getDirs(dirYear.getAbsolutePath());

			for (File monthDir : monthDirs) {
				List<File> dayDirs = getDirs(monthDir.getAbsolutePath());

				for (File dayDir : dayDirs) {
					List<File> hourDirs = getDirs(dayDir.getAbsolutePath());

					for (File hourDir : hourDirs) {
						List<File> snapshots = getFiles(hourDir.getAbsolutePath());

						for (File snapshot : snapshots) {

							int dotIdx = snapshot.getName().indexOf(".");

							if (dotIdx != -1) {
								Long timestamp = Long.parseLong(snapshot.getName().substring(0, dotIdx));

								// FIXME: this needs to be improved
								if (startTimestamp < timestamp && timestamp < endTimestamp
										&& snapshotCount < chunkSize) {
									startTimestamp = timestamp;
									result.add(snapshot);
									snapshotCount++;
								}
							}
						}

					}
				}
			}
		}

		Long endTime = System.currentTimeMillis();
		logger.debug("Explored " + snapshotCount + " snapshots (" + result.size() + " snapshots) after " + tmpLast
				+ ", in " + (endTime - startTime) + "ms");
		Entry<Long, List<File>> entry = new SimpleEntry<>(startTimestamp, result);
		return entry;
	}

	protected List<File> getDirs(String file) throws IOException {
		List<File> result = new ArrayList<>();

		File folder = new File(file);

		if (folder.exists() && folder.isDirectory()) {

			File[] listOfFiles = folder.listFiles();

			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {

					System.out.println("File " + listOfFiles[i].getName());
				} else if (listOfFiles[i].isDirectory()) {
					// directory name must be always parsable integer
					try {
						Integer.parseInt(listOfFiles[i].getName());
						result.add(listOfFiles[i]);
					} catch (NumberFormatException e) {
						// ignore directory
					}
				}
			}
			Collections.sort(result, DirComparator);

			return result;
		} else {
			throw new FileNotFoundException("Folder does not exist " + folder.getAbsolutePath());
		}
	}

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
			Collections.sort(result, FileComparator);

			return result;
		} else {
			throw new FileNotFoundException("Folder does not exist " + folder.getAbsolutePath());
		}
	}

	public String getFlashlistPersistenceDir() {
		return flashlistPersistenceDir;
	}

	public PersistenceFormat getFlashlistFormat() {
		return flashlistFormat;
	}

	public static Comparator<File> DirComparator = new Comparator<File>() {
		public int compare(File path1, File path2) {
			Integer filename1 = Integer.parseInt(path1.getName().toString());
			Integer filename2 = Integer.parseInt(path2.getName().toString());
			return filename1.compareTo(filename2);
		}
	};

	public static Comparator<File> FileComparator = new Comparator<File>() {
		public int compare(File path1, File path2) {
			String filename1 = path1.getName().toString();
			String filename2 = path2.getName().toString();
			return filename1.compareTo(filename2);
		}
	};

}
