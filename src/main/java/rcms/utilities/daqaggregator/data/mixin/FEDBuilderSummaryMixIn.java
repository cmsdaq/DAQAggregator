package rcms.utilities.daqaggregator.data.mixin;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;

import rcms.utilities.daqaggregator.data.DAQ;

/**
 * Class configuring json serialization
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
@JsonIdentityInfo(generator = IdGenerators.FEDBuilderSummaryIdGenerator.class, property = "@id")
public interface FEDBuilderSummaryMixIn {

	@JsonIdentityReference(alwaysAsId = true)
	abstract DAQ getDaq();
}
