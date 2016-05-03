package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * Fast Merging Module
 * 
 * @author Andre Georg Holzner (andre.georg.holzner@cern.ch)
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class FMM implements java.io.Serializable {

	// ----------------------------------------
	// fields set at beginning of session
	// ----------------------------------------

	/** parent TTCPartition */
	private TTCPartition ttcPartition;

	private FMMApplication fmmApplication;

	private int geoslot;

	private String url;

	private List<FED> feds = new ArrayList<FED>();

	public TTCPartition getTtcPartition() {
		return ttcPartition;
	}

	public FMMApplication getFmmApplication() {
		return fmmApplication;
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
}
