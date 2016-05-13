package rcms.utilities.daqaggregator;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

public class DataResolutionManager {

	private static final Logger logger = Logger.getLogger(DataResolutionManager.class);

	private void prepareMinuteResolutionData() {
		int lastMinute = -1;
		int elements = 0;
		DummyDAQ minuteDaq = new DummyDAQ();
		Calendar calendar = Calendar.getInstance();
		for (DummyDAQ daq : TaskManager.get().rawData) {
			elements++;
			Date date = new Date(daq.getLastUpdate());

			calendar.setTime(date);
			int currentMinute = calendar.get(Calendar.MINUTE);

			if (lastMinute == -1)
				lastMinute = currentMinute;

			minuteDaq.setValue(minuteDaq.getValue() + daq.getValue());

			/* finish the minute daq */
			if (lastMinute != currentMinute) {
				
				logger.debug("Next minute " + lastMinute + "!=" + currentMinute);

				minuteDaq.setValue(minuteDaq.getValue() / elements);
				minuteDaq.setLastUpdate(date.getTime()); // TODO: make minutes and seconds zero
				elements = 0;
				lastMinute = currentMinute;
				TaskManager.get().rawDataMinute.add(minuteDaq);
				minuteDaq = new DummyDAQ();
			}
			/* process another value to daq */
			else {
				logger.debug("Same minute already for " + elements + " time");
			}

		}

		logger.info("Prepared minute resolution data (" + TaskManager.get().rawDataMinute.size()
				+ " entries) from raw data (" + TaskManager.get().rawData.size() + " entries)");

	}

	/* prepare hour data */
	private void prepareHourResolutionData() {

		int lastHour = -1;
		int elements = 0;
		DummyDAQ hourDaq = new DummyDAQ();
		Calendar calendar = Calendar.getInstance();
		for (DummyDAQ daq : TaskManager.get().rawData) {
			elements++;
			Date date = new Date(daq.getLastUpdate());

			calendar.setTime(date);
			int hours = calendar.get(Calendar.HOUR_OF_DAY);

			if (lastHour == -1)
				lastHour = hours;

			hourDaq.setValue(hourDaq.getValue() + daq.getValue());

			/* finish the minute daq */
			if (lastHour != hours) {
				
				logger.debug("Next hour " + lastHour + "!=" + hours);

				hourDaq.setValue(hourDaq.getValue() / elements);
				hourDaq.setLastUpdate(date.getTime()); // TODO: make minutes and seconds zero
				elements = 0;
				lastHour = hours;
				TaskManager.get().rawDataHour.add(hourDaq);
				hourDaq = new DummyDAQ();
			}
			/* process another value to daq */
			else {
				logger.debug("Same hour already for " + elements + " time");
			}

		}

		logger.info("Prepared hourly resolution data (" + TaskManager.get().rawDataHour.size()
				+ " entries) from raw data (" + TaskManager.get().rawData.size() + " entries)");

	}

	private void prepareDayResolutionData() {

	}

	private void prepareMonthResolutionData() {

	}

	public void prepareMultipleResolutionData() {
		prepareMinuteResolutionData();
		prepareDayResolutionData();
		prepareHourResolutionData();
		prepareMonthResolutionData();
	}

}
