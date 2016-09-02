package rcms.utilities.daqaggregator.datasource;

import java.util.Collection;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.mappers.MappingManager;
import rcms.utilities.daqaggregator.mappers.MappingReporter;

public class FlashlistManager {

	/**
	 * Flashlists will be mapped to objects in DAQ structure referenced by
	 * mapping manager
	 */
	private final MappingManager mappingManager;

	private static final Logger logger = Logger.getLogger(FlashlistManager.class);

	public FlashlistManager(MappingManager mappingManager) {
		this.mappingManager = mappingManager;
	}

	/**
	 * Map flashlists to Snapshot object
	 */
	public void mapFlashlists(Collection<Flashlist> flashlists) {

		long startTime = System.currentTimeMillis();
		MappingReporter.get().clear();
		cleanStructure();
		for (Flashlist flashlist : flashlists) {

			if (flashlist.getFlashlistType().isDownload()) {
				FlashlistDispatcher dispatcher = new FlashlistDispatcher();
				dispatcher.dispatch(flashlist, mappingManager);
			}
		}
		long stopTime = System.currentTimeMillis();
		int time = (int) (stopTime - startTime);
		logger.debug("Mapping all flashlists finished in " + time + "ms");
	}

	private void cleanStructure() {
		// TODO: clean other objects if necessary
		for (FED fed : mappingManager.getObjectMapper().feds.values()) {
			fed.clean();
		}

	}
}
