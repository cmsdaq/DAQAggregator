package rcms.utilities.daqaggregator.reasoning;

import java.util.Date;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqaggregator.reasoning.base.Level;
import rcms.utilities.daqaggregator.reasoning.base.Condition;

public class WarningInTTCP implements Condition {
	private final static Logger logger = Logger.getLogger(WarningInTTCP.class);

	private String text =  "";
	
	@Override
	public Boolean satisfied(DAQ daq) {

		boolean result = false;
		
		for(TTCPartition ttcPartition: daq.getTtcPartitions()){

			if(ttcPartition.getPercentWarning() != 0F){
				result = true;
				logger.info("Warning identified: " + new Date(daq.getLastUpdate()) );
				text += ttcPartition.getName() + ": " + ttcPartition.getPercentWarning();
			}
		}
		

		return result;
	}

	@Override
	public Level getLevel() {
		return Level.Warning;
	}

	@Override
	public String getText() {
		return text;
	}

}
