package rcms.utilities.daqaggregator.datasource;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.DAQException;
import rcms.utilities.daqaggregator.DAQExceptionCode;

/**
 * Retrieves flashlists from Live Access Service (LAS)
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class LASFlashlistRetriever implements FlashlistRetriever {

	/**
	 * Executor for flashlist retrieval process. Note that flashlists are
	 * downloaded in parallel.
	 */
	private final ExecutorService executor;
	
	private final boolean suppressFailedRequests;

	public LASFlashlistRetriever(boolean suppressFailedRequests) {
		this.executor = Executors.newFixedThreadPool(10);
		this.suppressFailedRequests = suppressFailedRequests;
	}

	private static final Logger logger = Logger.getLogger(LASFlashlistRetriever.class);

	@Override
	public Map<FlashlistType, Flashlist> retrieveAllFlashlists(final int sessionId) {
		logger.debug("Downloading flashlists ...");

		if (sessionId == 0) {
			throw new DAQException(DAQExceptionCode.MissingSessionIdRetrievingFlashlists,
					"Cannot retrieve all flashlists without session id. Non zero session id required");
		}

		Collection<Future<?>> futures = new LinkedList<Future<?>>();
		long startTime = System.currentTimeMillis();
		final Map<FlashlistType, String> statuses = new ConcurrentHashMap<>();
		final Map<FlashlistType, Flashlist> flashlists = new ConcurrentHashMap<>();
		final Date retrievalDate = new Date();
		for (final FlashlistType flashlistType : FlashlistType.values()) {

			/*
			 * Some flashlist may be optional - they were not discovered in
			 * LAS-flashlist auto mapping and they has no url assigned
			 */
			if (flashlistType.getUrl() != null) {

				Runnable task = new Runnable() {
					public void run() {
						try {
							Pair<Flashlist, String> result;

							if (flashlistType.isSessionContext()) {
								result = downloadSessionContextFlashlist(flashlistType, retrievalDate, sessionId);
							} else {
								result = downloadNonSessionContextFlashlist(flashlistType, retrievalDate);
							}

							statuses.put(flashlistType, result.getRight());
							flashlists.put(flashlistType, result.getLeft());
							logger.debug("Flashlist definition:" + result.getLeft().getDefinitionNode());

						} catch (IOException e) {
							logger.error("Error reading flashlist " + flashlistType);
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
		logger.info("Reading all flashlists finished in " + time + "ms, flashlist specific results: " + statuses);
		return flashlists;
	}

	@Override
	public Pair<Flashlist, String> retrieveFlashlist(FlashlistType flashlistType) {
		logger.debug("Requested flashlist " + flashlistType + " retrieval");
		try {
			final Date retrievalDate = new Date();
			return downloadNonSessionContextFlashlist(flashlistType, retrievalDate);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Problem dowloading specific flashlist", e);
		}
	}

	private Pair<Flashlist, String> downloadNonSessionContextFlashlist(FlashlistType flashlistType, Date retrievalDate)
			throws IOException {

		Flashlist flashlistSnapshot = new Flashlist(flashlistType, suppressFailedRequests);
		try {
			int time = flashlistSnapshot.download(retrievalDate);
			logger.debug("Flashlist " + flashlistType + " downloaded in " + time + "ms, without sessionId.");
			return Pair.of(flashlistSnapshot, "HTTP-200 in " + time + "ms");
		} catch (DAQException e) {
			return Pair.of(flashlistSnapshot, e.getMessage());
		}

	}

	private Pair<Flashlist, String> downloadSessionContextFlashlist(FlashlistType flashlistType, Date retrievalDate,
			int sessionId) throws IOException {

		Flashlist flashlistSnapshot = new Flashlist(flashlistType, sessionId, suppressFailedRequests);
		try {
			int time = flashlistSnapshot.download(retrievalDate);
			logger.debug("Flashlist " + flashlistType + " downloaded in " + time + "ms, with sessionId: " + sessionId);
			return Pair.of(flashlistSnapshot, "HTTP-200 in " + time + "ms");
		} catch (DAQException e) {
			return Pair.of(flashlistSnapshot, e.getMessage());
		}

	}

}
