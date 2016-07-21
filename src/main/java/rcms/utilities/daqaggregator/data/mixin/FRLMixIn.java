package rcms.utilities.daqaggregator.data.mixin;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;

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

	@JsonIdentityReference(alwaysAsId = true)
	abstract SubFEDBuilder getSubFedbuilder();

	@JsonIdentityReference(alwaysAsId = true)
	abstract Map<Integer, FED> getFeds();

	@JsonIdentityReference(alwaysAsId = true)
	abstract FRLPc getFrlPc();

}
