package rcms.utilities.daqaggregator.data.mixin.ref;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FMM;
import rcms.utilities.daqaggregator.data.FRL;
import rcms.utilities.daqaggregator.data.TTCPartition;

/**
 * Class configuring json serialization
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
@JsonIdentityInfo(generator = IdGenerators.FEDIdGenerator.class, property = "@id")
public interface FEDMixIn {

	@JsonProperty("ref_frl")
	@JsonIdentityReference(alwaysAsId = true)
	public FRL getFrl();

	@JsonProperty("ref_fmm")
	@JsonIdentityReference(alwaysAsId = true)
	public FMM getFmm();

	@JsonProperty("ref_ttcp")
	@JsonIdentityReference(alwaysAsId = true)
	public TTCPartition getTtcp();

	@JsonProperty("ref_dependentFeds")
	@JsonIdentityReference(alwaysAsId = true)
	abstract List<FED> getDependentFeds();
}
