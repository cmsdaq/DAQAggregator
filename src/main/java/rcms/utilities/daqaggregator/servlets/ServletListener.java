package rcms.utilities.daqaggregator.servlets;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.DAQAggregator;
import rcms.utilities.daqaggregator.DataResolutionManager;
import rcms.utilities.daqaggregator.TaskManager;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.persistence.PersistorManager;
import rcms.utilities.daqaggregator.reasoning.base.CheckManager;
import rcms.utilities.daqaggregator.reasoning.base.EventProducer;

public class ServletListener implements ServletContextListener {

	private static final Logger logger = Logger.getLogger(ServletListener.class);

	PersistorManager persistorManager = new PersistorManager("/tmp/mgladki/persistence");

	public void contextInitialized(ServletContextEvent e) {
		test2();

	}

	public void contextDestroyed(ServletContextEvent e) {

	}

	private void test2() {
		try {
			logger.info("Walking through all data..");
			persistorManager.walkAll();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// prepare data
		DataResolutionManager dataSegmentator = new DataResolutionManager();
		dataSegmentator.prepareMultipleResolutionData();
	}

	/**
	 * Load recent data to memory Process data from memory
	 */
	private void test() {
		try {
			logger.info("Loading historic data");
			persistorManager.loadRecent();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		new Thread() {
			public void run() {

				DAQAggregator da = new DAQAggregator();
				// da.run();
			}
		}.start();

		logger.info("Servlet initialized");

		Iterator<DAQ> iter = TaskManager.get().buf.iterator();
		DAQ daq = null;
		CheckManager checkManager = new CheckManager();
		while (iter.hasNext()) {
			daq = iter.next();
			checkManager.runCheckers(daq);
		}
		EventProducer.get().finish(new Date(daq.getLastUpdate()));
		logger.info(EventProducer.get().toString());
	}

}