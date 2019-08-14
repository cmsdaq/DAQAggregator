package rcms.utilities.daqaggregator.datasource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import rcms.common.db.DBConnectorException;
import rcms.utilities.hwcfg.HardwareConfigurationException;
import rcms.utilities.hwcfg.InvalidNodeTypeException;
import rcms.utilities.hwcfg.PathNotFoundException;
import rcms.utilities.hwcfg.dp.DAQPartition;

public class HwdbToSqliteTest {

	/**
	 * read a configuration from the hardware database, convert it to sqlite and
	 * read it back
	 */
	@Test
	public void test01() throws FileNotFoundException, IOException, DBConnectorException,
					HardwareConfigurationException, PathNotFoundException, InvalidNodeTypeException, SQLException, IllegalArgumentException, IllegalAccessException {

		final String path = "/daq2/eq_170517_01_ECAL622Optical/fb_all/dp_bl456_75BU";

		//-----
		// get the DAQPartition object and write it out to sqlite
		//-----
		// TODO: we could use Sqlite in memory instead
		File sqliteFile = File.createTempFile("sqlite-test", ".sqlite");
		sqliteFile.deleteOnExit();

		HwdbToSqlite converter = new HwdbToSqlite();
		converter.run(path, sqliteFile);

		// get the original object
		DAQPartition daqPartition = converter.getDaqPartitions().get(0);

		//-----
		// read the DAQPartition object back from the sqlite database
		//-----
		Properties prop = new Properties();
		prop.load(new FileInputStream("DAQAggregator.properties"));

		prop.setProperty("hwcfgdb.type", "SQLITE");
		prop.setProperty("hwcfgdb.dburl", "jdbc:sqlite:" + sqliteFile.getAbsolutePath());

		HardwareConnector hardwareConnector = new HardwareConnector();
		hardwareConnector.initialize(prop);

		DAQPartition daqPartition2 = hardwareConnector.getPartition(path);

		// DAQPartition does not implement an .equals() method
		// so any test for equality will default to pointer comparison
		// which will result in 'not equal' when comparing daqPartition and daqPartition2
		assertEquals(daqPartition.getId(), daqPartition2.getId());

		// at least check some numbers of items are equal between the
		// original object and the one read back from the sqlite database
		// number of FEDs
		assertEquals(daqPartition.getDAQPartitionSet().getEquipmentSet().getFEDs().size(),
						daqPartition2.getDAQPartitionSet().getEquipmentSet().getFEDs().size());

		// number of FMMs
		assertEquals(daqPartition.getDAQPartitionSet().getEquipmentSet().getFMMs().size(),
						daqPartition2.getDAQPartitionSet().getEquipmentSet().getFMMs().size());

	}
}
