package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.mappers.FlashlistType;
import rcms.utilities.daqaggregator.mappers.FlashlistUpdatable;

/**
 * Fast Merging Module
 * 
 * @author Andre Georg Holzner (andre.georg.holzner@cern.ch)
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class FMM implements FlashlistUpdatable {

	// ----------------------------------------
	// fields set at beginning of session
	// ----------------------------------------

	/** parent TTCPartition */
	@JsonManagedReference(value = "fmm-ttcp")
	private TTCPartition ttcPartition;

	private FMMApplication fmmApplication;

	private int geoslot;

	private String url;

	@JsonManagedReference(value = "fmm-fed")
	private List<FED> feds = new ArrayList<FED>();

	@JsonIgnore
	public boolean takeB;

	public TTCPartition getTtcPartition() {
		return ttcPartition;
	}

	public FMMApplication getFmmApplication() {
		return fmmApplication;
	}

	@Override
	public String toString() {
		return "FMM [fmmApplication=" + fmmApplication + ", geoslot=" + geoslot + "]";
	}

	public int getGeoslot() {
		return geoslot;
	}

	public String getUrl() {
		return url;
	}

	public List<FED> getFeds() {
		return feds;
	}

	public void setTtcPartition(TTCPartition ttcPartition) {
		this.ttcPartition = ttcPartition;
	}

	public void setFmmApplication(FMMApplication fmmApplication) {
		this.fmmApplication = fmmApplication;
	}

	public void setGeoslot(int geoslot) {
		this.geoslot = geoslot;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setFeds(List<FED> feds) {
		this.feds = feds;
	}

	@Override
	public void updateFromFlashlist(FlashlistType flashlistType, JsonNode flashlistRow) {
		if (flashlistType == FlashlistType.FMM_STATUS) {
			url = flashlistRow.get("context").asText();
		}
	}

	@Override
	public void clean() {
		// nothing to do
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((feds == null) ? 0 : feds.hashCode());
		result = prime * result + geoslot;
		result = prime * result + ((ttcPartition == null) ? 0 : ttcPartition.hashCode());
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
		FMM other = (FMM) obj;
		if (feds == null) {
			if (other.feds != null)
				return false;
		} else if (!feds.equals(other.feds))
			return false;

		if (geoslot != other.geoslot)
			return false;
		if (ttcPartition == null) {
			if (other.ttcPartition != null)
				return false;
		} else if (!ttcPartition.equals(other.ttcPartition))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}
}
