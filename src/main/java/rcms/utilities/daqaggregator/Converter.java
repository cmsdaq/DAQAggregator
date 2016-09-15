package rcms.utilities.daqaggregator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqaggregator.datasource.Flashlist;
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
		Long startTimestamp = javax.xml.bind.DatatypeConverter.parseDateTime("2016-08-30T19:50:00").getTimeInMillis();
		Long endTimestamp = javax.xml.bind.DatatypeConverter.parseDateTime("2016-08-30T20:40:00").getTimeInMillis();
		String sourceDir = "/tmp/daqaggregator-dev/flashlists/LEVEL_ZERO_FM_DYNAMIC/";
		String targetDir = "/tmp/flashlists-request/flashlist-LEVEL_ZERO_FM_DYNAMIC-2016-08-30T20:00:00/";
		PersistenceFormat sourceFormat = PersistenceFormat.SMILE;
		PersistenceFormat targetFormat = PersistenceFormat.JSON;

		Converter converter = new Converter();

		converter.convert(startTimestamp, endTimestamp, sourceDir, targetDir, sourceFormat, targetFormat);
	}

	private void convert(Long startTimestamp, Long endTimestamp, String sourceDir, String targetDir,
			PersistenceFormat sourceFormat, PersistenceFormat targetFormat) throws IOException {

		StructureSerializer serializer = new StructureSerializer();

		Entry<Long, List<File>> result = (new PersistenceExplorer()).explore(startTimestamp, endTimestamp, sourceDir);

		System.out.println("Explored: " + result.getValue());

		for (File file : result.getValue()) {

			Flashlist flashlist = serializer.deserializeFlashlist(file, sourceFormat);

			String flashlistFilename = flashlist.getRetrievalDate().getTime() + targetFormat.getExtension();

			File targetFile = new File(targetDir + flashlistFilename);

			ObjectMapper mapper = targetFormat.getMapper();

			FileOutputStream fos = new FileOutputStream(targetFile);
			mapper.writerWithDefaultPrettyPrinter().writeValue(fos, flashlist);
		}
	}

}
