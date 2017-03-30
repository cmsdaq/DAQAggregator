package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.datasource.FlashlistType;
import rcms.utilities.daqaggregator.mappers.FlashlistUpdatable;

/**
 * Fast Merging Module
 * 
 * @author Andre Georg Holzner (andre.georg.holzner@cern.ch)
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
public class FMM implements FlashlistUpdatable {

	private String id;

	
	
	// ----------------------------------------
	// fields set at beginning of session
	// ----------------------------------------

	/** parent TTCPartition */
	private TTCPartition ttcPartition;

	private FMMApplication fmmApplication;

	private FMMType fmmType;
	
	private String serviceName;
	

	private int geoslot;

	private String url;

	private List<FED> feds = new ArrayList<FED>();

	private boolean takeB;
	
	private String stateName;
	

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

	public FMMType getFmmType() {
		return fmmType;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setFmmType(FMMType fmmType) {
		this.fmmType = fmmType;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
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
	

	public String getStateName() {
		return stateName;
	}

	public void setStateName(String stateName) {
		this.stateName = stateName;
	}

	@Override
	public void updateFromFlashlist(FlashlistType flashlistType, JsonNode flashlistRow) {
		if (flashlistType == FlashlistType.FMM_STATUS) {
			url = flashlistRow.get("context").asText();
			stateName = flashlistRow.get("stateName").asText();
		}
	}

	@Override
	public void clean() {
		url = null;
		stateName = null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((feds == null) ? 0 : feds.hashCode());
		result = prime * result + geoslot;
		result = prime * result + ((ttcPartition == null) ? 0 : ttcPartition.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		result = prime * result + ((stateName == null) ? 0 : stateName.hashCode());
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
		if (stateName == null) {
			if (other.stateName != null)
				return false;
		} else if (!stateName.equals(other.stateName))
			return false;
		return true;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public boolean isTakeB() {
		return takeB;
	}

	public void setTakeB(boolean takeB) {
		this.takeB = takeB;
	}

}
