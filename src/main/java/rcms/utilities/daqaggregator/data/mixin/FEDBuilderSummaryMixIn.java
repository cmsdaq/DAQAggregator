package rcms.utilities.daqaggregator.data.mixin;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import rcms.utilities.daqaggregator.data.DAQ;

/**
 * Class configuring json serialization
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
@JsonIdentityInfo(generator = IdGenerators.FEDBuilderSummaryIdGenerator.class, property = "@id")
@JsonIgnoreProperties(ignoreUnknown = true)
public interface FEDBuilderSummaryMixIn {

	@JsonIdentityReference(alwaysAsId = true)
	abstract DAQ getDaq();
}
