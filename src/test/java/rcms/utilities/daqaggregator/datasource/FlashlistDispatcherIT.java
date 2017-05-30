package rcms.utilities.daqaggregator.datasource;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import rcms.common.db.DBConnectorException;
import rcms.utilities.daqaggregator.Settings;
import rcms.utilities.daqaggregator.mappers.MappingManager;
import rcms.utilities.daqaggregator.mappers.MappingReporter;
import rcms.utilities.daqaggregator.persistence.PersistenceFormat;
import rcms.utilities.hwcfg.HardwareConfigurationException;
import rcms.utilities.hwcfg.InvalidNodeTypeException;
import rcms.utilities.hwcfg.PathNotFoundException;
import rcms.utilities.hwcfg.dp.DAQPartition;

public class FlashlistDispatcherIT {

	private static final Logger logger = Logger.getLogger(FlashlistDispatcherIT.class);

	@Test
	public void test() throws IOException, DBConnectorException, HardwareConfigurationException, PathNotFoundException,
			InvalidNodeTypeException {

		FlashlistDispatcher sut = new FlashlistDispatcher(null);

		long startTime = System.currentTimeMillis();
		MappingReporter.get().clear();
		final String versionDir = "1.6.0/flashlists";
		final String flashlistDir = "src/test/resources/compatibility/" + versionDir + "/";

		FileFlashlistRetriever flashlistRetriever = new FileFlashlistRetriever(flashlistDir, PersistenceFormat.JSON);

		flashlistRetriever.prepare(1496135196681L - 1);
		Map<FlashlistType, Flashlist> flashlists = flashlistRetriever.retrieveAllFlashlists(0);

		HardwareConnector hardwareConnector = new HardwareConnector();

		Properties prop = new Properties();
		prop.load(new FileInputStream("DAQAggregator.properties"));

		String url = prop.getProperty(Settings.HWCFGDB_DBURL.getKey());
		String host = prop.getProperty(Settings.HWCFGDB_HOST.getKey());
		String port = prop.getProperty(Settings.HWCFGDB_PORT.getKey());
		String sid = prop.getProperty(Settings.HWCFGDB_SID.getKey());
		String user = prop.getProperty(Settings.HWCFGDB_LOGIN.getKey());
		String passwd = prop.getProperty(Settings.HWCFGDB_PWD.getKey());
		String filter1 = prop.getProperty(Settings.SESSION_L0FILTER1.getKey());
		String filter2 = prop.getProperty(Settings.SESSION_L0FILTER2.getKey());

		// connect to the hardware database
		hardwareConnector.initialize(url, host, port, sid, user, passwd);

		SessionRetriever sr = new SessionRetriever(filter1, filter2);
		Triple<String, Integer, Long> r = sr.retrieveSession(flashlists.get(FlashlistType.LEVEL_ZERO_FM_DYNAMIC));

		DAQPartition daqPartition = hardwareConnector.getPartition(r.getLeft());

		// TODO: we could mock this object
		TCDSFMInfoRetriever tcdsFmInfoRetriever = new TCDSFMInfoRetriever(flashlistRetriever);

		MappingManager mappingManager = new MappingManager(daqPartition, tcdsFmInfoRetriever);
		mappingManager.map();
		for (Flashlist flashlist : flashlists.values()) {

			FlashlistDispatcher dispatcher = new FlashlistDispatcher(filter1);

			dispatcher.dispatch(flashlist, mappingManager);
		}
		long stopTime = System.currentTimeMillis();
		int time = (int) (stopTime - startTime);
		logger.debug("Mapping all flashlists finished in " + time + "ms");

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

}
