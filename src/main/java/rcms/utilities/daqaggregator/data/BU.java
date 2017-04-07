package rcms.utilities.daqaggregator.data;

import java.util.Comparator;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.datasource.FlashlistType;
import rcms.utilities.daqaggregator.mappers.FlashlistUpdatable;

/**
 * Builder Unit
 * 
 * @author Andre Georg Holzner (andre.georg.holzner@cern.ch)
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class BU implements FlashlistUpdatable {

	// ----------------------------------------
	// fields set at beginning of session
	// ----------------------------------------

	/** parent */
	private DAQ daq;

	private String hostname;
	
	private Integer port;

	// ----------------------------------------
	// fields updated periodically
	// ----------------------------------------

	private Boolean crashed;
	
	private String stateName;

	private String errorMsg;

	/** event rate in Hz */
	private Long rate;

	/** throughput in Byte/s */
	private Long throughput;

	/** in Byte */
	private Integer eventSizeMean;
	private Integer eventSizeStddev;

	/** events processed */
	private Long numEvents;

	private Long numEventsInBU;

	private Integer priority;

	private Integer numRequestsSent;

	private Integer numRequestsUsed;

	private Integer numRequestsBlocked;

	private Integer numFUsHLT;

	private Integer numFUsCrashed;

	private Integer numFUsStale;

	private Integer numFUsCloud;

	/** in percent */
	private Double ramDiskUsage;

	/** total amount of ramdisk in GB */
	private Double ramDiskTotal;

	/** total number of files written */
	private Integer numFiles;

	private Integer numLumisectionsWithFiles;

	private Integer currentLumisection;

	private Integer numLumisectionsForHLT;

	private Integer numLumisectionsOutHLT;

	private Double fuOutputBandwidthInMB;

	private Integer fragmentCount;

	private Integer nbCorruptedEvents;

	private Integer nbEventsMissingData;

	private Integer nbEventsWithCRCerrors;

	private Integer nbTotalResources;

	private Integer requestRate;

	private Double requestRetryRate;

	private Integer slowestRUtid;

	// ----------------------------------------------------------------------

	/**
	 * Update object based on given flashlist fragment
	 * 
	 * @param flashlistRow
	 *            JsonNode representing one row from flashlist
	 */
	@Override
	public void updateFromFlashlist(FlashlistType flashlistType, JsonNode flashlistRow) {

		if (flashlistType == FlashlistType.BU) {

			// direct values
			this.stateName = flashlistRow.get("stateName").asText();
			this.port = Integer.parseInt(flashlistRow.get("context").asText().split(":")[2]);
			this.errorMsg = flashlistRow.get("errorMsg").asText();
			this.rate = flashlistRow.get("eventRate").asLong();
			this.throughput = flashlistRow.get("throughput").asLong();
			this.eventSizeMean = flashlistRow.get("eventSize").asInt();
			this.eventSizeStddev = flashlistRow.get("eventSizeStdDev").asInt();
			this.numEvents = flashlistRow.get("nbEventsBuilt").asLong();
			this.numEventsInBU = flashlistRow.get("nbEventsInBU").asLong();
			this.priority = flashlistRow.get("priority").asInt();
			this.numRequestsSent = flashlistRow.get("nbSentResources").asInt();
			this.numRequestsUsed = flashlistRow.get("nbUsedResources").asInt();
			this.numRequestsBlocked = flashlistRow.get("nbBlockedResources").asInt();
			this.numFUsHLT = flashlistRow.get("fuSlotsHLT").asInt();
			this.numFUsCrashed = flashlistRow.get("fuSlotsQuarantined").asInt();
			this.numFUsStale = flashlistRow.get("fuSlotsStale").asInt();
			this.numFUsCloud = flashlistRow.get("fuSlotsCloud").asInt();
			this.ramDiskUsage = flashlistRow.get("ramDiskUsed").asDouble()*100;
			this.ramDiskTotal = flashlistRow.get("ramDiskSizeInGB").asDouble();
			this.numFiles = flashlistRow.get("nbFilesWritten").asInt();
			this.numLumisectionsWithFiles = flashlistRow.get("nbLumiSections").asInt();
			this.currentLumisection = flashlistRow.get("currentLumiSection").asInt();
			this.numLumisectionsForHLT = flashlistRow.get("queuedLumiSections").asInt();
			this.numLumisectionsOutHLT = flashlistRow.get("queuedLumiSectionsOnFUs").asInt();
			this.fuOutputBandwidthInMB = flashlistRow.get("fuOutputBandwidthInMB").asDouble();
			this.requestRate = flashlistRow.get("requestRate").asInt();
			this.requestRetryRate = flashlistRow.get("requestRetryRate").asDouble();

			this.fragmentCount = flashlistRow.get("fragmentRate").asInt();
			this.slowestRUtid = flashlistRow.get("slowestRUtid").asInt();
			this.nbCorruptedEvents = flashlistRow.get("nbCorruptedEvents").asInt();
			this.nbEventsMissingData = flashlistRow.get("nbEventsMissingData").asInt();
			this.nbEventsWithCRCerrors = flashlistRow.get("nbEventsWithCRCerrors").asInt();
			this.nbTotalResources = flashlistRow.get("nbTotalResources").asInt();
			this.stateName = flashlistRow.get("stateName").asText();

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
		this.stateName = null;
		this.port = null;
		this.errorMsg = null;
		this.rate = null;
		this.throughput = null;
		this.eventSizeMean = null;
		this.eventSizeStddev = null;
		this.numEvents = null;
		this.numEventsInBU = null;
		this.priority = null;
		this.numRequestsSent = null;
		this.numRequestsUsed = null;
		this.numRequestsBlocked = null;
		this.numFUsHLT = null;
		this.numFUsCrashed = null;
		this.numFUsStale = null;
		this.numFUsCloud = null;
		this.ramDiskUsage = null;
		this.ramDiskTotal = null;
		this.numFiles = null;
		this.numLumisectionsWithFiles = null;
		this.currentLumisection = null;
		this.numLumisectionsForHLT = null;
		this.numLumisectionsOutHLT = null;
		this.fuOutputBandwidthInMB = null;
		this.requestRate = null;
		this.requestRetryRate = null;

		this.fragmentCount = null;
		this.slowestRUtid = null;
		this.nbCorruptedEvents = null;
		this.nbEventsMissingData = null;
		this.nbEventsWithCRCerrors = null;
		this.nbTotalResources = null;
		
		this.crashed = null;
	}

	// ----------------------------------------------------------------------

	public static class HostNameComparator implements Comparator<BU> {
		@Override
		public int compare(BU bu1, BU bu2) {
			// assume both are non-null
			return bu1.getHostname().compareTo(bu2.getHostname());
		}
	}

	public DAQ getDaq() {
		return daq;
	}

	public void setDaq(DAQ daq) {
		this.daq = daq;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
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
	
	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public Boolean getCrashed() {
		return crashed;
	}

	public void setCrashed(Boolean crashed) {
		this.crashed = crashed;
	}

	public Long getRate() {
		return rate;
	}

	public void setRate(Long rate) {
		this.rate = rate;
	}

	public Long getThroughput() {
		return throughput;
	}

	public void setThroughput(Long throughput) {
		this.throughput = throughput;
	}

	public Integer getEventSizeMean() {
		return eventSizeMean;
	}

	public void setEventSizeMean(Integer eventSizeMean) {
		this.eventSizeMean = eventSizeMean;
	}

	public Integer getEventSizeStddev() {
		return eventSizeStddev;
	}

	public void setEventSizeStddev(Integer eventSizeStddev) {
		this.eventSizeStddev = eventSizeStddev;
	}

	public Long getNumEvents() {
		return numEvents;
	}

	public void setNumEvents(Long numEvents) {
		this.numEvents = numEvents;
	}

	public Long getNumEventsInBU() {
		return numEventsInBU;
	}

	public void setNumEventsInBU(Long numEventsInBU) {
		this.numEventsInBU = numEventsInBU;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public Integer getNumRequestsSent() {
		return numRequestsSent;
	}

	public void setNumRequestsSent(Integer numRequestsSent) {
		this.numRequestsSent = numRequestsSent;
	}

	public Integer getNumRequestsUsed() {
		return numRequestsUsed;
	}

	public void setNumRequestsUsed(Integer numRequestsUsed) {
		this.numRequestsUsed = numRequestsUsed;
	}

	public Integer getNumRequestsBlocked() {
		return numRequestsBlocked;
	}

	public void setNumRequestsBlocked(Integer numRequestsBlocked) {
		this.numRequestsBlocked = numRequestsBlocked;
	}

	public Integer getNumFUsHLT() {
		return numFUsHLT;
	}

	public void setNumFUsHLT(Integer numFUsHLT) {
		this.numFUsHLT = numFUsHLT;
	}

	public Integer getNumFUsCrashed() {
		return numFUsCrashed;
	}

	public void setNumFUsCrashed(Integer numFUsCrashed) {
		this.numFUsCrashed = numFUsCrashed;
	}

	public Integer getNumFUsStale() {
		return numFUsStale;
	}

	public void setNumFUsStale(Integer numFUsStale) {
		this.numFUsStale = numFUsStale;
	}

	public Integer getNumFUsCloud() {
		return numFUsCloud;
	}

	public void setNumFUsCloud(Integer numFUsCloud) {
		this.numFUsCloud = numFUsCloud;
	}

	public Double getRamDiskUsage() {
		return ramDiskUsage;
	}

	public void setRamDiskUsage(Double ramDiskUsage) {
		this.ramDiskUsage = ramDiskUsage;
	}

	public Double getRamDiskTotal() {
		return ramDiskTotal;
	}

	public void setRamDiskTotal(Double ramDiskTotal) {
		this.ramDiskTotal = ramDiskTotal;
	}

	public Integer getNumFiles() {
		return numFiles;
	}

	public void setNumFiles(Integer numFiles) {
		this.numFiles = numFiles;
	}

	public Integer getNumLumisectionsWithFiles() {
		return numLumisectionsWithFiles;
	}

	public void setNumLumisectionsWithFiles(Integer numLumisectionsWithFiles) {
		this.numLumisectionsWithFiles = numLumisectionsWithFiles;
	}

	public Integer getCurrentLumisection() {
		return currentLumisection;
	}

	public void setCurrentLumisection(Integer currentLumisection) {
		this.currentLumisection = currentLumisection;
	}

	public Integer getNumLumisectionsForHLT() {
		return numLumisectionsForHLT;
	}

	public void setNumLumisectionsForHLT(Integer numLumisectionsForHLT) {
		this.numLumisectionsForHLT = numLumisectionsForHLT;
	}

	public Integer getNumLumisectionsOutHLT() {
		return numLumisectionsOutHLT;
	}

	public void setNumLumisectionsOutHLT(Integer numLumisectionsOutHLT) {
		this.numLumisectionsOutHLT = numLumisectionsOutHLT;
	}

	public Double getFuOutputBandwidthInMB() {
		return fuOutputBandwidthInMB;
	}

	public void setFuOutputBandwidthInMB(Double fuOutputBandwidthInMB) {
		this.fuOutputBandwidthInMB = fuOutputBandwidthInMB;
	}

	public Integer getFragmentCount() {
		return fragmentCount;
	}

	public void setFragmentCount(Integer fragmentCount) {
		this.fragmentCount = fragmentCount;
	}

	public Integer getNbCorruptedEvents() {
		return nbCorruptedEvents;
	}

	public void setNbCorruptedEvents(Integer nbCorruptedEvents) {
		this.nbCorruptedEvents = nbCorruptedEvents;
	}

	public Integer getNbEventsMissingData() {
		return nbEventsMissingData;
	}

	public void setNbEventsMissingData(Integer nbEventsMissingData) {
		this.nbEventsMissingData = nbEventsMissingData;
	}

	public Integer getNbEventsWithCRCerrors() {
		return nbEventsWithCRCerrors;
	}

	public void setNbEventsWithCRCerrors(Integer nbEventsWithCRCerrors) {
		this.nbEventsWithCRCerrors = nbEventsWithCRCerrors;
	}

	public Integer getNbTotalResources() {
		return nbTotalResources;
	}

	public void setNbTotalResources(Integer nbTotalResources) {
		this.nbTotalResources = nbTotalResources;
	}

	public Integer getRequestRate() {
		return requestRate;
	}

	public void setRequestRate(Integer requestRate) {
		this.requestRate = requestRate;
	}

	public Double getRequestRetryRate() {
		return requestRetryRate;
	}

	public void setRequestRetryRate(Double requestRetryRate) {
		this.requestRetryRate = requestRetryRate;
	}

	public Integer getSlowestRUtid() {
		return slowestRUtid;
	}

	public void setSlowestRUtid(Integer slowestRUtid) {
		this.slowestRUtid = slowestRUtid;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((crashed == null) ? 0 : crashed.hashCode());
		result = prime * result + ((currentLumisection == null) ? 0 : currentLumisection.hashCode());
		result = prime * result + ((errorMsg == null) ? 0 : errorMsg.hashCode());
		result = prime * result + ((eventSizeMean == null) ? 0 : eventSizeMean.hashCode());
		result = prime * result + ((eventSizeStddev == null) ? 0 : eventSizeStddev.hashCode());
		result = prime * result + ((fragmentCount == null) ? 0 : fragmentCount.hashCode());
		result = prime * result + ((fuOutputBandwidthInMB == null) ? 0 : fuOutputBandwidthInMB.hashCode());
		result = prime * result + ((hostname == null) ? 0 : hostname.hashCode());
		result = prime * result + ((nbCorruptedEvents == null) ? 0 : nbCorruptedEvents.hashCode());
		result = prime * result + ((nbEventsMissingData == null) ? 0 : nbEventsMissingData.hashCode());
		result = prime * result + ((nbEventsWithCRCerrors == null) ? 0 : nbEventsWithCRCerrors.hashCode());
		result = prime * result + ((nbTotalResources == null) ? 0 : nbTotalResources.hashCode());
		result = prime * result + ((numEvents == null) ? 0 : numEvents.hashCode());
		result = prime * result + ((numEventsInBU == null) ? 0 : numEventsInBU.hashCode());
		result = prime * result + ((numFUsCloud == null) ? 0 : numFUsCloud.hashCode());
		result = prime * result + ((numFUsCrashed == null) ? 0 : numFUsCrashed.hashCode());
		result = prime * result + ((numFUsHLT == null) ? 0 : numFUsHLT.hashCode());
		result = prime * result + ((numFUsStale == null) ? 0 : numFUsStale.hashCode());
		result = prime * result + ((numFiles == null) ? 0 : numFiles.hashCode());
		result = prime * result + ((numLumisectionsForHLT == null) ? 0 : numLumisectionsForHLT.hashCode());
		result = prime * result + ((numLumisectionsOutHLT == null) ? 0 : numLumisectionsOutHLT.hashCode());
		result = prime * result + ((numLumisectionsWithFiles == null) ? 0 : numLumisectionsWithFiles.hashCode());
		result = prime * result + ((numRequestsBlocked == null) ? 0 : numRequestsBlocked.hashCode());
		result = prime * result + ((numRequestsSent == null) ? 0 : numRequestsSent.hashCode());
		result = prime * result + ((numRequestsUsed == null) ? 0 : numRequestsUsed.hashCode());
		result = prime * result + ((port == null) ? 0 : port.hashCode());
		result = prime * result + ((priority == null) ? 0 : priority.hashCode());
		result = prime * result + ((ramDiskTotal == null) ? 0 : ramDiskTotal.hashCode());
		result = prime * result + ((ramDiskUsage == null) ? 0 : ramDiskUsage.hashCode());
		result = prime * result + ((rate == null) ? 0 : rate.hashCode());
		result = prime * result + ((requestRate == null) ? 0 : requestRate.hashCode());
		result = prime * result + ((requestRetryRate == null) ? 0 : requestRetryRate.hashCode());
		result = prime * result + ((slowestRUtid == null) ? 0 : slowestRUtid.hashCode());
		result = prime * result + ((stateName == null) ? 0 : stateName.hashCode());
		result = prime * result + ((throughput == null) ? 0 : throughput.hashCode());
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
		BU other = (BU) obj;
		if (crashed == null) {
			if (other.crashed != null)
				return false;
		} else if (!crashed.equals(other.crashed))
			return false;
		if (currentLumisection == null) {
			if (other.currentLumisection != null)
				return false;
		} else if (!currentLumisection.equals(other.currentLumisection))
			return false;
		if (errorMsg == null) {
			if (other.errorMsg != null)
				return false;
		} else if (!errorMsg.equals(other.errorMsg))
			return false;
		if (eventSizeMean == null) {
			if (other.eventSizeMean != null)
				return false;
		} else if (!eventSizeMean.equals(other.eventSizeMean))
			return false;
		if (eventSizeStddev == null) {
			if (other.eventSizeStddev != null)
				return false;
		} else if (!eventSizeStddev.equals(other.eventSizeStddev))
			return false;
		if (fragmentCount == null) {
			if (other.fragmentCount != null)
				return false;
		} else if (!fragmentCount.equals(other.fragmentCount))
			return false;
		if (fuOutputBandwidthInMB == null) {
			if (other.fuOutputBandwidthInMB != null)
				return false;
		} else if (!fuOutputBandwidthInMB.equals(other.fuOutputBandwidthInMB))
			return false;
		if (hostname == null) {
			if (other.hostname != null)
				return false;
		} else if (!hostname.equals(other.hostname))
			return false;
		if (nbCorruptedEvents == null) {
			if (other.nbCorruptedEvents != null)
				return false;
		} else if (!nbCorruptedEvents.equals(other.nbCorruptedEvents))
			return false;
		if (nbEventsMissingData == null) {
			if (other.nbEventsMissingData != null)
				return false;
		} else if (!nbEventsMissingData.equals(other.nbEventsMissingData))
			return false;
		if (nbEventsWithCRCerrors == null) {
			if (other.nbEventsWithCRCerrors != null)
				return false;
		} else if (!nbEventsWithCRCerrors.equals(other.nbEventsWithCRCerrors))
			return false;
		if (nbTotalResources == null) {
			if (other.nbTotalResources != null)
				return false;
		} else if (!nbTotalResources.equals(other.nbTotalResources))
			return false;
		if (numEvents == null) {
			if (other.numEvents != null)
				return false;
		} else if (!numEvents.equals(other.numEvents))
			return false;
		if (numEventsInBU == null) {
			if (other.numEventsInBU != null)
				return false;
		} else if (!numEventsInBU.equals(other.numEventsInBU))
			return false;
		if (numFUsCloud == null) {
			if (other.numFUsCloud != null)
				return false;
		} else if (!numFUsCloud.equals(other.numFUsCloud))
			return false;
		if (numFUsCrashed == null) {
			if (other.numFUsCrashed != null)
				return false;
		} else if (!numFUsCrashed.equals(other.numFUsCrashed))
			return false;
		if (numFUsHLT == null) {
			if (other.numFUsHLT != null)
				return false;
		} else if (!numFUsHLT.equals(other.numFUsHLT))
			return false;
		if (numFUsStale == null) {
			if (other.numFUsStale != null)
				return false;
		} else if (!numFUsStale.equals(other.numFUsStale))
			return false;
		if (numFiles == null) {
			if (other.numFiles != null)
				return false;
		} else if (!numFiles.equals(other.numFiles))
			return false;
		if (numLumisectionsForHLT == null) {
			if (other.numLumisectionsForHLT != null)
				return false;
		} else if (!numLumisectionsForHLT.equals(other.numLumisectionsForHLT))
			return false;
		if (numLumisectionsOutHLT == null) {
			if (other.numLumisectionsOutHLT != null)
				return false;
		} else if (!numLumisectionsOutHLT.equals(other.numLumisectionsOutHLT))
			return false;
		if (numLumisectionsWithFiles == null) {
			if (other.numLumisectionsWithFiles != null)
				return false;
		} else if (!numLumisectionsWithFiles.equals(other.numLumisectionsWithFiles))
			return false;
		if (numRequestsBlocked == null) {
			if (other.numRequestsBlocked != null)
				return false;
		} else if (!numRequestsBlocked.equals(other.numRequestsBlocked))
			return false;
		if (numRequestsSent == null) {
			if (other.numRequestsSent != null)
				return false;
		} else if (!numRequestsSent.equals(other.numRequestsSent))
			return false;
		if (numRequestsUsed == null) {
			if (other.numRequestsUsed != null)
				return false;
		} else if (!numRequestsUsed.equals(other.numRequestsUsed))
			return false;
		if (port == null) {
			if (other.port != null)
				return false;
		} else if (!port.equals(other.port))
			return false;
		if (priority == null) {
			if (other.priority != null)
				return false;
		} else if (!priority.equals(other.priority))
			return false;
		if (ramDiskTotal == null) {
			if (other.ramDiskTotal != null)
				return false;
		} else if (!ramDiskTotal.equals(other.ramDiskTotal))
			return false;
		if (ramDiskUsage == null) {
			if (other.ramDiskUsage != null)
				return false;
		} else if (!ramDiskUsage.equals(other.ramDiskUsage))
			return false;
		if (rate == null) {
			if (other.rate != null)
				return false;
		} else if (!rate.equals(other.rate))
			return false;
		if (requestRate == null) {
			if (other.requestRate != null)
				return false;
		} else if (!requestRate.equals(other.requestRate))
			return false;
		if (requestRetryRate == null) {
			if (other.requestRetryRate != null)
				return false;
		} else if (!requestRetryRate.equals(other.requestRetryRate))
			return false;
		if (slowestRUtid == null) {
			if (other.slowestRUtid != null)
				return false;
		} else if (!slowestRUtid.equals(other.slowestRUtid))
			return false;
		if (stateName == null) {
			if (other.stateName != null)
				return false;
		} else if (!stateName.equals(other.stateName))
			return false;
		if (throughput == null) {
			if (other.throughput != null)
				return false;
		} else if (!throughput.equals(other.throughput))
			return false;
		return true;
	}


	// ----------------------------------------------------------------------
	
	

}
