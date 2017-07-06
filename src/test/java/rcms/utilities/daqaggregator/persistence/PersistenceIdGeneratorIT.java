package rcms.utilities.daqaggregator.persistence;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.Assert;
import org.junit.Test;

import rcms.common.db.DBConnectorException;
import rcms.utilities.daqaggregator.Application;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.data.SubFEDBuilder;
import rcms.utilities.daqaggregator.datasource.FileFlashlistRetriever;
import rcms.utilities.daqaggregator.datasource.Flashlist;
import rcms.utilities.daqaggregator.datasource.HardwareConnector;
import rcms.utilities.daqaggregator.datasource.MonitorManager;
import rcms.utilities.daqaggregator.datasource.SessionRetriever;
import rcms.utilities.hwcfg.HardwareConfigurationException;
import rcms.utilities.hwcfg.InvalidNodeTypeException;
import rcms.utilities.hwcfg.PathNotFoundException;

public class PersistenceIdGeneratorIT {

	/**
	 * This snapshot is broken. There is 1 SFB missing. That SFB doesn't have and FRL ant that was the problem
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void testJson() throws URISyntaxException {
		DAQ s = getSnapshot("src/test/resources/id-generator/case/problematic-snapshot/", "1499335390070.json");
		Assert.assertNull("Expect problem with deserialization of this snapshot, one referred SFB is missing ", s);
	}

	@Test
	public void getFlashlsits() throws IOException, DBConnectorException, HardwareConfigurationException,
			PathNotFoundException, InvalidNodeTypeException, URISyntaxException {
		hardwareConnector = new HardwareConnector();
		Application.initialize("DAQAggregator.properties");
		// 1 load flashlists
		loadFlashlists("src/test/resources/id-generator/case/flashlists/");

		// 2 generate snapshot
		Triple<DAQ, Collection<Flashlist>, Boolean> snapshot1 = monitorManager.getSystemSnapshot();
		Assert.assertTrue(snapshot1.getRight());

		// 3 test snapshot
		Assert.assertNotNull(snapshot1.getLeft());

		for (SubFEDBuilder sfb : snapshot1.getLeft().getSubFEDBuilders()) {
			System.out.println("SFB_" + sfb.getFedBuilder().getName() + "$" + sfb.getTtcPartition().getName() + "$"
					+ ((sfb.getFrlPc() != null) ? sfb.getFrlPc().getHostname() : "-"));
		}

		Assert.assertEquals(78, snapshot1.getLeft().getSubFEDBuilders().size());

		Set<SubFEDBuilder> refferedSFB = new HashSet<>();
		for (FEDBuilder fb : snapshot1.getLeft().getFedBuilders()) {
			for (SubFEDBuilder sfb : fb.getSubFedbuilders()) {
				refferedSFB.add(sfb);
			}
		}

		for (SubFEDBuilder sfb : refferedSFB) {
			System.out.println("SFB_" + sfb.getFedBuilder().getName() + "$" + sfb.getTtcPartition().getName() + "$"
					+ ((sfb.getFrlPc() != null) ? sfb.getFrlPc().getHostname() : "-"));
		}
		Assert.assertEquals(78, refferedSFB.size());

		DAQ sref = getSnapshot("src/test/resources/id-generator/case/fixed-snapshot/", "test-output.json");
		doBasicAssertionsOnSnapshot(sref);
		
		PersistorManager pm = new PersistorManager("src/test/resources/id-generator/case/fixed-snapshot/", null,
				PersistenceFormat.JSON, null);
		pm.persistSnapshot(snapshot1.getLeft());
		
		DAQ s = getSnapshot("src/test/resources/id-generator/case/fixed-snapshot/2017/7/6/10/", "1499335390070.json");
		doBasicAssertionsOnSnapshot(s);
		File f = new File("src/test/resources/id-generator/case/fixed-snapshot/2017/7/6/10/1499335390070.json");
		f.delete();
	}

	private static HardwareConnector hardwareConnector;
	private static MonitorManager monitorManager;

	private void doBasicAssertionsOnSnapshot(DAQ s) {
		Assert.assertNotNull(s);
		Assert.assertEquals("Initialized", s.getDaqState());
		Assert.assertNotNull(s.getSubFEDBuilders());
		Assert.assertEquals(78, s.getSubFEDBuilders().size());
	}

	/**
	 * method to load a deserialize a snapshot given a file name
	 */
	public static DAQ getSnapshot(String dir, String fname) throws URISyntaxException {
		StructureSerializer serializer = new StructureSerializer();
		File file = new File(dir + fname);
		return serializer.deserialize(file.getAbsolutePath());
	}

	private void loadFlashlists(String flashlistDir)
			throws IOException, DBConnectorException, HardwareConfigurationException {
		FileFlashlistRetriever flashlistRetriever = new FileFlashlistRetriever(flashlistDir, PersistenceFormat.JSON);
		long startLimit = DatatypeConverter.parseDateTime("2016-09-01T15:00:00Z").getTimeInMillis();
		flashlistRetriever.prepare(startLimit);
		SessionRetriever sessionRetriever = new SessionRetriever("cmsrc-daqdev", "31000");

		hardwareConnector.initialize(Application.get().getProp());
		monitorManager = new MonitorManager(flashlistRetriever, sessionRetriever, hardwareConnector);
	}
}
