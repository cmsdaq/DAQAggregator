package rcms.utilities.daqaggregator.data;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.datasource.FlashlistType;
import rcms.utilities.daqaggregator.mappers.FlashlistUpdatable;

/**
 * Front-end Readout Link
 * 
 * @author Andre Georg Holzner (andre.georg.holzner@cern.ch)
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
public class FRL implements FlashlistUpdatable {

	// ----------------------------------------
	// fields set at beginning of session
	// ----------------------------------------
	
	private String id;

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

	private FRLPc frlPc;

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

	@Override
	public void updateFromFlashlist(FlashlistType flashlistType, JsonNode flashlistRow) {

		if (flashlistType == FlashlistType.FEROL_STATUS) {
			state = flashlistRow.get("stateName").asText();
			substate = flashlistRow.get("subState").asText();
			url = flashlistRow.get("context").asText();
		}
		
		if (flashlistType == FlashlistType.FEROL40_STATUS) {
			state = flashlistRow.get("stateName").asText();
			substate = flashlistRow.get("subState").asText();
			url = flashlistRow.get("context").asText();
		}

	}

	@Override
	public void clean() {
		// nothing to do
	}

	public FRLPc getFrlPc() {
		return frlPc;
	}

	public void setFrlPc(FRLPc frlPc) {
		this.frlPc = frlPc;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((feds == null) ? 0 : feds.hashCode());
		// result = prime * result + ((frlPc == null) ? 0 : frlPc.hashCode());
		result = prime * result + geoSlot;
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((substate == null) ? 0 : substate.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FRL other = (FRL) obj;
		if (feds == null) {
			if (other.feds != null)
				return false;
		} else if (!feds.equals(other.feds))
			return false;
		// if (frlPc == null) {
		// if (other.frlPc != null)
		// return false;
		// } else if (!frlPc.equals(other.frlPc))
		// return false;
		if (geoSlot != other.geoSlot)
			return false;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		if (substate == null) {
			if (other.substate != null)
				return false;
		} else if (!substate.equals(other.substate))
			return false;
		if (type != other.type)
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	// ----------------------------------------------------------------------

}
