package rcms.utilities.daqaggregator;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.common.db.DBConnectorException;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.datasource.*;
import rcms.utilities.daqaggregator.datasource.F3DataRetriever.CpuLoadType;
import rcms.utilities.daqaggregator.persistence.PersistenceFormat;
import rcms.utilities.daqaggregator.persistence.PersistorManager;
import rcms.utilities.hwcfg.HardwareConfigurationException;
import rcms.utilities.hwcfg.InvalidNodeTypeException;
import rcms.utilities.hwcfg.PathNotFoundException;

public class DAQAggregator {

    private static final Logger logger = Logger.getLogger(DAQAggregator.class);

    public static void main(String[] args) {

        try {
            String propertiesFile = "DAQAggregator.properties";
            if (args.length > 0)
                propertiesFile = args[0];
            logger.info("DAQAggregator started with properties file '" + propertiesFile + "'");

            try {
                String execPath = URLDecoder
                        .decode(DAQ.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");
                logger.info("Path of current executable: " + execPath);

            } catch (UnsupportedEncodingException e2) {

            }

            try {
                Application.initialize(propertiesFile);
            } catch (DAQException e) {
                logger.fatal(e.getCode().getName() + ": " + e.getMessage());
                System.exit(e.getCode().getCode());
            }

			/*
             * Run mode from properties file
			 */
            RunMode runMode = RunMode.decode(Application.get().getProp(Settings.RUN_MODE));
            logger.info("Run mode:" + runMode);

            int minimumSnapshotPeriod = 2000;
            String minPeriod = Application.get().getProp(Settings.RUN_SAMPLING);
            if (minPeriod != null) {
                try {
                    minimumSnapshotPeriod = Integer.parseInt(minPeriod);
                } catch (NumberFormatException e) {
                    logger.warn("Could not parse minimum snapshot period from: " + minimumSnapshotPeriod);
                }
            } else {
                logger.info("No minimum snapshot period supplied. Using default of " + minimumSnapshotPeriod + "ms");
            }

			/*
			 * Persist mode from properties file
			 */
            PersistMode persistMode = PersistMode.decode(Application.get().getProp(Settings.PERSISTENCE_MODE));
            logger.info("Persist mode:" + persistMode);

            Pair<MonitorManager, PersistorManager> initializedManagers;

            initializedManagers = initialize(runMode);

            MonitorManager monitorManager = initializedManagers.getLeft();
            PersistorManager persistenceManager = initializedManagers.getRight();

            switch (runMode) {
                case FILE:
                    try {
                        int problems = 0;
                        while (true) {
                            try {

                                Triple<DAQ, Collection<Flashlist>, Boolean> result = monitor(monitorManager);

                                if (result == null) {
                                    problems++;
                                    logger.info("Unsuccessful iteration, already for " + problems + "time(s)");
                                } else {
                                    persist(persistenceManager, persistMode, result);
                                }
                            } catch (DAQException e) {
                                if (e.getCode() == DAQExceptionCode.NoMoreFlashlistSourceFiles) {
                                    throw e;
                                } else {
                                    logger.error(e);
                                    monitorManager.skipToNextSnapshot();
                                }
                            }
                        }
                    } catch (DAQException e) {
                        if (e.getCode() == DAQExceptionCode.NoMoreFlashlistSourceFiles)
                            logger.info("All flashlist files processed");
                        else
                            throw e;
                    } catch (HardwareConfigurationException | PathNotFoundException | InvalidNodeTypeException e) {
                        e.printStackTrace();
                    }
                    break;
                case RT:
                    while (true) {
                        try {

                            long start = System.currentTimeMillis();

                            Triple<DAQ, Collection<Flashlist>, Boolean> result = monitor(monitorManager);
                            persist(persistenceManager, persistMode, result);

                            long end = System.currentTimeMillis();
                            int resultTime = (int) (end - start);

                            logger.info("Monitor & persist in " + resultTime);

                            if (resultTime < minimumSnapshotPeriod) {
                                int diff = minimumSnapshotPeriod - resultTime;

                                logger.info("Minimum snapshot period is set to: " + minimumSnapshotPeriod + "ms, waiting "
                                        + diff + "ms before next snapshot");

                                try {
                                    Thread.sleep(diff);
                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();
                                }
                            }

                        } catch (DAQException e) {
                            logger.error(e.getMessage());
                            logger.info("Going to sleep for 10 seconds before trying again...");
                            try {
                                Thread.sleep(10000);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        } catch (Exception e) {
                            logger.fatal("Fatal problem in RT loop, unknown problem, going to sleep for 2 minutes");
                            e.printStackTrace();

                            try {
                                Thread.sleep(120000);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                case SPECIAL:
                    List<Pair<Long, Float>> values = new ArrayList<>();
                    try {
                        while (true) {
                            try {
                                logger.info("Special iteration ...");
                                Triple<DAQ, Collection<Flashlist>, Boolean> result = monitor(monitorManager);
                                long a = result.getLeft().getLastUpdate();
                                float b = result.getLeft().getFedBuilderSummary().getRate();
                                values.add(Pair.of(a, b));

                            } catch (PathNotFoundException | InvalidNodeTypeException e) {
                                e.printStackTrace();
                            } catch (DAQException e) {
                                if (e.getCode() == DAQExceptionCode.NoMoreFlashlistSourceFiles) {
                                    throw e;
                                } else {
                                    logger.error(e);
                                    monitorManager.skipToNextSnapshot();
                                }
                            }
                        }
                    } catch (DAQException e) {
                        if (e.getCode() == DAQExceptionCode.NoMoreFlashlistSourceFiles) {
                            logger.info("All flashlist files processed");
                            ObjectMapper om = new ObjectMapper();
                            String result = om.writeValueAsString(values);
                            System.out.println(result);
                        } else
                            throw e;
                    }

            }

        } catch (DBConnectorException | HardwareConfigurationException | IOException e1) {
            e1.printStackTrace();
        }
    }

    private static Triple<DAQ, Collection<Flashlist>, Boolean> monitor(MonitorManager monitorManager)
            throws HardwareConfigurationException, PathNotFoundException, InvalidNodeTypeException {

        Triple<DAQ, Collection<Flashlist>, Boolean> a = monitorManager.getSystemSnapshot();
        return a;
    }

    private static void persist(PersistorManager persistorManager, PersistMode persistMode,
                                Triple<DAQ, Collection<Flashlist>, Boolean> a) {
        switch (persistMode) {
            case SNAPSHOT:
                persistorManager.persistSnapshot(a.getLeft());
                break;
            case FLASHLIST:
                persistorManager.persistFlashlists(a.getMiddle());
                break;
            case ALL:
                persistorManager.persistSnapshot(a.getLeft());
                persistorManager.persistFlashlists(a.getMiddle());
                break;
        }
    }

    public static Pair<MonitorManager, PersistorManager> initialize(RunMode runMode)
            throws DBConnectorException, HardwareConfigurationException, IOException {

        long start = System.currentTimeMillis();
		/*
		 * Setup database
		 */
        HardwareConnector hardwareConnector = new HardwareConnector();
        hardwareConnector.initialize(Application.get().getProp());

		/*
		 * Get persistence dirs
		 */
        String snapshotPersistenceDir = Application.get().getProp(Settings.PERSISTENCE_SNAPSHOT_DIR);
        String flashlistPersistenceDir = Application.get().getProp(Settings.PERSISTENCE_FLASHLIST_DIR);

        PersistMode persistMode = PersistMode.decode(Application.get().getProp(Settings.PERSISTENCE_MODE));

        if (persistMode == PersistMode.SNAPSHOT || persistMode == PersistMode.ALL)
            logger.info("Snapshots will be persisted at: " + snapshotPersistenceDir);
        if (persistMode == PersistMode.FLASHLIST || persistMode == PersistMode.ALL)
            logger.info("Flashlists will be persisted at: " + flashlistPersistenceDir);

		/*
		 * Format of snapshot from properties file
		 */
        PersistenceFormat flashlistFormat = PersistenceFormat
                .decode(Application.get().getProp(Settings.PERSISTENCE_FLASHLIST_FORMAT));
        PersistenceFormat snapshotFormat = PersistenceFormat
                .decode(Application.get().getProp(Settings.PERSISTENCE_SNAPSHOT_FORMAT));

        PersistorManager persistorManager = new PersistorManager(snapshotPersistenceDir, flashlistPersistenceDir,
                snapshotFormat, flashlistFormat);

        boolean suppressFailedRequests = false;
        suppressFailedRequests = Boolean.parseBoolean(Application.get().getProp(Settings.SUPPRESS_HTTP_FAILED));

        FlashlistRetriever flashlistRetriever = null;
        switch (runMode) {
            case RT:
                flashlistRetriever = new LASFlashlistRetriever(suppressFailedRequests);
                break;
            case FILE:
            case SPECIAL:
                FileFlashlistRetriever fileFlashlistRetriever = new FileFlashlistRetriever(flashlistPersistenceDir,
                        flashlistFormat);
                flashlistRetriever = fileFlashlistRetriever;
                long startLimit = Long.parseLong(Application.get().getProp(Settings.PERSISTENCE_LIMIT));
                fileFlashlistRetriever.prepare(startLimit);
                break;
        }

        String filter1 = Application.get().getProp(Settings.SESSION_L0FILTER1);
        String filter2 = Application.get().getProp(Settings.SESSION_L0FILTER2);
        SessionRetriever sessionRetriever = new SessionRetriever(filter1, filter2);


        boolean f3Enabled = Boolean.parseBoolean(Application.get().getProp(Settings.F3_ENABLED));
        String hltUrl = Application.get().getProp(Settings.F3_HLT_URL);
        String diskUrl = Application.get().getProp(Settings.F3_DISK_URL);
        String crashesUrl = Application.get().getProp(Settings.F3_CRASHES_URL);
        String cpuLoadUrl = Application.get().getProp(Settings.F3_CPU_LOAD_URL);
        String cpuLoadType = Application.get().getProp(Settings.F3_CPU_LOAD_TYPE);
        String storageManagerUrl = Application.get().getProp(Settings.F3_STORAGE_MANAGER_URL);
        F3DataRetriever f3DataRetriever = null;


        if (f3Enabled && !"".equals(hltUrl) && !"".equals(diskUrl) && !"".equals(crashesUrl) && !"".equals(cpuLoadUrl) && !"".equals(cpuLoadType)) {
            logger.info("F3 monitoring is enabled and set to following urls: " + hltUrl + ", " + diskUrl);
            f3DataRetriever = new F3DataRetriever(new Connector(false), hltUrl, diskUrl,crashesUrl,
                    cpuLoadUrl, CpuLoadType.getByKey(cpuLoadType),
                    storageManagerUrl);
        } else if (f3Enabled) {
            throw new DAQException(DAQExceptionCode.MissingProperty, "Specify url for F3 data retrieval. Required: " + Settings.F3_DISK_URL.getKey() + ", " + Settings.F3_HLT_URL.getKey() + ", " + Settings.F3_CPU_LOAD_URL.getKey() + ", " + Settings.F3_CPU_LOAD_TYPE.getKey());
        } else {
            logger.info("F3 monitoring is disabled");
        }


        MonitorManager monitorManager = new MonitorManager(flashlistRetriever, sessionRetriever, hardwareConnector, f3DataRetriever);

        int timeToInitialize = (int) (System.currentTimeMillis() - start);

        logger.info("DAQAggregator initialized in " + timeToInitialize + "ms");
        logger.info("----------------------------------");
        return Pair.of(monitorManager, persistorManager);

    }

}
