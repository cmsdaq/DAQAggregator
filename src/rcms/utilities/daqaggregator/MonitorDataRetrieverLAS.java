package rcms.utilities.daqaggregator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonitorDataRetrieverLAS extends MonitorDataRetriever {

	List<String> _lasURLs;
	Map<String, String> _flashlistLASMap = new HashMap<String, String>();
	String _lasBaseURLfe = null;
	String _lasBaseURLru = null;
	int _sessionId = 0;
	int _retry_count = 0;

	public MonitorDataRetrieverLAS(List<String> lasURLs, int sessionId) {
		_lasURLs = lasURLs;
		_sessionId = sessionId;
	}

	private void readCatalogs() throws IOException {
		_flashlistLASMap.clear();
		for (String lasURL : _lasURLs) {
			// Create an URL instance
			URL url;
			String php = "";
			if (lasURL.contains("escaped"))
				php = ".php";
			url = new URL( lasURL + "/retrieveCatalog"+php+"?fmt=plain");

			// Get an input stream for reading
			HttpURLConnection conn = null;
			InputStream in = null;
			InputStreamReader isr = null;
			BufferedReader reader = null;
			try {
				conn = (HttpURLConnection) url.openConnection();
				in = conn.getInputStream();
				isr = new InputStreamReader(in);
				reader = new BufferedReader(isr);

				for (boolean firstLine = true;;firstLine = false)
				{
					String line = reader.readLine();
					if (line == null)
						break;

					if (firstLine)
						continue;

					if (line.startsWith("urn:")) {
						_flashlistLASMap.put(line, lasURL);
						System.out.println("'" + line + "' : " + lasURL);
					}
				}
			}
			catch (IOException e) {
				System.out.println("\n\nError retrieving ctatalog from URL=" +url);
				e.printStackTrace();
				System.out.println("\n\nContinuing catalog retrieve...");
			}
			finally {
				if (reader != null)
					reader.close();
				if (isr != null)
					isr.close();
				if (in != null)
					in.close();
				if (conn != null)
					conn.disconnect();
			}

		}

	}

	private String makeLASURL(String flashListName, boolean addTrailer) throws IOException {

		String lasTrailer = "&fmt=plain&sessionid=" + _sessionId + "&delimiter=|";
//		String lasTrailer = "&fmt=plain&delimiter=|";

		String fullFlashName = "urn:xdaq-flashlist:" + flashListName;

		if ( !_flashlistLASMap.containsKey(fullFlashName) && _retry_count%60==0 ) {
			readCatalogs();
		}
		if (! _flashlistLASMap.containsKey(fullFlashName)) {
			_retry_count++;
			throw new IOException("Flash list '" + flashListName + "' not found in catalogs.");
		}

		String php = "";
		if (_flashlistLASMap.get(fullFlashName).contains("escaped"))
			php = ".php";
		String lasUrl = _flashlistLASMap.get(fullFlashName) + "/retrieveCollection"+php+"?flash=" +
		fullFlashName;
		if (addTrailer)
			lasUrl += lasTrailer;

		return lasUrl;
	}


	public void retrieveMonitorData() throws IOException {

		long t0 = System.currentTimeMillis();


		if (_flashlistLASMap.isEmpty())
			readCatalogs();

		_delimiter = "\\|";

		String ruFields[] = {
				"context",
				"timestamp",
				"stateName",
				"runNumber",
				"eventCount",
				"eventsInRU",
				"activeRequests",
				"eventRate",
				"superFragmentSize",
				"superFragmentSizeStdDev",
				"fedIdsWithErrors",
				"fedDataCorruption",
				"fedOutOfSync",
				"fedCRCerrors",
				"fedBXerrors",
				"fedIdsWithoutFragments",
				"incompleteSuperFragmentCount"};

		try {
			String lasUrl = makeLASURL("RU", true);
			System.out.println("lasurl=" + lasUrl);
			readInfospaceByHM(lasUrl, _ruDataByHM, ruFields);
		} catch (Exception e) {
			System.err.println(e.toString()+" Continuing...");
		}

		try {
			String lasUrl = makeLASURL("EVM", true);
			System.out.println("lasurl=" + lasUrl);
			readInfospaceByHM(lasUrl, _evmDataByHM, ruFields);
		} catch (Exception e) {
			System.err.println(e.toString()+" Continuing...");
		}

		try {
			String lasUrl = makeLASURL("BU", true);
			System.out.println("lasurl=" + lasUrl);
			String fields[] = {
					"context",
					"timestamp",
					"stateName",
					"eventRate",
					"bandwidth",
					"eventSize",
					"eventSizeStdDev",
					"nbEventsBuilt",
					"nbEventsInBU",
					"nbCorruptedEvents",
					"nbEventsMissingData",
					"nbEventsWithCRCerrors",
					"outstandingRequests",
					"priority",
					"fuSlotsHLT",
					"fuSlotsCloud",
					"fuSlotsStale",
					"fuSlotsQuarantined",
					"queuedLumiSections",
					"queuedLumiSectionsOnFUs",
					"nbBlockedResources",
					"nbTotalResources",
					"ramDiskUsed",
					"ramDiskSizeInGB",
					"nbFilesWritten",
					"nbLumiSections",
					"currentLumiSection"
			};

			readInfospaceByHG(lasUrl, _buDataByHI, "instance", fields);
		} catch (Exception e) {
			System.err.println(e.toString()+" Continuing...");
		}

		try {
			String lasUrl = makeLASURL("FMMStatus", true);
			String fields[] = { "context", "geoslot", "stateName", "outputStateA", "outputStateB", "outputFractionWarningA", "outputFractionWarningB", "outputFractionBusyA", "outputFractionBusyB", "timestamp" };
			readInfospaceByHG(lasUrl, _fmmStatusDataByHG, "geoslot", fields);
		} catch (Exception e) {
			System.err.println(e.toString()+" Continuing...");
		}

		try {
			String lasUrl = makeLASURL("FMMInput", true);
			String fields[] = { "context", "geoslot", "io", "isActive", "inputState", "fractionBusy", "fractionWarning", "timestamp"  };
			readInfospaceByHGI(lasUrl, _fmmInputDataByHGI, "geoslot", "io", fields);
		} catch (Exception e) {
			System.err.println(e.toString()+" Continuing...");
		}

		try {
			String lasUrl = makeLASURL("ferolStatus", true);
			String fields[] = {
					"context",
					"timestamp",
					"stateName"
			};
			readInfospaceByHG(lasUrl, _ferolStatusDataByHG, "slotNumber", fields);
		} catch (Exception e) {
			System.err.println(e.toString()+" Continuing...");
		}

		try {
			String lasUrl = makeLASURL("ferolInputStream", true);
			String fields[] = {
					"context",
					"timestamp",
					"expectedFedId",
					"EventCounter",
					"TriggerNumber",
					"FEDCRCError",
					"LinkCRCError",
					"BackpressureCounter",
					"FEDFrequency",
					"AccSlinkFullSecond",
					"WrongFEDIdDetected",
					"WrongFEDId",
					"SyncLostDraining"
			};
			readInfospaceByHGI(lasUrl, _ferolInputStreamDataByHGI, "slotNumber", "streamNumber", fields);
		} catch (Exception e) {
			System.err.println(e.toString()+" Continuing...");
		}

		try {
			String lasUrl = makeLASURL("jobcontrol", false);
			lasUrl += "&fmt=plain";
			System.out.println("lasurl=" + lasUrl);
			this.readJCFlashList(lasUrl);
		} catch (Exception e) {
			System.err.println(e.toString()+" Continuing...");
		}


		long t1 = System.currentTimeMillis();
		System.out.println("Time to retrieve and parse all flash lists: " + (t1-t0) + " ms.");

	}

}
