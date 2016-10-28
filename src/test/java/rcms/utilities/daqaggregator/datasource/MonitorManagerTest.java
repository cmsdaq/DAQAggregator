package rcms.utilities.daqaggregator.datasource;

import java.io.IOException;
import java.util.Collection;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import rcms.common.db.DBConnectorException;
import rcms.utilities.daqaggregator.Application;
import rcms.utilities.daqaggregator.Settings;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.persistence.PersistenceFormat;
import rcms.utilities.hwcfg.HardwareConfigurationException;
import rcms.utilities.hwcfg.InvalidNodeTypeException;
import rcms.utilities.hwcfg.PathNotFoundException;

/**
 * This test ensures that {@link MonitorManager#getSystemSnapshot()} method
 * returns correct results. Tests if returned triple is correct:
 * <ul>
 * <li>briefly tests DAQ snapshot</li>
 * <li>asserts date of snapshot update</li>
 * </ul>
 * 
 * @throws HardwareConfigurationException
 * @throws PathNotFoundException
 * @throws InvalidNodeTypeException
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 * 
 * @TODO: make it independent on hardware availability
 * 
 */
public class MonitorManagerTest {

	private static MonitorManager monitorManager;

	private static final String TEST_FLASHLISTS_DIR = "src/test/resources/compatibility/1.4.0/";

	@BeforeClass
	public static void initialize() throws IOException, DBConnectorException, HardwareConfigurationException {

		FileFlashlistRetriever flashlistRetriever = new FileFlashlistRetriever(TEST_FLASHLISTS_DIR,
				PersistenceFormat.JSON);
		long startLimit = DatatypeConverter.parseDateTime("2016-09-01T15:00:00Z").getTimeInMillis();
		flashlistRetriever.prepare(startLimit);
		SessionRetriever sessionRetriever = new SessionRetriever("toppro", "toppro");
		HardwareConnector hardwareConnector = new HardwareConnector();

		Application.initialize("DAQAggregator.properties");
		String url = Application.get().getProp(Settings.HWCFGDB_DBURL);
		String host = Application.get().getProp(Settings.HWCFGDB_HOST);
		String port = Application.get().getProp(Settings.HWCFGDB_PORT);
		String sid = Application.get().getProp(Settings.HWCFGDB_SID);
		String user = Application.get().getProp(Settings.HWCFGDB_LOGIN);
		String passwd = Application.get().getProp(Settings.HWCFGDB_PWD);
		hardwareConnector.initialize(url, host, port, sid, user, passwd);
		monitorManager = new MonitorManager(flashlistRetriever, sessionRetriever, hardwareConnector);
	}

	@Test
	public void simpleTest() throws HardwareConfigurationException, PathNotFoundException, InvalidNodeTypeException {

		Triple<DAQ, Collection<Flashlist>, Boolean> snapshot1 = monitorManager.getSystemSnapshot();
		Assert.assertTrue(snapshot1.getRight());
		testSnapshot(snapshot1.getLeft(), 1472743072594L, 0);

		Triple<DAQ, Collection<Flashlist>, Boolean> snapshot2 = monitorManager.getSystemSnapshot();
		Assert.assertFalse(snapshot2.getRight());
		testSnapshot(snapshot2.getLeft(), 1472743080834L, 0);

		Triple<DAQ, Collection<Flashlist>, Boolean> snapshot3 = monitorManager.getSystemSnapshot();
		Assert.assertFalse(snapshot3.getRight());
		testSnapshot(snapshot3.getLeft(), 1472743088565L, 0);
	}

	/**
	 * General method for asserting DAQ snapshot
	 * 
	 * @param snapshot
	 * @param expectedTime
	 * @param expectedEvents
	 */
	private void testSnapshot(DAQ snapshot, long expectedTime, int expectedEvents) {
		Assert.assertNotNull(snapshot);
		Assert.assertEquals(expectedTime, snapshot.getLastUpdate());
		Assert.assertEquals(expectedEvents, snapshot.getFedBuilderSummary().getSumEventsInRU());

	}

}
