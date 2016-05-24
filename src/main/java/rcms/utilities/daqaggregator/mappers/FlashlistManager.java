package rcms.utilities.daqaggregator.mappers;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.Connector;
import rcms.utilities.daqaggregator.data.SubFEDBuilder;

public class FlashlistManager {

	/**
	 * Loaded flashlists
	 */
	private final Set<Flashlist> flashlists;

	/**
	 * Live Access Service urls
	 */
	private final Set<String> lasUrls;

	// TODO: refactor this field
	private final StructureMapper structureMapper;

	// TODO: refactor this field
	private final int sessionId;

	private static final Logger logger = Logger.getLogger(FlashlistManager.class);

	public FlashlistManager(Set<String> lasUrls, StructureMapper structureMapper, int sessionId) {
		this.flashlists = new HashSet<Flashlist>();
		this.lasUrls = lasUrls;
		this.structureMapper = structureMapper;
		this.sessionId = sessionId;
	}

	/**
	 * Retrieve list of available flashlists
	 */
	public void retrieveAvailableFlashlists() {

		for (String lasUrl : lasUrls) {
			try {
				List<String> resultLines = Connector.get().retrieveLines(lasUrl + "/retrieveCatalog?fmt=plain");

				for (String line : resultLines) {
					if (line.startsWith("urn:")) {
						flashlists.add(new Flashlist(lasUrl, line, sessionId));
					}
				}
			} catch (IOException e) {
				logger.error("Error retrieving flashlists from LAS " + lasUrl);
				logger.error("Exception message: " + e.getMessage());
				e.printStackTrace();
			}

		}
		logger.info("Retrieved " + flashlists.size() + " flashlists");
	}

	/**
	 * Read flashlists
	 */
	public void readFlashlists() {

		int timeResult;
		long startTime = System.currentTimeMillis();

		MappingReporter.get().clear();

		for (Flashlist flashlist : flashlists) {

			/* read only this flashlists */
			if (flashlist.getFlashlistType() == FlashlistType.RU
					|| flashlist.getFlashlistType() == FlashlistType.FEROL_INPUT_STREAM
					|| flashlist.getFlashlistType() == FlashlistType.FEROL_CONFIGURATION
					|| flashlist.getFlashlistType() == FlashlistType.BU
					|| flashlist.getFlashlistType() == FlashlistType.FMM_INPUT
					|| flashlist.getFlashlistType() == FlashlistType.FMM_STATUS
					|| flashlist.getFlashlistType() == FlashlistType.EVM
					|| flashlist.getFlashlistType() == FlashlistType.JOB_CONTROL
					|| flashlist.getFlashlistType() == FlashlistType.LEVEL_ZERO_FM_SUBSYS)
				try {

					flashlist.initialize();
					logger.debug("Flashlist definition:" + flashlist.getDefinitionNode());
					ObjectUpdater updater = new ObjectUpdater();
					updater.update(flashlist, structureMapper);

				} catch (IOException e) {
					logger.error("Error reading flashlist " + flashlist);
					e.printStackTrace();
				}
		}

		long stopTime = System.currentTimeMillis();
		timeResult = (int) (stopTime - startTime);
		logger.info("Reading and mapping all flashlists finished in " + timeResult + "ms");

		MappingReporter.get().summarize();

	}

}
