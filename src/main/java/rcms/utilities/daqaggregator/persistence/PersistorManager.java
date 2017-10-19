package rcms.utilities.daqaggregator.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.datasource.Flashlist;

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
			String pathname = getTimeDir(snapshotPersistenceDir, current) + snapshotFilename;
			String tmpPathname = pathname + ".tmp";
			File tmpfile = new File(tmpPathname);
			File file = new File(pathname);

			FileOutputStream fos = new FileOutputStream(tmpfile);

			persistor.serialize(daq, fos, snapshotFormat);

			boolean success = tmpfile.renameTo(file);
			if(success){
				String filename = file.getAbsolutePath();
				logger.info("Successfully persisted in " + snapshotPersistenceDir + " as file " + filename);
				return filename;
			}
			else {
				logger.error("Problem renaming file: " + tmpPathname);
				return null;
			}

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
		
		OutputStream finalOutputStream  = new FileOutputStream(file);

		PersistenceFormat format = getFlashlistFormat();
		
		switch (format) {

		case JSON:
			// nothing to do
			break;
		case ZIPPED:
			GZIPOutputStream gzis = new GZIPOutputStream(finalOutputStream);
			finalOutputStream = gzis;
			break;
		default:
			logger.warn("Format of flashlist not available");
		}

		// mapper.addMixIn(Flashlist.class,
		// rcms.utilities.daqaggregator.FlashlistMixin.class);

		mapper.writerWithDefaultPrettyPrinter().writeValue(finalOutputStream, flashlist);
		return file.getAbsolutePath();
	}

	/**
	 * Persist all flashlists. There will be one separate directory created for
	 * each flashlist
	 */
	public void persistFlashlists(Collection<Flashlist> flashlists) {

		int success = 0, fail = 0;

		for (Flashlist flashlist : flashlists) {

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
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
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

	public String getFlashlistPersistenceDir() {
		return flashlistPersistenceDir;
	}

	public PersistenceFormat getFlashlistFormat() {
		return flashlistFormat;
	}

	public String getSnapshotPersistenceDir() {
		return snapshotPersistenceDir;
	}

}
