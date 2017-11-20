package rcms.utilities.daqaggregator.datasource;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Ignore;
import org.junit.Test;
import rcms.utilities.daqaggregator.Application;
import rcms.utilities.daqaggregator.Settings;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.StorageManager;

/**
 * integration test for retrieving data from Icinga
 * @author holzner
 */
public class IcingaDataRetrieverIT {
	
	private static final Logger logger = Logger.getLogger(IcingaDataRetrieverIT.class);
	
	/** checks that we can open a connection to the Icinga 
	 *  server (which is normally via HTTPS)
	 */
	@Ignore
	@Test
	public void testIcingaConnection() throws IOException {

		System.getProperties().setProperty("javax.net.debug", "all");

		Application.initialize("DAQAggregator.properties");
		String url = Application.get().getProp(Settings.ICINGA_SM_OCCUPANCY_URL);

		Connector connector = new Connector(true);
		Pair<Integer, List<String>> result = connector.retrieveLines(url);

		Integer httpStatus = result.getLeft();

		// in case of an SSL exception we get httpStatus = -1
		// (but we could also get this status if another exception is thrown)
		assertNotEquals(-1, (int) httpStatus);
	}
	
	/** contacts Icinga and retrieves the data. Does not insist
	 *  on any particular value but checks that there is no 
	 *  exception throw and that the values are non-null
	 */
	@Ignore
	@Test
	public void testGetStorageManagerInfo() throws IOException {

		Application.initialize("DAQAggregator.properties");

		String icingaUser = Application.get().getProp(Settings.ICINGA_USER);
		String icingaPassword = Application.get().getProp(Settings.ICINGA_PASSWORD);

		Connector connector = new Connector(false, icingaUser, icingaPassword);

		String url = Application.get().getProp(Settings.ICINGA_SM_OCCUPANCY_URL);

		IcingaDataRetriever retriever = new IcingaDataRetriever(connector,
						url);

		DAQ daq = new DAQ();
		// TODO: use MappingManager.map() instead
		daq.setStorageManager(new StorageManager());

		retriever.dispatchSmOccupancy(daq);

		Float occupancyFraction = daq.getStorageManager().getOccupancyFraction();
		Long occupancyLastUpdate = daq.getStorageManager().getOccupancyLastUpdate();
		
		Date lastUpdateDate = null;
		if (occupancyLastUpdate != null) {
			lastUpdateDate = new Date(occupancyLastUpdate);
		}
						
		logger.info("Lustre occupancy fraction " + occupancyFraction);
		logger.info("Lustre occupancy last update " + lastUpdateDate);
		
		assertNotNull(occupancyFraction);
		assertNotNull(occupancyLastUpdate);
	}
	
}
