package rcms.utilities.daqaggregator.reasoning.base;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.servlets.Entry;

public class EventProducer {

	private static final Logger logger = Logger.getLogger(EventProducer.class);

	private static EventProducer instance;

	public static EventProducer get() {
		if (instance == null)
			instance = new EventProducer();
		return instance;
	}

	@Override
	public String toString() {
		return "EventProducer [result=" + result + ", states=" + states + ", unfinished=" + unfinished + "]";
	}

	private EventProducer() {
	}

	private final List<Entry> result = new ArrayList<>();

	public List<Entry> getResult() {
		return result;
	}

	public void finish(Date date) {
		for (Entry entry : unfinished.values()) {
			entry.setEnd(date);
			entry.calculateDuration();
		}
	}

	private final Map<String, Boolean> states = new HashMap<>();
	private final Map<String, Entry> unfinished = new HashMap<>();

	/**
	 * Produces events for value 111000111000 will produce 2 events
	 * corresponding to 1 start and end time
	 */
	public void produce(SimpleProblem checker, boolean value, Date date) {
		produce(checker, value, date, checker.getLevel());
	}

	/**
	 * 00000100000100000100 will produce 3 events corresponding to 1 start and
	 * ending on next 1 start
	 */
	public void produce(Comparator comparator, boolean value, Date last, Date current) {

		if (value) {
			logger.info("New lazy event " + current);
			produce(comparator, !value, last, comparator.getLevel());
			produce(comparator, value, current, comparator.getLevel());
		}
	}

	private void produce(Classificable classificable, boolean value, Date date, Level level) {
		// get current state
		String className = classificable.getClass().getSimpleName();
		String content = classificable.getText();
		if (states.containsKey(className)) {
			boolean currentState = states.get(className);

			if (currentState != value) {
				finishOldAddNew(className, content, value, date, level);
				states.put(className, value);
			}
		}

		// no prior states
		else {
			states.put(className, value);
			finishOldAddNew(className, content, value, date, level);
		}
	}

	private void finishOldAddNew(String className, String content, Boolean value, Date date, Level level) {


		/* finish old entry */
		if (unfinished.containsKey(className)) {
			Entry toFinish = unfinished.get(className);
			toFinish.setEnd(date);
			toFinish.calculateDuration();
		}

		/* add new entry */
		Entry entry = new Entry();
		entry.setContent(content);
		entry.setShow(value);
		entry.setStart(date);
		entry.setGroup(level.getCode());

		result.add(entry);
		unfinished.put(className, entry);
	}

}
