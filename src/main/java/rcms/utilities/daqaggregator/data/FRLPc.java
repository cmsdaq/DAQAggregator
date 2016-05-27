package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.mappers.Derivable;
import rcms.utilities.daqaggregator.mappers.FlashlistType;
import rcms.utilities.daqaggregator.mappers.FlashlistUpdatable;

/**
 * Front-end Readout Link PC
 * 
 * @author Andre Georg Holzner (andre.georg.holzner@cern.ch)
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class FRLPc implements java.io.Serializable, FlashlistUpdatable, Derivable {

	// ----------------------------------------
	// fields set at beginning of session
	// ----------------------------------------

	private String hostname;

	private boolean masked;

	@JsonBackReference
	private List<FRL> frls = new ArrayList<FRL>();

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

	public boolean isMasked() {
		return masked;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
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

			for (JsonNode row : rows) {

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
		crashed = false;
	}

	@Override
	public void calculateDerivedValues() {

		masked = false;
		int maskedFeds = 0;
		int allFeds = 0;

		for (FRL frl : frls) {
			for (FED fed : frl.getFeds().values()) {
				allFeds++;
				if (fed.isFrlMasked()) {
					maskedFeds++;
				}
			}
		}

		/* FRLPc is mask if all of FEDs are masked */
		if (maskedFeds == allFeds) {
			masked = true;
		}

	}

}
