package rcms.utilities.daqaggregator.datasource;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.DAQException;
import rcms.utilities.daqaggregator.DAQExceptionCode;
import rcms.utilities.daqaggregator.persistence.PersistenceExplorer;
import rcms.utilities.daqaggregator.persistence.PersistenceFormat;
import rcms.utilities.daqaggregator.persistence.StructureSerializer;

/**
 * Retrieves flashlists from files
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class FileFlashlistRetriever implements FlashlistRetriever {

	private static final Logger logger = Logger.getLogger(FileFlashlistRetriever.class);

	protected static final String EXCEPTION_NO_FLASHLISTS_AVAILABLE = "No (more) flashlists available";
	protected static final String EXCEPTION_OTHER_PROBLEM = "Problem retrieving flashlists occurred";

	private int flashlistSnapshotCount;

	/**
	 * Files with flashists explored to process
	 */
	private Map<FlashlistType, List<File>> exploredFlashlists = new HashMap<>();

	/** Index of current flashlist to count */
	private int i;

	private final String persistenceDirectory;

	/** Serializer */
	private final StructureSerializer structureSerialzier;

	private final PersistenceFormat flashlistFormat;

	public FileFlashlistRetriever(String persistenceDirectory, PersistenceFormat flashlistFormat) {
		this.persistenceDirectory = persistenceDirectory;
		this.flashlistFormat = flashlistFormat;
		this.structureSerialzier = new StructureSerializer();
	}

	/**
	 * Prepare flashists to process. Explore files in peristence dir.
	 * 
	 * @throws IOException
	 */
	public void prepare() throws IOException {
		Set<Integer> exploredFlashlistCount = new HashSet<>();
		for (FlashlistType flashlistType : FlashlistType.values()) {
			Entry<Long, List<File>> explored = PersistenceExplorer.get().explore(0L, Long.MAX_VALUE,
					persistenceDirectory + flashlistType.name(), Integer.MAX_VALUE);
			exploredFlashlists.put(flashlistType, explored.getValue());
			logger.info("Explored " + explored.getValue().size() + " for flashlist " + flashlistType.name());

			exploredFlashlistCount.add(explored.getValue().size());

		}

		int flashlistCount = 0;
		if (exploredFlashlistCount.size() > 1) {
			logger.warn(
					"Flashlist snapshot count is not equal, some of them will be ommited. Here is flashlist summaries: "
							+ exploredFlashlistCount);

			flashlistCount = Integer.MAX_VALUE;
			for (int currCount : exploredFlashlistCount) {
				if (flashlistCount > currCount)
					flashlistCount = currCount;
			}
		} else if (exploredFlashlistCount.size() == 1) {
			logger.info("All flashlists has the same number of snapshots");
			flashlistCount = exploredFlashlistCount.iterator().next();
		} else {
			throw new RuntimeException(EXCEPTION_OTHER_PROBLEM);
		}
		logger.info("Explored " + flashlistCount + " sets of flashlists");
		flashlistSnapshotCount = flashlistCount;
	}

	/**
	 * Retrieve next batch of flashlists to process
	 */
	@Override
	public Map<FlashlistType, Flashlist> retrieveAllFlashlists() {

		if (i >= flashlistSnapshotCount)
			throw new RuntimeException(EXCEPTION_NO_FLASHLISTS_AVAILABLE);

		HashMap<FlashlistType, Flashlist> result = new HashMap<>();

		for (FlashlistType flashlistType : FlashlistType.values()) {
			File flashistFile = exploredFlashlists.get(flashlistType).get(i);
			Flashlist flashlist = structureSerialzier.deserializeFlashlist(flashistFile, flashlistFormat);
			result.put(flashlistType, flashlist);
		}
		i++;
		return result;
	}

	/**
	 * Retrieve specific flashlist. Note that index of current flashlist set
	 * remains unchanged
	 */
	@Override
	public Flashlist retrieveFlashlist(FlashlistType flashlistType) {
		if (exploredFlashlists.get(flashlistType).size() <= i)
			throw new DAQException(DAQExceptionCode.NoMoreFlashlistSourceFiles,
					"Cannot retrieve flashlist, all flashlist source files has been processed");
		File flashistFile = exploredFlashlists.get(flashlistType).get(i);
		Flashlist flashlist = structureSerialzier.deserializeFlashlist(flashistFile, flashlistFormat);
		return flashlist;
	}

	@Override
	public void retrieveAvailableFlashlists(int sessionId) {
		// nothing to do here

	}

}

/*
 * int idx = flashistFile.getName().indexOf("."); long currentTimestamp =
 * Long.parseLong(flashistFile.getName().substring(0, idx)); if
 * (currentTimestamp > timestamp) timestamp = currentTimestamp;
 */
