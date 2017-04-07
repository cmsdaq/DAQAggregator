package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.datasource.FlashlistType;
import rcms.utilities.daqaggregator.mappers.FlashlistUpdatable;

/**
 * Fast Merging Module Application
 * 
 * @author Andre Georg Holzner (andre.georg.holzner@cern.ch)
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
public class FMMApplication implements FlashlistUpdatable {

	// ----------------------------------------
	// fields set at beginning of session
	// ----------------------------------------

	private DAQ daq;

	private String hostname;
	
	private int port;

	private final List<FMM> fmms = new ArrayList<FMM>();

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

	public DAQ getDaq() {
		return daq;
	}

	public String getHostname() {
		return hostname;
	}

	public List<FMM> getFmms() {
		return fmms;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setDaq(DAQ daq) {
		this.daq = daq;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	@Override
	public String toString() {
		return "FMMApplication [hostname=" + hostname + "]";
	}

	@Override
	public void updateFromFlashlist(FlashlistType flashlistType, JsonNode flashlistRow) {

		if (flashlistType == FlashlistType.JOB_CONTROL) {
			JsonNode jobTable = flashlistRow.get("jobTable");

			JsonNode rows = jobTable.get("rows");
			this.crashed = false;
			for (JsonNode row : rows) {
				//TODO: get the row with matching jid to the context (additional field)
				String status = row.get("status").asText();

				// if not alive than crashed, if no data than default value
				// witch is not crashed
				if (!status.equalsIgnoreCase("alive"))
					this.crashed = true;

			}

		}
	}

	@Override
	public void clean() {
		this.crashed = null;
	}
	// ----------------------------------------------------------------------

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((crashed == null) ? 0 : crashed.hashCode());
		result = prime * result + ((hostname == null) ? 0 : hostname.hashCode());
		result = prime * result + port;
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
		FMMApplication other = (FMMApplication) obj;
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
		if (port != other.port)
			return false;
		return true;
	}
	

}
