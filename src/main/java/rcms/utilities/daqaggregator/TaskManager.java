package rcms.utilities.daqaggregator;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;

public class TaskManager {

	private static TaskManager instance;

	private static final Logger logger = Logger.getLogger(TaskManager.class);

	private TaskManager() {
		buf = new CircularFifoQueue<>(5000);
		rawData = new ArrayList<>();
		rawDataMinute = new ArrayList<>();
		rawDataHour = new ArrayList<>();
		rawDataDay = new ArrayList<>();
	}

	public static TaskManager get() {
		if (instance == null)
			instance = new TaskManager();
		return instance;
	}

	public CircularFifoQueue<DAQ> buf;
	
	
	public List<DummyDAQ> rawData;
	public List<DummyDAQ> rawDataMinute;
	public List<DummyDAQ> rawDataHour;
	public List<DummyDAQ> rawDataDay;

	public static void main(String[] args) {
		TaskManager tm = TaskManager.get();
	}

}
