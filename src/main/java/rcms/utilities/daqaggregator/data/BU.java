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
	
	private int port;

	// ----------------------------------------
	// fields updated periodically
	// ----------------------------------------

	private boolean crashed;
	
	private String stateName;

	private String errorMsg;

	/** event rate in Hz */
	private long rate;

	/** throughput in Byte/s */
	private long throughput;

	/** in Byte */
	private int eventSizeMean;
	private int eventSizeStddev;

	/** events processed */
	private long numEvents;

	private long numEventsInBU;

	private int priority;

	private int numRequestsSent;

	private int numRequestsUsed;

	private int numRequestsBlocked;

	private int numFUsHLT;

	private int numFUsCrashed;

	private int numFUsStale;

	private int numFUsCloud;

	/** in percent */
	private double ramDiskUsage;

	/** total amount of ramdisk in GB */
	private double ramDiskTotal;

	/** total number of files written */
	private int numFiles;

	private int numLumisectionsWithFiles;

	private int currentLumisection;

	private int numLumisectionsForHLT;

	private int numLumisectionsOutHLT;

	private double fuOutputBandwidthInMB;

	private int fragmentCount;

	private int nbCorruptedEvents;

	private int nbEventsMissingData;

	private int nbEventsWithCRCerrors;

	private int nbTotalResources;

	private int requestRate;

	private double requestRetryRate;

	private int slowestRUtid;

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
			this.rate = flashlistRow.get("eventRate").asInt();
			this.throughput = flashlistRow.get("throughput").asInt();
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
		this.port = 0;
		this.errorMsg = null;
		this.rate = 0;
		this.throughput = 0;
		this.eventSizeMean = 0;
		this.eventSizeStddev = 0;
		this.numEvents = 0;
		this.numEventsInBU = 0;
		this.priority = 0;
		this.numRequestsSent = 0;
		this.numRequestsUsed = 0;
		this.numRequestsBlocked = 0;
		this.numFUsHLT = 0;
		this.numFUsCrashed = 0;
		this.numFUsStale = 0;
		this.numFUsCloud = 0;
		this.ramDiskUsage = 0;
		this.ramDiskTotal = 0;
		this.numFiles = 0;
		this.numLumisectionsWithFiles = 0;
		this.currentLumisection = 0;
		this.numLumisectionsForHLT = 0;
		this.numLumisectionsOutHLT = 0;
		this.fuOutputBandwidthInMB = 0;
		this.requestRate = 0;
		this.requestRetryRate = 0;

		this.fragmentCount = 0;
		this.slowestRUtid = 0;
		this.nbCorruptedEvents = 0;
		this.nbEventsMissingData = 0;
		this.nbEventsWithCRCerrors = 0;
		this.nbTotalResources = 0;
		
		this.crashed = false;
	}

	// ----------------------------------------------------------------------

	public static class HostNameComparator implements Comparator<BU> {
		@Override
		public int compare(BU bu1, BU bu2) {
			// assume both are non-null
			return bu1.getHostname().compareTo(bu2.getHostname());
		}
	}

	public boolean isCrashed() {
		return crashed;
	}

	public void setCrashed(boolean crashed) {
		this.crashed = crashed;
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

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
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

	public long getRate() {
		return rate;
	}

	public void setRate(long rate) {
		this.rate = rate;
	}

	public long getThroughput() {
		return throughput;
	}

	public void setThroughput(long throughput) {
		this.throughput = throughput;
	}

	public int getEventSizeMean() {
		return eventSizeMean;
	}

	public void setEventSizeMean(int eventSizeMean) {
		this.eventSizeMean = eventSizeMean;
	}

	public int getEventSizeStddev() {
		return eventSizeStddev;
	}

	public void setEventSizeStddev(int eventSizeStddev) {
		this.eventSizeStddev = eventSizeStddev;
	}

	public long getNumEvents() {
		return numEvents;
	}

	public void setNumEvents(long numEvents) {
		this.numEvents = numEvents;
	}

	public long getNumEventsInBU() {
		return numEventsInBU;
	}

	public void setNumEventsInBU(long numEventsInBU) {
		this.numEventsInBU = numEventsInBU;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getNumRequestsSent() {
		return numRequestsSent;
	}

	public void setNumRequestsSent(int numRequestsSent) {
		this.numRequestsSent = numRequestsSent;
	}

	public int getNumRequestsUsed() {
		return numRequestsUsed;
	}

	public void setNumRequestsUsed(int numRequestsUsed) {
		this.numRequestsUsed = numRequestsUsed;
	}

	public int getNumRequestsBlocked() {
		return numRequestsBlocked;
	}

	public void setNumRequestsBlocked(int numRequestsBlocked) {
		this.numRequestsBlocked = numRequestsBlocked;
	}

	public int getNumFUsHLT() {
		return numFUsHLT;
	}

	public void setNumFUsHLT(int numFUsHLT) {
		this.numFUsHLT = numFUsHLT;
	}

	public int getNumFUsCrashed() {
		return numFUsCrashed;
	}

	public void setNumFUsCrashed(int numFUsCrashed) {
		this.numFUsCrashed = numFUsCrashed;
	}

	public int getNumFUsStale() {
		return numFUsStale;
	}

	public void setNumFUsStale(int numFUsStale) {
		this.numFUsStale = numFUsStale;
	}

	public int getNumFUsCloud() {
		return numFUsCloud;
	}

	public void setNumFUsCloud(int numFUsCloud) {
		this.numFUsCloud = numFUsCloud;
	}

	public double getRamDiskUsage() {
		return ramDiskUsage;
	}

	public void setRamDiskUsage(double ramDiskUsage) {
		this.ramDiskUsage = ramDiskUsage;
	}

	public double getRamDiskTotal() {
		return ramDiskTotal;
	}

	public void setRamDiskTotal(double ramDiskTotal) {
		this.ramDiskTotal = ramDiskTotal;
	}

	public int getNumFiles() {
		return numFiles;
	}

	public void setNumFiles(int numFiles) {
		this.numFiles = numFiles;
	}

	public int getNumLumisectionsWithFiles() {
		return numLumisectionsWithFiles;
	}

	public void setNumLumisectionsWithFiles(int numLumisectionsWithFiles) {
		this.numLumisectionsWithFiles = numLumisectionsWithFiles;
	}

	public int getCurrentLumisection() {
		return currentLumisection;
	}

	public void setCurrentLumisection(int currentLumisection) {
		this.currentLumisection = currentLumisection;
	}

	public int getNumLumisectionsForHLT() {
		return numLumisectionsForHLT;
	}

	public void setNumLumisectionsForHLT(int numLumisectionsForHLT) {
		this.numLumisectionsForHLT = numLumisectionsForHLT;
	}

	public int getNumLumisectionsOutHLT() {
		return numLumisectionsOutHLT;
	}

	public void setNumLumisectionsOutHLT(int numLumisectionsOutHLT) {
		this.numLumisectionsOutHLT = numLumisectionsOutHLT;
	}

	public double getFuOutputBandwidthInMB() {
		return fuOutputBandwidthInMB;
	}

	public void setFuOutputBandwidthInMB(double fuOutputBandwidthInMB) {
		this.fuOutputBandwidthInMB = fuOutputBandwidthInMB;
	}

	public int getFragmentCount() {
		return fragmentCount;
	}

	public void setFragmentCount(int fragmentCount) {
		this.fragmentCount = fragmentCount;
	}

	public int getNbCorruptedEvents() {
		return nbCorruptedEvents;
	}

	public void setNbCorruptedEvents(int nbCorruptedEvents) {
		this.nbCorruptedEvents = nbCorruptedEvents;
	}

	public int getNbEventsMissingData() {
		return nbEventsMissingData;
	}

	public void setNbEventsMissingData(int nbEventsMissingData) {
		this.nbEventsMissingData = nbEventsMissingData;
	}

	public int getNbEventsWithCRCerrors() {
		return nbEventsWithCRCerrors;
	}

	public void setNbEventsWithCRCerrors(int nbEventsWithCRCerrors) {
		this.nbEventsWithCRCerrors = nbEventsWithCRCerrors;
	}

	public int getNbTotalResources() {
		return nbTotalResources;
	}

	public void setNbTotalResources(int nbTotalResources) {
		this.nbTotalResources = nbTotalResources;
	}

	public int getRequestRate() {
		return requestRate;
	}

	public void setRequestRate(int requestRate) {
		this.requestRate = requestRate;
	}

	public double getRequestRetryRate() {
		return requestRetryRate;
	}

	public void setRequestRetryRate(double requestRetryRate) {
		this.requestRetryRate = requestRetryRate;
	}

	public int getSlowestRUtid() {
		return slowestRUtid;
	}

	public void setSlowestRUtid(int slowestRUtid) {
		this.slowestRUtid = slowestRUtid;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + currentLumisection;
		result = prime * result + ((errorMsg == null) ? 0 : errorMsg.hashCode());
		result = prime * result + eventSizeMean;
		result = prime * result + eventSizeStddev;
		result = prime * result + fragmentCount;
		long temp;
		temp = Double.doubleToLongBits(fuOutputBandwidthInMB);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((hostname == null) ? 0 : hostname.hashCode());
		result = prime * result + port;
		result = prime * result + nbCorruptedEvents;
		result = prime * result + nbEventsMissingData;
		result = prime * result + nbEventsWithCRCerrors;
		result = prime * result + nbTotalResources;
		result = prime * result + (int) (numEvents ^ (numEvents >>> 32));
		result = prime * result + (int) (numEventsInBU ^ (numEventsInBU >>> 32));
		result = prime * result + numFUsCloud;
		result = prime * result + numFUsCrashed;
		result = prime * result + numFUsHLT;
		result = prime * result + numFUsStale;
		result = prime * result + numFiles;
		result = prime * result + numLumisectionsForHLT;
		result = prime * result + numLumisectionsOutHLT;
		result = prime * result + numLumisectionsWithFiles;
		result = prime * result + numRequestsBlocked;
		result = prime * result + numRequestsSent;
		result = prime * result + numRequestsUsed;
		result = prime * result + priority;
		temp = Double.doubleToLongBits(ramDiskTotal);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(ramDiskUsage);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (rate ^ (rate >>> 32));
		result = prime * result + requestRate;
		temp = Double.doubleToLongBits(requestRetryRate);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + slowestRUtid;
		result = prime * result + ((stateName == null) ? 0 : stateName.hashCode());
		result = prime * result + (int) (throughput ^ (throughput >>> 32));
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
		if (currentLumisection != other.currentLumisection)
			return false;
		if (errorMsg == null) {
			if (other.errorMsg != null)
				return false;
		} else if (!errorMsg.equals(other.errorMsg))
			return false;
		if (eventSizeMean != other.eventSizeMean)
			return false;
		if (eventSizeStddev != other.eventSizeStddev)
			return false;
		if (fragmentCount != other.fragmentCount)
			return false;
		if (Double.doubleToLongBits(fuOutputBandwidthInMB) != Double.doubleToLongBits(other.fuOutputBandwidthInMB))
			return false;
		if (hostname == null) {
			if (other.hostname != null)
				return false;
		} else if (!hostname.equals(other.hostname))
			return false;
		if (port != other.port)
			return false;
		if (nbCorruptedEvents != other.nbCorruptedEvents)
			return false;
		if (nbEventsMissingData != other.nbEventsMissingData)
			return false;
		if (nbEventsWithCRCerrors != other.nbEventsWithCRCerrors)
			return false;
		if (nbTotalResources != other.nbTotalResources)
			return false;
		if (numEvents != other.numEvents)
			return false;
		if (numEventsInBU != other.numEventsInBU)
			return false;
		if (numFUsCloud != other.numFUsCloud)
			return false;
		if (numFUsCrashed != other.numFUsCrashed)
			return false;
		if (numFUsHLT != other.numFUsHLT)
			return false;
		if (numFUsStale != other.numFUsStale)
			return false;
		if (numFiles != other.numFiles)
			return false;
		if (numLumisectionsForHLT != other.numLumisectionsForHLT)
			return false;
		if (numLumisectionsOutHLT != other.numLumisectionsOutHLT)
			return false;
		if (numLumisectionsWithFiles != other.numLumisectionsWithFiles)
			return false;
		if (numRequestsBlocked != other.numRequestsBlocked)
			return false;
		if (numRequestsSent != other.numRequestsSent)
			return false;
		if (numRequestsUsed != other.numRequestsUsed)
			return false;
		if (priority != other.priority)
			return false;
		if (Double.doubleToLongBits(ramDiskTotal) != Double.doubleToLongBits(other.ramDiskTotal))
			return false;
		if (Double.doubleToLongBits(ramDiskUsage) != Double.doubleToLongBits(other.ramDiskUsage))
			return false;
		if (rate != other.rate)
			return false;
		if (requestRate != other.requestRate)
			return false;
		if (Double.doubleToLongBits(requestRetryRate) != Double.doubleToLongBits(other.requestRetryRate))
			return false;
		if (slowestRUtid != other.slowestRUtid)
			return false;
		if (stateName == null) {
			if (other.stateName != null)
				return false;
		} else if (!stateName.equals(other.stateName))
			return false;
		if (throughput != other.throughput)
			return false;
		return true;
	}

	
	

	// ----------------------------------------------------------------------

}
