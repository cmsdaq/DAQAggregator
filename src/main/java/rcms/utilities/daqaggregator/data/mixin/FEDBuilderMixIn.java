package rcms.utilities.daqaggregator.data.mixin;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;

import rcms.utilities.daqaggregator.data.SubFEDBuilder;

/**
 * Class configuring json serialization
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
@JsonIdentityInfo(generator = IdGenerators.FEDBuilderIdGenerator.class, property = "@id")
public interface FEDBuilderMixIn {

	@JsonIdentityReference(alwaysAsId = true)
	public List<SubFEDBuilder> getSubFedbuilders();

}
