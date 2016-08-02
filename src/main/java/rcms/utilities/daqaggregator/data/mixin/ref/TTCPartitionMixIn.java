package rcms.utilities.daqaggregator.data.mixin.ref;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

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

	@JsonProperty("ref_feds")
	@JsonIdentityReference(alwaysAsId = true)
	abstract List<FED> getFeds();

	@JsonProperty("ref_fmm")
	@JsonIdentityReference(alwaysAsId = true)
	abstract FMM getFmm();

	@JsonProperty("ref_subsystem")
	@JsonIdentityReference(alwaysAsId = true)
	abstract SubSystem getSubsystem();

}
