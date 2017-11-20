package rcms.utilities.daqaggregator.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import org.apache.commons.io.IOUtils;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.StorageManager;

/**
 *
 * @author holzner
 */
public class IcingaDataRetrieverTest {
	
	/** reads a potentially zipped file */
	private String readStream(String fname) throws IOException {

		InputStream is = IcingaDataRetriever.class.getResourceAsStream(fname);

		if (fname.endsWith(".gz")) {
			is = new GZIPInputStream(is);
		}

		return IOUtils.toString(is, StandardCharsets.UTF_8);

	}
	
	@Test
	public void testGetStorageManagerInfo() throws IOException {

		String response = readStream("sm-occupancy-01.json.gz");
		
		IcingaDataRetriever retriever = new IcingaDataRetriever(new F3DataRetrieverTest.ConnectorFake(response), null);
		
		DAQ daq = new DAQ();
		
		// this would normally be added by the ObjectMapper
		daq.setStorageManager(new StorageManager());
		
		retriever.dispatchSmOccupancy(daq);
		
		assertEquals(0.1892, daq.getStorageManager().getOccupancyFraction(), 0.0001f);
		assertEquals(1510251693558L, (long)daq.getStorageManager().getOccupancyLastUpdate());
	}
	
}
