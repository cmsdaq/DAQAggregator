package rcms.utilities.daqaggregator.mappers.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.mappers.Flashlist;

public class TCDSFlashlistHelpers {

	public static String decodeTCDSTTSState(int tts_value) {

		// now need to decode pm tts value
		// 

		/* - 0x0-0xf: true, legacy TTS state

		- 0x10: link lost at PI input (unaligned)
		- 0x11: link lost at PI input (RX lost)
		- 0x12: link lost at PI input (no SFP)

		- 0x20: link lost at LPM input (unaligned)
		- 0x21: link lost at LPM input (RX lost)
		- 0x22: link lost at LPM input (no SFP)

		- 0x30: link lost at CPM input

		- 0x98: ignored (i.e., forced ready)

		- 0xee: internal TCDS error (This is the fall-through in the code. This value always indicates a bug.)


		 */

		switch (tts_value) {
		case 0x0 : return "D_0"; 
		case 0x1 : return "W_1";
		case 0x2 : return "S_2";
		case 0x4 : return "B_4";
		case 0x8 : return "R_8";
		case 0xc : return "E_c";
		case 0xf : return "D_f";
		case 0x98 : return "-";
		}

		if (tts_value < 0xf)
			return "I_" + String.format("%x",tts_value); 
		else 
			return "X_" + String.format("%x",tts_value); // new TCDS error code

	}

	public static Map<String, Map<String, Map<Integer, Map<Integer, Map<String, String>>>>> getTreeFromFlashlist(
			Flashlist flashlist) {

		//initialize hierarchical index
		Map<String, Map<String, Map<Integer, Map<Integer, Map<String, String>>>>> rootToReturn = new HashMap<String, Map<String, Map<Integer, Map<Integer, Map<String, String>>>>>();

		//init index by service
		for (JsonNode row : flashlist.getRowsNode()) {
			String service = row.get("service").asText();

			if (!rootToReturn.containsKey(service)){
				rootToReturn.put(service, new HashMap<String, Map<Integer, Map<Integer, Map<String, String>>>>());
			}
		}

		//init index by service.type
		for (JsonNode row : flashlist.getRowsNode()) {
			String service = row.get("service").asText();
			String type = row.get("type").asText();

			if (!rootToReturn.get(service).containsKey(type)){
				rootToReturn.get(service).put(type, new HashMap<Integer, Map<Integer, Map<String, String>>>());
			}
		}

		//init index by service.type.pm_number
		for (JsonNode row : flashlist.getRowsNode()) {
			String service = row.get("service").asText();
			String type = row.get("type").asText();
			int pmNr = row.get("pm_number").asInt();

			if (!rootToReturn.get(service).get(type).containsKey(pmNr)){
				rootToReturn.get(service).get(type).put(pmNr, new HashMap<Integer, Map<String, String>>());
			}
		}

		//init index by service.type.pm_number.id_number
		for (JsonNode row : flashlist.getRowsNode()) {
			String service = row.get("service").asText();
			String type = row.get("type").asText();
			int pmNr = row.get("pm_number").asInt();
			int idNr = row.get("id_number").asInt();

			if (!rootToReturn.get(service).get(type).get(pmNr).containsKey(idNr)){
				rootToReturn.get(service).get(type).get(pmNr).put(idNr, new HashMap<String, String>()); //innermost map contains rest attributes by name
			}
		}
		
		
		
		//populate index with tuples
		
		//list attribute names
		List<String> fields = new ArrayList<String>();
		for (int i=0;i<flashlist.getDefinitionNode().size();i++){
			fields.add(flashlist.getDefinitionNode().get(i).get("key").asText());
		}
		
		//iterate over data tuples and index them by fieldName:value in the innermost map
		for (JsonNode row : flashlist.getRowsNode()) {
			
			//needed to insert into index as defined above
			String service = row.get("service").asText();
			String type = row.get("type").asText();
			int pmNr = row.get("pm_number").asInt();
			int idNr = row.get("id_number").asInt();
			
			for (String field : fields){
				rootToReturn.get(service).get(type).get(pmNr).get(idNr).put(field, row.get(field).asText());
			}

		}
		
		return rootToReturn;
	}

}
