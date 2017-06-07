package rcms.utilities.daqaggregator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.bind.DatatypeConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.datasource.Flashlist;
import rcms.utilities.daqaggregator.persistence.FileSystemConnector;
import rcms.utilities.daqaggregator.persistence.PersistenceExplorer;
import rcms.utilities.daqaggregator.persistence.PersistenceFormat;
import rcms.utilities.daqaggregator.persistence.StructureSerializer;

/**
 * Converts flashlists from one format to another
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class Converter {

	public static void main(String[] args) throws IOException {

		// Times here are in UTC
		Long startTimestamp = DatatypeConverter.parseDateTime("2016-10-25T06:50:59+02:00").getTimeInMillis();
		Long endTimestamp = DatatypeConverter.parseDateTime("2017-10-25T14:09:59+02:00").getTimeInMillis();
		String sourceDir = "/tmp/1.12.2/flashlists/";
		String targetDir = "/tmp/1.12.2/snapshots-json/";
		PersistenceFormat sourceFormat = PersistenceFormat.SMILE;
		PersistenceFormat targetFormat = PersistenceFormat.JSON;

		Converter converter = new Converter();

		converter.convertFlashlist(startTimestamp, endTimestamp, sourceDir, targetDir, sourceFormat, targetFormat);
	}

	private void convertFlashlist(Long startTimestamp, Long endTimestamp, String sourceDir, String targetDir,
			PersistenceFormat sourceFormat, PersistenceFormat targetFormat) throws IOException {

		StructureSerializer serializer = new StructureSerializer();

		File[] listOfFiles = new File(sourceDir).listFiles();

		for (File dir : listOfFiles) {
			if (dir.isDirectory()) {
				System.out.println("Converting flashlist: " + dir.getName());
				Entry<Long, List<File>> result = (new PersistenceExplorer(new FileSystemConnector()))
						.explore(startTimestamp, endTimestamp, dir.getAbsolutePath());

				System.out.println("Explored: " + result.getValue());

				for (File file : result.getValue()) {

					if(file.getName().endsWith(".json")){
						file.delete();
						continue;
					}
					Flashlist flashlist = serializer.deserializeFlashlist(file, sourceFormat);

					String flashlistFilename = flashlist.getRetrievalDate().getTime() + targetFormat.getExtension();

					File targetFile = new File(file.getParent() + "/" + flashlistFilename);

					ObjectMapper mapper = targetFormat.getMapper();

					FileOutputStream fos = new FileOutputStream(targetFile);
					mapper.writerWithDefaultPrettyPrinter().writeValue(fos, flashlist);
					file.delete();
				}
			}
		}

	}

	private void convertSnapshot(Long startTimestamp, Long endTimestamp, String sourceDir, String targetDir,
			PersistenceFormat sourceFormat, PersistenceFormat targetFormat) throws IOException {

		StructureSerializer serializer = new StructureSerializer();

		Entry<Long, List<File>> result = (new PersistenceExplorer(new FileSystemConnector())).explore(startTimestamp,
				endTimestamp, sourceDir);

		System.out.println("Explored: " + result.getValue());

		for (File file : result.getValue()) {

			DAQ snapshot = serializer.deserialize(file.getAbsolutePath(), sourceFormat);

			String snapshotFileName = snapshot.getLastUpdate() + targetFormat.getExtension();

			File targetFile = new File(targetDir + snapshotFileName);

			FileOutputStream fos = new FileOutputStream(targetFile);
			serializer.serialize(snapshot, fos, targetFormat);
		}
	}

}
