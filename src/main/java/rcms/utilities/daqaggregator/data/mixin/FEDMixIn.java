package rcms.utilities.daqaggregator.data.mixin;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;

import rcms.utilities.daqaggregator.data.FMM;
import rcms.utilities.daqaggregator.data.FRL;
import rcms.utilities.daqaggregator.data.TTCPartition;

/**
 * Class configuring json serialization
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
@JsonIdentityInfo(generator = IdGenerators.ObjectUniqueIntIdGenerator.class, property = "@id")
public interface FEDMixIn {

	@JsonIdentityReference(alwaysAsId = true)
	public FRL getFrl();

	@JsonIdentityReference(alwaysAsId = true)
	public FMM getFmm();

	@JsonIdentityReference(alwaysAsId = true)
	public TTCPartition getTtcp();

}
