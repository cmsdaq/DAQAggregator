package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Front End Driver Builder
 * 
 * @author Andre Georg Holzner (andre.georg.holzner@cern.ch)
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
public class FEDBuilder {

	// ----------------------------------------
	// fields set at beginning of session
	// ----------------------------------------

	/* not in the structure but added tmprly */
	private String name;

	/** parent */
	private DAQ daq;

	private final List<SubFEDBuilder> subFedbuilders = new ArrayList<SubFEDBuilder>();

	/** the RU in this FEDBuilder */
	private RU ru;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DAQ getDaq() {
		return daq;
	}

	public List<SubFEDBuilder> getSubFedbuilders() {
		return subFedbuilders;
	}

	public RU getRu() {
		return ru;
	}

	public void setDaq(DAQ daq) {
		this.daq = daq;
	}

	public void setRu(RU ru) {
		this.ru = ru;
	}

	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		FEDBuilder other = (FEDBuilder) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FEDBuilder [name=" + name + ", daq=" + daq + ", subFedbuilders=" + subFedbuilders + ", ru=" + ru + "]";
	}

	// ----------------------------------------------------------------------

}
