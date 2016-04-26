package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * Front End Driver Builder
 * 
 * @author Andre Georg Holzner (andre.georg.holzner@cern.ch)
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class FEDBuilder implements java.io.Serializable {

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

	// ----------------------------------------------------------------------

}
