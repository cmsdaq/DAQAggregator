package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Class representing one line in DAQView
 * 
 * @author Andre Georg Holzner (andre.georg.holzner@cern.ch)
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 * 
 */
public class SubFEDBuilder {

	
	// ----------------------------------------
	// fields set at beginning of session
	// ----------------------------------------

	/** the 'parent' FEDBuilder */
	private FEDBuilder fedBuilder;

	private TTCPartition ttcPartition;

	/** can be null */
	private FRLPc frlPc;

	private final List<FRL> frls = new ArrayList<FRL>();
	
	/** only filled with pseudofeds, if applicable, but never with normal feds (these are reachable through the FRL(s)) */
	private final List<FED> feds = new ArrayList<FED>();

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
	

	public List<FED> getFeds() {
		return feds;
	}

	public void calculateDerived() {

		maxTrig = Long.MIN_VALUE;
		minTrig = Long.MAX_VALUE;

		// just derived from feds
		
		int total = 0;
		int masked = 0;
		for (FRL frl : getFrls()) {
			for (FED fed : frl.getFeds().values()) {
				total++;
				
				//do not take into account masked FEDs in this aggregation
				if (fed.isFrlMasked()){
					masked++;
					continue;
				}
				
				if (fed.getEventCounter() > maxTrig) {
					maxTrig = fed.getEventCounter();
				}
				if (fed.getEventCounter() < minTrig) {
					minTrig = fed.getEventCounter();
				}
			}
		}
		
		if (total == masked){
			maxTrig = -1;
			minTrig = -1;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fedBuilder == null) ? 0 : fedBuilder.hashCode());
		result = prime * result + ((feds == null) ? 0 : feds.hashCode());
		result = prime * result + ((frlPc == null) ? 0 : frlPc.hashCode());
		result = prime * result + ((frls == null) ? 0 : frls.hashCode());
		result = prime * result + (int) (maxTrig ^ (maxTrig >>> 32));
		result = prime * result + (int) (minTrig ^ (minTrig >>> 32));
		result = prime * result + ((ttcPartition == null) ? 0 : ttcPartition.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SubFEDBuilder other = (SubFEDBuilder) obj;
		if (fedBuilder == null) {
			if (other.fedBuilder != null)
				return false;
		} else if (!fedBuilder.equals(other.fedBuilder))
			return false;
		if (feds == null) {
			if (other.feds != null)
				return false;
		} else if (!feds.equals(other.feds))
			return false;
		if (frlPc == null) {
			if (other.frlPc != null)
				return false;
		} else if (!frlPc.equals(other.frlPc))
			return false;
		if (frls == null) {
			if (other.frls != null)
				return false;
		} else if (!frls.equals(other.frls))
			return false;
		if (maxTrig != other.maxTrig)
			return false;
		if (minTrig != other.minTrig)
			return false;
		if (ttcPartition == null) {
			if (other.ttcPartition != null)
				return false;
		} else if (!ttcPartition.equals(other.ttcPartition))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SubFEDBuilder [fedBuilder=" + fedBuilder + ", ttcPartition=" + ttcPartition + ", frlPc=" + frlPc
				+ ", frls=" + frls + ", (pseudo)feds=" + feds + ", minTrig=" + minTrig + ", maxTrig=" + maxTrig + "]";
	}
	

	
}
