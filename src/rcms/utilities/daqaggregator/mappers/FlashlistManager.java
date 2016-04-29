package rcms.utilities.daqaggregator.mappers;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.Connector;

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
	public StructureMapper structureMapper;

	// TODO: refactor this field
	public int sessionId;

	private static final Logger logger = Logger.getLogger(FlashlistManager.class);

	public FlashlistManager(Set<String> lasUrls) {
		this.flashlists = new HashSet<Flashlist>();
		this.lasUrls = lasUrls;
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

		for (Flashlist flashlist : flashlists) {
			if (flashlist.getFlashlistType() == FlashlistType.RU
					|| flashlist.getFlashlistType() == FlashlistType.FEROL_INPUT_STREAM
					|| flashlist.getFlashlistType() == FlashlistType.BU
					|| flashlist.getFlashlistType() == FlashlistType.FMM_INPUT
					|| flashlist.getFlashlistType() == FlashlistType.FMM_STATUS)
				try {

					flashlist.initialize();
					logger.info("Flashlist definition:" + flashlist.getDefinitionNode());
					ObjectUpdater updater = new ObjectUpdater();
					updater.update(flashlist, structureMapper);

				} catch (IOException e) {
					logger.error("Error reading flashlist " + flashlist);
					e.printStackTrace();
				}
		}

		long stopTime = System.currentTimeMillis();
		timeResult = (int) (stopTime - startTime);
		logger.info("Reading all flashlists finished in " + timeResult + "ms");

	}

}
