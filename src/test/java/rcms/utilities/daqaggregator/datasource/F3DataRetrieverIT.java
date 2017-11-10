package rcms.utilities.daqaggregator.datasource;

import java.io.IOException;
import org.apache.log4j.Logger;
import static org.junit.Assert.assertNotNull;
import org.junit.BeforeClass;
import org.junit.Test;
import rcms.utilities.daqaggregator.Application;
import rcms.utilities.daqaggregator.Settings;
import rcms.utilities.daqaggregator.data.BUSummary;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.HltInfo;

/**
 * Integration test for F3 retrieval: contacts the actual F3 server
 * and checks that the values can be retrieved
 *
 */
public class F3DataRetrieverIT {
	private static final Logger logger = Logger.getLogger(F3DataRetriever.class);
	
	private static F3DataRetriever f3dataRetriever;
	private static DAQ daq;

	/** retrieve the information only once and check it in the individual test cases */
	@BeforeClass
	public static void makeRetriever() {

		Application.initialize("DAQAggregator.properties");
		
		String hltUrl = Application.get().getProp(Settings.F3_HLT_URL);
		String diskUrl = Application.get().getProp(Settings.F3_DISK_URL);
		String crashesUrl = Application.get().getProp(Settings.F3_CRASHES_URL);
		f3dataRetriever = new F3DataRetriever(new Connector(false), hltUrl, diskUrl,crashesUrl);

		daq = new DAQ();
		
		// TODO: use MappingManager.map() instead
		daq.setBuSummary(new BUSummary());
		daq.setHltInfo(new HltInfo());
		
		f3dataRetriever.dispatch(daq);
	}
	
	/** checks that the F3DataRetriever could retrieve non-null values */
	@Test
	public void diskInfoDirectTest() throws IOException {

		// retrieve data from the F3mon serverse
		F3DataRetriever.DiskInfo di = f3dataRetriever.getDiskInfo();
		
		logger.info("disk output occupancy fraction is "  + di.getOutputOccupancyFraction());
		logger.info("disk total output is "               + di.getOutputTotal());
		logger.info("ramdisk occupancy fraction is "      + di.getRamdiskOccupancyFraction());
		logger.info("ramdisk total is "                   + di.getRamdiskTotal());
		
		assertNotNull(di.getOutputOccupancyFraction());
		assertNotNull(di.getOutputTotal());
		assertNotNull(di.getRamdiskOccupancyFraction());
		assertNotNull(di.getRamdiskTotal());

	}
	
	/** checks that some values were dispatched to the DAQ object. Note that
	    it looks like not all quantities in F3DataRetriever.DiskInfo are
			put into the DAQ object */
	@Test
	public void diskInfoDispatchTest()  {

		BUSummary buSummary = daq.getBuSummary();
		assertNotNull(buSummary);
		
		assertNotNull(buSummary.getOutputDiskTotal());
		assertNotNull(buSummary.getOutputDiskUsage());
		
		// F3DataRetriever.DiskInfo.ramdiskOccupancyFraction and
		// F3DataRetriever.DiskInfo.ramdiskTotal are not mapped into the DAQ
		// object
		
	}
	
}
