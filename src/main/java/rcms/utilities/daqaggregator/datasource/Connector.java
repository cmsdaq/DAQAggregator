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

	private final boolean suppressFailedRequests;

	/** username and password if set */
	private final String userPass;

	public Connector(boolean suppressFailedRequests) {
		this.suppressFailedRequests = suppressFailedRequests;
		userPass = null;
	}

	/** connector pages which need basic authentication */
	public Connector(boolean suppressFailedRequests, String username, String password) {
		this.suppressFailedRequests = suppressFailedRequests;
		userPass = username + ":" + password;
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

			if (userPass != null) {
				// see https://stackoverflow.com/a/12603622/288875
				String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userPass.getBytes());
				conn.setRequestProperty ("Authorization", basicAuth);
			}

			httpCode = conn.getResponseCode();

			if (conn.getResponseCode() == 200) {
				in = conn.getInputStream();
				isr = new InputStreamReader(in);
				reader = new BufferedReader(isr);

				for (String line = reader.readLine(); line != null; line = reader.readLine()) {
					result.add(line);
				}
			} else {
				if (!suppressFailedRequests) {
					logger.error("HTTP error " + conn.getResponseCode() + " in retrieving flashlist lines at: " + url);
				}
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
