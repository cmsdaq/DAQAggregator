package rcms.utilities.daqaggregator.datasource;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;

/**
 * This class parses date transparently on bug in flashlists fixed 2016-10-07.
 * 
 * Before this date all flashlists date fields have format
 * "EEE, MMM dd yyyy HH:mm:ss Z". After this date ISO8601
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class DateParser {

	private static final Logger logger = Logger.getLogger(DateParser.class);

	public static Date parseDateTransparently(String dateString) {
		logger.debug("Parsing date from string: " + dateString);
		try {
			Date date = DatatypeConverter.parseDateTime(dateString).getTime();
			return date;
		} catch (IllegalArgumentException e) {
			DateFormat df2 = new SimpleDateFormat("EEE, MMM dd yyyy HH:mm:ss Z");
			Date date;
			try {
				date = df2.parse(dateString);
				logger.debug("Parsed date old way: " + date + ", from string: " + dateString);
				return date;
			} catch (ParseException e1) {
				logger.fatal("Cannot parse date string " + dateString + " using known formats.");
				// e1.printStackTrace();
				return null;
			}

		}
	}

}
