package rcms.utilities.daqaggregator.datasource;

import java.io.IOException;
import java.util.Collection;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import rcms.common.db.DBConnectorException;
import rcms.utilities.daqaggregator.Application;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.persistence.PersistenceFormat;
import rcms.utilities.hwcfg.HardwareConfigurationException;
import rcms.utilities.hwcfg.InvalidNodeTypeException;
import rcms.utilities.hwcfg.PathNotFoundException;

public class MonitorManagerTest {

	private static MonitorManager monitorManager;

	private static final String TEST_FLASHLISTS_DIR = "src/test/resources/test-flashlists/";

	@BeforeClass
	public static void initialize() throws IOException, DBConnectorException, HardwareConfigurationException {

		FileFlashlistRetriever flashlistRetriever = new FileFlashlistRetriever(TEST_FLASHLISTS_DIR,
				PersistenceFormat.SMILE);
		flashlistRetriever.prepare();
		SessionRetriever sessionRetriever = new SessionRetriever("toppro", "toppro");
		HardwareConnector hardwareConnector = new HardwareConnector();

		Application.initialize("DAQAggregator.test.properties");
		String url = Application.get().getProp().getProperty(Application.PROPERTYNAME_HWCFGDB_DBURL);
		String host = Application.get().getProp().getProperty(Application.PROPERTYNAME_HWCFGDB_HOST);
		String port = Application.get().getProp().getProperty(Application.PROPERTYNAME_HWCFGDB_PORT);
		String sid = Application.get().getProp().getProperty(Application.PROPERTYNAME_HWCFGDB_SID);
		String user = Application.get().getProp().getProperty(Application.PROPERTYNAME_HWCFGDB_LOGIN);
		String passwd = Application.get().getProp().getProperty(Application.PROPERTYNAME_HWCFGDB_PWD);
		hardwareConnector.initialize(url, host, port, sid, user, passwd);
		monitorManager = new MonitorManager(flashlistRetriever, sessionRetriever, hardwareConnector);
	}

	@Test
	public void simpleTest() throws HardwareConfigurationException, PathNotFoundException, InvalidNodeTypeException {

		Triple<DAQ, Collection<Flashlist>, Boolean> snapshot1 = monitorManager.getSystemSnapshot();
		Assert.assertTrue(snapshot1.getRight());
		testSnapshot(snapshot1.getLeft(), 1472630406999L, 0);

		Triple<DAQ, Collection<Flashlist>, Boolean> snapshot2 = monitorManager.getSystemSnapshot();
		Assert.assertFalse(snapshot2.getRight());
		testSnapshot(snapshot2.getLeft(), 1472630415772L, 0);

		Triple<DAQ, Collection<Flashlist>, Boolean> snapshot3 = monitorManager.getSystemSnapshot();
		Assert.assertFalse(snapshot3.getRight());
		testSnapshot(snapshot3.getLeft(), 1472630424552L, 0);
	}

	private void testSnapshot(DAQ snapshot, long expectedTime, int expectedEvents) {
		Assert.assertNotNull(snapshot);
		Assert.assertEquals(expectedTime, snapshot.getLastUpdate());
		Assert.assertEquals(expectedEvents, snapshot.getFedBuilderSummary().getSumEventsInRU());

	}

}
