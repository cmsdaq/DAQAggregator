package rcms.utilities.daqaggregator.datasource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

public class Connector {

	private static final Logger logger = Logger.getLogger(Connector.class);


	public Connector() {

	}

	/**
	 * Retrieve, generic function
	 */
	public Pair<Integer, List<String>> retrieveLines(String urlString) throws IOException {

		List<String> result = new ArrayList<>();
		URL url = new URL(urlString);

		HttpURLConnection conn = null;
		InputStream in = null;
		InputStreamReader isr = null;
		BufferedReader reader = null;
		int httpCode = -1;
		
		try {
			conn = (HttpURLConnection) url.openConnection();
			httpCode = conn.getResponseCode();
			
			if (conn.getResponseCode() < 400){
				in = conn.getInputStream();
				isr = new InputStreamReader(in);
				reader = new BufferedReader(isr);

				for (String line = reader.readLine(); line != null; line = reader.readLine()) {
					result.add(line);
				}
			}
			else{
				logger.error("HTTP error "+conn.getResponseCode()+" in retrieving flashlist lines at: "+url);
			}


		} catch (IOException e) {
			System.out.println("\n\nError retrieving ctatalog from URL=" + url);
			e.printStackTrace();
			System.out.println("\n\nContinuing catalog retrieve...");
		} finally {
			if (reader != null)
				reader.close();
			if (isr != null)
				isr.close();
			if (in != null)
				in.close();
			if (conn != null)
				conn.disconnect();
		}

		logger.debug("Generic rows fetched: " + result.size() + " for request " + urlString);

		return Pair.of(httpCode, result);
	}
}
