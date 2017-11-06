package rcms.utilities.daqaggregator.datasource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import rcms.utilities.daqaggregator.Application;
import rcms.utilities.daqaggregator.ProxyManager;
import rcms.utilities.daqaggregator.datasource.F3DataRetriever.DiskInfo;

/**
 * Tests F3 retrieval. Fake API responses used - quicker junit tests.
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class F3DataRetrieverTest {
	private static final Logger logger = Logger.getLogger(F3DataRetriever.class);


	@Test
	public void diskInfoTest() throws IOException {
		logger.info("Test connection");
		String fakeResponse = "{\"ramdisk_occ\":0,\"output_occ\":0.012734683851866,\"ramdisk_tot\":16370688,\"output_tot\":278240437}";
		F3DataRetriever f3dataRetriever = new F3DataRetriever(new ConnectorFake(fakeResponse),null,null,null);
		DiskInfo di = f3dataRetriever.getDiskInfo();
		Assert.assertEquals(new Double(0.012734683851866), di.getOutputOccupancyFraction());
		Assert.assertEquals(new Integer(278240437), di.getOutputTotal());
		Assert.assertEquals(new Double(0), di.getRamdiskOccupancyFraction());
		Assert.assertEquals(new Integer(16370688), di.getRamdiskTotal());
	}

	@Test
	public void testHlt() throws IOException {
		String fakeResponse = "{\"219\":{\"ALCALUMIPIXELS\":47.190047190047,\"ALCAPHISYM\":0,\"Calibration\":79.365079365079,\"DQM\":6.8640068640069,\"DQMCalibration\":7.9365079365079,\"DQMEventDisplay\":18.618618618619,\"DQMHistograms\":612.44101244101,\"EcalCalibration\":79.365079365079,\"Error\":0,\"ExpressCosmics\":19.004719004719,\"HLTRates\":612.44101244101,\"L1Rates\":612.44101244101,\"NanoDST\":5.7915057915058,\"Physics\":71.600171600172,\"RPCMON\":27.627627627628}}";
		F3DataRetriever f3dataRetriever = new F3DataRetriever(new ConnectorFake(fakeResponse),"","","", null);
		Assert.assertEquals(new Double(71.600171600172d), f3dataRetriever.getHLToutputInfo(0).getEventRate(F3DataRetriever.PHYSICS_STREAM_NAME));
	}

	@Test
	public void testHltOutputBandwidth() throws IOException {
		String fakeResponse = "{\"58\":{\"ALCALUMIPIXELS\":127814.84341484,\"ALCAPHISYM\":0,\"Calibration\":2844043.7580438,\"DQM\":64910.424710425,\"DQMCalibration\":290670.87087087,\"DQMEventDisplay\":0,\"DQMHistograms\":0,\"EcalCalibration\":118622.05062205,\"Error\":0,\"ExpressCosmics\":181467.43886744,\"HLTRates\":41108.193908194,\"L1Rates\":254211.66881167,\"NanoDST\":4938.4384384384,\"Physics\":439103.56070356,\"RPCMON\":0}}";
		F3DataRetriever f3dataRetriever = new F3DataRetriever(new ConnectorFake(fakeResponse),null,null,null);
		Assert.assertEquals(new Double(439103.56070356d), f3dataRetriever.getHLToutputInfo(0).getBandwidth(F3DataRetriever.PHYSICS_STREAM_NAME));
	}

	@Test
	public void testHltCrashes() throws IOException {
		String fakeResponse = "{\"last_run\":306155,\"num_BUs_with_last_run\":73,\"quarantinedRes\":0,\"crashedRes\":0,\"crashedOrQuarantinedRes\":0,\"crashes\":123,\"activeRes\":50728,\"activeResOldRuns\":0}";
		F3DataRetriever f3dataRetriever = new F3DataRetriever(new ConnectorFake(fakeResponse),null,null,null);
		Assert.assertEquals(new Integer(123), f3dataRetriever.getCrashes());
	}


	public class ConnectorFake extends Connector {

		private final String response;

		public ConnectorFake(String response) {
			super(false);
			this.response = response;
		}

		@Override
		public Pair<Integer, List<String>> retrieveLines(String urlString) throws IOException {
			List<String> rows = new ArrayList<String>();
			rows.add(response);
			return Pair.of(200, rows);
		}

	}
}
