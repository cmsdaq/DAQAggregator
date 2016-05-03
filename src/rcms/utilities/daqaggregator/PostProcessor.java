package rcms.utilities.daqaggregator;

import rcms.utilities.daqaggregator.data.BU;
import rcms.utilities.daqaggregator.data.BUSummary;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FEDBuilderSummary;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.data.SubFEDBuilder;
import rcms.utilities.daqaggregator.mappers.StructureMapper;

public class PostProcessor {

	private final DAQ daq;
	private final StructureMapper structureMapper;

	public PostProcessor(DAQ daq, StructureMapper structureMapper) {
		super();
		this.daq = daq;
		this.structureMapper = structureMapper;
	}

	public void postProcess() {
		calculateDerivedValues();
		summarizeBus();
		summarizeFedBuilder();
	}

	public void summarizeFedBuilder() {
		FEDBuilderSummary fedBuilderSummary = new FEDBuilderSummary(daq);
		int numberOfRus = structureMapper.getObjectMapper().rus.values().size();

		/* delta between min and max (min not 0) */
		int deltaEvents = 0;
		int maxEvents = 0;
		int minEvents = Integer.MAX_VALUE;

		/* Averages */
		float superFragmentSizeMean = 0;
		float superFragmentSizeStddev = 0;

		/* Sums */
		float throughput = 0;
		int sumEventsInRU = 0;
		int sumFragmentsInRU = 0;
		int sumRequests = 0;
		float rate = 0;

		for (RU ru : structureMapper.getObjectMapper().rus.values()) {
			rate += ru.getRate();
			sumEventsInRU += ru.getEventsInRU();
			sumFragmentsInRU += ru.getFragmentsInRU();
			sumRequests += ru.getRequests();
			superFragmentSizeMean += ru.getSuperFragmentSizeMean();
			superFragmentSizeStddev += ru.getSuperFragmentSizeStddev();
			throughput += ru.getThroughput();

			if (maxEvents < ru.getEventsInRU())
				maxEvents = ru.getEventsInRU();
			if (minEvents > ru.getEventsInRU() && ru.getEventsInRU() != 0) {
				minEvents = ru.getEventsInRU();
			}

		}

		/* avarage values */
		superFragmentSizeMean = superFragmentSizeMean / (float) numberOfRus;
		superFragmentSizeStddev = superFragmentSizeStddev / (float) numberOfRus;

		/* deltas */
		fedBuilderSummary.setDeltaEvents(maxEvents - minEvents);

		fedBuilderSummary.setDeltaEvents(deltaEvents);
		fedBuilderSummary.setRate(rate);
		fedBuilderSummary.setSumEventsInRU(sumEventsInRU);
		fedBuilderSummary.setSumFragmentsInRU(sumFragmentsInRU);
		fedBuilderSummary.setSumRequests(sumRequests);
		fedBuilderSummary.setSuperFragmentSizeMean(superFragmentSizeMean);
		fedBuilderSummary.setSuperFragmentSizeStddev(superFragmentSizeStddev);
		fedBuilderSummary.setThroughput(throughput);

		daq.setFedBuilderSummary(fedBuilderSummary);

	}

	private void summarizeBus() {

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

		BUSummary buSummary = new BUSummary(daq);
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

		buSummary.setNumEvents(numEvents);
		buSummary.setNumEventsInBU(numEventsInBU);
		buSummary.setCurrentLumisection(currentLumisection);
		buSummary.setEventSizeMean(eventSizeMean);
		buSummary.setEventSizeStddev(eventSizeStddev);
		buSummary.setNumFiles(numFiles);
		buSummary.setNumFUsCloud(numFUsCloud);
		buSummary.setNumFUsCrashed(numFUsCrashed);
		buSummary.setNumFUsHlt(numFUsHlt);
		buSummary.setNumFUsStale(numFUsStale);
		buSummary.setNumLumisectionsForHLT(numLumisectionsForHLT);
		buSummary.setNumLumisectionsOutHLT(numLumisectionsOutHLT);
		buSummary.setNumLumisectionsWithFiles(numLumisectionsWithFiles);
		buSummary.setNumRequestsBlocked(numRequestsBlocked);
		buSummary.setNumRequestsSent(numRequestsSent);
		buSummary.setNumRequestsUsed(numRequestsUsed);
		buSummary.setPriority(priority);
		buSummary.setRamDiskTotal(ramDiskTotal);
		buSummary.setRamDiskUsage(ramDiskUsage);
		buSummary.setRate(rate);
		buSummary.setThroughput(throughput);
		daq.setBuSummary(buSummary);
	}

	private void calculateDerivedValues() {

		// calculate min/max triger per SubFEDBuilder
		for (SubFEDBuilder subFedBuilder : structureMapper.getObjectMapper().subFedBuilders.values()) {
			subFedBuilder.calculateDerived();
		}

	}

}
