package rcms.utilities.daqaggregator.data.mixin;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;

import rcms.utilities.daqaggregator.data.FEDBuilder;

/**
 * Class configuring json serialization
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
@JsonIdentityInfo(generator = IdGenerators.RUIdGenerator.class, property = "@id")
public interface RUMixIn {

	@JsonIdentityReference(alwaysAsId = true)
	abstract FEDBuilder getFedBuilder();

}
