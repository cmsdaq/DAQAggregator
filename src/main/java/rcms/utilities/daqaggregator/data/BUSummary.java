package rcms.utilities.daqaggregator.data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import rcms.utilities.daqaggregator.mappers.Derivable;

/**
 * Summary statistics of BUs
 * 
 * @author Andre Georg Holzner (andre.georg.holzner@cern.ch)
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class BUSummary implements Serializable , Derivable{

	// ----------------------------------------
	// fields set at beginning of session
	// ----------------------------------------

	/** parent */
	private DAQ daq;

	// ----------------------------------------
	// fields updated periodically
	// ----------------------------------------

	/** event rate in Hz */
	private int rate;

	/** throughput in Byte/s */
	private long throughput;

	/** in Bytes */
	private double eventSizeMean;
	private double eventSizeStddev;

	/** is this processed or requested ? */
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

	/** in percent ? */
	private float ramDiskUsage;

	/** total amount of ramdisk in GB */
	private float ramDiskTotal;

	/** total number of files written */
	private int numFiles;

	private int numLumisectionsWithFiles;

	private int currentLumisection;

	private int numLumisectionsForHLT;

	private int numLumisectionsOutHLT;

	private double fuOutputBandwidthInMB;
	
	// ----------------------------------------------------------------------

	// ----------------------------------------------------------------------

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

	public double getEventSizeMean() {
		return eventSizeMean;
	}

	public void setEventSizeMean(double eventSizeMean) {
		this.eventSizeMean = eventSizeMean;
	}

	public double getEventSizeStddev() {
		return eventSizeStddev;
	}

	public void setEventSizeStddev(double eventSizeStddev) {
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

	public double getFuOutputBandwidthInMB() {
		return fuOutputBandwidthInMB;
	}

	public void setFuOutputBandwidthInMB(double fuOutputBandwidthInMB) {
		this.fuOutputBandwidthInMB = fuOutputBandwidthInMB;
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

		/* TODO sum or avg? */
		int currentLumisection = 0;
		int priority = 0;

		/* Averages */
		int rate = 0;
		double eventSizeMean = 0;
		double eventSizeStddev = 0;

		/* sums */
		float ramDiskTotal = 0;
		float ramDiskUsage = 0;
		long throughput = 0;
		int numEvents = 0;
		int numEventsInBU = 0;
		int numFiles = 0;
		int numFUsCloud = 0;
		int numFUsCrashed = 0;
		int numFUsHLT = 0;
		int numFUsStale = 0;
		int numLumisectionsForHLT = 0;
		int numLumisectionsOutHLT = 0;
		int numLumisectionsWithFiles = 0;
		int numRequestsBlocked = 0;
		int numRequestsSent = 0;
		int numRequestsUsed = 0;

		int numberOfBus = 0;

		this.setDaq(daq);
		for (BU bu : daq.getBus()) {
			numEvents += bu.getNumEvents();
			numEventsInBU += bu.getNumEventsInBU();
			numFiles += bu.getNumFiles();
			numFUsCloud += bu.getNumFUsCloud();
			numFUsCrashed += bu.getNumFUsCrashed();
			numFUsHLT += bu.getNumFUsHLT();
			numFUsStale += bu.getNumFUsStale();
			numRequestsBlocked += bu.getNumRequestsBlocked();
			numRequestsSent += bu.getNumRequestsSent();
			numRequestsUsed += bu.getNumRequestsUsed();
			ramDiskTotal += bu.getRamDiskTotal();
			ramDiskUsage += bu.getRamDiskUsage() * bu.getRamDiskTotal();
			fuOutputBandwidthInMB += bu.getFuOutputBandwidthInMB();
			
			if ( bu.getRate() > 0) {
				++numberOfBus;
				rate += bu.getRate();
				throughput += bu.getThroughput();
				eventSizeMean += bu.getEventSizeMean();
				eventSizeStddev += Math.pow(bu.getEventSizeStddev(),2);
				numLumisectionsWithFiles += bu.getNumLumisectionsWithFiles();				
			}
			
			if ( bu.getCurrentLumisection() > currentLumisection )
				currentLumisection = bu.getCurrentLumisection();
			if ( bu.getNumLumisectionsForHLT() > numLumisectionsForHLT )
				numLumisectionsForHLT = bu.getNumLumisectionsForHLT();
			if ( bu.getNumLumisectionsOutHLT() > numLumisectionsOutHLT )
				numLumisectionsOutHLT = bu.getNumLumisectionsOutHLT();
			if ( bu.getPriority() > priority )
				priority = bu.getPriority();
			
		}

		/* avarage values */
		if ( numberOfBus > 0 ) {
			rate = rate / numberOfBus;
			eventSizeMean = eventSizeMean / numberOfBus;
			eventSizeStddev = Math.sqrt(eventSizeStddev);
		}
		ramDiskUsage = ramDiskTotal>0 ? ramDiskUsage/ramDiskTotal*100 : 0;

		this.setNumEvents(numEvents);
		this.setNumEventsInBU(numEventsInBU);
		this.setCurrentLumisection(currentLumisection);
		this.setEventSizeMean(eventSizeMean);
		this.setEventSizeStddev(eventSizeStddev);
		this.setNumFiles(numFiles);
		this.setNumFUsCloud(numFUsCloud);
		this.setNumFUsCrashed(numFUsCrashed);
		this.setNumFUsHLT(numFUsHLT);
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
