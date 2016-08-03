import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.persistence.StructureSerializer;

public class RefPrefix {
	
	public static void main(String[] args) {
		StructureSerializer serializer = new StructureSerializer();
		DAQ daq = serializer.deserializeFromJSON("/afs/cern.ch/user/m/mvougiou/Desktop/tmp/snapshots/1469177290765.json");
		
		try {
			serializer.serializeToRefJSON(daq, "1469177290765.ref", "/afs/cern.ch/user/m/mvougiou/Desktop/tmp/snapshots/");
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
