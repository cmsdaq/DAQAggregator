package rcms.utilities.daqaggregator.data;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.mappers.FlashlistType;
import rcms.utilities.daqaggregator.mappers.FlashlistUpdatable;


/**
 * Front-end Readout Link PC
 * 
 * @author Andre Georg Holzner (andre.georg.holzner@cern.ch)
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class FRLPc implements java.io.Serializable, FlashlistUpdatable{

	// ----------------------------------------
	// fields set at beginning of session
	// ----------------------------------------

	private String hostname;

	/** xdaq application url */
	private String url;

	private boolean masked;

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

	public String getHostname() {
		return hostname;
	}

	public String getUrl() {
		return url;
	}

	public boolean isMasked() {
		return masked;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setMasked(boolean masked) {
		this.masked = masked;
	}

	@Override
	public void updateFromFlashlist(FlashlistType flashlistType, JsonNode flashlistRow) {
		
		// TODO Auto-generated method stub
		
	}
	// ----------------------------------------------------------------------

}
