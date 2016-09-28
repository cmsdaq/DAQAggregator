package rcms.utilities.daqaggregator.persistence;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

/**
 * This class explores time based directory structure
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class PersistenceExplorer {

	public PersistenceExplorer(FileSystemConnector fileSystemConnector) {
		super();
		this.fileSystemConnector = fileSystemConnector;
		df.setTimeZone(tz);
	}

	private final FileSystemConnector fileSystemConnector;

	private static final Logger logger = Logger.getLogger(PersistenceExplorer.class);

	TimeZone tz = TimeZone.getTimeZone("UTC");
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

	/**
	 * Explore time-based directory structure. Only chunk of data will be
	 * returned.
	 * 
	 * @param startTimestamp
	 *            explore after this date
	 * @param directory
	 *            base directory when exploration will take place
	 * @return tuple with chunk of explored files and timestamp of most recent
	 *         one
	 * @throws IOException
	 */
	public Pair<Long, List<File>> explore(Long startTimestamp, String directory) throws IOException {
		return explore(startTimestamp, Long.MAX_VALUE, directory, 2000);
	}

	/**
	 * Explore time-based directory structure. Only chunk of data will be
	 * returned.
	 * 
	 * @param startTimestamp
	 *            explore after this date
	 * @param endTimestamp
	 *            explore before this date
	 * @param directory
	 *            base directory when exploration will take place
	 * @return tuple with chunk of explored files and timestamp of most recent
	 *         one
	 * @throws IOException
	 */
	public Pair<Long, List<File>> explore(Long startTimestamp, Long endTimestamp, String directory) throws IOException {
		return explore(startTimestamp, endTimestamp, directory, 2000);
	}

	protected boolean enterDirectory(long startOfPeriod, long endOfPeriod, long startTimestamp, long endTimestamp) {

		logger.debug("Enter search period [" + df.format(new Date(startOfPeriod)) + "-"
				+ df.format(new Date(endOfPeriod)) + "] for target period [" + df.format(new Date(startTimestamp)) + "-"
				+ df.format(new Date(endTimestamp)) + "]?");
		if (endTimestamp <= startOfPeriod)
			return false;
		if (endOfPeriod <= startTimestamp)
			return false;
		return true;
	}

	/**
	 * Explore time-based directory structure. Only chunk of data will be
	 * returned.
	 * 
	 * @param startTimestamp
	 *            explore after this date
	 * @param endTimestamp
	 *            explore before this date
	 * @param chunkSize
	 *            chunk size to limit the returned data
	 * @param directory
	 *            base directory when exploration will take place
	 * @return tuple with chunk of explored files and timestamp of most recent
	 *         one
	 * @throws IOException
	 */
	public Pair<Long, List<File>> explore(Long startTimestamp, Long endTimestamp, String dir, int chunkSize)
			throws IOException {

		logger.debug("Exploring " + startTimestamp + "-" + endTimestamp + " in directory " + dir
				+ ", maximum chunk size " + chunkSize);

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 0);
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 1);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		Long startTime = System.currentTimeMillis();
		Long snapshotCount = 0L;
		long mostRecentExplored = 0L;

		List<File> result = new ArrayList<>();
		List<File> yearDirs = fileSystemConnector.getDirs(dir);

		long startOfSearchPeriod = 0L;
		long endOfSearchPeriod = 0L;

		boolean chunkComplete = false;
		for (File dirYear : yearDirs) {
			List<File> monthDirs = fileSystemConnector.getDirs(dirYear.getAbsolutePath());

			int year = Integer.parseInt(dirYear.getName());
			cal.set(Calendar.YEAR, year);
			startOfSearchPeriod = cal.getTimeInMillis();
			cal.set(Calendar.YEAR, year + 1);
			endOfSearchPeriod = cal.getTimeInMillis();

			boolean exploreYear = enterDirectory(startOfSearchPeriod, endOfSearchPeriod, startTimestamp, endTimestamp);
			logger.info("In year directory " + dirYear + ", explore it? " + exploreYear);

			if (exploreYear && !chunkComplete) {
				for (File monthDir : monthDirs) {
					int month = Integer.parseInt(monthDir.getName()) - 1;
					cal.set(Calendar.YEAR, year);
					cal.set(Calendar.MONTH, month);
					startOfSearchPeriod = cal.getTimeInMillis();
					cal.set(Calendar.MONTH, month + 1);
					endOfSearchPeriod = cal.getTimeInMillis();

					boolean exploreMonth = enterDirectory(startOfSearchPeriod, endOfSearchPeriod, startTimestamp,
							endTimestamp);
					logger.info("In month directory " + monthDir + ", explore it? " + exploreMonth);

					if (exploreMonth && !chunkComplete) {
						List<File> dayDirs = fileSystemConnector.getDirs(monthDir.getAbsolutePath());
						for (File dayDir : dayDirs) {

							int day = Integer.parseInt(dayDir.getName());
							cal.set(Calendar.YEAR, year);
							cal.set(Calendar.MONTH, month);
							cal.set(Calendar.DAY_OF_MONTH, day);
							startOfSearchPeriod = cal.getTimeInMillis();
							cal.set(Calendar.DAY_OF_MONTH, day + 1);
							endOfSearchPeriod = cal.getTimeInMillis();

							boolean exploreDay = enterDirectory(startOfSearchPeriod, endOfSearchPeriod, startTimestamp,
									endTimestamp);
							logger.info("In day directory " + dayDir + ", explore it? " + exploreDay);

							if (exploreDay && !chunkComplete) {
								List<File> hourDirs = fileSystemConnector.getDirs(dayDir.getAbsolutePath());
								for (File hourDir : hourDirs) {

									int hour = Integer.parseInt(hourDir.getName()) - 1;
									cal.set(Calendar.YEAR, year);
									cal.set(Calendar.MONTH, month);
									cal.set(Calendar.DAY_OF_MONTH, day);
									cal.set(Calendar.HOUR_OF_DAY, hour);
									startOfSearchPeriod = cal.getTimeInMillis();
									cal.set(Calendar.HOUR_OF_DAY, hour + 1);
									endOfSearchPeriod = cal.getTimeInMillis();

									boolean exploreHour = enterDirectory(startOfSearchPeriod, endOfSearchPeriod,
											startTimestamp, endTimestamp);
									logger.info("In hour directory " + hour + ", explore it? " + exploreHour);

									if (exploreHour && !chunkComplete) {
										List<File> snapshots = fileSystemConnector.getFiles(hourDir.getAbsolutePath());
										for (File snapshot : snapshots) {

											int dotIdx = snapshot.getName().indexOf(".");

											/**
											 * check for the extension existence
											 */
											if (dotIdx != -1) {
												Long timestamp = Long
														.parseLong(snapshot.getName().substring(0, dotIdx));

												// FIXME: this needs to be
												// improved
												if (startTimestamp < timestamp && timestamp < endTimestamp
														&& !chunkComplete) {

													if (timestamp > mostRecentExplored)
														mostRecentExplored = timestamp;

													result.add(snapshot);
													snapshotCount++;

													if (snapshotCount >= chunkSize) {
														chunkComplete = true;
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}

		Long endTime = System.currentTimeMillis();
		if (snapshotCount > 0)
			logger.info("Explored " + snapshotCount + " in " + (endTime - startTime) + "ms");
		Pair<Long, List<File>> entry = Pair.of(mostRecentExplored, result);
		return entry;
	}

}
