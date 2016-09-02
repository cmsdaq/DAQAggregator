package rcms.utilities.daqaggregator.datasource;

import java.util.Collection;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.DAQAggregatorException;
import rcms.utilities.daqaggregator.DAQAggregatorExceptionCode;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.mappers.MappingManager;
import rcms.utilities.daqaggregator.mappers.PostProcessor;
import rcms.utilities.hwcfg.HardwareConfigurationException;
import rcms.utilities.hwcfg.InvalidNodeTypeException;
import rcms.utilities.hwcfg.PathNotFoundException;
import rcms.utilities.hwcfg.dp.DAQPartition;

public class MonitorManager {

	private static final Logger logger = Logger.getLogger(MonitorManager.class);
	private final FlashlistRetriever flashlistRetriever;
	private final SessionDetector sessionDetector;
	private final HardwareConnector hardwareConnector;

	/** Manager for mapping the structure */
	private MappingManager mappingManager;

	/** Current mapped structure */
	private DAQ daq;

	private FlashlistManager flashlistManager;

	public MonitorManager(FlashlistRetriever flashlistRetriever, SessionRetriever sessionRetriever,
			HardwareConnector hardwareConnector) {

		this.flashlistRetriever = flashlistRetriever;
		this.hardwareConnector = hardwareConnector;
		this.sessionDetector = new SessionDetector(sessionRetriever, flashlistRetriever);

	}

	public Triple<DAQ, Collection<Flashlist>, Boolean> getSystemSnapshot()
			throws HardwareConfigurationException, PathNotFoundException, InvalidNodeTypeException {

		try {
			boolean newSession = sessionDetector.detectNewSession();

			if (newSession) {
				logger.info("Loading hardware for new session");
				daq = rebuildDaqModel(sessionDetector.getResult());

				flashlistRetriever.retrieveAvailableFlashlists(sessionDetector.getResult().getMiddle());
			}

			Collection<Flashlist> flashlists = flashlistRetriever.retrieveAllFlashlists().values();
			flashlistManager.mapFlashlists(flashlists);

			long lastUpdate = 0L;
			for (Flashlist flashlist : flashlists) {
				// why null here?
				logger.debug(flashlist.getRetrievalDate() + ", " + flashlist.getFlashlistType());
				if (flashlist.getRetrievalDate() != null && lastUpdate < flashlist.getRetrievalDate().getTime()) {
					lastUpdate = flashlist.getRetrievalDate().getTime();
				}
			}

			daq.setLastUpdate(lastUpdate);
			// postprocess daq (derived values, summary classes)
			PostProcessor postProcessor = new PostProcessor(daq);
			postProcessor.postProcess();
			return Triple.of(daq, flashlists, newSession);

		} catch (DAQAggregatorException e) {
			if (e.getCode() == DAQAggregatorExceptionCode.SessionCannotBeRetrieved) {
				logger.info("session cannot be retrieved temporarly");
				flashlistRetriever.retrieveAllFlashlists();
			} else
				throw e;
			return null;
		}

	}

	/**
	 * Rebuilds DAQ object when session changes:
	 * <ul>
	 * <li>gets the new daq partition from hardware database</li>
	 * <li>maps new DAQ model based on new hardware database configuration</li>
	 * <li>clears the flashistManager</li>
	 * <li></li>
	 * </ul>
	 * 
	 * @param triple
	 * @throws InvalidNodeTypeException
	 * @throws PathNotFoundException
	 * @throws HardwareConfigurationException
	 */
	private DAQ rebuildDaqModel(Triple<String, Integer, Long> triple)
			throws HardwareConfigurationException, PathNotFoundException, InvalidNodeTypeException {
		String path = triple.getLeft();
		int sid = triple.getMiddle();

		// get daqpartition from hardwareconnector
		DAQPartition daqPartition = hardwareConnector.getPartition(path);

		// map the structure to new DAQ
		mappingManager = new MappingManager(daqPartition);
		logger.info("New DAQ structure");
		daq = mappingManager.map();
		daq.setSessionId(sid);
		daq.setDpsetPath(path);
		flashlistManager = new FlashlistManager(mappingManager);

		logger.info("Done for session " + daq.getSessionId());
		return daq;
	}

}
