package rcms.utilities.daqaggregator.mappers;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.Connector;
import rcms.utilities.daqaggregator.data.FED;

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
	private final MappingManager mappingManager;

	// TODO: refactor this field
	private final int sessionId;

	private static final Logger logger = Logger.getLogger(FlashlistManager.class);

	private final ExecutorService executor;

	public FlashlistManager(Set<String> lasUrls, MappingManager mappingManager, int sessionId) {
		this.flashlists = new HashSet<Flashlist>();
		this.lasUrls = lasUrls;
		this.mappingManager = mappingManager;
		this.sessionId = sessionId;
		this.executor = Executors.newFixedThreadPool(10);
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
		logger.info("There are " + flashlists.size() + " flashlists available");
	}

	/**
	 * This method retrieves data only from necessary flashlists. After
	 * retrieving it passes flashlist to dispatcher {@link FlashlistDispatcher}
	 */
	public void readFlashlists() {

		long startTime = System.currentTimeMillis();

		MappingReporter.get().clear();
		downloadFlashlists();
		cleanStructure();
		mapFlashlists();

		long stopTime = System.currentTimeMillis();
		int time = (int) (stopTime - startTime);
		logger.info("Reading and mapping all flashlists finished in " + time + "ms");

	}

	private void downloadFlashlists() {

		Collection<Future<?>> futures = new LinkedList<Future<?>>();
		long startTime = System.currentTimeMillis();
		for (final Flashlist flashlist : flashlists) {

			/* read only this flashlists */
			if (flashlist.getFlashlistType().isDownload()) {
				Runnable task = new Runnable() {
					public void run() {
						try {
							flashlist.initialize();
							logger.debug("Flashlist definition:" + flashlist.getDefinitionNode());

						} catch (IOException e) {
							logger.error("Error reading flashlist " + flashlist);
							e.printStackTrace();
						}
					}
				};
				futures.add(executor.submit(task));
			}
		}

		try {
			for (Future<?> future : futures) {
				future.get();
			}
		} catch (InterruptedException | ExecutionException e) {
			logger.error("Problem waiting for flahlists download threads to join");
			e.printStackTrace();
		}

		long stopTime = System.currentTimeMillis();
		int time = (int) (stopTime - startTime);
		logger.debug("Reading all flashlists finished in " + time + "ms");
	}

	private void mapFlashlists() {

		long startTime = System.currentTimeMillis();
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
		//TODO: clean other objects if necessary
		for (FED fed : mappingManager.getObjectMapper().feds.values()) {
			fed.clean();
		}

	}
}
