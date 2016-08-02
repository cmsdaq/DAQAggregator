package rcms.utilities.daqaggregator.data.mixin.ref;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import rcms.utilities.daqaggregator.data.TTCPartition;

/**
 * Class configuring json serialization
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
@JsonIdentityInfo(generator = IdGenerators.SubSystemIdGenerator.class, property = "@id")
public interface SubSystemMixIn {

	@JsonProperty("ref_ttcPartitions")
	@JsonIdentityReference(alwaysAsId = true)
	Set<TTCPartition> getTtcPartitions();

}
