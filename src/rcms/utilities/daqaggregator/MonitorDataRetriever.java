package rcms.utilities.daqaggregator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class MonitorDataRetriever extends MonitorData {

	protected String _delimiter = ",";

	protected String getHostFromContext(String context) {
		String parts[] = context.split(":");
		return parts[1].substring(2);
	}
	protected int getPortFromContext(String context) {
		String parts[] = context.split(":");
		return Integer.parseInt(parts[2]);
	}

	private boolean addTimeStampToHisto( String histoName, String ts) {
		ts = ts.substring(0, ts.length()-4) + " GMT";
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS z");
		Date d = null;
		try {
			d = df.parse( ts );
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Date now = new Date();
		double diff = (d.getTime() - now.getTime()) / 1000.;
		//	System.out.println("txt=" + histoName + " ts=" + ts + " diff= " + diff + "s");
		return (diff>-600);
	}


	protected void readInfospaceByHM(
			String isbUrl,
			Map<String, Map <Integer, Map<String, String>>> destMap,
			String fields[]) throws IOException {
		readInfospaceByHM(isbUrl, destMap, fields, true);
	}

	protected void readInfospaceByHM(
			String isbUrl,
			Map<String, Map <Integer, Map<String, String>>> destMap,
			String fields[], boolean checkTimeStamp) throws IOException {


		long t0 = System.currentTimeMillis();
		long ttr = 0;
		// Create an URL instance
		URL url;
		url = new URL( isbUrl );

		// Get an input stream for reading
		HttpURLConnection conn = null;
		InputStream in = null;
		InputStreamReader isr = null;
		BufferedReader reader = null;
		try {
			long tt0 = System.nanoTime();
			conn = (HttpURLConnection) url.openConnection();
			in = conn.getInputStream();
			isr = new InputStreamReader(in);
			reader = new BufferedReader(isr);
			ttr += System.nanoTime() - tt0;

			// Repeat until end of file
			Map<String, Integer> fieldMap = new HashMap<String, Integer>();
			for (boolean firstLine = true;;firstLine = false)
			{
				tt0 = System.nanoTime();
				String line = reader.readLine();
				ttr += System.nanoTime() - tt0;
				if (line == null)
					break;

				if (firstLine) {
					String fieldNames[] = line.split(_delimiter);
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


					String context = values[fieldMap.get("context")];
					String hostName = getHostFromContext(context);

					if (fieldMap.containsKey("timestamp"))
						addTimeStampToHisto(isbUrl, values[fieldMap.get("timestamp")]);

					if (! destMap.containsKey( hostName )) {
						destMap.put( hostName, new HashMap<Integer, Map<String, String>>());
						destMap.get( hostName ).put(0, new HashMap<String, String>());
					}

					if (  (!checkTimeStamp) || (! fieldMap.containsKey("timestamp") ) ||
							(! destMap.get( hostName ).get(0).containsKey("timestamp")) ||
							( destMap.get( hostName ).get(0).containsKey("timestamp") && destMap.get( hostName ).get(0).get("timestamp").compareTo( values[fieldMap.get("timestamp")] ) <= 0 ) ) {



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
							destMap.get( hostName ).get(0).put(fieldKey, value);
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

		long t1 = System.currentTimeMillis();
		System.out.println("Time to retrieve & parse " + url + " : " + (t1-t0) + " ms. read: " + (ttr/1000000) + " ms.");
	}

	protected void readInfospaceByHG(
			String isbUrl,
			Map<String, Map <Integer, Map<String, String>>> destMap,
			String keySlot,
			String fields[]) throws IOException {

		readInfospaceByHG(isbUrl, destMap, keySlot, fields, true);

	}

	protected void readInfospaceByHG(
			String isbUrl,
			Map<String, Map <Integer, Map<String, String>>> destMap,
			String keySlot,
			String fields[],
			boolean checkTimeStamp) throws IOException {

		long t0 = System.currentTimeMillis();
		long ttr = 0;
		// Create an URL instance
		URL url  = new URL( isbUrl );

		// Get an input stream for reading
		HttpURLConnection conn = null;
		InputStream in = null;
		InputStreamReader isr = null;
		BufferedReader reader = null;
		try {
			long tt0 = System.nanoTime();
			conn = (HttpURLConnection) url.openConnection();
			in = conn.getInputStream();
			isr = new InputStreamReader(in);
			reader = new BufferedReader(isr);
			ttr += System.nanoTime() - tt0;
			// Repeat until end of file
			String fieldNames[] = { "" };
			Map<String, Integer> fieldMap = new HashMap<String, Integer>();
			for (boolean firstLine = true;;firstLine = false)
			{
				tt0 = System.nanoTime();
				String line = reader.readLine();
				ttr += System.nanoTime() - tt0;
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
					if (values.length != fieldNames.length) {


						// Hack fro ISB SM flashlist:
						if (isbUrl.contains("StorageManager")) {


							System.out.println("Problem in split: fields:" + fieldNames.length + " in line:" + values.length);
							System.out.println("line='" + line +"'");

							String line1 = "";

							while ((line1 = reader.readLine()) != null) {
								line += line1;
							}

							line.replaceAll("\n", "");

							System.out.println("line concat='" + line +"'");

							int i1 = line.indexOf('{');
							int i2 = line.lastIndexOf('}');
							String line2 = line.substring(0, i1) + line.substring(i2);

							System.out.println("line after='" + line2 +"'");

							values = line2.split(_delimiter);
						}


					}


					for (int i=0;i<values.length;++i) {
						if (values[i].startsWith("\"") && values[i].endsWith("\""))
							values[i] = values[i].substring(1, values[i].length()-1);
					}

					String context = values[fieldMap.get("context")];
					String hostName = getHostFromContext(context);

					if (fieldMap.containsKey("timestamp")) {
						addTimeStampToHisto(isbUrl, values[fieldMap.get("timestamp")]);
						//continue;
					}

					if (! destMap.containsKey( hostName )) {
						destMap.put( hostName, new HashMap<Integer, Map<String, String>>());
					}

					int geoSlot;
					if (keySlot.equals("PORT"))
						geoSlot = getPortFromContext(context);
					else
						geoSlot = Integer.parseInt( values[fieldMap.get(keySlot)] );

					if (! destMap.get(hostName).containsKey(geoSlot) ) {
						destMap.get(hostName).put( geoSlot, new HashMap<String, String>());
					}

					if (  (!checkTimeStamp) || (! fieldMap.containsKey("timestamp") ) ||
							(! destMap.get( hostName ).get(geoSlot).containsKey("timestamp")) ||
							( destMap.get( hostName ).get(geoSlot).containsKey("timestamp") && destMap.get( hostName ).get(geoSlot).get("timestamp").compareTo(values[fieldMap.get("timestamp")]) <= 0 ) ) {

						for (String field : fields) {

							String fieldName = field;
							String fieldKey = field;
							if (field.contains(":")) {
								fieldName = field.split(":")[0];
								fieldKey = field.split(":")[1];
							}

							Integer idx = fieldMap.get( fieldName );
							if (idx == null)
								throw new IOException("field " + fieldName + " not found");
							String value = values[ idx ];
							destMap.get( hostName ).get(geoSlot).put(fieldKey, value);
						}
					}
				}
			}
		}
		finally {
			if (reader != null)
				reader.close();
			if (conn != null)
				conn.disconnect();
		}
		long t1 = System.currentTimeMillis();
		System.out.println("Time to retrieve & parse " + url + " : " + (t1-t0) + " ms. read: " + (ttr/1000000) + " ms.");
	}







	protected void readInfospaceByHGI(
			String isbUrl,
			Map<String, Map<Integer, Map <Integer, Map<String, String>>>> destMap,
			String keySlot,
			String keyIO,
			String fields[]) throws IOException {

		long t0 = System.currentTimeMillis();
		long ttr = 0;
		// Create an URL instance
		URL url;
		url = new URL( isbUrl );

		// Get an input stream for reading
		HttpURLConnection conn = null;
		InputStream in = null;
		InputStreamReader isr = null;
		BufferedReader reader = null;
		try {
			long tt0 = System.nanoTime();
			conn = (HttpURLConnection) url.openConnection();
			in = conn.getInputStream();
			isr = new InputStreamReader(in);
			reader = new BufferedReader(isr);
			ttr += System.nanoTime()-tt0;

			// Repeat until end of file
			Map<String, Integer> fieldMap = new HashMap<String, Integer>();
			for (boolean firstLine = true;;firstLine = false)
			{
				tt0 = System.nanoTime();
				String line = reader.readLine();
				ttr += System.nanoTime()-tt0;
				if (line == null)
					break;

				if (firstLine) {
					String fieldNames[] = line.split(_delimiter);
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

					String context = values[fieldMap.get("context")];
					String hostName = getHostFromContext(context);

					if (fieldMap.containsKey("timestamp"))
						addTimeStampToHisto(isbUrl, values[fieldMap.get("timestamp")]);

					if (! destMap.containsKey( hostName )) {
						destMap.put( hostName, new HashMap<Integer, Map<Integer, Map<String, String>>>());
					}

					int geoSlot = Integer.parseInt( values[fieldMap.get(keySlot)] );
					if (! destMap.get(hostName).containsKey(geoSlot) ) {
						destMap.get(hostName).put( geoSlot, new HashMap<Integer, Map<String, String>>());
					}

					int io = Integer.parseInt( values[fieldMap.get(keyIO)] );
					if (! destMap.get(hostName).get(geoSlot).containsKey(io) ) {
						destMap.get(hostName).get( geoSlot).put(io, new HashMap<String, String>());
					}



					if (  (! fieldMap.containsKey("timestamp") ) ||
							(! destMap.get( hostName ).get(geoSlot).get(io).containsKey("timestamp")) ||
							( destMap.get( hostName ).get(geoSlot).get(io).containsKey("timestamp") && destMap.get( hostName ).get(geoSlot).get(io).get("timestamp").compareTo(values[fieldMap.get("timestamp")]) <= 0 ) ) {


						for (String field : fields) {

							Integer fieldIndex = fieldMap.get( field );
							if (fieldIndex == null) {
								reader.close();
								throw new IOException("Error: field with name '" + field + "' not found in flash list.");
							}
							String value = values[fieldIndex];
							destMap.get(hostName).get(geoSlot).get(io).put(field, value);
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
		long t1 = System.currentTimeMillis();
		System.out.println("Time to retrieve & parse " + url + " : " + (t1-t0) + " ms. read: " + (ttr/1000000) + " ms.");
	}







	protected void readJCFlashList(
			String isbUrl) throws IOException {


		long time1 = System.currentTimeMillis();
		long njc = 0;
		long nexec = 0;
		// Create an URL instance
		URL url  = new URL( isbUrl );

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

				if (firstLine) {
					continue;
				}


				++njc;
				// String line1 = line.replaceAll("\\{.*\\}", "");
				// System.out.println(line1);

				String jt = line.split("\\{")[1].split("\\}")[0];
				//System.out.println(jt);


				String data = jt.split("\\\"data\\\"\\:")[1];
				//		System.out.println(data);

				data = data.substring(1,data.length()-1);

				if (data.length()>0) {
					String jtRows[] = data.split(",(?=\\[.*\\])");

					for (String row : jtRows) {
						String rowTrimmed = row.substring(1,row.length()-1);

						//			System.out.println("row: " + rowTrimmed);


						String cols[] = rowTrimmed.split(",");



						if (cols.length >= 7) {
							String context = cols[2];
							if (context.startsWith("\"") && context.endsWith("\""))
								context = context.substring(1, context.length()-1);

							if (context.endsWith( "/urn:xdaq-application:lid=0" ))
								context = context.substring(0, context.length() - 27);

							String state = cols[6];
							if (state.startsWith("\"") && state.endsWith("\""))
								state = state.substring(1, state.length()-1);


//							_jcMap.put(context, state);
							++nexec;
							//System.out.println(context + " => " + state);
						}
					}
				}
			}
		}
		finally {
			if (reader != null)
				reader.close();
			if (conn != null)
				conn.disconnect();
		}

		long time2 = System.currentTimeMillis();

		System.out.println("Flash lists for " + njc + " jobcontrols / " + nexec + " executives retrieved and parsed in " + (time2-time1) + " ms.");
	}


	public abstract void retrieveMonitorData() throws IOException;

}
