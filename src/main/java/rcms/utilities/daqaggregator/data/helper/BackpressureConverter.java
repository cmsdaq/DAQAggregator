package rcms.utilities.daqaggregator.data.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

public class BackpressureConverter {

	private double lastValue;
	private long lastTime;
	private double lastResult;

	private static final Logger logger = Logger.getLogger(BackpressureConverter.class);

	public double calculate(double value, long time) {

		int deltaTime = (int) (time - lastTime);

		double deltaVal = value - lastValue;

		double backpressure = (1000 * deltaVal) / deltaTime;

		/* restart of accumulated backpressure will be handled here */
		if (backpressure < 0)
			backpressure = 0;

		// Regular conditions
		if (time > lastTime) {
			lastValue = value;
			lastTime = time;
			lastResult = backpressure;
			return backpressure;
		}

		// repeat in case delta time cannot be >0
		else {
			return lastResult;
		}
	}

	private static final SimpleDateFormat df = new SimpleDateFormat("EEE, MMM dd yyyy HH:mm:ss Z");

	public double calculate(double value, String time) {
		long timeLong;
		try {
			timeLong = df.parse(time).getTime();
			return calculate(value, timeLong);
		} catch (ParseException e) {
			logger.warn("Problem parsing timestamp " + time + " using " + df.toPattern());
			return lastResult;
		}
	}

	public float calculatePercent(double value, String time) {
		return (float) (calculate(value, time) * 100);
	}
}
