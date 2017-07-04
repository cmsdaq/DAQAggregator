package rcms.utilities.daqaggregator.data.helper;

import org.apache.commons.math3.util.Precision;
import org.apache.log4j.Logger;

public class BackpressureConverter {

	/** Last value of accumulated backpressure in seconds */
	private double lastValue;

	/** Last latched time */
	private double lastTime;

	/** Last backpressure value */
	private double lastResult;

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(BackpressureConverter.class);

	/**
	 * Calculate backpressure
	 * 
	 * @param accumulatedBackpressure
	 *            accumulated backpressure in seconds
	 * @param latchedTime
	 *            latched time in seconds
	 * @return backpressure value 0-1
	 */
	public double calculate(double accumulatedBackpressure, double latchedTime) {

		/*
		 * restart the whole thing if the timer is restarted - that would result
		 * in negative delta
		 */
		if (latchedTime < lastTime) {
			lastValue = 0;
			lastTime = 0;
		}

		double deltaTime = latchedTime - lastTime;

		double deltaVal = accumulatedBackpressure - lastValue;

		double backpressure = deltaVal / deltaTime;

		/* restart of accumulated backpressure will be handled here */
		if (backpressure < 0)
			backpressure = 0;

		/* Regular conditions */
		if (latchedTime > lastTime) {
			lastValue = accumulatedBackpressure;
			lastTime = latchedTime;
			lastResult = backpressure;
			return backpressure;
		}

		// repeat in case delta time cannot be >0
		else {
			return lastResult;
		}
	}

	/**
	 * Calculate backpressure
	 * 
	 * @param accumulatedBackpressure
	 *            accumulated backpressure in seconds
	 * @param latchedTime
	 *            latched time in seconds
	 * @return backpressure value in percentage
	 */
	public float calculatePercent(double accumulatedBackpressure, double latchedTime, boolean round) {
		float value = (float) (calculate(accumulatedBackpressure, latchedTime) * 100);
		if (round) {
			return Precision.round(value, 1);
		} else {
			return value;
		}
	}
}
