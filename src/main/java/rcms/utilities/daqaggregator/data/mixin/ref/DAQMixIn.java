package rcms.utilities.daqaggregator.data.mixin.ref;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import rcms.utilities.daqaggregator.data.BU;
import rcms.utilities.daqaggregator.data.BUSummary;
import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.data.FEDBuilderSummary;
import rcms.utilities.daqaggregator.data.FMMApplication;
import rcms.utilities.daqaggregator.data.FRLPc;
import rcms.utilities.daqaggregator.data.SubSystem;

/**
 * Class configuring json serialization
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
@JsonIdentityInfo(generator = IdGenerators.DAQIdGenerator.class, property = "@id")
@JsonPropertyOrder({ "sessionId", "runNumber", "lhcMachineMode", "lhcBeamMode", "daqState", "levelZeroState",
		"dpsetPath", "lastUpdate", "buSummary", "fedBuilderSummary", "subSystems", "ttcPartitions", "bus", "rus",
		"fmmApplications", "fmms", "fedBuilders", "subFEDBuilders", "frlPcs", "frls", "feds" })
public interface DAQMixIn {

	// @JsonIdentityReference(alwaysAsId = true)
	abstract Set<SubSystem> getSubSystems();

	// @JsonIdentityReference(alwaysAsId = true)
	abstract List<FMMApplication> getFmmApplications();

	// @JsonIdentityReference(alwaysAsId = true)
	abstract List<FEDBuilder> getFedBuilders();

	// @JsonIdentityReference(alwaysAsId = true)
	abstract List<FRLPc> getFrlPcs();

	// @JsonIdentityReference(alwaysAsId = true)
	abstract List<BU> getBus();

	// @JsonIdentityReference(alwaysAsId = true)
	abstract FEDBuilderSummary getFedBuilderSummary();

	// @JsonIdentityReference(alwaysAsId = true)
	abstract BUSummary getBuSummary();

}
