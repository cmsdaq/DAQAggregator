package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;

public class FEDBuilder {

	//----------------------------------------
	// fields set at beginning of session
	//----------------------------------------
	
	/** parent */
	private final DAQ daq;
  
	private final List<SubFEDBuilder> subFedbuilders = new ArrayList<SubFEDBuilder>();
  
	/** the RU in this FEDBuilder */
	private final RU ru;
	
	//----------------------------------------------------------------------
		
	public FEDBuilder(DAQ daq, RU ru) {
		this.daq = daq;
		this.ru = ru;

		// TODO: fill subFedbuilders
	}
	
	//----------------------------------------------------------------------

}
