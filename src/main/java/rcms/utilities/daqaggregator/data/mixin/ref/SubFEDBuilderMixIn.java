package rcms.utilities.daqaggregator.data.mixin.ref;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.data.FRL;
import rcms.utilities.daqaggregator.data.FRLPc;
import rcms.utilities.daqaggregator.data.TTCPartition;

/**
 * Class configuring json serialization
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
@JsonIdentityInfo(generator = IdGenerators.SubFEDBuilderIdGenerator.class, property = "@id")
public interface SubFEDBuilderMixIn {

	@JsonProperty("ref_fedBuilder")
	@JsonIdentityReference(alwaysAsId = true)
	abstract FEDBuilder getFedBuilder();

	@JsonProperty("ref_ttcPartition")
	@JsonIdentityReference(alwaysAsId = true)
	abstract TTCPartition getTtcPartition();

	@JsonProperty("ref_frlPc")
	@JsonIdentityReference(alwaysAsId = true)
	abstract FRLPc getFrlPc();

	@JsonProperty("ref_frls")
	@JsonIdentityReference(alwaysAsId = true)
	abstract List<FRL> getFrls();

}
