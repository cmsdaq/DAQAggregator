package rcms.utilities.daqaggregator.datasource;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

/**
 * Retrieves flashlists from Live Access Service (LAS)
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class LASFlashlistRetriever implements FlashlistRetriever {

	private Map<FlashlistType, Flashlist> flashlists;

	/**
	 * Executor for flashlist retrieval process. Note that flashlists are
	 * downloaded in parallel.
	 */
	private final ExecutorService executor;

	private final List<String> lasUrls;

	private final String mainUrl;

	public LASFlashlistRetriever(String mainUrl, List<String> lasUrls) {
		this.mainUrl = mainUrl;
		this.lasUrls = lasUrls;
		this.flashlists = new HashMap<>();
		this.executor = Executors.newFixedThreadPool(10);
	}

	private static final Logger logger = Logger.getLogger(LASFlashlistRetriever.class);

	@Override
	public Map<FlashlistType, Flashlist> retrieveAllFlashlists() {
		logger.info("Downloading flashlists ...");

		Collection<Future<?>> futures = new LinkedList<Future<?>>();
		long startTime = System.currentTimeMillis();
		final Map<FlashlistType, Integer> times = new HashMap<>();
		final Date retrievalDate = new Date();
		for (final Flashlist flashlist : flashlists.values()) {

			/* read only this flashlists */
			// TODO: true is for flashlist persistence - for snapshots without
			if (flashlist.getFlashlistType().isDownload() || true) {
				Runnable task = new Runnable() {
					public void run() {
						try {
							int time = flashlist.initialize(retrievalDate);
							times.put(flashlist.getFlashlistType(), time);
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
		logger.info("Reading all flashlists finished in " + time + "ms, flashlist specific times: " + times);
		return flashlists;
	}

	@Override
	public Flashlist retrieveFlashlist(FlashlistType flashlistType) {
		Flashlist flashlist = new Flashlist(mainUrl, "urn:xdaq-flashlist:levelZeroFM_static", 0);
		try {
			flashlist.initialize(new Date());
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Problem dowloading specific flashlist", e);
		}
		return flashlist;
	}

	/**
	 * Retrieve list of available flashlists
	 */
	public void retrieveAvailableFlashlists(int sessionId) {
		flashlists.clear();

		logger.info("Retrieving available flashlists from LAS");

		for (String lasUrl : lasUrls) {

			/*
			 * hack to download only the tcds-prefixed flashlists
			 * from the LAS at pc-c2e11-23-01:9945
			 */
			if (lasUrl.equals("http://pc-c2e11-23-01.cms:9945/urn:xdaq-application:service=xmaslas2g")) {
				try {
					List<String> resultLines = Connector.get().retrieveLines(lasUrl + "/retrieveCatalog?fmt=plain");
					for (String line : resultLines) {
						if (line.startsWith("urn:xdaq-flashlist:tcds_")) {
							flashlists.put(FlashlistType.inferTypeByName(line), new Flashlist(lasUrl, line, sessionId));
						}
					}
				} catch (IOException e) {
					logger.error("Error retrieving flashlists from LAS " + lasUrl);
					logger.error("Exception message: " + e.getMessage());
					e.printStackTrace();
				}
			} else {
				try {
					List<String> resultLines = Connector.get().retrieveLines(lasUrl + "/retrieveCatalog?fmt=plain");
					for (String line : resultLines) {
						if (line.startsWith("urn:")) {
							flashlists.put(FlashlistType.inferTypeByName(line), new Flashlist(lasUrl, line, sessionId));
						}
					}
				} catch (IOException e) {
					logger.error("Error retrieving flashlists from LAS " + lasUrl);
					logger.error("Exception message: " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
		logger.info("There are " + flashlists.size() + " flashlists available");
	}
}
