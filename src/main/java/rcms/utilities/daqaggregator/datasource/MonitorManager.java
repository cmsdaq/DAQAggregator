package rcms.utilities.daqaggregator.datasource;

import java.util.Collection;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.DAQException;
import rcms.utilities.daqaggregator.DAQExceptionCode;
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
	private final TCDSFMInfoRetriever tcdsFmInfoRetriever;
	private final HardwareConnector hardwareConnector;
	
	private final F3DataRetriever f3dataRetriever;

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
		this.tcdsFmInfoRetriever = new TCDSFMInfoRetriever(flashlistRetriever);
		this.f3dataRetriever = new F3DataRetriever(new Connector());
	}

	public void skipToNextSnapshot() {
		if (flashlistRetriever instanceof FileFlashlistRetriever) {
			((FileFlashlistRetriever) flashlistRetriever).skip();
		}
	}

	public Triple<DAQ, Collection<Flashlist>, Boolean> getSystemSnapshot()
			throws HardwareConfigurationException, PathNotFoundException, InvalidNodeTypeException {

		logger.debug("Detecting new session");
		boolean newSession = sessionDetector.detectNewSession();
		boolean newTrigger = tcdsFmInfoRetriever.detectNewTrigger(); //if true, it will update tcds fm info internally on the tcdsFmInfoRetriever object and they will be picked up later in structure building
		
		logger.debug("New session: " + newSession);
		
		
		//rebuild structure if newSession or newTrigger (in both cases we need the session information for HW)
		if (newSession || newTrigger) {
			
			String reason = newSession? "session" : "trigger";
			
			logger.info("New "+reason+" detected. Rebuilding the DAQ model.");
			long start = System.currentTimeMillis();
			daq = rebuildDaqModel(sessionDetector.getResult());
			int timeToRebuild = (int) (System.currentTimeMillis() - start);
			logger.info("Structure rebuilded in " + timeToRebuild + "ms");
			logger.info("--------------------------------------");

		}

		int sessionId = sessionDetector.getResult().getMiddle();
		
		Collection<Flashlist> flashlists = flashlistRetriever.retrieveAllFlashlists(sessionId).values();
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
		

		f3dataRetriever.dispatch(daq);
		return Triple.of(daq, flashlists, newSession);

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
		mappingManager = new MappingManager(daqPartition, this.tcdsFmInfoRetriever);
		logger.info("New DAQ structure");
		daq = mappingManager.map();
		daq.setSessionId(sid);
		daq.setDpsetPath(path);
		flashlistManager = new FlashlistManager(mappingManager);

		logger.info("Done for session " + daq.getSessionId());
		return daq;
	}

}
