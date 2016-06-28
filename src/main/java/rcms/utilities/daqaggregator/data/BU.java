package rcms.utilities.daqaggregator.data;

import java.util.Comparator;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.mappers.FlashlistType;
import rcms.utilities.daqaggregator.mappers.FlashlistUpdatable;

/**
 * Builder Unit
 * 
 * @author Andre Georg Holzner (andre.georg.holzner@cern.ch)
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class BU implements FlashlistUpdatable {

	// ----------------------------------------
	// fields set at beginning of session
	// ----------------------------------------

	/** parent */
	private DAQ daq;

	private String hostname;

	// ----------------------------------------
	// fields updated periodically
	// ----------------------------------------

	/** event rate in kHz ? */
	private float rate;

	/** throughput in MByte/s ? */
	private float throughput;

	public void setDaq(DAQ daq) {
		this.daq = daq;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	/** in MByte/s ? */
	private float eventSizeMean;
	private float eventSizeStddev;

	/** is this processed or requested ? */
	private long numEvents;

	private long numEventsInBU;

	private int priority;

	private int numRequestsSent;

	private int numRequestsUsed;

	private int numRequestsBlocked;

	private int numFUsHlt;

	private int numFUsCrashed;

	private int numFUsStale;

	private int numFUsCloud;

	/** in percent ? */
	private float ramDiskUsage;

	/** total amount of ramdisk */
	private float ramDiskTotal;

	/** processed ? to be processed ? on ramdisk ? */
	private int numFiles;

	private int numLumisectionsWithFiles;

	private int currentLumisection;

	private int numLumisectionsForHLT;

	private int numLumisectionsOutHLT;

	private int fragmentCount;

	private int nbCorruptedEvents;

	private int nbEventsMissingData;

	private int nbEventsWithCRCerrors;

	private int nbTotalResources;

	private String stateName;

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
			this.rate = flashlistRow.get("eventRate").asInt();
			this.throughput = flashlistRow.get("bandwidth").asInt();
			this.eventSizeMean = flashlistRow.get("eventSize").asInt();
			this.eventSizeStddev = flashlistRow.get("eventSizeStdDev").asInt();
			this.numEvents = flashlistRow.get("nbEventsBuilt").asLong();
			this.numEventsInBU = flashlistRow.get("nbEventsInBU").asLong();
			this.priority = flashlistRow.get("priority").asInt();
			this.numRequestsSent = flashlistRow.get("requestCount").asInt();
			// this.numRequestsUsed =
			// flashlistRow.get("outstandingRequests").asInt(); //FIXME change
			this.numRequestsBlocked = flashlistRow.get("nbBlockedResources").asInt();
			this.numFUsHlt = flashlistRow.get("fuSlotsHLT").asInt();
			this.numFUsCrashed = flashlistRow.get("fuSlotsQuarantined").asInt();
			this.numFUsStale = flashlistRow.get("fuSlotsStale").asInt();
			this.numFUsCloud = flashlistRow.get("fuSlotsCloud").asInt();
			this.ramDiskUsage = flashlistRow.get("ramDiskUsed").asInt();
			this.ramDiskTotal = flashlistRow.get("ramDiskSizeInGB").asInt();
			this.numFiles = flashlistRow.get("nbFilesWritten").asInt();
			this.numLumisectionsWithFiles = flashlistRow.get("nbLumiSections").asInt();
			this.currentLumisection = flashlistRow.get("currentLumiSection").asInt();
			this.numLumisectionsForHLT = flashlistRow.get("queuedLumiSections").asInt();
			this.numLumisectionsOutHLT = flashlistRow.get("queuedLumiSectionsOnFUs").asInt();

			this.fragmentCount = flashlistRow.get("fragmentCount").asInt();
			this.nbCorruptedEvents = flashlistRow.get("nbCorruptedEvents").asInt();
			this.nbEventsMissingData = flashlistRow.get("nbEventsMissingData").asInt();
			this.nbEventsWithCRCerrors = flashlistRow.get("nbEventsWithCRCerrors").asInt();
			this.nbTotalResources = flashlistRow.get("nbTotalResources").asInt();
			this.stateName = flashlistRow.get("stateName").asText();

		}
	}

	@Override
	public void clean() {
		// nothing to do
	}

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

	public float getEventSizeMean() {
		return eventSizeMean;
	}

	public void setEventSizeMean(float eventSizeMean) {
		this.eventSizeMean = eventSizeMean;
	}

	public float getEventSizeStddev() {
		return eventSizeStddev;
	}

	public void setEventSizeStddev(float eventSizeStddev) {
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

	public int getNumFUsHlt() {
		return numFUsHlt;
	}

	public void setNumFUsHlt(int numFUsHlt) {
		this.numFUsHlt = numFUsHlt;
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

	public float getRamDiskUsage() {
		return ramDiskUsage;
	}

	public void setRamDiskUsage(float ramDiskUsage) {
		this.ramDiskUsage = ramDiskUsage;
	}

	public float getRamDiskTotal() {
		return ramDiskTotal;
	}

	public void setRamDiskTotal(float ramDiskTotal) {
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

	public String getStateName() {
		return stateName;
	}

	public void setStateName(String stateName) {
		this.stateName = stateName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + currentLumisection;
		result = prime * result + Float.floatToIntBits(eventSizeMean);
		result = prime * result + Float.floatToIntBits(eventSizeStddev);
		result = prime * result + fragmentCount;
		result = prime * result + ((hostname == null) ? 0 : hostname.hashCode());
		result = prime * result + nbCorruptedEvents;
		result = prime * result + nbEventsMissingData;
		result = prime * result + nbEventsWithCRCerrors;
		result = prime * result + nbTotalResources;
		result = prime * result + (int) (numEvents ^ (numEvents >>> 32));
		result = prime * result + (int) (numEventsInBU ^ (numEventsInBU >>> 32));
		result = prime * result + numFUsCloud;
		result = prime * result + numFUsCrashed;
		result = prime * result + numFUsHlt;
		result = prime * result + numFUsStale;
		result = prime * result + numFiles;
		result = prime * result + numLumisectionsForHLT;
		result = prime * result + numLumisectionsOutHLT;
		result = prime * result + numLumisectionsWithFiles;
		result = prime * result + numRequestsBlocked;
		result = prime * result + numRequestsSent;
		result = prime * result + numRequestsUsed;
		result = prime * result + priority;
		result = prime * result + Float.floatToIntBits(ramDiskTotal);
		result = prime * result + Float.floatToIntBits(ramDiskUsage);
		result = prime * result + Float.floatToIntBits(rate);
		result = prime * result + ((stateName == null) ? 0 : stateName.hashCode());
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
		BU other = (BU) obj;
		if (currentLumisection != other.currentLumisection)
			return false;
		if (Float.floatToIntBits(eventSizeMean) != Float.floatToIntBits(other.eventSizeMean))
			return false;
		if (Float.floatToIntBits(eventSizeStddev) != Float.floatToIntBits(other.eventSizeStddev))
			return false;
		if (fragmentCount != other.fragmentCount)
			return false;
		if (hostname == null) {
			if (other.hostname != null)
				return false;
		} else if (!hostname.equals(other.hostname))
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
		if (numFUsHlt != other.numFUsHlt)
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
		if (Float.floatToIntBits(ramDiskTotal) != Float.floatToIntBits(other.ramDiskTotal))
			return false;
		if (Float.floatToIntBits(ramDiskUsage) != Float.floatToIntBits(other.ramDiskUsage))
			return false;
		if (Float.floatToIntBits(rate) != Float.floatToIntBits(other.rate))
			return false;
		if (stateName == null) {
			if (other.stateName != null)
				return false;
		} else if (!stateName.equals(other.stateName))
			return false;
		if (Float.floatToIntBits(throughput) != Float.floatToIntBits(other.throughput))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BU [daq=" + daq + ", hostname=" + hostname + ", rate=" + rate + ", throughput=" + throughput
				+ ", eventSizeMean=" + eventSizeMean + ", eventSizeStddev=" + eventSizeStddev + ", numEvents="
				+ numEvents + ", numEventsInBU=" + numEventsInBU + ", priority=" + priority + ", numRequestsSent="
				+ numRequestsSent + ", numRequestsUsed=" + numRequestsUsed + ", numRequestsBlocked="
				+ numRequestsBlocked + ", numFUsHlt=" + numFUsHlt + ", numFUsCrashed=" + numFUsCrashed
				+ ", numFUsStale=" + numFUsStale + ", numFUsCloud=" + numFUsCloud + ", ramDiskUsage=" + ramDiskUsage
				+ ", ramDiskTotal=" + ramDiskTotal + ", numFiles=" + numFiles + ", numLumisectionsWithFiles="
				+ numLumisectionsWithFiles + ", currentLumisection=" + currentLumisection + ", numLumisectionsForHLT="
				+ numLumisectionsForHLT + ", numLumisectionsOutHLT=" + numLumisectionsOutHLT + ", fragmentCount="
				+ fragmentCount + ", nbCorruptedEvents=" + nbCorruptedEvents + ", nbEventsMissingData="
				+ nbEventsMissingData + ", nbEventsWithCRCerrors=" + nbEventsWithCRCerrors + ", nbTotalResources="
				+ nbTotalResources + ", stateName=" + stateName + "]";
	}

	// ----------------------------------------------------------------------

}
