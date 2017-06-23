package rcms.utilities.daqaggregator.datasource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import rcms.common.db.DBConnectorException;
import rcms.utilities.daqaggregator.Settings;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FRL;
import rcms.utilities.daqaggregator.data.FRLType;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.mappers.MappingManager;
import rcms.utilities.daqaggregator.mappers.MappingReporter;
import rcms.utilities.daqaggregator.persistence.PersistenceFormat;
import rcms.utilities.hwcfg.HardwareConfigurationException;
import rcms.utilities.hwcfg.InvalidNodeTypeException;
import rcms.utilities.hwcfg.PathNotFoundException;
import rcms.utilities.hwcfg.dp.DAQPartition;

/**
 * Integration test verifying how the flashlists data is dispatched to objects.
 * This tests both dispatcher and all individual matchers
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 * @author holzner
 * 
 *         TODO: in this test we are dependent on external subsystem: hardware
 *         database
 *
 *         TODO: in this test we need properties file with all paswords to
 *         connect to hardware configuration - this makes it impossible to run
 *         in CI environment only based on repository (properties files cannot
 *         be commited to project repo). Try to make a dump of hardware database
 *         object and bring it to project
 */
public class FlashlistDispatcherIT {

	private static final Logger logger = Logger.getLogger(FlashlistDispatcherIT.class);
	private int forcedSessionId;
	private String forcedPath;

	@Before
	public void prepare() {
		MappingReporter.get().clear();
	}

	/**
	 * Run dispatch procedure on given flash-list directory
	 * 
	 * @param forceIgnoreAutomaticSessionDetection
	 *            flag to explicitly ignore automatic session detection
	 */
	private DAQ runDispatch(String flashlistDir, boolean forceIgnoreAutomaticSessionDetection) throws IOException,
			DBConnectorException, HardwareConfigurationException, PathNotFoundException, InvalidNodeTypeException {

		FileFlashlistRetriever flashlistRetriever = new FileFlashlistRetriever(flashlistDir, PersistenceFormat.JSON);

		flashlistRetriever.prepare(Long.MIN_VALUE);
		Map<FlashlistType, Flashlist> flashlists = flashlistRetriever.retrieveAllFlashlists(0);

		HardwareConnector hardwareConnector = new HardwareConnector();

		Properties prop = new Properties();
		prop.load(new FileInputStream("DAQAggregator.properties"));

		String filter1 = prop.getProperty(Settings.SESSION_L0FILTER1.getKey());
		String filter2 = prop.getProperty(Settings.SESSION_L0FILTER2.getKey());

		// connect to the hardware database
		hardwareConnector.initialize(prop);

		SessionRetriever sr = new SessionRetriever(filter1, filter2);
		int sessionId;
		String path;
		if (!forceIgnoreAutomaticSessionDetection) {
			Triple<String, Integer, Long> r = sr.retrieveSession(flashlists.get(FlashlistType.LEVEL_ZERO_FM_DYNAMIC));
			sessionId = r.getMiddle();
			path = r.getLeft();
		} else {
			sessionId = forcedSessionId;
			path = forcedPath;
		}

		DAQPartition daqPartition = hardwareConnector.getPartition(path);

		// TODO: we could mock this object
		TCDSFMInfoRetriever tcdsFmInfoRetriever = new TCDSFMInfoRetriever(flashlistRetriever);

		FlashlistDispatcher dispatcher = new FlashlistDispatcher();

		MappingManager mappingManager = new MappingManager(daqPartition, tcdsFmInfoRetriever);
		DAQ daq = mappingManager.map();

		daq.setSessionId(sessionId);
		daq.setDpsetPath(path);

		for (Flashlist flashlist : flashlists.values()) {
			dispatcher.dispatch(flashlist, mappingManager);
		}

		return daq;
	}

	/**
	 * Test the mapping procedure by asserting how much objects have been
	 * successfully and unsuccessfully dispatched
	 * 
	 * TODO: ideally 0 objects should be unsuccessfully dispatched - investigate
	 * further
	 */
	@Test
	public void test() throws IOException, DBConnectorException, HardwareConfigurationException, PathNotFoundException,
			InvalidNodeTypeException {

		runDispatch("src/test/resources/compatibility/1.6.0/flashlists/", false);

		Assert.assertEquals(1196, MappingReporter.get().getTotalObjects().get("FRL_MONITORING").intValue());
		Assert.assertEquals(83, MappingReporter.get().getTotalObjects().get("RU").intValue());
		Assert.assertEquals(450, MappingReporter.get().getTotalObjects().get("FEROL_STATUS").intValue());
		Assert.assertEquals(777, MappingReporter.get().getTotalObjects().get("FMM_INPUT").intValue());
		Assert.assertEquals(1004, MappingReporter.get().getTotalObjects().get("JOB_CONTROL").intValue());
		Assert.assertEquals(450, MappingReporter.get().getTotalObjects().get("FEROL40_STATUS").intValue());
		Assert.assertEquals(777, MappingReporter.get().getTotalObjects().get("FEROL40_INPUT_STREAM").intValue());
		Assert.assertEquals(57, MappingReporter.get().getTotalObjects().get("BU").intValue());
		Assert.assertEquals(777, MappingReporter.get().getTotalObjects().get("FEROL_INPUT_STREAM").intValue());
		Assert.assertEquals(777,
				MappingReporter.get().getTotalObjects().get("FEROL40_STREAM_CONFIGURATION").intValue());
		Assert.assertEquals(96, MappingReporter.get().getTotalObjects().get("FMM_STATUS").intValue());
		Assert.assertEquals(1195, MappingReporter.get().getTotalObjects().get("FEROL_CONFIGURATION").intValue());
		Assert.assertEquals(32, MappingReporter.get().getTotalObjects().get("FEROL40_CONFIGURATION").intValue());

		logger.info(MappingReporter.get().getMissingObjects());
		Assert.assertEquals(144, MappingReporter.get().getMissingObjects().get("FRL_MONITORING").intValue());
		Assert.assertEquals(0, MappingReporter.get().getMissingObjects().get("RU").intValue());
		Assert.assertEquals(32, MappingReporter.get().getMissingObjects().get("FEROL_STATUS").intValue());
		Assert.assertEquals(185, MappingReporter.get().getMissingObjects().get("FMM_INPUT").intValue());
		Assert.assertEquals(813, MappingReporter.get().getMissingObjects().get("JOB_CONTROL").intValue());
		Assert.assertEquals(418, MappingReporter.get().getMissingObjects().get("FEROL40_STATUS").intValue());
		Assert.assertEquals(669, MappingReporter.get().getMissingObjects().get("FEROL40_INPUT_STREAM").intValue());
		Assert.assertEquals(0, MappingReporter.get().getMissingObjects().get("BU").intValue());
		Assert.assertEquals(144, MappingReporter.get().getMissingObjects().get("FEROL_INPUT_STREAM").intValue());
		Assert.assertEquals(669,
				MappingReporter.get().getMissingObjects().get("FEROL40_STREAM_CONFIGURATION").intValue());
		Assert.assertEquals(33, MappingReporter.get().getMissingObjects().get("FMM_STATUS").intValue());
		Assert.assertEquals(139, MappingReporter.get().getMissingObjects().get("FEROL_CONFIGURATION").intValue());
		Assert.assertEquals(0, MappingReporter.get().getMissingObjects().get("FEROL40_CONFIGURATION").intValue());

	}

	@Test
	public void testmultipleSessions1() throws IOException, DBConnectorException, HardwareConfigurationException,
			PathNotFoundException, InvalidNodeTypeException {

		forcedPath = "/daq2/eq_160913_01/fb_all_with1240_withCASTOR_w582_583/dp_bl381_75BU";
		forcedSessionId = 286004;
		DAQ daq = runDispatch("src/test/resources/compatibility/1.5.0/flashlists/", true);

		Set<String> subsystemNames = new HashSet<>();
		for (SubSystem subsystem : daq.getSubSystems()) {
			if (subsystem.getStatus() != null) {
				subsystemNames.add(subsystem.getName());
			} else {
				logger.info("Ignoring subsystem " + subsystem.getName() + " as the status is null");
			}
		}

		Set<String> expected = new HashSet<>(Arrays.asList("CASTOR", "CSC", "CTPPS_TOT",
				/* "DAQ", "DCS", "DQM", */ "DT", "ECAL", "ES", "HCAL", "HF", "PIXEL", "PIXEL_UP", "RPC", "SCAL", "TCDS",
				"TRACKER", "TRG"));

		// andre: DAQ, DCS and DQM seem not to be detected as subsystems with
		// non-null state for some reason
		assertThat(subsystemNames, is(expected));
	}

	@Test
	public void testmultipleSessions2() throws IOException, DBConnectorException, HardwareConfigurationException,
			PathNotFoundException, InvalidNodeTypeException {

		forcedPath = "/daq2/eq_150929/fb_all_withuTCA/dp_bl116_64BU";
		forcedSessionId = 284766;
		DAQ daq = runDispatch("src/test/resources/compatibility/1.5.0/flashlists/", true);

		Set<String> subsystemNames = new HashSet<>();
		for (SubSystem subsystem : daq.getSubSystems()) {
			if (subsystem.getStatus() != null) {
				subsystemNames.add(subsystem.getName());
			} else {
				logger.info("Ignoring subsystem " + subsystem.getName() + " as the status is null");
			}
		}

		Set<String> expected = new HashSet<>(Arrays.asList(
				/* "DAQ", "DCS", "DQM", */ "ECAL", "ES", "HCAL", "HF", "PIXEL", "PIXEL_UP", "TCDS", "TRACKER", "TRG"));

		// andre: DAQ, DCS and DQM seem not to be detected as subsystems with
		// non-null state for some reason
		assertThat(subsystemNames, is(expected));
	}

	/**
	 * Verify how many FEDs there is, how may legacy, updated feds. How many of
	 * them has TTS state
	 */
	@Test
	public void testDispatchedValuesFromFlashlistTcdsPiSummary() throws IOException, DBConnectorException,
			HardwareConfigurationException, PathNotFoundException, InvalidNodeTypeException {

		DAQ daq = runDispatch("src/test/resources/compatibility/1.12.0/flashlists/", false);

		int failed = 0;
		Set<FED> upgradedFeds = new HashSet<>();
		Set<FED> legacyFeds = new HashSet<>();
		Set<FED> pseudoFeds = new HashSet<>();
		for (FED fed : daq.getFeds()) {
			if (fed.getFrl() != null) {
				FRL frl = fed.getFrl();
				if (frl.getType() != null) {
					if (frl.getType() == FRLType.FEROL40_10G || frl.getType() == FRLType.FEROL40_6G) {
						logger.debug("FED " + fed.getSrcIdExpected() + " is upgraded FED with " + frl.getType()
								+ " link, it has deps: " + fed.getDependentFeds());
						upgradedFeds.add(fed);
					} else {
						logger.debug("FED " + fed.getSrcIdExpected() + " is legacy FED, it has deps: "
								+ fed.getDependentFeds());
						legacyFeds.add(fed);
					}
				} else {
					logger.warn("FRL has no type: " + frl);
					failed++;
				}
			} else {
				logger.debug("FED " + fed.getSrcIdExpected() + " has no FRL");
				pseudoFeds.add(fed);
			}
		}

		/*
		 * for (FED fed : upgradedFeds) { if (fed.getDependentFeds().size() > 0)
		 * { for (FED dep : fed.getDependentFeds()) { if (dep.getTtsState() !=
		 * null && fed.getTtsState() == null) {
		 * fed.setTtsState(dep.getTtsState()); } } } }
		 */

		logger.info("Summary of FEDs:");
		logger.info(" - all FEDS                  : " + daq.getFeds().size());
		logger.info(" - upgraded FEDS             : " + upgradedFeds.size());
		logger.info(" - legacy FEDS               : " + legacyFeds.size());
		logger.info(" - FEDS without FRL (pseudo?): " + pseudoFeds.size());
		logger.info(" - FRL without type          : " + failed);
		Assert.assertEquals(777, daq.getFeds().size());
		Assert.assertEquals(108, upgradedFeds.size());
		Assert.assertEquals(638, legacyFeds.size());
		Assert.assertEquals(31, pseudoFeds.size());
		Assert.assertEquals(0, failed);

		int upgradedFedsWithoutTTSState = 0;
		int upgradedFedsWithoutTTSStateButWithPseudoWithTTS = 0;
		for (FED fed : upgradedFeds) {
			if (fed.getTtsState() == null) {
				upgradedFedsWithoutTTSState++;
				for (FED dep : fed.getDependentFeds()) {
					if (dep.getTtsState() != null) {
						upgradedFedsWithoutTTSStateButWithPseudoWithTTS++;
					}
				}
			}
			// Assert.assertNull(fed.getTtsState());
		}
		logger.info("Summary of ferol40 FEDs:");
		logger.info(" - FEDS with TTS state       : " + (upgradedFeds.size() - upgradedFedsWithoutTTSState));
		logger.info(" - FEDS without TTS state    : " + upgradedFedsWithoutTTSState);
		logger.info(" - FEDS withoTTS (pseudo has): " + upgradedFedsWithoutTTSStateButWithPseudoWithTTS);
		Assert.assertEquals(0, (upgradedFeds.size() - upgradedFedsWithoutTTSState)); // without
																						// pi_flashlist
																						// 0
		Assert.assertEquals(108, upgradedFedsWithoutTTSState);// without
																// pi_flashlist
																// 108
		Assert.assertEquals(108, upgradedFedsWithoutTTSStateButWithPseudoWithTTS);

		int pseudoWithoudTTSState = 0;
		for (FED fed : pseudoFeds) {
			if (fed.getTtsState() == null) {
				pseudoWithoudTTSState++;
			}
		}
		logger.info("Summary of pseudo FEDs:");
		logger.info(" - FEDS with TTS state       : " + (pseudoFeds.size() - pseudoWithoudTTSState));
		logger.info(" - FEDS without TTS state    : " + pseudoWithoudTTSState);
		Assert.assertEquals(31, (pseudoFeds.size() - pseudoWithoudTTSState)); // without
																				// pi_flashlist
																				// 5
		Assert.assertEquals(0, pseudoWithoudTTSState);// without pi_flashlist 26

		int legacyFedsWithoutTTSState = 0;
		for (FED fed : legacyFeds) {
			if (!fed.isFrlMasked()) {
				if (fed.getTtsState() == null) {
					logger.debug("FED " + fed.getSrcIdExpected() + " has null tts state");
					legacyFedsWithoutTTSState++;
				}
			}
		}

		logger.info("Summary of legacy feds:");
		logger.info(" - FEDS with TTS state       : " + (legacyFeds.size() - legacyFedsWithoutTTSState));
		logger.info(" - FEDS without TTS state    : " + legacyFedsWithoutTTSState);
		Assert.assertEquals(608, (legacyFeds.size() - legacyFedsWithoutTTSState));// without
																					// pi_flashlist
																					// 595
		Assert.assertEquals(30, legacyFedsWithoutTTSState);// without
															// pi_flashlist 43

	}

	/**
	 * Verify how many FEDs there is during STABLE BEAMS
	 */
	@Test
	public void testDispatchedValuesFromFlashlistTcdsPiSummary2() throws IOException, DBConnectorException,
			HardwareConfigurationException, PathNotFoundException, InvalidNodeTypeException {

		DAQ daq = runDispatch("src/test/resources/compatibility/1.12.1/flashlists/", false);

		int failed = 0;
		Set<FED> upgradedFeds = new HashSet<>();
		Set<FED> legacyFeds = new HashSet<>();
		Set<FED> pseudoFeds = new HashSet<>();
		for (FED fed : daq.getFeds()) {
			if (fed.getFrl() != null) {
				FRL frl = fed.getFrl();
				if (frl.getType() != null) {
					if (frl.getType() == FRLType.FEROL40_10G || frl.getType() == FRLType.FEROL40_6G) {
						logger.debug("FED " + fed.getSrcIdExpected() + " is upgraded FED with " + frl.getType()
								+ " link, it has deps: " + fed.getDependentFeds());
						upgradedFeds.add(fed);
					} else {
						logger.debug("FED " + fed.getSrcIdExpected() + " is legacy FED, it has deps: "
								+ fed.getDependentFeds());
						legacyFeds.add(fed);
					}
				} else {
					logger.warn("FRL has no type: " + frl);
					failed++;
				}
			} else {
				logger.debug("FED " + fed.getSrcIdExpected() + " has no FRL");
				pseudoFeds.add(fed);
			}
		}

		/*
		 * for (FED fed : upgradedFeds) { if (fed.getDependentFeds().size() > 0)
		 * { for (FED dep : fed.getDependentFeds()) { if (dep.getTtsState() !=
		 * null && fed.getTtsState() == null) {
		 * fed.setTtsState(dep.getTtsState()); } } } }
		 */

		logger.info("Summary of FEDs:");
		logger.info(" - all FEDS                  : " + daq.getFeds().size());
		logger.info(" - upgraded FEDS             : " + upgradedFeds.size());
		logger.info(" - legacy FEDS               : " + legacyFeds.size());
		logger.info(" - FEDS without FRL (pseudo?): " + pseudoFeds.size());
		logger.info(" - FRL without type          : " + failed);
		Assert.assertEquals(777, daq.getFeds().size());
		Assert.assertEquals(108, upgradedFeds.size());
		Assert.assertEquals(638, legacyFeds.size());
		Assert.assertEquals(31, pseudoFeds.size());
		Assert.assertEquals(0, failed);

		int upgradedFedsWithoutTTSState = 0;
		int upgradedFedsWithoutTTSStateButWithPseudoWithTTS = 0;
		for (FED fed : upgradedFeds) {
			if (fed.getTtsState() == null) {
				upgradedFedsWithoutTTSState++;
				for (FED dep : fed.getDependentFeds()) {
					if (dep.getTtsState() != null) {
						upgradedFedsWithoutTTSStateButWithPseudoWithTTS++;
					}
				}
			}
			// Assert.assertNull(fed.getTtsState());
		}
		logger.info("Summary of ferol40 FEDs:");
		logger.info(" - FEDS with TTS state       : " + (upgradedFeds.size() - upgradedFedsWithoutTTSState));
		logger.info(" - FEDS without TTS state    : " + upgradedFedsWithoutTTSState);
		logger.info(" - FEDS withoTTS (pseudo has): " + upgradedFedsWithoutTTSStateButWithPseudoWithTTS);
		Assert.assertEquals(0, (upgradedFeds.size() - upgradedFedsWithoutTTSState)); // without
																						// pi_flashlist
																						// 0
		Assert.assertEquals(108, upgradedFedsWithoutTTSState);// without
																// pi_flashlist
																// 108
		Assert.assertEquals(108, upgradedFedsWithoutTTSStateButWithPseudoWithTTS);

		int pseudoWithoudTTSState = 0;
		for (FED fed : pseudoFeds) {
			if (fed.getTtsState() == null) {
				pseudoWithoudTTSState++;
			}
		}
		logger.info("Summary of pseudo FEDs:");
		logger.info(" - FEDS with TTS state       : " + (pseudoFeds.size() - pseudoWithoudTTSState));
		logger.info(" - FEDS without TTS state    : " + pseudoWithoudTTSState);
		Assert.assertEquals(31, (pseudoFeds.size() - pseudoWithoudTTSState)); // without
																				// pi_flashlist
																				// 5
		Assert.assertEquals(0, pseudoWithoudTTSState);// without pi_flashlist 26

		int legacyFedsWithoutTTSState = 0;
		for (FED fed : legacyFeds) {
			if (!fed.isFrlMasked()) {
				if (fed.getTtsState() == null) {
					logger.debug("FED " + fed.getSrcIdExpected() + " has null tts state");
					legacyFedsWithoutTTSState++;
				}
			}
		}

		logger.info("Summary of legacy feds:");
		logger.info(" - FEDS with TTS state       : " + (legacyFeds.size() - legacyFedsWithoutTTSState));
		logger.info(" - FEDS without TTS state    : " + legacyFedsWithoutTTSState);
		Assert.assertEquals(608, (legacyFeds.size() - legacyFedsWithoutTTSState));// without
																					// pi_flashlist
																					// 595
		Assert.assertEquals(30, legacyFedsWithoutTTSState);// without
															// pi_flashlist 43

	}

}
