package rcms.utilities.daqaggregator.datasource;

import java.util.Collection;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.BU;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FMM;
import rcms.utilities.daqaggregator.data.FRL;
import rcms.utilities.daqaggregator.data.FRLPc;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqaggregator.mappers.MappingManager;
import rcms.utilities.daqaggregator.mappers.MappingReporter;

public class FlashlistManager {

	/**
	 * Flashlists will be mapped to objects in DAQ structure referenced by
	 * mapping manager
	 */
	private final MappingManager mappingManager;

	private static final Logger logger = Logger.getLogger(FlashlistManager.class);

	public FlashlistManager(MappingManager mappingManager) {
		this.mappingManager = mappingManager;
	}

	/**
	 * Map flashlists to Snapshot object
	 */
	public void mapFlashlists(Collection<Flashlist> flashlists) {

		long startTime = System.currentTimeMillis();
		MappingReporter.get().clear();

		cleanStructure(); // first clean structure and set default values

		for (Flashlist flashlist : flashlists) {

			FlashlistDispatcher dispatcher = new FlashlistDispatcher();

			long dispatchStartTime = System.currentTimeMillis();
			dispatcher.dispatch(flashlist, mappingManager);
			logger.info(String.format("Mapped flashlist %s in %d ms.", flashlist.getName(), System.currentTimeMillis() - dispatchStartTime));
		}
		long stopTime = System.currentTimeMillis();
		int time = (int) (stopTime - startTime);
		logger.info("Mapping all flashlists finished in " + time + "ms");
	}

	private void cleanStructure() {

		mappingManager.getObjectMapper().daq.clean();

		mappingManager.getObjectMapper().daq.getTcdsGlobalInfo().clean();

		for (FED fed : mappingManager.getObjectMapper().feds.values()) {
			fed.clean();
		}

		for (BU bu : mappingManager.getObjectMapper().bus.values()) {
			bu.clean();
		}

		for (FMM fmm : mappingManager.getObjectMapper().fmms.values()) {
			fmm.clean();
		}

		for (FRL frl : mappingManager.getObjectMapper().frls.values()) {
			frl.clean();
		}

		for (FRLPc frlpc : mappingManager.getObjectMapper().frlPcs.values()) {
			frlpc.clean();
		}

		for (RU ru : mappingManager.getObjectMapper().rus.values()) {
			ru.clean();
		}

		for (SubSystem subsys : mappingManager.getObjectMapper().subSystems.values()) {
			subsys.clean();
		}

		for (TTCPartition ttcPartition : mappingManager.getObjectMapper().ttcPartitions.values()) {
			ttcPartition.clean();
		}

	}
}
