package rcms.utilities.daqaggregator.datasource;

import java.util.Collection;

import org.apache.log4j.Logger;
import rcms.utilities.daqaggregator.Application;
import rcms.utilities.daqaggregator.Settings;

import rcms.utilities.daqaggregator.data.*;
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
		
		cleanStructure(); //first clean structure and set default values
		
		for (Flashlist flashlist : flashlists) {

			FlashlistDispatcher dispatcher = new FlashlistDispatcher(
				Application.get().getProp(Settings.SESSION_L0FILTER1)
      );
			
			dispatcher.dispatch(flashlist, mappingManager);
		}
		long stopTime = System.currentTimeMillis();
		int time = (int) (stopTime - startTime);
		logger.debug("Mapping all flashlists finished in " + time + "ms");
	}

	private void cleanStructure() {
		
		mappingManager.getObjectMapper().daq.clean();
		
		mappingManager.getObjectMapper().daq.getTcdsGlobalInfo().clean();
		
		for (FED fed : mappingManager.getObjectMapper().feds.values()) {
			fed.clean();
		}
		
		for (BU bu : mappingManager.getObjectMapper().bus.values()){
			bu.clean();
		}
		
		for (FMM fmm : mappingManager.getObjectMapper().fmms.values()){
			fmm.clean();
		}
		
		for (FRL frl : mappingManager.getObjectMapper().frls.values()){
			frl.clean();
		}
		
		for (FRLPc frlpc : mappingManager.getObjectMapper().frlPcs.values()){
			frlpc.clean();
		}
		
		for (RU ru : mappingManager.getObjectMapper().rus.values()){
			ru.clean();
		}
		
		for (SubSystem subsys : mappingManager.getObjectMapper().subSystems.values()){
			subsys.clean();
		}
		
		for (TTCPartition ttcPartition : mappingManager.getObjectMapper().ttcPartitions.values()){
			ttcPartition.clean();
		}

	}
}
