package rcms.utilities.daqaggregator.data;


import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.datasource.FlashlistType;
import rcms.utilities.daqaggregator.mappers.Derivable;
import rcms.utilities.daqaggregator.mappers.FlashlistUpdatable;

/**
 * Readout Unit
 * 
 * @author Andre Georg Holzner (andre.georg.holzner@cern.ch)
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 * @author Michail Vougioukas (michail.vougioukas@cern.ch)
 */

public class RU implements FlashlistUpdatable, Derivable {

	// ----------------------------------------
	// fields set at beginning of session
	// ----------------------------------------

	/** the FEDbuilder this RU corresponds to */
	private FEDBuilder fedBuilder;

	private String hostname;
	
	private Integer port;

	private boolean isEVM;

	private Boolean masked;

	private int instance;

	// ----------------------------------------
	// fields updated periodically
	// ----------------------------------------
	
	private Boolean crashed;

	private String stateName;

	private String errorMsg;

	private String warnMsg;

	private String infoMsg;

	/** events rate in kHz ? */
	private Float rate;

	/** MByte per second ? */
	private Float throughput;

	/**
	 * TODO: mean superfragment size in kByte?, TODO: mean over what period ?
	 */
	private Float superFragmentSizeMean;

	/** spread of superfragment size in kByte ? */
	private Float superFragmentSizeStddev;

	/** total rate of fragment messages going to the BUs */
	private Integer fragmentsInRU;

	private Integer eventsInRU;

	private Long eventCount;

	/** requests from BUs */
	private Integer requests;

	private Integer incompleteSuperFragmentCount;
	
	/** per-BU lists, all subject to the same order of BUs */
	private List<Long> throughputPerBU;
	
	private List<Integer> buTids;
	
	private List<Integer> fragmentRatePerBU;
	
	private List<Double> retryRatePerBU;

	/** only for the EVM case */
	private Integer allocateRate;
	
	private Double allocateRetryRate;
	
	public Boolean isCrashed() {
		return crashed;
	}

	public void setCrashed(Boolean crashed) {
		this.crashed = crashed;
	}

	public String getStateName() {
		return stateName;
	}

	public void setStateName(String stateName) {
		this.stateName = stateName;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public String getWarnMsg() {
		return warnMsg;
	}

	public void setWarnMsg(String warnMsg) {
		this.warnMsg = warnMsg;
	}

	public String getInfoMsg() {
		return infoMsg;
	}

	public void setInfoMsg(String infoMsg) {
		this.infoMsg = infoMsg;
	}

	public Float getRate() {
		return rate;
	}

	public void setRate(Float rate) {
		this.rate = rate;
	}

	public Float getThroughput() {
		return throughput;
	}

	public void setThroughput(Float throughput) {
		this.throughput = throughput;
	}

	public Float getSuperFragmentSizeMean() {
		return superFragmentSizeMean;
	}

	public void setSuperFragmentSizeMean(Float superFragmentSizeMean) {
		this.superFragmentSizeMean = superFragmentSizeMean;
	}

	public Float getSuperFragmentSizeStddev() {
		return superFragmentSizeStddev;
	}

	public void setSuperFragmentSizeStddev(Float superFragmentSizeStddev) {
		this.superFragmentSizeStddev = superFragmentSizeStddev;
	}

	public Integer getFragmentsInRU() {
		return fragmentsInRU;
	}

	public void setFragmentsInRU(Integer fragmentsInRU) {
		this.fragmentsInRU = fragmentsInRU;
	}

	public Integer getEventsInRU() {
		return eventsInRU;
	}

	public void setEventsInRU(Integer eventsInRU) {
		this.eventsInRU = eventsInRU;
	}

	public Long getEventCount() {
		return eventCount;
	}

	public void setEventCount(Long eventCount) {
		this.eventCount = eventCount;
	}

	public Integer getRequests() {
		return requests;
	}

	public void setRequests(Integer requests) {
		this.requests = requests;
	}

	public FEDBuilder getFedBuilder() {
		return fedBuilder;
	}

	public String getHostname() {
		return hostname;
	}

	public boolean isEVM() {
		return isEVM;
	}

	public Boolean isMasked() {
		return masked;
	}

	public void setFedBuilder(FEDBuilder fedBuilder) {
		this.fedBuilder = fedBuilder;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public void setEVM(boolean isEVM) {
		this.isEVM = isEVM;
	}

	public void setMasked(Boolean masked) {
		this.masked = masked;
	}

	public int getInstance() {
		return instance;
	}

	public void setInstance(int instance) {
		this.instance = instance;
	}

	public List<Long> getThroughputPerBU() {
		return throughputPerBU;
	}

	public void setThroughputPerBU(List<Long> throughputPerBU) {
		this.throughputPerBU = throughputPerBU;
	}

	public List<Integer> getBuTids() {
		return buTids;
	}

	public void setBuTids(List<Integer> buTids) {
		this.buTids = buTids;
	}

	public List<Integer> getFragmentRatePerBU() {
		return fragmentRatePerBU;
	}

	public void setFragmentRatePerBU(List<Integer> fragmentRatePerBU) {
		this.fragmentRatePerBU = fragmentRatePerBU;
	}

	public List<Double> getRetryRatePerBU() {
		return retryRatePerBU;
	}

	public void setRetryRatePerBU(List<Double> retryRatePerBU) {
		this.retryRatePerBU = retryRatePerBU;
	}

	public Integer getAllocateRate() {
		return allocateRate;
	}

	public void setAllocateRate(Integer allocateRate) {
		this.allocateRate = allocateRate;
	}

	public Double getAllocateRetryRate() {
		return allocateRetryRate;
	}

	public void setAllocateRetryRate(Double allocateRetryRate) {
		this.allocateRetryRate = allocateRetryRate;
	}

	@Override
	public String toString() {
		return "RU [rate=" + rate + ", throughput=" + throughput + ", superFragmentSizeMean=" + superFragmentSizeMean
				+ ", superFragmentSizeStddev=" + superFragmentSizeStddev + ", fragmentsInRU=" + fragmentsInRU
				+ ", eventsInRU=" + eventsInRU + ", eventCount=" + eventCount + "]";
	}


	public Integer getIncompleteSuperFragmentCount() {
		return incompleteSuperFragmentCount;
	}

	public void setIncompleteSuperFragmentCount(Integer incompleteSuperFragmentCount) {
		this.incompleteSuperFragmentCount = incompleteSuperFragmentCount;
	}

	@Override
	public void calculateDerivedValues() {

		masked = false;
		int maskedFeds = 0;
		int allFeds = 0;

		//this will iterate over FEDs which are not pseudoFEDs, so no need to check if they have SLINK
		for (SubFEDBuilder subFedBuilder : fedBuilder.getSubFedbuilders()) {
			for (FRL frl : subFedBuilder.getFrls()) {
				for (FED fed : frl.getFeds().values()) {
					allFeds++;
					if (fed.isFrlMasked()==null) {
						maskedFeds++;
					}else{
						if (fed.isFrlMasked()) {
							maskedFeds++;
						}
					}
				}
			}
		}

		/* Ru is masked if all FEDs are masked */
		if (maskedFeds == allFeds) {
			masked = true;
		}

	}

	/**
	 * Update object based on given flashlist fragment
	 * 
	 * @param flashlistRow
	 *            JsonNode representing one row from flashlist
	 */
	@Override
	public void updateFromFlashlist(FlashlistType flashlistType, JsonNode flashlistRow) {
		
		/* ignore data from RU flashlist for EVM */
		if (isEVM && flashlistType == FlashlistType.RU)
			return;

		/**
		 * For dispatching Flashlist RU to RU objects and flashist EVM to EVM
		 * objects see {@link FlashlistDispatcher}
		 */
		if (flashlistType == FlashlistType.RU || flashlistType == FlashlistType.EVM) {
			// direct values

			this.setStateName(flashlistRow.get("stateName").asText());
			this.setErrorMsg(flashlistRow.get("errorMsg").asText());
			this.requests = flashlistRow.get("activeRequests").asInt();
			this.port = Integer.parseInt(flashlistRow.get("context").asText().split(":")[2]);
			this.rate = (float)flashlistRow.get("eventRate").asDouble();
			this.eventsInRU = flashlistRow.get("eventsInRU").asInt();
			this.eventCount = flashlistRow.get("eventCount").asLong();
			this.superFragmentSizeMean = (float)flashlistRow.get("superFragmentSize").asDouble();
			this.superFragmentSizeStddev = (float)flashlistRow.get("superFragmentSizeStdDev").asDouble();
			this.incompleteSuperFragmentCount = flashlistRow.get("incompleteSuperFragmentCount").asInt();
			this.fragmentsInRU = flashlistRow.get("incompleteSuperFragmentCount").asInt();

			// derived values
			this.throughput = rate * superFragmentSizeMean;
			
			// lists of values per BU
			
			this.throughputPerBU = new ArrayList<Long>();
			this.buTids = new ArrayList<Integer>();
			this.fragmentRatePerBU = new ArrayList<Integer>();
			this.retryRatePerBU = new ArrayList<Double>();
			
			JsonNode flashlist_tempArray;
			int size;
			
			flashlist_tempArray = flashlistRow.get("throughputPerBU");
			size = flashlist_tempArray.size();
			for (int j=0;j<size;j++){
				this.throughputPerBU.add(flashlist_tempArray.get(j).asLong());
			}
			
			flashlist_tempArray = flashlistRow.get("buTids");
			size = flashlist_tempArray.size();
			for (int j=0;j<size;j++){
				this.buTids.add(flashlist_tempArray.get(j).asInt());
			}
			
			flashlist_tempArray = flashlistRow.get("fragmentRatePerBU");
			size = flashlist_tempArray.size();
			for (int j=0;j<size;j++){
				this.fragmentRatePerBU.add(flashlist_tempArray.get(j).asInt());
			}
			
			flashlist_tempArray = flashlistRow.get("retryRatePerBU");
			size = flashlist_tempArray.size();
			for (int j=0;j<size;j++){
				this.retryRatePerBU.add(flashlist_tempArray.get(j).asDouble());
			}
			
			// values set only when RU is of type EVM
			if (flashlistType == FlashlistType.EVM){
				this.allocateRate = flashlistRow.get("allocateRate").asInt();
				this.allocateRetryRate = flashlistRow.get("allocateRate").asDouble();
			}
		}
		
		if (flashlistType == FlashlistType.JOB_CONTROL) {
			JsonNode jobTable = flashlistRow.get("jobTable");

			JsonNode rows = jobTable.get("rows");
			this.crashed = false;
			for (JsonNode row : rows) {
				//TODO: get the row with matching jid to the context (additional field)
				String status = row.get("status").asText();

				// if not alive than crashed, if no data than default value
				// witch is not crashed
				if (!status.equalsIgnoreCase("alive"))
					this.crashed = true;

			}

		}
	}


	@Override
	public void clean() {
		
		this.setStateName(null);
		this.setErrorMsg(null);
		this.requests = null;
		this.port = null;
		this.rate = null;
		this.eventsInRU = null;
		this.eventCount = null;
		this.superFragmentSizeMean = null;
		this.superFragmentSizeStddev = null;
		this.incompleteSuperFragmentCount = null;
		this.fragmentsInRU = null;
		
		this.throughput = null;
		
		// values per BU
		this.throughputPerBU = new ArrayList<Long>();
		this.buTids = new ArrayList<Integer>();
		this.fragmentRatePerBU = new ArrayList<Integer>();
		this.retryRatePerBU = new ArrayList<Double>();

		
		this.allocateRate = null;
		this.allocateRetryRate = null;
			
		this.crashed = null;
		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((allocateRate == null) ? 0 : allocateRate.hashCode());
		result = prime * result + ((allocateRetryRate == null) ? 0 : allocateRetryRate.hashCode());
		result = prime * result + ((buTids == null) ? 0 : buTids.hashCode());
		result = prime * result + ((crashed == null) ? 0 : crashed.hashCode());
		result = prime * result + ((errorMsg == null) ? 0 : errorMsg.hashCode());
		result = prime * result + ((eventCount == null) ? 0 : eventCount.hashCode());
		result = prime * result + ((eventsInRU == null) ? 0 : eventsInRU.hashCode());
		result = prime * result + ((fragmentRatePerBU == null) ? 0 : fragmentRatePerBU.hashCode());
		result = prime * result + ((fragmentsInRU == null) ? 0 : fragmentsInRU.hashCode());
		result = prime * result + ((hostname == null) ? 0 : hostname.hashCode());
		result = prime * result
				+ ((incompleteSuperFragmentCount == null) ? 0 : incompleteSuperFragmentCount.hashCode());
		result = prime * result + ((infoMsg == null) ? 0 : infoMsg.hashCode());
		result = prime * result + instance;
		result = prime * result + (isEVM ? 1231 : 1237);
		result = prime * result + ((masked == null) ? 0 : masked.hashCode());
		result = prime * result + ((port == null) ? 0 : port.hashCode());
		result = prime * result + ((rate == null) ? 0 : rate.hashCode());
		result = prime * result + ((requests == null) ? 0 : requests.hashCode());
		result = prime * result + ((retryRatePerBU == null) ? 0 : retryRatePerBU.hashCode());
		result = prime * result + ((stateName == null) ? 0 : stateName.hashCode());
		result = prime * result + ((superFragmentSizeMean == null) ? 0 : superFragmentSizeMean.hashCode());
		result = prime * result + ((superFragmentSizeStddev == null) ? 0 : superFragmentSizeStddev.hashCode());
		result = prime * result + ((throughput == null) ? 0 : throughput.hashCode());
		result = prime * result + ((throughputPerBU == null) ? 0 : throughputPerBU.hashCode());
		result = prime * result + ((warnMsg == null) ? 0 : warnMsg.hashCode());
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
		RU other = (RU) obj;
		if (allocateRate == null) {
			if (other.allocateRate != null)
				return false;
		} else if (!allocateRate.equals(other.allocateRate))
			return false;
		if (allocateRetryRate == null) {
			if (other.allocateRetryRate != null)
				return false;
		} else if (!allocateRetryRate.equals(other.allocateRetryRate))
			return false;
		if (buTids == null) {
			if (other.buTids != null)
				return false;
		} else if (!buTids.equals(other.buTids))
			return false;
		if (crashed == null) {
			if (other.crashed != null)
				return false;
		} else if (!crashed.equals(other.crashed))
			return false;
		if (errorMsg == null) {
			if (other.errorMsg != null)
				return false;
		} else if (!errorMsg.equals(other.errorMsg))
			return false;
		if (eventCount == null) {
			if (other.eventCount != null)
				return false;
		} else if (!eventCount.equals(other.eventCount))
			return false;
		if (eventsInRU == null) {
			if (other.eventsInRU != null)
				return false;
		} else if (!eventsInRU.equals(other.eventsInRU))
			return false;
		if (fragmentRatePerBU == null) {
			if (other.fragmentRatePerBU != null)
				return false;
		} else if (!fragmentRatePerBU.equals(other.fragmentRatePerBU))
			return false;
		if (fragmentsInRU == null) {
			if (other.fragmentsInRU != null)
				return false;
		} else if (!fragmentsInRU.equals(other.fragmentsInRU))
			return false;
		if (hostname == null) {
			if (other.hostname != null)
				return false;
		} else if (!hostname.equals(other.hostname))
			return false;
		if (incompleteSuperFragmentCount == null) {
			if (other.incompleteSuperFragmentCount != null)
				return false;
		} else if (!incompleteSuperFragmentCount.equals(other.incompleteSuperFragmentCount))
			return false;
		if (infoMsg == null) {
			if (other.infoMsg != null)
				return false;
		} else if (!infoMsg.equals(other.infoMsg))
			return false;
		if (instance != other.instance)
			return false;
		if (isEVM != other.isEVM)
			return false;
		if (masked == null) {
			if (other.masked != null)
				return false;
		} else if (!masked.equals(other.masked))
			return false;
		if (port == null) {
			if (other.port != null)
				return false;
		} else if (!port.equals(other.port))
			return false;
		if (rate == null) {
			if (other.rate != null)
				return false;
		} else if (!rate.equals(other.rate))
			return false;
		if (requests == null) {
			if (other.requests != null)
				return false;
		} else if (!requests.equals(other.requests))
			return false;
		if (retryRatePerBU == null) {
			if (other.retryRatePerBU != null)
				return false;
		} else if (!retryRatePerBU.equals(other.retryRatePerBU))
			return false;
		if (stateName == null) {
			if (other.stateName != null)
				return false;
		} else if (!stateName.equals(other.stateName))
			return false;
		if (superFragmentSizeMean == null) {
			if (other.superFragmentSizeMean != null)
				return false;
		} else if (!superFragmentSizeMean.equals(other.superFragmentSizeMean))
			return false;
		if (superFragmentSizeStddev == null) {
			if (other.superFragmentSizeStddev != null)
				return false;
		} else if (!superFragmentSizeStddev.equals(other.superFragmentSizeStddev))
			return false;
		if (throughput == null) {
			if (other.throughput != null)
				return false;
		} else if (!throughput.equals(other.throughput))
			return false;
		if (throughputPerBU == null) {
			if (other.throughputPerBU != null)
				return false;
		} else if (!throughputPerBU.equals(other.throughputPerBU))
			return false;
		if (warnMsg == null) {
			if (other.warnMsg != null)
				return false;
		} else if (!warnMsg.equals(other.warnMsg))
			return false;
		return true;
	}
	
	
}
