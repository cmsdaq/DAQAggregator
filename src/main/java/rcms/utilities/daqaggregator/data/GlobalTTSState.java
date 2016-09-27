package rcms.utilities.daqaggregator.data;


/**
 * Object with global TTS states (for one type) from the TCDS pm flashlist 
 * 
 * @author Michail Vougioukas (michail.vougioukas@cern.ch)
 */
public class GlobalTTSState {

	private String state;

	private float percentWarning;

	private float percentBusy;
	
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public float getPercentWarning() {
		return percentWarning;
	}

	public void setPercentWarning(float percentWarning) {
		this.percentWarning = percentWarning;
	}

	public float getPercentBusy() {
		return percentBusy;
	}

	public void setPercentBusy(float percentBusy) {
		this.percentBusy = percentBusy;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(percentBusy);
		result = prime * result + Float.floatToIntBits(percentWarning);
		result = prime * result + ((state == null) ? 0 : state.hashCode());
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
		GlobalTTSState other = (GlobalTTSState) obj;
		if (Float.floatToIntBits(percentBusy) != Float.floatToIntBits(other.percentBusy))
			return false;
		if (Float.floatToIntBits(percentWarning) != Float.floatToIntBits(other.percentWarning))
			return false;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "GlobalTTSState [state=" + state + ", percentWarning=" + percentWarning + ", percentBusy=" + percentBusy
				+ "]";
	}

	
	
	
	
	
}