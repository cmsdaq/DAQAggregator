package rcms.utilities.daqaggregator.data.mixin;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;

/**
 * Class configuring json serialization
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
@JsonIdentityInfo(generator = IdGenerators.ObjectUniqueIntIdGenerator.class, property = "@id")
public interface FEDBuilderSummaryMixIn {

	// nothing to alter

}
