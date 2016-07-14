package rcms.utilities.daqaggregator.data.mixin;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;

import rcms.utilities.daqaggregator.data.TTCPartition;

/**
 * Class configuring json serialization
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
@JsonIdentityInfo(generator = IdGenerators.ObjectUniqueIntIdGenerator.class, property = "@id")
public interface SubSystemMixIn {

	@JsonIdentityReference(alwaysAsId = true)
	Set<TTCPartition> getTtcPartitions();

}
