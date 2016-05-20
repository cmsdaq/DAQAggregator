package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.mappers.FlashlistType;
import rcms.utilities.daqaggregator.mappers.FlashlistUpdatable;

/**
 * Fast Merging Module Application
 * 
 * @author Andre Georg Holzner (andre.georg.holzner@cern.ch)
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class FMMApplication implements java.io.Serializable , FlashlistUpdatable{

	// ----------------------------------------
	// fields set at beginning of session
	// ----------------------------------------

	private DAQ daq;

	private String hostname;

	private String url;

	private final List<FMM> fmms = new ArrayList<FMM>();

	// ----------------------------------------
	// fields updated periodically
	// ----------------------------------------

	private boolean crashed;

	// ----------------------------------------------------------------------

	public boolean isCrashed() {
		return crashed;
	}

	public void setCrashed(boolean crashed) {
		this.crashed = crashed;
	}

	public DAQ getDaq() {
		return daq;
	}

	public String getHostname() {
		return hostname;
	}

	public String getUrl() {
		return url;
	}

	public List<FMM> getFmms() {
		return fmms;
	}

	public void setDaq(DAQ daq) {
		this.daq = daq;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	@Override
	public void updateFromFlashlist(FlashlistType flashlistType, JsonNode flashlistRow) {
		if(flashlistType == FlashlistType.JOB_CONTROL){
			System.out.println(flashlistRow); //TODO: use job control for crashed
		}
	}

	// ----------------------------------------------------------------------

}
