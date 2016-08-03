package rcms.utilities.daqaggregator.data.mixin.ref;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FRLPc;
import rcms.utilities.daqaggregator.data.SubFEDBuilder;

/**
 * Class configuring json serialization
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
@JsonIdentityInfo(generator = IdGenerators.FRLIdGenerator.class, property = "@id")
public interface FRLMixIn {
	
	@JsonProperty("ref_subFedbuilder")
	@JsonIdentityReference(alwaysAsId = true)
	abstract SubFEDBuilder getSubFedbuilder();

	@JsonProperty("ref_feds")
	@JsonIdentityReference(alwaysAsId = true)
	abstract Map<Integer, FED> getFeds();

	@JsonProperty("ref_frlPc")
	@JsonIdentityReference(alwaysAsId = true)
	abstract FRLPc getFrlPc();

}
