package rcms.utilities.daqaggregator.data;

import java.io.Serializable;
import java.util.Comparator;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.mappers.FlashlistUpdatable;
import rcms.utilities.daqaggregator.mappers.FlashlistType;

/**
 * Builder Unit
 * 
 * @author Andre Georg Holzner (andre.georg.holzner@cern.ch)
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class BU implements Serializable, FlashlistUpdatable {

	// ----------------------------------------
	// fields set at beginning of session
	// ----------------------------------------

	/** parent */
	private DAQ daq;

	private String hostname;

	// ----------------------------------------
	// fields updated periodically
	// ----------------------------------------

	public void setDaq(DAQ daq) {
		this.daq = daq;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	private String stateName;

	private String errorMsg;

	/** event rate in Hz */
	private int rate;

	/** throughput in Byte/s */
	private long throughput;

	/** in Byte */
	private int eventSizeMean;
	private int eventSizeStddev;

	/** events processed */
	private int numEvents;

	private int numEventsInBU;

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
			this.setStateName(flashlistRow.get("stateName").asText());
			this.setErrorMsg(flashlistRow.get("errorMsg").asText());
			this.rate = flashlistRow.get("eventRate").asInt();
			this.throughput = flashlistRow.get("bandwidth").asInt();
			this.eventSizeMean = flashlistRow.get("eventSize").asInt();
			this.eventSizeStddev = flashlistRow.get("eventSizeStdDev").asInt();
			this.numEvents = flashlistRow.get("nbEventsBuilt").asInt();
			this.numEventsInBU = flashlistRow.get("nbEventsInBU").asInt();
			this.priority = flashlistRow.get("priority").asInt();
			this.numRequestsSent = flashlistRow.get("nbSentResources").asInt();
			this.numRequestsUsed = flashlistRow.get("nbUsedResources").asInt();
			this.numRequestsBlocked = flashlistRow.get("nbBlockedResources").asInt();
			this.numFUsHLT = flashlistRow.get("fuSlotsHLT").asInt();
			this.numFUsCrashed = flashlistRow.get("fuSlotsQuarantined").asInt();
			this.numFUsStale = flashlistRow.get("fuSlotsStale").asInt();
			this.numFUsCloud = flashlistRow.get("fuSlotsCloud").asInt();
			this.ramDiskUsage = flashlistRow.get("ramDiskUsed").asDouble();
			this.ramDiskTotal = flashlistRow.get("ramDiskSizeInGB").asDouble();
			this.numFiles = flashlistRow.get("nbFilesWritten").asInt();
			this.numLumisectionsWithFiles = flashlistRow.get("nbLumiSections").asInt();
			this.currentLumisection = flashlistRow.get("currentLumiSection").asInt();
			this.numLumisectionsForHLT = flashlistRow.get("queuedLumiSections").asInt();
			this.numLumisectionsOutHLT = flashlistRow.get("queuedLumiSectionsOnFUs").asInt();
			this.fuOutputBandwidthInMB = flashlistRow.get("fuOutputBandwidthInMB").asDouble();

		}
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

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
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

	public int getNumEvents() {
		return numEvents;
	}

	public void setNumEvents(int numEvents) {
		this.numEvents = numEvents;
	}

	public int getNumEventsInBU() {
		return numEventsInBU;
	}

	public void setNumEventsInBU(int numEventsInBU) {
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

	public void setFuOutputBandwidthInMB(double fuOutputBandwidthInMB) {
		this.fuOutputBandwidthInMB = fuOutputBandwidthInMB;
	}

	public double getFuOutputBandwidthInMB() {
		return fuOutputBandwidthInMB;
	}

	public DAQ getDaq() {
		return daq;
	}

	public String getHostname() {
		return hostname;
	}

	// ----------------------------------------------------------------------

	public static class HostNameComparator implements Comparator<BU> {
		@Override
		public int compare(BU bu1, BU bu2) {
			// assume both are non-null
			return bu1.getHostname().compareTo(bu2.getHostname());
		}
	}

	// ----------------------------------------------------------------------

}
