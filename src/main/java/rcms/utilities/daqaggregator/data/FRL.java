package rcms.utilities.daqaggregator.data;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * Front-end Readout Link
 * 
 * @author Andre Georg Holzner (andre.georg.holzner@cern.ch)
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class FRL implements java.io.Serializable {

	// ----------------------------------------
	// fields set at beginning of session
	// ----------------------------------------

	/** the parent SubFEDBuilder this FRL belongs to */
	private SubFEDBuilder subFedbuilder;

	private int geoSlot;

	/** what type it is, values enumerated in enum */
	private FRLType type;

	/**
	 * maps from 0, 1 to FED. Note that some FRLs have only FED 1 connected but
	 * not FED 0
	 */
	private final Map<Integer, FED> feds = new HashMap<>();

	// ----------------------------------------
	// fields updated periodically
	// ----------------------------------------
	private String state;
	
	private String substate;

	/** xdaq application url */
	private String url;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

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

	public FRLType getType() {
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

	public void setType(FRLType type) {
		this.type = type;
	}

	public String getSubstate() {
		return substate;
	}

	public void setSubstate(String substate) {
		this.substate = substate;
	}

	// ----------------------------------------------------------------------

}
