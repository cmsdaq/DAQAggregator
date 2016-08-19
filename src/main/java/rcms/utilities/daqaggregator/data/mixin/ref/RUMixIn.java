package rcms.utilities.daqaggregator.data.mixin.ref;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import rcms.utilities.daqaggregator.data.FEDBuilder;

/**
 * Class configuring json serialization
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
@JsonIdentityInfo(generator = IdGenerators.RUIdGenerator.class, property = "@id")
public interface RUMixIn {

	@JsonProperty("ref_fedBuilder")
	@JsonIdentityReference(alwaysAsId = true)
	abstract FEDBuilder getFedBuilder();
	
	@JsonProperty("isEVM")
	abstract boolean isEVM();

}
