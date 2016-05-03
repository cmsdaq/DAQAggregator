package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * 
 * Class representing one line in DAQView
 * 
 * @author Andre Georg Holzner (andre.georg.holzner@cern.ch)
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 * 
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class SubFEDBuilder implements java.io.Serializable {

	// ----------------------------------------
	// fields set at beginning of session
	// ----------------------------------------

	/** the 'parent' FEDBuilder */
	private FEDBuilder fedBuilder;

	private TTCPartition ttcPartition;

	/** can be null */
	private FRLPc frlPc;

	private final List<FRL> frls = new ArrayList<FRL>();

	// ----------------------------------------
	// fields updated periodically
	// ----------------------------------------

	private long minTrig, maxTrig;

	public long getMinTrig() {
		return minTrig;
	}

	public void setMinTrig(long minTrig) {
		this.minTrig = minTrig;
	}

	public long getMaxTrig() {
		return maxTrig;
	}

	public void setMaxTrig(long maxTrig) {
		this.maxTrig = maxTrig;
	}

	public FEDBuilder getFedBuilder() {
		return fedBuilder;
	}

	public TTCPartition getTtcPartition() {
		return ttcPartition;
	}

	public FRLPc getFrlPc() {
		return frlPc;
	}

	public List<FRL> getFrls() {
		return frls;
	}

	public void setFedBuilder(FEDBuilder fedBuilder) {
		this.fedBuilder = fedBuilder;
	}

	public void setTtcPartition(TTCPartition ttcPartition) {
		this.ttcPartition = ttcPartition;
	}

	public void setFrlPc(FRLPc frlPc) {
		this.frlPc = frlPc;
	}

	public void calculateDerived() {

		// just derived from feds
		for (FRL frl : getFrls()) {
			for (FED fed : frl.getFeds().values()) {
				if (fed.getEventCounter() > maxTrig) {
					maxTrig = fed.getEventCounter();
				}
				if (fed.getEventCounter() < minTrig) {
					minTrig = fed.getEventCounter();
				}
			}
		}
	}

}
