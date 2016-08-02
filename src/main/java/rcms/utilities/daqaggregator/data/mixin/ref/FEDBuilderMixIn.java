package rcms.utilities.daqaggregator.data.mixin.ref;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FRL;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.data.SubFEDBuilder;

/**
 * Class configuring json serialization
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
@JsonIdentityInfo(generator = IdGenerators.FEDBuilderIdGenerator.class, property = "@id")
public interface FEDBuilderMixIn {

	@JsonProperty("ref_subFedbuilders")
	@JsonIdentityReference(alwaysAsId = true)
	public List<SubFEDBuilder> getSubFedbuilders();

	@JsonProperty("ref_ru")
	@JsonIdentityReference(alwaysAsId = true)
	public RU getRu();
	
	@JsonProperty("ref_daq")
	@JsonIdentityReference(alwaysAsId = true)
	public DAQ getDaq();
	
}
