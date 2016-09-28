package rcms.utilities.daqaggregator.persistence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class FileSystemConnectorFake extends FileSystemConnector {

	private static final Logger logger = Logger.getLogger(FileSystemConnectorFake.class);

	private Pattern yearPattern = Pattern.compile("/base");
	private Pattern monthPattern = Pattern.compile("/base/(\\d+)");
	private Pattern dayPattern = Pattern.compile("/base/(\\d+)/(\\d+)");
	private Pattern hoursPattern = Pattern.compile("/base/(\\d+)/(\\d+)/(\\d+)");

	/**
	 * Linked hash map implementation - predicatable oreder is very important
	 * for explorer
	 */
	protected LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, List<String>>>>>> fakeFiles;

	/**
	 * This fake object represents sample data in file system. Time span of data
	 * is 6 months beginning on 2016/01/01
	 */
	public FileSystemConnectorFake() {

		long start = System.currentTimeMillis();
		int counter = 0;

		Long timestamp = 0L;
		logger.debug("Starging date of tests " + new Date(timestamp));
		fakeFiles = new LinkedHashMap<>();

		LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, List<String>>>>> years = new LinkedHashMap<>();
		for (Integer year = 2016; year <= 2016; year++) {

			LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, List<String>>>> months = new LinkedHashMap<>();
			for (Integer month = 1; month <= 6; month++) {

				LinkedHashMap<String, LinkedHashMap<String, List<String>>> days = new LinkedHashMap<>();
				for (Integer day = 1; day <= 31; day++) {

					LinkedHashMap<String, List<String>> hours = new LinkedHashMap<>();
					for (Integer hour = 0; hour <= 23; hour++) {

						Calendar cal = Calendar.getInstance();
						cal.set(Calendar.YEAR, year);
						cal.set(Calendar.MONTH, month - 1);
						cal.set(Calendar.DAY_OF_MONTH, day);
						cal.set(Calendar.HOUR_OF_DAY, hour);
						cal.set(Calendar.MINUTE, 0);
						cal.set(Calendar.SECOND, 0);
						cal.set(Calendar.MILLISECOND, 0);
						timestamp = cal.getTimeInMillis();
						// int initHour = (new Date(timestamp)).getHours();

						Calendar tmpCal = Calendar.getInstance();
						tmpCal.setTimeInMillis(timestamp);

						List<String> snapshots = new ArrayList<>();

						int snapshotCounter = 0;
						while (hour == tmpCal.get(Calendar.HOUR_OF_DAY)) {
							snapshots.add(timestamp.toString());
							timestamp += 3000;
							tmpCal.setTimeInMillis(timestamp);
							logger.trace(tmpCal.getTime());
							snapshotCounter++;
							counter++;
						}
						logger.trace("Prepared " + snapshotCounter + " fake snapshots");
						hours.put(((Integer) (hour + 1)).toString(), snapshots);
					}
					days.put(day.toString(), hours);
				}
				months.put(month.toString(), days);
			}
			years.put(year.toString(), months);
		}

		fakeFiles.put("base", years);

		long end = System.currentTimeMillis();

		logger.info("Fake file system initialization completed in " + (end - start) + " ms, with " + counter
				+ " fake files inside");
	}

	/**
	 * 
	 */
	@Override
	public List<File> getDirs(String file) throws IOException {

		logger.debug("Requested directories from: " + file);

		if (yearPattern.matcher(file).matches()) {
			List<File> result = new ArrayList<>();
			for (String year : fakeFiles.get("base").keySet()) {
				result.add(new File("/base/" + year));
			}
			logger.debug("Returning fake years: " + result);
			return result;
		} else if (monthPattern.matcher(file).matches()) {
			List<File> result = new ArrayList<>();
			String[] parts = file.split("/");
			logger.debug(Arrays.toString(parts));
			for (String month : fakeFiles.get("base").get(parts[2]).keySet()) {
				result.add(new File(file + "/" + month));
			}
			logger.debug("Returning fake months: " + result);
			return result;
		} else if (dayPattern.matcher(file).matches()) {
			List<File> result = new ArrayList<>();
			String[] parts = file.split("/");
			for (String day : fakeFiles.get("base").get(parts[2]).get(parts[3]).keySet()) {
				result.add(new File(file + "/" + day));
			}
			logger.debug("Returning fake days: " + result);
			return result;
		} else if (hoursPattern.matcher(file).matches()) {
			List<File> result = new ArrayList<>();
			String[] parts = file.split("/");
			for (String hour : fakeFiles.get("base").get(parts[2]).get(parts[3]).get(parts[4]).keySet()) {
				result.add(new File(file + "/" + hour));
			}
			logger.debug("Returning fake hours: " + result);
			return result;
		}

		return super.getDirs(file);
	}

	@Override
	public List<File> getFiles(String file) throws IOException {

		logger.debug("Requested files from directory: " + file);
		List<File> result = new ArrayList<>();
		String[] parts = file.split("/");
		for (String name : fakeFiles.get("base").get(parts[2]).get(parts[3]).get(parts[4]).get(parts[5])) {
			result.add(new File(file + "/" + name + ".smile"));
		}
		logger.debug("Returning " + result.size() + " fake files: " + result);
		return result;
	}

}
