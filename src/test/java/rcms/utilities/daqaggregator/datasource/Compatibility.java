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

public class Compatibility {

	private static HardwareConnector hardwareConnector;
	private static MonitorManager monitorManager;

	@BeforeClass
	public static void initialize() {
		hardwareConnector = new HardwareConnector();
		Application.initialize("DAQAggregator.properties");
	}

	@Test
	public void testFlashlistsOfVersion_1_4_0() throws IOException, DBConnectorException,
			HardwareConfigurationException, PathNotFoundException, InvalidNodeTypeException {
		testFlashlists("1.4.0", 1472743072594L, 0);
	}
	
	@Test
	public void testFlashlistsOfVersion_1_5_0() throws IOException, DBConnectorException,
			HardwareConfigurationException, PathNotFoundException, InvalidNodeTypeException {
		testFlashlists("1.5.0/flashlists", 1477557890932L, 77);
	}

	public void testFlashlists(String versionDir, long timestamp, int sumEvents) throws IOException, DBConnectorException,
			HardwareConfigurationException, PathNotFoundException, InvalidNodeTypeException {
		// 1 load flashlists
		loadFlashlists("src/test/resources/compatibility/" + versionDir + "/");

		// 2 generate snapshot
		Triple<DAQ, Collection<Flashlist>, Boolean> snapshot1 = monitorManager.getSystemSnapshot();
		Assert.assertTrue(snapshot1.getRight());

		// 3 test snapshot
		Assert.assertNotNull(snapshot1.getLeft());
		Assert.assertEquals(timestamp, snapshot1.getLeft().getLastUpdate().longValue());
		Assert.assertEquals(sumEvents, snapshot1.getLeft().getFedBuilderSummary().getSumEventsInRU());

	}

	private void loadFlashlists(String flashlistDir)
			throws IOException, DBConnectorException, HardwareConfigurationException {
		FileFlashlistRetriever flashlistRetriever = new FileFlashlistRetriever(flashlistDir, PersistenceFormat.JSON);
		long startLimit = DatatypeConverter.parseDateTime("2016-09-01T15:00:00Z").getTimeInMillis();
		flashlistRetriever.prepare(startLimit);
		SessionRetriever sessionRetriever = new SessionRetriever("toppro", "toppro");

		String url = Application.get().getProp(Settings.HWCFGDB_DBURL);
		String host = Application.get().getProp(Settings.HWCFGDB_HOST);
		String port = Application.get().getProp(Settings.HWCFGDB_PORT);
		String sid = Application.get().getProp(Settings.HWCFGDB_SID);
		String user = Application.get().getProp(Settings.HWCFGDB_LOGIN);
		String passwd = Application.get().getProp(Settings.HWCFGDB_PWD);
		hardwareConnector.initialize(url, host, port, sid, user, passwd);
		monitorManager = new MonitorManager(flashlistRetriever, sessionRetriever, hardwareConnector);
	}

	/**
	 * General method for asserting DAQ snapshot
	 * 
	 * @param snapshot
	 * @param expectedTime
	 * @param expectedEvents
	 */
	private void testSnapshot(DAQ snapshot, long expectedTime, int expectedEvents) {

	}

}
