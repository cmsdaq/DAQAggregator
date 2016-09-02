package rcms.utilities.daqaggregator.persistence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

public class PersistenceExplorer {

	private Logger logger = Logger.getLogger(PersistenceExplorer.class);

	private PersistenceExplorer() {
	}

	private static PersistenceExplorer instance;

	public static PersistenceExplorer get() {
		if (instance == null)
			instance = new PersistenceExplorer();
		return instance;
	}

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
	public Entry<Long, List<File>> explore(Long startTimestamp, String directory) throws IOException {
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
	public Entry<Long, List<File>> explore(Long startTimestamp, Long endTimestamp, String directory)
			throws IOException {
		return explore(startTimestamp, endTimestamp, directory, 2000);
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
	public Entry<Long, List<File>> explore(Long startTimestamp, Long endTimestamp, String dir, int chunkSize)
			throws IOException {

		logger.info("Exploring " + startTimestamp + "-" + endTimestamp + " in directory " + dir
				+ ", maximum chunk size " + chunkSize);

		Long tmpLast = startTimestamp;
		Long startTime = System.currentTimeMillis();
		Long snapshotCount = 0L;

		List<File> result = new ArrayList<>();

		List<File> yearDirs = getDirs(dir);

		for (File dirYear : yearDirs) {
			List<File> monthDirs = getDirs(dirYear.getAbsolutePath());

			for (File monthDir : monthDirs) {
				List<File> dayDirs = getDirs(monthDir.getAbsolutePath());

				for (File dayDir : dayDirs) {
					List<File> hourDirs = getDirs(dayDir.getAbsolutePath());

					for (File hourDir : hourDirs) {
						List<File> snapshots = getFiles(hourDir.getAbsolutePath());

						for (File snapshot : snapshots) {

							int dotIdx = snapshot.getName().indexOf(".");

							if (dotIdx != -1) {
								Long timestamp = Long.parseLong(snapshot.getName().substring(0, dotIdx));

								// FIXME: this needs to be improved
								if (startTimestamp < timestamp && timestamp < endTimestamp
										&& snapshotCount < chunkSize) {
									startTimestamp = timestamp;
									result.add(snapshot);
									snapshotCount++;
								}
							}
						}

					}
				}
			}
		}

		Long endTime = System.currentTimeMillis();
		logger.info("Explored " + snapshotCount + " in " + (endTime - startTime) + "ms");
		Entry<Long, List<File>> entry = new SimpleEntry<>(startTimestamp, result);
		return entry;
	}

	public List<File> getDirs(String file) throws IOException {
		List<File> result = new ArrayList<>();

		File folder = new File(file);

		if (folder.exists() && folder.isDirectory()) {

			File[] listOfFiles = folder.listFiles();

			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {

					System.out.println("File " + listOfFiles[i].getName());
				} else if (listOfFiles[i].isDirectory()) {
					// directory name must be always parsable integer
					try {
						Integer.parseInt(listOfFiles[i].getName());
						result.add(listOfFiles[i]);
					} catch (NumberFormatException e) {
						// ignore directory
					}
				}
			}
			Collections.sort(result, DirComparator);

			return result;
		} else {
			throw new FileNotFoundException("Folder does not exist " + folder.getAbsolutePath());
		}
	}

	public List<File> getFiles(String file) throws IOException {
		List<File> result = new ArrayList<>();

		File folder = new File(file);

		if (folder.exists() && folder.isDirectory()) {

			File[] listOfFiles = folder.listFiles();

			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {

					result.add(listOfFiles[i]);
				} else if (listOfFiles[i].isDirectory()) {
					System.out.println("Directory " + listOfFiles[i].getName());
				}
			}
			Collections.sort(result, FileComparator);

			return result;
		} else {
			throw new FileNotFoundException("Folder does not exist " + folder.getAbsolutePath());
		}
	}

	public static Comparator<File> DirComparator = new Comparator<File>() {
		public int compare(File path1, File path2) {
			Integer filename1 = Integer.parseInt(path1.getName().toString());
			Integer filename2 = Integer.parseInt(path2.getName().toString());
			return filename1.compareTo(filename2);
		}
	};

	public static Comparator<File> FileComparator = new Comparator<File>() {
		public int compare(File path1, File path2) {
			String filename1 = path1.getName().toString();
			String filename2 = path2.getName().toString();
			return filename1.compareTo(filename2);
		}
	};

}
