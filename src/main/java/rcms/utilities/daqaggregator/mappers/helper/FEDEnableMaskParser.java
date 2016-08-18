package rcms.utilities.daqaggregator.mappers.helper;

import java.util.HashMap;
import java.util.Map;

/**
 * This class helps parsing the FED_ENABLE_MASK session-aware string, which contains masking information for FEDs
 * 
 * @author Michail Vougioukas (michail.vougioukas@cern.ch)
 *
 */

public class FEDEnableMaskParser {
	
	/**Key is the srcExpectedId of a FED
	 * Value contains is a string that contains two dash-separated substrings, each to be parsed as boolean,
	 * the first of which is the flag for the fmmmasked and the second is the flag for the frlmasked
	 * */
	
	private final Map<Integer, String> fedByExpectedIdToMaskingFlags = new HashMap<Integer, String>();

	
	
	public FEDEnableMaskParser(String fedEnableMask) {
		super();
		parse(fedEnableMask);
	}



	private void parse(String fedEnableMask) {
		
		String [] fedEntries = fedEnableMask.split("%");
		for (String s : fedEntries){
			String [] splitEntry = s.split("&");
			
			Integer fedSrcId = Integer.parseInt(splitEntry[0]);
			Integer maskSum = Integer.parseInt(splitEntry[1]);
			
			int [] lowerBits = {0,0,0,0};
			String binaryMaskSum = Integer.toString(maskSum, 2);

			//String padding
			if (binaryMaskSum.length()==3){
				binaryMaskSum = "0"+binaryMaskSum;
			}else if (binaryMaskSum.length()==2){
				binaryMaskSum = "00"+binaryMaskSum;
			}else if (binaryMaskSum.length()==1){
				binaryMaskSum = "000"+binaryMaskSum;
			}
			
			lowerBits[0] = Integer.parseInt(Character.toString(binaryMaskSum.charAt(0))); //Bit 3 (TTS)
			lowerBits[1] = Integer.parseInt(Character.toString(binaryMaskSum.charAt(1))); //Bit 2 (SLINK)
			lowerBits[2] = Integer.parseInt(Character.toString(binaryMaskSum.charAt(2))); //Bit 1 (TTS)
			lowerBits[3] = Integer.parseInt(Character.toString(binaryMaskSum.charAt(3))); //Bit 0 (SLINK)
			
			
			String resolvedMasks = "";
			
			if (lowerBits[0]==0 && lowerBits[2]==1){
				//FED has TTS output and TTS output is active
				resolvedMasks = resolvedMasks +"false"; //fmm masked false
			}else{
				resolvedMasks = resolvedMasks +"true"; //fmm masked true
			}
			
			resolvedMasks = resolvedMasks+ "-"; //delimiter
			
			if (lowerBits[1]==0 && lowerBits[3]==1){
				//FED has SLINK and SLINK is active
				resolvedMasks = resolvedMasks +"false"; //frl masked false
			}else{
				resolvedMasks = resolvedMasks +"true"; //frl masked true
			}
			
			//add to map
			fedByExpectedIdToMaskingFlags.put(fedSrcId, resolvedMasks);
		}
		
	}


	public Map<Integer, String> getFedByExpectedIdToMaskingFlags() {
		return fedByExpectedIdToMaskingFlags;
	}
	
	
}
