package rcms.utilities.daqaggregator.data.mixin.ref;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import rcms.utilities.daqaggregator.data.DAQ;

/**
 * Class configuring json serialization
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
@JsonIdentityInfo(generator = IdGenerators.BUSummaryIdGenerator.class, property = "@id")
public interface BUSummaryMixIn {

	@JsonProperty("ref_daq")
	@JsonIdentityReference(alwaysAsId = true)
	abstract DAQ getDaq();

}
