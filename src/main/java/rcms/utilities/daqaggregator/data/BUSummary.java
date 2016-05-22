package rcms.utilities.daqaggregator.data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * Summary statistics of BUs
 * 
 * @author Andre Georg Holzner (andre.georg.holzner@cern.ch)
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class BUSummary implements Serializable {

	// ----------------------------------------
	// fields set at beginning of session
	// ----------------------------------------

	/** parent */
	private DAQ daq;

	// ----------------------------------------
	// fields updated periodically
	// ----------------------------------------

	/** event rate in kHz ? */
	private float rate;

	/** throughput in MByte/s ? */
	private float throughput;

	/** in MByte/s ? */
	private float eventSizeMean;
	private float eventSizeStddev;

	/** is this processed or requested ? */
	private int numEvents;

	private int numEventsInBU;

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

	public void setDaq(DAQ daq) {
		this.daq = daq;
	}

	// ----------------------------------------------------------------------

	

	public void summarize() {

		/* TODO sum or avg? */
		int currentLumisection = 0;
		int priority = 0;

		/* Averages */
		float rate = 0;
		float eventSizeMean = 0;
		float eventSizeStddev = 0;

		/* sums */
		float ramDiskTotal = 0;
		float ramDiskUsage = 0;
		float throughput = 0;
		int numEvents = 0;
		int numEventsInBU = 0;
		int numFiles = 0;
		int numFUsCloud = 0;
		int numFUsCrashed = 0;
		int numFUsHlt = 0;
		int numFUsStale = 0;
		int numLumisectionsForHLT = 0;
		int numLumisectionsOutHLT = 0;
		int numLumisectionsWithFiles = 0;
		int numRequestsBlocked = 0;
		int numRequestsSent = 0;
		int numRequestsUsed = 0;

		int numberOfBus = daq.getBus().size();

		this.setDaq(daq);
		for (BU bu : daq.getBus()) {
			numEvents += bu.getNumEvents();
			numEventsInBU += bu.getNumEventsInBU();
			currentLumisection += bu.getCurrentLumisection();
			eventSizeMean += bu.getEventSizeMean();
			eventSizeStddev += bu.getEventSizeStddev();
			numFiles += bu.getNumFiles();
			numFUsCloud += bu.getNumFUsCloud();
			numFUsCrashed += bu.getNumFUsCrashed();
			numFUsHlt += bu.getNumFUsHlt();
			numFUsStale += bu.getNumFUsStale();
			numLumisectionsForHLT += bu.getNumLumisectionsForHLT();
			numLumisectionsOutHLT += bu.getNumLumisectionsOutHLT();
			numLumisectionsWithFiles += bu.getNumLumisectionsWithFiles();
			numRequestsBlocked += bu.getNumRequestsBlocked();
			numRequestsSent += bu.getNumRequestsSent();
			numRequestsUsed += bu.getNumRequestsUsed();
			priority += bu.getPriority();
			ramDiskTotal += bu.getRamDiskTotal();
			ramDiskUsage += bu.getRamDiskUsage();

			rate += bu.getRate();
			throughput += bu.getThroughput();
		}

		/* avarage values */
		rate = rate / (float) numberOfBus;
		eventSizeMean = eventSizeMean / (float) numberOfBus;
		eventSizeStddev = eventSizeStddev / (float) numberOfBus;

		this.setNumEvents(numEvents);
		this.setNumEventsInBU(numEventsInBU);
		this.setCurrentLumisection(currentLumisection);
		this.setEventSizeMean(eventSizeMean);
		this.setEventSizeStddev(eventSizeStddev);
		this.setNumFiles(numFiles);
		this.setNumFUsCloud(numFUsCloud);
		this.setNumFUsCrashed(numFUsCrashed);
		this.setNumFUsHlt(numFUsHlt);
		this.setNumFUsStale(numFUsStale);
		this.setNumLumisectionsForHLT(numLumisectionsForHLT);
		this.setNumLumisectionsOutHLT(numLumisectionsOutHLT);
		this.setNumLumisectionsWithFiles(numLumisectionsWithFiles);
		this.setNumRequestsBlocked(numRequestsBlocked);
		this.setNumRequestsSent(numRequestsSent);
		this.setNumRequestsUsed(numRequestsUsed);
		this.setPriority(priority);
		this.setRamDiskTotal(ramDiskTotal);
		this.setRamDiskUsage(ramDiskUsage);
		this.setRate(rate);
		this.setThroughput(throughput);
	}
}
