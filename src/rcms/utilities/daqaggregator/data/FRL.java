package rcms.utilities.daqaggregator.data;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class FRL {

	//----------------------------------------
	// fields set at beginning of session
	//----------------------------------------

	/** the parent SubFEDBuilder this FRL belongs to */
	private SubFEDBuilder subFedbuilder;

	private int geoSlot;
  
	/** what type it is: SLINK, SLINKEXPRESS, SLINKEXPRESS10G 
	 *  TODO: should we make this an enum instead ? */
	private String type;
	
	/** maps from 0, 1 to FED. Note that some FRLs have only FED 1 connected
	 *  but not FED 0
	 */
	private final Map<Integer, FED> feds = new HashMap<>();

	//----------------------------------------
	// fields updated periodically
	//----------------------------------------
	private String state;


	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public SubFEDBuilder getSubFedbuilder() {
		return subFedbuilder;
	}

	public int getGeoSlot() {
		return geoSlot;
	}

	public String getType() {
		return type;
	}

	public Map<Integer, FED> getFeds() {
		return feds;
	}

	public void setSubFedbuilder(SubFEDBuilder subFedbuilder) {
		this.subFedbuilder = subFedbuilder;
	}

	public void setGeoSlot(int geoSlot) {
		this.geoSlot = geoSlot;
	}

	public void setType(String type) {
		this.type = type;
	}

	//----------------------------------------------------------------------

}
