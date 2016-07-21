package rcms.utilities.daqaggregator.data.mixin;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;

import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FMM;
import rcms.utilities.daqaggregator.data.SubSystem;

/**
 * Class configuring json serialization
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
@JsonIdentityInfo(generator = IdGenerators.TTCPartitionIdGenerator.class, property = "@id")
public interface TTCPartitionMixIn {

	@JsonIdentityReference(alwaysAsId = true)
	abstract List<FED> getFeds();

	@JsonIdentityReference(alwaysAsId = true)
	abstract FMM getFmm();

	@JsonIdentityReference(alwaysAsId = true)
	abstract SubSystem getSubsystem();

}
