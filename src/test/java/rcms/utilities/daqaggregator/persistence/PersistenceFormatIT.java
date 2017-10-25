package rcms.utilities.daqaggregator.persistence;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import rcms.common.db.DBConnectorException;
import rcms.utilities.daqaggregator.Application;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.datasource.FileFlashlistRetriever;
import rcms.utilities.daqaggregator.datasource.Flashlist;
import rcms.utilities.daqaggregator.datasource.HardwareConnector;
import rcms.utilities.daqaggregator.datasource.MonitorManager;
import rcms.utilities.daqaggregator.datasource.SessionRetriever;
import rcms.utilities.hwcfg.HardwareConfigurationException;
import rcms.utilities.hwcfg.InvalidNodeTypeException;
import rcms.utilities.hwcfg.PathNotFoundException;

public class PersistenceFormatIT {

	private static HardwareConnector hardwareConnector;
	private static MonitorManager monitorManager;

	/**
	 * method to load a deserialize a snapshot given a file name
	 */
	public static DAQ getSnapshot(String fname) throws URISyntaxException {
		StructureSerializer serializer = new StructureSerializer();
		File file = new File("src/test/resources/format/" + fname);
		return serializer.deserialize(file.getAbsolutePath());
	}

	@Test
	public void testJson() throws URISyntaxException {
		DAQ s = getSnapshot("1496217954303.json");
		doBasicAssertionsOnSnapshot(s);
	}

	@Test
	public void testSmile() throws URISyntaxException {
		DAQ s = getSnapshot("1496217954303.smile");
		doBasicAssertionsOnSnapshot(s);
	}

	@Test
	public void testJsonZipped() throws URISyntaxException {
		DAQ s = getSnapshot("1496217954303.json.gz");
		doBasicAssertionsOnSnapshot(s);
	}

	@Test
	public void testSmileZipped() throws URISyntaxException {
		DAQ s = getSnapshot("1496217954303.smile.gz");
		doBasicAssertionsOnSnapshot(s);
	}

	private void doBasicAssertionsOnSnapshot(DAQ s) {
		Assert.assertNotNull(s);
		Assert.assertEquals("Running", s.getDaqState());
		Assert.assertNotNull(s.getBuSummary());
		Assert.assertEquals(774, s.getBuSummary().getRate());
	}

	/*
	 * Method to produce snapshots in differend formats based on flashlists stored, produced by 1.12.0
	 */
	public void produceSnapshot() throws IOException, DBConnectorException, HardwareConfigurationException,
			PathNotFoundException, InvalidNodeTypeException {

		hardwareConnector = new HardwareConnector();
		Application.initialize("DAQAggregator.properties");
		testFlashlists("1.12.0/flashlists", 1496217954303L);
	}

	public void testFlashlists(String versionDir, long timestamp) throws IOException, DBConnectorException,
			HardwareConfigurationException, PathNotFoundException, InvalidNodeTypeException {
		// 1 load flashlists
		loadFlashlists("src/test/resources/compatibility/" + versionDir + "/");

		// 2 generate snapshot
		Triple<DAQ, Collection<Flashlist>, Boolean> snapshot1 = monitorManager.getSystemSnapshot();
		Assert.assertTrue(snapshot1.getRight());

		// 3 test snapshot
		Assert.assertNotNull(snapshot1.getLeft());
		Assert.assertEquals(timestamp, snapshot1.getLeft().getLastUpdate());

		// 4 persist in different formats

		/*
		 * 1496217954303.json 1.2M
		 * 
		 * 1496217954303.json.gz 74K
		 * 
		 * 1496217954303.smile 404K
		 * 
		 * 1496217954303.smile.gz 61K
		 */
		PersistorManager pm = new PersistorManager("per/", null, PersistenceFormat.JSON, null);
		pm.persistSnapshot(snapshot1.getLeft());
		PersistorManager pm2 = new PersistorManager("per/", null, PersistenceFormat.SMILE, null);
		pm2.persistSnapshot(snapshot1.getLeft());
		PersistorManager pm3 = new PersistorManager("per/", null, PersistenceFormat.ZIPPED, null);
		pm3.persistSnapshot(snapshot1.getLeft());
		PersistorManager pm4 = new PersistorManager("per/", null, PersistenceFormat.SMILE_ZIPPED, null);
		pm4.persistSnapshot(snapshot1.getLeft());

	}

	private void loadFlashlists(String flashlistDir)
			throws IOException, DBConnectorException, HardwareConfigurationException {
		FileFlashlistRetriever flashlistRetriever = new FileFlashlistRetriever(flashlistDir, PersistenceFormat.JSON);
		long startLimit = DatatypeConverter.parseDateTime("2016-09-01T15:00:00Z").getTimeInMillis();
		flashlistRetriever.prepare(startLimit);
		SessionRetriever sessionRetriever = new SessionRetriever("toppro", "toppro");

		hardwareConnector.initialize(Application.get().getProp());
		monitorManager = new MonitorManager(flashlistRetriever, sessionRetriever, hardwareConnector,null);
	}

}
