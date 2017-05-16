package rcms.utilities.daqaggregator.data;

import rcms.utilities.daqaggregator.mappers.Derivable;

/**
 * Summary statistics of FED builders
 * 
 * @author Andre Georg Holzner (andre.georg.holzner@cern.ch)
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */

public class FEDBuilderSummary implements Derivable {

	// ----------------------------------------
	// fields set at beginning of session
	// ----------------------------------------

	/** parent */
	private DAQ daq;

	// ----------------------------------------
	// fields updated periodically
	// ----------------------------------------

	/** event building rate in kHz */
	private float rate;

	/** throughput in MByte/s ? */
	private float throughput;

	/** mean superfragment size in kByte ? */
	private double superFragmentSizeMean;

	/** spread of superfragment size in kByte ? */
	private double superFragmentSizeStddev;

	private long deltaEvents;

	private int sumFragmentsInRU;

	private int sumEventsInRU;

	/** requests from BUs ? */
	private long sumRequests;

	// ----------------------------------------------------------------------

	// ----------------------------------------------------------------------

	public float getRate() {
		return rate;
	}

	public void setRate(float rate) {
		this.rate = rate;
	}

	public float getThroughput() {
		return throughput;
	}

	public void setThroughput(float throughput) {
		this.throughput = throughput;
	}

	public double getSuperFragmentSizeMean() {
		return superFragmentSizeMean;
	}

	public void setSuperFragmentSizeMean(double superFragmentSizeMean) {
		this.superFragmentSizeMean = superFragmentSizeMean;
	}

	public double getSuperFragmentSizeStddev() {
		return superFragmentSizeStddev;
	}

	public void setSuperFragmentSizeStddev(double superFragmentSizeStddev) {
		this.superFragmentSizeStddev = superFragmentSizeStddev;
	}

	public long getDeltaEvents() {
		return deltaEvents;
	}

	public void setDeltaEvents(long deltaEvents) {
		this.deltaEvents = deltaEvents;
	}

	public int getSumFragmentsInRU() {
		return sumFragmentsInRU;
	}

	public void setSumFragmentsInRU(int sumFragmentsInRU) {
		this.sumFragmentsInRU = sumFragmentsInRU;
	}

	public int getSumEventsInRU() {
		return sumEventsInRU;
	}

	public void setSumEventsInRU(int sumEventsInRU) {
		this.sumEventsInRU = sumEventsInRU;
	}

	public long getSumRequests() {
		return sumRequests;
	}

	public void setSumRequests(long sumRequests) {
		this.sumRequests = sumRequests;
	}

	public DAQ getDaq() {
		return daq;
	}

	public void setDaq(DAQ daq) {
		this.daq = daq;
	}

	// ----------------------------------------------------------------------

	@Override
	public void calculateDerivedValues() {
		this.setDaq(daq);
		int numberOfRus = daq.getFedBuilders().size();

		/* delta between min and max (min not 0) */
		long maxEvents = Long.MIN_VALUE;
		long minEvents = Long.MAX_VALUE;

		/* Averages */
		double superFragmentSizeMean = 0;
		double superFragmentSizeStddev = 0;
		float rate = 0;

		/* Sums */
		float throughput = 0;
		int sumEventsInRU = 0;
		int sumFragmentsInRU = 0;
		long sumRequests = 0;

		for (FEDBuilder fb : daq.getFedBuilders()) {
			RU ru = fb.getRu();
			
			/* do not take into account masked rus*/
			if (ru.isMasked()){
				continue;
			}
		
			/* average event building rate can be taken from the EVM as this already holds an average*/ 
			if (ru.isEVM()){
				rate = ru.getRate();
			}
			
			sumEventsInRU += ru.getEventsInRU();
			sumFragmentsInRU += ru.getFragmentsInRU();
			sumRequests += ru.getRequests();
			superFragmentSizeMean += ru.getSuperFragmentSizeMean();
			superFragmentSizeStddev += Math.pow(ru.getSuperFragmentSizeStddev(),2);
			throughput += ru.getThroughput();

			
			if (maxEvents < ru.getEventCount())
				maxEvents = ru.getEventCount();
			if (minEvents > ru.getEventCount()) {
				minEvents = ru.getEventCount();
			}
		}

		/* average values */
		
		//we do not average the superFragmentSizeMean, because we need the sum of RU sizes in the summary
		superFragmentSizeStddev = Math.sqrt(superFragmentSizeStddev);
		
		/* deltas */

		this.setDeltaEvents(maxEvents - minEvents);
		this.setRate(rate);
		this.setSumEventsInRU(sumEventsInRU);
		this.setSumFragmentsInRU(sumFragmentsInRU);
		this.setSumRequests(sumRequests);
		this.setSuperFragmentSizeMean(superFragmentSizeMean);
		this.setSuperFragmentSizeStddev(superFragmentSizeStddev);
		this.setThroughput(throughput);

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (deltaEvents ^ (deltaEvents >>> 32));
		result = prime * result + Float.floatToIntBits(rate);
		result = prime * result + sumEventsInRU;
		result = prime * result + sumFragmentsInRU;
		result = prime * result + (int) (sumRequests ^ (sumRequests >>> 32));
		long temp;
		temp = Double.doubleToLongBits(superFragmentSizeMean);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(superFragmentSizeStddev);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + Float.floatToIntBits(throughput);
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
		FEDBuilderSummary other = (FEDBuilderSummary) obj;
		if (deltaEvents != other.deltaEvents)
			return false;
		if (Float.floatToIntBits(rate) != Float.floatToIntBits(other.rate))
			return false;
		if (sumEventsInRU != other.sumEventsInRU)
			return false;
		if (sumFragmentsInRU != other.sumFragmentsInRU)
			return false;
		if (sumRequests != other.sumRequests)
			return false;
		if (Double.doubleToLongBits(superFragmentSizeMean) != Double.doubleToLongBits(other.superFragmentSizeMean))
			return false;
		if (Double.doubleToLongBits(superFragmentSizeStddev) != Double.doubleToLongBits(other.superFragmentSizeStddev))
			return false;
		if (Float.floatToIntBits(throughput) != Float.floatToIntBits(other.throughput))
			return false;
		return true;
	}




}
