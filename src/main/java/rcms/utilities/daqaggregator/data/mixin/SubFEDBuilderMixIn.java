package rcms.utilities.daqaggregator.data.mixin;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;

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
@JsonIdentityInfo(generator = IdGenerators.ObjectUniqueIntIdGenerator.class, property = "@id")
public interface SubFEDBuilderMixIn {

	@JsonIdentityReference(alwaysAsId = true)
	abstract FEDBuilder getFedBuilder();

	@JsonIdentityReference(alwaysAsId = true)
	abstract TTCPartition getTtcPartition();

	@JsonIdentityReference(alwaysAsId = true)
	abstract FRLPc getFrlPc();

	@JsonIdentityReference(alwaysAsId = true)
	abstract List<FRL> getFrls();

}
