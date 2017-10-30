package rcms.utilities.daqaggregator.datasource;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.lang3.tuple.Pair;
import rcms.utilities.daqaggregator.Application;
import rcms.utilities.daqaggregator.ProxyManager;
import rcms.utilities.daqaggregator.persistence.PersistorManager;

/**
 * Class to download the known flashlists in JSON format from the LAS
 * and create a .zip file containing the downloaded flashlists
 * in the directory structure similar to what the DAQAggregator persists.
 */
public class RawFlashListDownloader {

	private final Connector connector;

	static public void main(String argv[]) throws IOException {

		// first argument must be a .properties file
		// which contains the URLs of the LASes

		if (argv.length != 2) {
			System.err.println("must two arguments: daqaggregator.properties outputFile.zip");
			System.exit(1);
		}

		String propertiesFname = argv[0];
		String outputFname = argv[1];

		new RawFlashListDownloader(propertiesFname).run(outputFname);
	}

	private RawFlashListDownloader(String propertiesFname) {

		Application.initialize("DAQAggregator.properties");
		ProxyManager.get().startProxy();

		// to perform HTTP requests
		connector = new Connector(false);
	}

	private String retrieveFlashList(String url) throws IOException {

		Pair<Integer, List<String>> content = connector.retrieveLines(url);

		// concatenate it again
		StringBuilder sb = new StringBuilder();

		for (String line : content.getRight()) {
			sb.append(line);
		}

		return sb.toString();

	}

	private void run(String outputFname) throws IOException {

		ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(outputFname));

		Date now = new Date();
		long nowSeconds = now.getTime();

		// to get the directory names
		PersistorManager persistorManager = new PersistorManager(null, null, null, null);

		// loop over all known flashlists
		for (FlashlistType flashlistType : FlashlistType.values()) {

			// download the flashlist from the LAS in json format
			String jsonText = retrieveFlashList(flashlistType.getUrl());

			// build the name for the .json file
			String destFname = persistorManager.getTimeDir(flashlistType.name() + "/", now)
							+ nowSeconds + ".json";

			ZipEntry zipEntry = new ZipEntry(destFname);
			zip.putNextEntry(zipEntry);

			// append the JSON string to the output .zip file
			byte[] data = jsonText.getBytes();
			zip.write(data);

		} // end loop over flashlists

		zip.close();

	}

}
