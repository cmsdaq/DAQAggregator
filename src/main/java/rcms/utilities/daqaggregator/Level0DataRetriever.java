package rcms.utilities.daqaggregator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class Level0DataRetriever  {
	
	private static final Logger logger = Logger.getLogger(Level0DataRetriever.class);

	protected String _delimiter = "\177";
	private String _lasBaseURLge = null;
	private String _fmURLFilter1 = null;
	private String _fmURLFilter2 = null;
	private String _dpsetPath = null;
	private Integer _sid = null;

	
	public Level0DataRetriever(String lasBaseURLge, String fmURLFilter1, String fmURLFilter2) {
		_lasBaseURLge = lasBaseURLge;
		_fmURLFilter1 = fmURLFilter1;
		_fmURLFilter2 = fmURLFilter2;
	}


	public String getDPsetPath() throws IOException {
		if ( _dpsetPath == null )
			retrieveMonitorData();
		return _dpsetPath;
	}


	public Integer getSID() throws IOException {
		if ( _sid == null )
			retrieveMonitorData();
		return _sid;
	}

	/*
	 * * prepare to download the flashlist levelZeroFM_static
	 */
	protected void retrieveMonitorData() throws IOException {

		String lasTrailer = "&fmt=plain&delimiter=" + _delimiter ;

		{
			String lasUrl = _lasBaseURLge + "levelZeroFM_static" + lasTrailer;
			Map<String, String> l0data = new HashMap<String, String>();
			logger.debug("LASURL = " + lasUrl);
			String fields[] = { "FMURL", "HWCFG_KEY", "SID", "timestamp" };
			readInfospaceByFilter(lasUrl, _fmURLFilter1, _fmURLFilter2, l0data, fields);
			if ( ! l0data.isEmpty() ) {
				_dpsetPath = l0data.get("HWCFG_KEY").split(":")[0];
				_sid = Integer.parseInt(l0data.get("SID"));			
			}
		}
	}

	
	/*
	 * * download the flashlist
	 * * put the data to hashmap ("FMURL", "HWCFG_KEY", "SID", "timestamp" )
	 * * get row only if FMURL contains filter1 && filter2
	 * * if timestamp > last timestamp
	 */
	protected void readInfospaceByFilter(
			String isbUrl,
			String fmURLFilter1,
			String fmURLFilter2,
			Map<String, String> destMap,
			String fields[]) throws IOException {

		// Create an URL instance
		URL url;
		url = new URL( isbUrl );

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

			// Repeat until end of file
			Map<String, Integer> fieldMap = new HashMap<String, Integer>();
			String fieldNames[] = null;
			for (boolean firstLine = true;;firstLine = false)
			{
				String line = reader.readLine();
				if (line == null)
					break;


				if (firstLine) {
					fieldNames = line.split(_delimiter);
					int idx = 0;
					for (String name : fieldNames)
						fieldMap.put(name, idx++);
				}
				else {
					String values[] = line.split(_delimiter);
					for (int i=0;i<values.length;++i) {
						if (values[i].startsWith("\"") && values[i].endsWith("\""))
							values[i] = values[i].substring(1, values[i].length()-1);
					}


					String fmURL = values[fieldMap.get("FMURL")];

					logger.debug("  checking fm url " + fmURL + "  with filters '"+fmURLFilter1+"' and '"+fmURLFilter2+"'");

					if (fmURL.contains(fmURLFilter1) && fmURL.contains(fmURLFilter2)) {

						logger.debug("    URL contains filters '" + fmURLFilter1 + "' and '" + fmURLFilter2 + "'.");


						String timestamp = values[fieldMap.get("timestamp")];
						if (  (! destMap.containsKey("timestamp")) ||
								( destMap.containsKey("timestamp") && destMap.get("timestamp").compareTo(timestamp) < 0 ) ) {


							for (String field : fields) {

								String fieldName = field;
								String fieldKey = field;
								if (field.contains(":")) {
									fieldName = field.split(":")[0];
									fieldKey = field.split(":")[1];
								}


								Integer idx = fieldMap.get( fieldName );
								if (idx == null) {
									reader.close();
									throw new IOException("field " + fieldName + " not found");
								}
								String value = values[ idx ];
								destMap.put(fieldKey, value);
							}
						}
					}
				}
			}
		}
		finally {
			if (in != null)
				in.close();
			if (conn != null)
				conn.disconnect();
		}
	}
}
