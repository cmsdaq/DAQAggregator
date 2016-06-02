package rcms.utilities.daqaggregator;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

public class DataResolutionManager {

	long timestampOfLastMinute = 0;
	long timestampOfLastHour = 0;

	private static final Logger logger = Logger.getLogger(DataResolutionManager.class);

	private void prepareMinuteResolutionData() {
		int lastMinute = -1;
		int elements = 0;
		int thisround = 0;
		DummyDAQ minuteDaq = new DummyDAQ();
		Calendar calendar = Calendar.getInstance();
		for (DummyDAQ daq : TaskManager.get().rawData) {

			long curr = daq.getLastUpdate();

			if (curr > timestampOfLastMinute) {
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

					Calendar cal = Calendar.getInstance();
					cal.setTime(date);
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MILLISECOND, 0);
					Date refinedDate = cal.getTime();

					minuteDaq.setLastUpdate(refinedDate.getTime());
					timestampOfLastMinute = refinedDate.getTime();
					thisround++;
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

		}

		logger.trace("Prepared minute resolution data (" + TaskManager.get().rawDataMinute.size()
				+ " entries) from raw data (" + TaskManager.get().rawData.size() + " entries), " + thisround
				+ " in this round");
		logger.debug("Prepared minute data" + thisround + " in this round");

	}

	/* prepare hour data */
	private void prepareHourResolutionData() {

		int lastHour = -1;
		int elements = 0;
		int thisround = 0;
		DummyDAQ hourDaq = new DummyDAQ();
		Calendar calendar = Calendar.getInstance();
		for (DummyDAQ daq : TaskManager.get().rawData) {

			long curr = daq.getLastUpdate();
			if (curr > timestampOfLastHour) {
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

					Calendar cal = Calendar.getInstance();
					cal.setTime(date);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MILLISECOND, 0);
					Date refinedDate = cal.getTime();

					timestampOfLastHour = refinedDate.getTime();
					thisround++;
					hourDaq.setLastUpdate(refinedDate.getTime());

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
		}

		logger.trace(
				"Prepared hourly resolution data (" + TaskManager.get().rawDataHour.size() + " entries) from raw data ("
						+ TaskManager.get().rawData.size() + " entries), " + thisround + " in this round");
		logger.debug("Prepared hour data" + thisround + " in this round");

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
