package rcms.utilities.daqaggregator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.persistence.PersistenceFormat;
import rcms.utilities.daqaggregator.persistence.StructureSerializer;

/**
 * 
 * TODO: this test will not run automatically
 * 
 * TODO: avoid absolute path since it will not work on all machines
 * 
 * @author Michail Vougioukas (michail.vougioukas@cern.ch)
 */
public class RefPrefix {

	public static void main(String[] args) {
		StructureSerializer serializer = new StructureSerializer();
		DAQ daq = serializer.deserialize("/afs/cern.ch/user/m/mvougiou/Desktop/tmp/snapshots/1470665696624.json",
				PersistenceFormat.JSON);

		try {
			File file = new File("/afs/cern.ch/user/m/mvougiou/Desktop/tmp/snapshots/");

			FileOutputStream fos = new FileOutputStream(file);
			serializer.serialize(daq, fos, PersistenceFormat.JSONREFPREFIXED);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
