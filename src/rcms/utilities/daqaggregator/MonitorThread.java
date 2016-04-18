package rcms.utilities.daqaggregator;

import java.util.List;

import rcms.utilities.hwcfg.dp.DAQPartition;

public class MonitorThread extends Thread {

	private List<String> lasURLs;

	private int sessionId;
	
	public MonitorThread(List<String> lasURLs, DAQPartition dp, String dpset_path, int sid, String setupName) {

		this.lasURLs = lasURLs;
		sessionId = sid;
	}

	public void run() {

		try {
			MonitorDataRetriever mdr1 = new MonitorDataRetrieverLAS(lasURLs, sessionId);

			while (true) {

				try {
					try {
						System.out.println("Thread1: Starting data retrieve.");
						mdr1.clearCache();
						System.out.println("Thread1: Cache cleared.");
						mdr1.retrieveMonitorData();
						System.out.println("Thread1: Calculating derived quantities.");
						mdr1.calculateDerivedQuantities();
					}
					catch (Exception e) {
						e.printStackTrace();
						System.out.println("Thread1: Error calculating derived quantities. Trying to continue, anyways ... ");
					}

					//					System.out.println("Thread1: Generating pages.");
					//					viz.generatePages(mdr1, linkToISBMon, "../" + setupName + "_isb/", "click here for experimental real-time monitoring");

					System.out.println("Thread1: Created DAQ XMAS page(s). Sleeping 0.5 seconds...");
					try {
						sleep(500);
					} catch (InterruptedException e) {
						return;
					}
				}
				catch (Exception e) {
					System.out.println("Thread1: Error creating XMAS pages in second thread.");
					e.printStackTrace();
					try {
						sleep(10000);
					} catch (InterruptedException e1) {
						return;
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
