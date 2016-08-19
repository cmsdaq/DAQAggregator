package rcms.utilities.daqaggregator.persistence;

import java.io.IOException;

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
			String filename = null;
			filename = persistor.serialize(daq, persistenceDir, format);

			logger.info("Successfully persisted in " + persistenceDir + " as file " + filename);
			return filename;
		} catch (IOException e) {
			logger.warn("Problem persisting " + e.getMessage());
			return null;
		}
	}

}
