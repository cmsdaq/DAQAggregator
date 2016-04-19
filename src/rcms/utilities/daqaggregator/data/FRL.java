package rcms.utilities.daqaggregator.data;

import java.util.HashMap;
import java.util.Map;

public class FRL {

	//----------------------------------------
	// fields set at beginning of session
	//----------------------------------------

	/** the parent SubFEDBuilder this FRL belongs to */
	private final SubFEDBuilder subFedbuilder;

	private final int geoSlot;
  
	/** what type it is: SLINK, SLINKEXPRESS, SLINKEXPRESS10G 
	 *  TODO: should we make this an enum instead ? */
	private final String type;
	
	/** maps from 0, 1 to FED. Note that some FRLs have only FED 1 connected
	 *  but not FED 0
	 */
	private final Map<Integer, FED> feds = new HashMap<>();

	//----------------------------------------
	// fields updated periodically
	//----------------------------------------
	private String state;

	//----------------------------------------------------------------------

	public FRL(SubFEDBuilder subFedbuilder, int geoSlot, String type) {
		this.subFedbuilder = subFedbuilder;
		this.geoSlot = geoSlot;
		this.type = type;

		// TODO: fill feds
	}
	//----------------------------------------------------------------------

}
