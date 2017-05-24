package rcms.utilities.daqaggregator.datasource;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.BeforeClass;
import rcms.common.db.DBConnectorException;
import rcms.utilities.daqaggregator.Settings;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.mappers.MappingManager;
import rcms.utilities.daqaggregator.persistence.PersistenceFormat;
import rcms.utilities.hwcfg.HardwareConfigurationException;
import rcms.utilities.hwcfg.InvalidNodeTypeException;
import rcms.utilities.hwcfg.PathNotFoundException;
import rcms.utilities.hwcfg.dp.DAQPartition;

/**
 * Tests for class FlaslistDispatcher
 * @author holzner
 */
public class FlashlistDispatcherTest {

	/** to connect to the hardware database */
	private static HardwareConnector hardwareConnector;
	private static Properties prop;

	@BeforeClass
	public static void initialize() throws HardwareConfigurationException, 
					DBConnectorException,
					IOException {
		
		hardwareConnector = new HardwareConnector();

		// we avoid using Application.initialize(..) here
		// because it needs a connection to a LAS and thus makes
		// this test dependent on external services

		// read application configuration
		prop = new Properties();
		prop.load(new FileInputStream("DAQAggregator.properties"));
		
		String url    = prop.getProperty(Settings.HWCFGDB_DBURL.getKey());
		String host   = prop.getProperty(Settings.HWCFGDB_HOST.getKey());
		String port   = prop.getProperty(Settings.HWCFGDB_PORT.getKey());
		String sid    = prop.getProperty(Settings.HWCFGDB_SID.getKey());
		String user   = prop.getProperty(Settings.HWCFGDB_LOGIN.getKey());
		String passwd = prop.getProperty(Settings.HWCFGDB_PWD.getKey());
		
		// connect to the hardware database
		hardwareConnector.initialize(url, host, port, sid, user, passwd);
	}
	
	/** helper function to get those subsystems which are not in 'null' state */
	private static Set<String> getNonNullSubsystems(Map<String, SubSystem> subsystems) {
		
		Set<String> retval = new HashSet<String>();
		
		for (Map.Entry<String, SubSystem> entry : subsystems.entrySet()) {
			if (entry.getValue().getStatus() != null) {
				retval.add(entry.getKey());
			}
		}
		
		return retval;
	}
	
	/** retrieves the flashlists (from files), calls FlashlistDispatcher.dispatch(..)
	 *  (the method to be tested) for the given filter on the function 
	 *  manager URL and returns the subsystems found for this FM URL.
	 */
	private Set<String> getSubsystems(String flashlistDir, String fmurlFilter, 
					long timestamp, String dpsetPath) 
					throws HardwareConfigurationException, 
					PathNotFoundException,
					InvalidNodeTypeException,
					IOException {

		FileFlashlistRetriever flashlistRetriever = new FileFlashlistRetriever(flashlistDir, PersistenceFormat.JSON);
		
		// note that prepare reads snapshots strictly after the given timestamp 
		// so we subtract one
		flashlistRetriever.prepare(timestamp - 1);
	
		// get the Level0 subsystem flashlist
		Flashlist flashlist = flashlistRetriever.retrieveFlashlist(FlashlistType.LEVEL_ZERO_FM_SUBSYS).getKey();

		DAQPartition daqPartition = hardwareConnector.getPartition(dpsetPath);

		// TODO: we could mock this object
		TCDSFMInfoRetriever tcdsFmInfoRetriever = new TCDSFMInfoRetriever(flashlistRetriever);

		MappingManager mappingManager = new MappingManager(daqPartition, tcdsFmInfoRetriever);
		mappingManager.map();
		
		FlashlistDispatcher instance = new FlashlistDispatcher(fmurlFilter);
		
		// the actual method to be tested
		instance.dispatch(flashlist, mappingManager);
	
		// note that the list of subsystems comes from the hardware database, it says
		// nothing about whether a subsystem is in or not so we filter by requring
		// the status not to be null.
		return getNonNullSubsystems(mappingManager.getObjectMapper().subsystemByName);
	
	}
	
	/** makes a detailed comparison between the expected and actually retrieved
	 *  subsystems
	 * @param sid the session id under test (for messaging purposes)
	 */
	private void testSubsystems(int sid, String[] expectedSubsystems, Set<String> actualSubsystems) {

		Set<String> expectedSubsystemsSet = new HashSet<String>(Arrays.asList(expectedSubsystems));

		for (String expected : expectedSubsystemsSet) {
			if (! actualSubsystems.contains(expected)) {
				fail("expected subsystem " + expected + " not found for SID=" + sid);
			}
		}

		for (String actual : actualSubsystems) {
			if (! expectedSubsystemsSet.contains(actual)) {
				fail("actual subsystem " + actual + " not expected for SID=" + sid);
			}
		}
	}
	
	/** test with a case which has multiple session IDs (SIDs) in
	 *  the level0 subsys mailing list
	 */
	@Test
	public void testMultipleSIDs() throws IOException, HardwareConfigurationException, PathNotFoundException, InvalidNodeTypeException {
		System.out.println("multipleSIDs");
		
		// borrowed from rcms.utilities.daqaggregator.datasource.Compatibility
		// test
		final String versionDir = "1.5.0/flashlists";
		final String flashlistDir = "src/test/resources/compatibility/" + versionDir + "/";
		final long timestamp = 1477557890932L;
		
		{
			//----------
			// SID 286004 toppro
			//----------
			String dpsetPath = "/daq2/eq_160913_01/fb_all_with1240_withCASTOR_w582_583/dp_bl381_75BU";

			Set<String> subsystems = this.getSubsystems(flashlistDir, "toppro", timestamp, dpsetPath);

			// DAQ, DCS and DQM seem not to be detected as subsystems with non-null
			// state for some reason
			testSubsystems(286004, new String[] {
			  "CASTOR", "CSC", "CTPPS_TOT", /* "DAQ", "DCS", "DQM", */ "DT", "ECAL", "ES",
        "HCAL", "HF", "PIXEL", "PIXEL_UP", "RPC", "SCAL", "TCDS", "TRACKER", "TRG"
			}, subsystems);
		}					
		
		{					
			//----------
			// SID 284766 topdev
			//----------
	
			// the following only exist in toppro: CASTOR, CSC, CTPPS_TOT, DT, RPC, SCAL
		
		  String dpsetPath = "/daq2/eq_150929/fb_all_withuTCA/dp_bl116_64BU";
							
			Set<String> subsystems = this.getSubsystems(flashlistDir, "topdev", timestamp, dpsetPath);

			System.out.println("subsystems=" + subsystems);
			
			testSubsystems(284766, new String[] {
			  /* "DAQ", "DCS", "DQM", */ "ECAL", "ES", "HCAL", "HF", "PIXEL", "PIXEL_UP",
				"TCDS", "TRACKER", "TRG"
			}, subsystems);
			
		}				
	}
}
