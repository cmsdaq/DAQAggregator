package rcms.utilities.daqaggregator;

import rcms.utilities.daqaggregator.data.BUSummary;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FEDBuilderSummary;

public class DummyDAQ {
	private long lastUpdate;
	private int value;
	
	public DummyDAQ(){
		
	}

	
	public void setValue(int value) {
		this.value = value;
	}


	public DummyDAQ(DAQ daq){
		this.value = (int) daq.getFedBuilderSummary().getRate();
		this.lastUpdate = daq.getLastUpdate();
	}


	public long getLastUpdate() {
		return lastUpdate;
	}


	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}


	public int getValue() {
		return this.value;
	}

}
