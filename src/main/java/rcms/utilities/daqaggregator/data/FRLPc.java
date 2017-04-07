package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.datasource.FlashlistType;
import rcms.utilities.daqaggregator.mappers.Derivable;
import rcms.utilities.daqaggregator.mappers.FlashlistUpdatable;

/**
 * Front-end Readout Link PC
 * 
 * @author Andre Georg Holzner (andre.georg.holzner@cern.ch)
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */

public class FRLPc implements FlashlistUpdatable, Derivable {

	// ----------------------------------------
	// fields set at beginning of session
	// ----------------------------------------

	private String hostname;

	private Integer port;

	private boolean masked;

	private List<FRL> frls = new ArrayList<>();

	// ----------------------------------------
	// fields updated periodically
	// ----------------------------------------
	private Boolean crashed;

	// ----------------------------------------------------------------------

	public Boolean isCrashed() {
		return crashed;
	}

	public void setCrashed(Boolean crashed) {
		this.crashed = crashed;
	}

	public String getHostname() {
		return hostname;
	}

	public boolean isMasked() {
		return masked;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public void setMasked(boolean masked) {
		this.masked = masked;
	}

	public List<FRL> getFrls() {
		return frls;
	}

	public void setFrls(List<FRL> frls) {
		this.frls = frls;
	}

	@Override
	public void updateFromFlashlist(FlashlistType flashlistType, JsonNode flashlistRow) {

		if (flashlistType == FlashlistType.JOB_CONTROL) {

			JsonNode jobTable = flashlistRow.get("jobTable");
			JsonNode rows = jobTable.get("rows");
			this.crashed = false;
			for (JsonNode row : rows) {

				String status = row.get("status").asText();

				// if not alive than crashed, if no data than default value
				// witch is not crashed
				if (!status.equalsIgnoreCase("alive"))
					this.crashed = true;
			}
		}else if (flashlistType == FlashlistType.FEROL_CONFIGURATION || flashlistType == FlashlistType.FEROL40_CONFIGURATION) {
			this.port = Integer.parseInt(flashlistRow.get("context").asText().split(":")[2]);
		}
	}

	@Override
	public void clean() {
		port = null;
		crashed = null;
	}

	@Override
	public void calculateDerivedValues() {

		masked = false;
		int maskedFeds = 0;
		int allFeds = 0;

		for (FRL frl : frls) {
			for (FED fed : frl.getFeds().values()) {
				allFeds++;
				if (fed.isFrlMasked()==null) {
					maskedFeds++;
				}else{
					if (fed.isFrlMasked()) {
						maskedFeds++;
					}
				}
			}
		}

		/* FRLPc is mask if all of FEDs are masked */
		if (maskedFeds == allFeds) {
			masked = true;
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((crashed == null) ? 0 : crashed.hashCode());
		result = prime * result + ((hostname == null) ? 0 : hostname.hashCode());
		result = prime * result + (masked ? 1231 : 1237);
		result = prime * result + ((port == null) ? 0 : port.hashCode());
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
		FRLPc other = (FRLPc) obj;
		if (crashed == null) {
			if (other.crashed != null)
				return false;
		} else if (!crashed.equals(other.crashed))
			return false;
		if (hostname == null) {
			if (other.hostname != null)
				return false;
		} else if (!hostname.equals(other.hostname))
			return false;
		if (masked != other.masked)
			return false;
		if (port == null) {
			if (other.port != null)
				return false;
		} else if (!port.equals(other.port))
			return false;
		return true;
	}
	
	

}
