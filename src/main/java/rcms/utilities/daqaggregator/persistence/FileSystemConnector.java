package rcms.utilities.daqaggregator.persistence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FileSystemConnector {

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
