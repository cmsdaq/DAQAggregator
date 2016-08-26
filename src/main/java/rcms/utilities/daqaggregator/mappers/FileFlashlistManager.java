package rcms.utilities.daqaggregator.mappers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.persistence.PersistenceFormat;
import rcms.utilities.daqaggregator.persistence.StructureSerializer;

public class FileFlashlistManager extends FlashlistManager {

	private static final Logger logger = Logger.getLogger(FileFlashlistManager.class);

	private Map<FlashlistType, File> currentIterationData = new HashMap<>();
	private final StructureSerializer structureSerialzier;

	private final PersistenceFormat flashlistFormat;

	public FileFlashlistManager(PersistenceFormat format) {
		super(null);
		structureSerialzier = new StructureSerializer();
		flashlistFormat = format;
	}

	/**
	 * Retrieve list of available flashlists
	 */
	public void retrieveAvailableFlashlists() {

		for (FlashlistType flashlistType : FlashlistType.values()) {
			flashlists.add(new Flashlist("", "FileFlashlist:" + flashlistType.getFlashlistName(), sessionId));
		}
	}

	public void downloadFlashlists(boolean downloadAll) {

		long startTime = System.currentTimeMillis();
		flashlists.clear();
		for (FlashlistType flashlistType : FlashlistType.values()) {

			flashlists.add(
					structureSerialzier.deserializeFlashlist(currentIterationData.get(flashlistType), flashlistFormat));
		}

		long stopTime = System.currentTimeMillis();
		int time = (int) (stopTime - startTime);
		logger.debug("Reading all flashlists from file finished in " + time + "ms");
	}

	public Map<FlashlistType, File> getCurrentIterationData() {
		return currentIterationData;
	}

	public void setCurrentIterationData(Map<FlashlistType, File> currentIterationData) {
		this.currentIterationData = currentIterationData;
	}
}
