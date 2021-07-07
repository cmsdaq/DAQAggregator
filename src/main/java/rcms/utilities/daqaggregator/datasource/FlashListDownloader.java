package rcms.utilities.daqaggregator.datasource;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.lang3.tuple.Triple;
import rcms.utilities.daqaggregator.Application;
import rcms.utilities.daqaggregator.ProxyManager;
import rcms.utilities.daqaggregator.Settings;
import rcms.utilities.daqaggregator.persistence.PersistenceFormat;
import rcms.utilities.daqaggregator.persistence.PersistorManager;

/**
 * Class to download the known flashlists from the LAS
 * and create a .zip file containing the downloaded flashlists
 * in persisted format with the same directory structure as the DAQAggregator
 * uses to persist the downloaded flashlists.
 */
public class FlashListDownloader {

	private final Connector connector;

	private final LASFlashlistRetriever flashlistRetriever;

	/** the format in which the flashlist object should be written out */
	private final PersistenceFormat persistenceFormat;

	static public void main(String argv[]) throws IOException {

		// first argument must be a .properties file
		// which contains the URLs of the LASes
		if (argv.length != 2) {
			System.err.println("must two arguments: daqaggregator.properties outputFile.zip");
			System.exit(1);
		}

		String propertiesFname = argv[0];
		String outputFname = argv[1];

		new FlashListDownloader(propertiesFname).run(outputFname);
	}

	private FlashListDownloader(String propertiesFname) {

		Application.initialize("DAQAggregator.properties");
		ProxyManager.get().startProxy();

		// to perform HTTP requests
		connector = new Connector(false);

		persistenceFormat = PersistenceFormat.decode(Application.get().getProp(Settings.PERSISTENCE_FLASHLIST_FORMAT));

		flashlistRetriever = new LASFlashlistRetriever(false);
	}

	/** serializes a flashlist.
	 *
	 *	TODO: some of the code below could be factored out from PersistorManager.persistFlashlist()
	 */
	private String serializeFlashlist(Flashlist flashlist) throws IOException {

		ObjectMapper mapper = persistenceFormat.getMapper();

		OutputStream finalOutputStream  = new ByteArrayOutputStream();

		switch (persistenceFormat) {

		case JSON:
			// nothing to do
			break;
		case ZIPPED:
			GZIPOutputStream gzis = new GZIPOutputStream(finalOutputStream);
			finalOutputStream = gzis;
			break;
		}

		mapper.writerWithDefaultPrettyPrinter().writeValue(finalOutputStream, flashlist);
		return finalOutputStream.toString();
	}

	private void run(String outputFname) throws IOException {

		ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(outputFname));

		Date now = new Date();
		long nowSeconds = now.getTime();

		// read Level0 dynamic flashlist to get the session id
		Flashlist l0dynamic = flashlistRetriever.retrieveFlashlist(FlashlistType.LEVEL_ZERO_FM_DYNAMIC).getLeft();

		SessionRetriever sessionRetriever = new SessionRetriever(
			Application.get().getProp(Settings.SESSION_L0FILTER1),
			Application.get().getProp(Settings.SESSION_L0FILTER2)
		);

		// to get the directory names
		PersistorManager persistorManager = new PersistorManager(null, null, null, null);

		Triple<String, Integer, Long> sidInfo = sessionRetriever.retrieveSession(l0dynamic);
		Integer sid = sidInfo.getMiddle();

		// download all flashlists
		Map<FlashlistType, Flashlist> flashlists = flashlistRetriever.retrieveAllFlashlists(sid);

		for (Map.Entry<FlashlistType, Flashlist> entry : flashlists.entrySet()) {

			FlashlistType flashlistType = entry.getKey();
			Flashlist flashlist = entry.getValue();

			String serialized = serializeFlashlist(flashlist);

			// build the name for the .json file
			String destFname = persistorManager.getTimeDir(flashlistType.name() + "/", now)
							+ nowSeconds + persistenceFormat.getExtension();

			ZipEntry zipEntry = new ZipEntry(destFname);
			zip.putNextEntry(zipEntry);

			// append the JSON string to the output .zip file
			byte[] data = serialized.getBytes();
			zip.write(data);

		} // end loop over flashlists

		zip.close();

		// avoid waiting forever for the program to exit
		flashlistRetriever.stopExecutors();
	}

}
