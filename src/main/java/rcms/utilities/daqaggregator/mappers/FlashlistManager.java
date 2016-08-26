package rcms.utilities.daqaggregator.mappers;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.Connector;
import rcms.utilities.daqaggregator.RunMode;
import rcms.utilities.daqaggregator.data.FED;

public class FlashlistManager {

	/**
	 * Loaded flashlists
	 */
	protected final Set<Flashlist> flashlists;

	/**
	 * Live Access Service urls
	 */
	private final Set<String> lasUrls;

	// TODO: refactor this field
	private MappingManager mappingManager;

	// TODO: refactor this field
	protected int sessionId;

	private static final Logger logger = Logger.getLogger(FlashlistManager.class);

	private final ExecutorService executor;

	private String lastDetectedSession = "";

	public FlashlistManager(Set<String> lasUrls) {
		this.flashlists = new HashSet<Flashlist>();
		this.lasUrls = lasUrls;
		this.executor = Executors.newFixedThreadPool(10);
	}

	/**
	 * This method retrieves data only from necessary flashlists. After
	 * retrieving it passes flashlist to dispatcher {@link FlashlistDispatcher}
	 */
	public void downloadAndMapFlashlists() {

		long startTime = System.currentTimeMillis();

		downloadFlashlists(false);

		mapFlashlists();

		long stopTime = System.currentTimeMillis();
		int time = (int) (stopTime - startTime);
		logger.info("Reading and mapping all flashlists finished in " + time + "ms");

	}

	/**
	 * Map flashlists to Snapshot object
	 */
	public void mapFlashlists() {

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

	public void downloadFlashlists(boolean downloadAll) {

		Collection<Future<?>> futures = new LinkedList<Future<?>>();
		long startTime = System.currentTimeMillis();
		for (final Flashlist flashlist : flashlists) {

			/* read only this flashlists */
			if (flashlist.getFlashlistType().isDownload() || downloadAll) {
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

	/**
	 * Retrieve list of available flashlists
	 */
	public void retrieveAvailableFlashlists() {

		for (String lasUrl : lasUrls) {

			/*
			 * hack to filter out a single flashlist ('tcds_pm_tts_channel')
			 * from the LAS at pc-c2e11-23-01, while in any other case
			 * (dedicated DAQAggregator LAS) all flashlists are retrieved with
			 * no exception TODO: This is not a final solution, review
			 * flashlists
			 */
			if (lasUrl.equals("http://pc-c2e11-23-01.cms:9945/urn:xdaq-application:service=xmaslas2g")) {
				try {
					List<String> resultLines = Connector.get().retrieveLines(lasUrl + "/retrieveCatalog?fmt=plain");
					for (String line : resultLines) {
						if (line.startsWith("urn:xdaq-flashlist:tcds_pm_tts_channel")) {
							flashlists.add(new Flashlist(lasUrl, line, sessionId));
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
							flashlists.add(new Flashlist(lasUrl, line, sessionId));
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

	private void cleanStructure() {
		// TODO: clean other objects if necessary
		for (FED fed : mappingManager.getObjectMapper().feds.values()) {
			fed.clean();
		}

	}

	public Set<Flashlist> getFlashlists() {
		return flashlists;
	}

	public MappingManager getMappingManager() {
		return mappingManager;
	}

	public void setMappingManager(MappingManager mappingManager) {
		this.mappingManager = mappingManager;
	}

	public int getSessionId() {
		return sessionId;
	}

	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}

	public Entry<String, Integer> detectSession(RunMode runMode, String filter1, String filter2) throws IOException {
		retrieveAvailableFlashlists();
		downloadFlashlists(false);
		for (Flashlist flashlist : flashlists) {
			if (flashlist.getFlashlistType() == FlashlistType.LEVEL_ZERO_FM_STATIC) {

				if (flashlist.getRowsNode().isArray()) {
					Iterator<JsonNode> rowIterator = flashlist.getRowsNode().iterator();
					while (rowIterator.hasNext()) {
						JsonNode row = rowIterator.next();
						logger.debug(row);
						String timestamp = row.get("timestamp").asText();
						String hwcfgKey = row.get("HWCFG_KEY").asText();
						String fmUrl = row.get("FMURL").asText();
						int sid = row.get("SID").asInt();

						logger.info(timestamp + ", " + hwcfgKey + ", " + sid + ", " + fmUrl);

						if (fmUrl.contains(filter1) && fmUrl.contains(filter2)) {

							if (lastDetectedSession.equals("") || timestamp.compareTo(lastDetectedSession) < 0) {
								lastDetectedSession = timestamp;

								String dpsetPath = hwcfgKey.split(":")[0];

								Entry<String, Integer> result = new SimpleEntry<>(dpsetPath, sid);
								return result;
							} else {

								logger.info("timestamp not new..");
							}
						} else {
							logger.info("filters not there..");
						}

					}

				} else {
					logger.warn("Problem accessing rows in " + FlashlistType.LEVEL_ZERO_FM_STATIC);
				}
			}
		}
		return null;
	}
}
