package rcms.utilities.daqaggregator.data.mixin;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FMMApplication;
import rcms.utilities.daqaggregator.data.TTCPartition;

/**
 * Class configuring json serialization
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
@JsonIdentityInfo(generator = IdGenerators.FMMIdGenerator.class, property = "@id")
public interface FMMMixIn {

	@JsonIdentityReference(alwaysAsId = true)
	abstract TTCPartition getTtcPartition();

	@JsonIdentityReference(alwaysAsId = true)
	abstract FMMApplication getFmmApplication();

	@JsonIdentityReference(alwaysAsId = true)
	abstract List<FED> getFeds();
	
	@JsonIgnore
	abstract boolean isTakeB();

}
