package rcms.utilities.daqaggregator.data.mixin;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FMM;

/**
 * Class configuring json serialization
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
@JsonIdentityInfo(generator = IdGenerators.FMMApplicationIdGenerator.class, property = "@id")
public interface FMMApplicationMixIn {

	@JsonIdentityReference(alwaysAsId = true)
	abstract DAQ getDaq();

	@JsonIdentityReference(alwaysAsId = true)
	abstract List<FMM> getFmms();
}
