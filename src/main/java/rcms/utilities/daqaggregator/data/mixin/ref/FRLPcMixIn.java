package rcms.utilities.daqaggregator.data.mixin.ref;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import rcms.utilities.daqaggregator.data.FRL;

/**
 * Class configuring json serialization
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
@JsonIdentityInfo(generator = IdGenerators.FRLPcIdGenerator.class, property = "@id")
public interface FRLPcMixIn {

	@JsonProperty("ref_frls")
	@JsonIdentityReference(alwaysAsId = true)
	abstract List<FRL> getFrls();
}
