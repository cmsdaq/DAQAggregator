package rcms.utilities.daqaggregator.datasource;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import rcms.utilities.daqaggregator.Application;
import rcms.utilities.daqaggregator.ProxyManager;
import rcms.utilities.daqaggregator.data.DAQ;

/**
 * Retrieves date from F3
 *
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
public class F3DataRetriever {
    public final static String PHYSICS_STREAM_NAME = "Physics";
    private static final Logger logger = Logger.getLogger(F3DataRetriever.class);
    private final Connector connector;
    private final ObjectMapper mapper;
    private final String hltUrl;
    private final String diskUrl;
    private final String crashUrl;
    private final String cpuLoadUrl;

    /** currently known CPU load types returned by F3mon (the strings
        corresponds to what F3mon uses in the returned results) */
    public static enum CpuLoadType {

        AVG_UNCORR("avg uncorr"),
        HTCORR_20PERCENT("20% htcor"),
        HTCORR_QUADRATIC("20% htcor(2x-x*x)");

        private CpuLoadType(String key) {
            this.key = key;
        }

        private final String key;

        public String getKey() {
            return key;
        }

        public static CpuLoadType getByKey(String key) {
            for (CpuLoadType result : CpuLoadType.values()) {
                if (result.getKey().equals(key)) {
                    return result;
                }
            }
            
            // not found
            throw new IllegalArgumentException("could not find CpuLoadType with key \"" + key + "\"");
        }
    }

    /** type of cpu load to retrieve from F3mon */
    private final CpuLoadType cpuLoadType;

    public F3DataRetriever(Connector connector, String hltUrl, String diskUrl, String crashUrl,
            String cpuLoadUrl, CpuLoadType cpuLoadType) {
        this.mapper = new ObjectMapper();
        this.connector = connector;
        this.hltUrl = hltUrl;
        this.diskUrl = diskUrl;
        this.crashUrl = crashUrl;
        this.cpuLoadUrl = cpuLoadUrl + "?setup=cdaq&intlen=30&int=1";
        this.cpuLoadType = cpuLoadType;
    }

    /**
     * Quick test F3
     *
     * @param args
     */
    public static void main(String[] args) {
        F3DataRetriever f3dr = new F3DataRetriever(new Connector(false), "http://es-cdaq.cms/sc/php/stream_summary_last.php", "http://es-cdaq.cms/sc/php/summarydisks.php", "http://es-cdaq.cms/sc/php/resource_status.php", "http://cmsdaqfff/prod/sc/php/cpuusage.php", CpuLoadType.HTCORR_QUADRATIC);
        try {
            Application.initialize("DAQAggregator.properties");
            ProxyManager.get().startProxy();
            DiskInfo d = f3dr.getDiskInfo();
            Double h = f3dr.getHLToutputInfo(288498).getEventRate(PHYSICS_STREAM_NAME);
            Integer c = f3dr.getCrashes();

            logger.info("Disk info: " + d);
            logger.info("Hlt output: " + h);
            logger.info("Crashes: " + c);

            Float cpuLoad = f3dr.getCpuLoad();
            logger.info("cpu load: " + cpuLoad);
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dispatch(DAQ daq) {

        long start = System.currentTimeMillis();

        boolean diskSuccessful = dispatchDisk(daq);
        boolean hltSuccessful = dispatchHLT(daq);
        boolean crashSuccessful = dispatchCrashes(daq);

        long end = System.currentTimeMillis();

        if (diskSuccessful && hltSuccessful && crashSuccessful)
            logger.info("F3 data successfully retrieved and mapped in: " + (end - start) + "ms");
        else {
            logger.warn("Problem retrieving F3 data [disk successful,hlt successful]=[" + diskSuccessful + ","
                    + hltSuccessful + "," +crashSuccessful+ "]");
        }
    }

    /**
     * @return true if retrieving HLT output information from F3mon
     * was successful, false otherwise
     */
    protected boolean dispatchHLT(DAQ daq) {

        try {
            HLToutputInfo hltInfo = getHLToutputInfo(daq.getRunNumber());

            Double hltOutputRate = hltInfo.getEventRate(PHYSICS_STREAM_NAME);
            Double hltOutputBW = hltInfo.getBandwidth(PHYSICS_STREAM_NAME);

            daq.setHltRate(hltOutputRate);
            daq.setHltBandwidth(hltOutputBW);

            return hltOutputRate != null;

        } catch (JsonMappingException e) {
            logger.warn("Could not retrieve F3 HLT rate,  json mapping exception: ", e);
        } catch (IOException e) {
            logger.warn("Could not retrieve F3 HLT rate, IO exception: ", e);
        }
        return false;

    }

    protected boolean dispatchCrashes(DAQ daq) {
        Integer crashes = getCrashes();
        daq.getHltInfo().setCrashes(crashes);
        if (crashes != null)
            return true;
        else
            return false;
    }

    protected Integer getCrashes() {
        Pair<Integer, List<String>> a = null;
        try {
            a = connector.retrieveLines(crashUrl + "?setup=cdaq");

            List<String> result = a.getRight();

            long count = result.size();
            if (count == 1) {
                JsonNode resultJson = mapper.readValue(result.get(0), JsonNode.class);

                try {
                    Integer crashes = resultJson.get("crashes").asInt();
                    return crashes;

                } catch (NoSuchElementException e) {
                    logger.warn("Cannot retrieve HLT crashes (no such element) from response: " + result.get(0));
                } catch (NullPointerException e) {
                    logger.warn("Cannot retrieve HLT crashes from response: " + result.get(0));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    protected boolean dispatchDisk(DAQ daq) {

        try {
            DiskInfo d = getDiskInfo();
            if (d != null) {
                daq.getBuSummary().setOutputDiskTotal(d.getOutputTotal());
                daq.getBuSummary().setOutputDiskUsage(d.getOutputOccupancyFraction());
                return true;
            } else {
                daq.getBuSummary().setOutputDiskTotal(null);
                daq.getBuSummary().setOutputDiskUsage(null);
            }

        } catch (JsonMappingException e) {
            logger.warn("Could not retrieve F3 disk info,  json mapping exception: ", e);
        } catch (IOException e) {
            logger.warn("Could not retrieve F3 disk info, IO exception: ", e);
        }
        return false;

    }

    /**
     * @param events if true, fills event rates otherwise fills bandwidths
     */
    private void fillHLTInfo(int runNumber, HLToutputInfo hltOutputInfo, boolean events) throws IOException {

        final String unit;
        if (events)
            unit = "events";
        else
            unit = "bytes";

        Pair<Integer, List<String>> a = connector.retrieveLines(
                hltUrl + "?setup=cdaq&run=" + runNumber + "&unit=" + unit);

        List<String> result = a.getRight();

        long count = result.size();
        if (count == 1) {
            JsonNode resultJson = mapper.readValue(result.get(0), JsonNode.class);

            logger.debug(resultJson);
            try {

                // node corresponding to the latest lumi section
                JsonNode node = resultJson.elements().next();

                // loop over streams
                for (Iterator<Map.Entry<String, JsonNode>> it = node.fields();
                     it.hasNext(); ) {

                    Map.Entry<String, JsonNode> entry = it.next();
                    String streamName = entry.getKey();
                    double rate = entry.getValue().asDouble();

                    if (events)
                        hltOutputInfo.setEventRate(streamName, rate);
                    else
                        hltOutputInfo.setBandwidth(streamName, rate);

                }
            } catch (NoSuchElementException e) {

                logger.warn("Cannot retrieve hlt rate (no such element) from response: " + result.get(0));
            } catch (NullPointerException e) {
                logger.warn("Cannot retrieve hlt rate from response: " + result.get(0));
            }
        } else {
            logger.warn("Expected 1 node as a response but was " + count);
        }
    }

    protected HLToutputInfo getHLToutputInfo(int runNumber) throws IOException {
        HLToutputInfo info = new HLToutputInfo();

        // fill event rates
        this.fillHLTInfo(runNumber, info, true);

        // fill bandwidths
        this.fillHLTInfo(runNumber, info, false);

        return info;
    }

    /**
     * Gets ramdisk and output disk occupancy levels. It's summary of all cdaq
     * BU's
     *
     * @return
     * @throws IOException
     * @throws JsonMappingException
     */
    protected DiskInfo getDiskInfo() throws IOException, JsonMappingException {

        Pair<Integer, List<String>> a = connector.retrieveLines(diskUrl);
        List<String> result = a.getRight();

        long count = result.size();
        if (count == 1) {
            JsonNode resultJson = mapper.readValue(result.get(0), JsonNode.class);

            try {
                DiskInfo diskInfo = new DiskInfo();
                diskInfo.setOutputOccupancyFraction(resultJson.get("output_occ").asDouble());
                diskInfo.setOutputTotal(resultJson.get("output_tot").asInt());
                diskInfo.setRamdiskOccupancyFraction(resultJson.get("ramdisk_occ").asDouble());
                diskInfo.setRamdiskTotal(resultJson.get("ramdisk_tot").asInt());
                return diskInfo;
            } catch (NoSuchElementException e) {
                logger.warn("Cannot retrieve disk info (no such element) from response: " + result.get(0));
                return null;
            } catch (NullPointerException e) {
                logger.warn("Cannot retrieve disk info from response: " + result.get(0));
                return null;
            }
        } else {
            logger.warn("Expected 1 node as a response but was " + count);
            return null;
        }

    }

    /** retrieves the CPU load from an F3mon web application */
    public Float getCpuLoad() throws IOException {

        Pair<Integer, List<String>> a = connector.retrieveLines(cpuLoadUrl);
        List<String> result = a.getRight();

        long count = result.size();
        if (count == 1) {
            JsonNode resultJson = mapper.readValue(result.get(0), JsonNode.class);

            try {
                for (JsonNode line : resultJson.get("fusyscpu2")) {
                    String name = line.get("name").asText();

                    if (! cpuLoadType.getKey().equals(name)) {
                        continue;
                    }

                    Float cpuLoad = null;
                    long maxTimestamp = -1;
                    
                    // check items in data, take the one with the
                    // highest timestamp
                    for (JsonNode line2 : line.get("data")) {

                        long timestamp = line2.get(0).asLong();
                        
                        if (timestamp > maxTimestamp) {
                            maxTimestamp = timestamp;
                            cpuLoad = (float)line2.get(1).asDouble();
                        }
                    } // loop over items in line

                    return cpuLoad;

                } // loop over returned lines
                
                // not found
                return null;

            } catch (NoSuchElementException e) {
                logger.warn("Cannot retrieve CPU load (no such element) from response: " + result.get(0));
                return null;
            } catch (NullPointerException e) {
                logger.warn("Cannot retrieve CPU load from response: " + result.get(0));
                return null;
            }
        } else {
            logger.warn("Expected 1 node as a response but was " + count);
            return null;
        }
    }

    public class DiskInfo {
        private Double ramdiskOccupancyFraction;
        private Integer ramdiskTotal;
        private Double outputOccupancyFraction;
        private Integer outputTotal;

        public Double getRamdiskOccupancyFraction() {
            return ramdiskOccupancyFraction;
        }

        public void setRamdiskOccupancyFraction(Double ramdiskOccupancyFraction) {
            this.ramdiskOccupancyFraction = ramdiskOccupancyFraction;
        }

        public Integer getRamdiskTotal() {
            return ramdiskTotal;
        }

        public void setRamdiskTotal(Integer ramdiskTotal) {
            this.ramdiskTotal = ramdiskTotal;
        }

        public Double getOutputOccupancyFraction() {
            return outputOccupancyFraction;
        }

        public void setOutputOccupancyFraction(Double outputOccupancyFraction) {
            this.outputOccupancyFraction = outputOccupancyFraction;
        }

        public Integer getOutputTotal() {
            return outputTotal;
        }

        public void setOutputTotal(Integer outputTotal) {
            this.outputTotal = outputTotal;
        }

        @Override
        public String toString() {
            return "DiskInfo [ramdiskOccupancyFraction=" + ramdiskOccupancyFraction + ", ramdiskTotal=" + ramdiskTotal
                    + ", outputOccupancyFraction=" + outputOccupancyFraction + ", outputTotal=" + outputTotal + "]";
        }

    }

    /**
     * contains information about the HLT output, retrieved in one go
     * from the F3 monitoring
     */
    public class HLToutputInfo {

        /**
         * HLT output event rates (events per second) by stream name
         */
        private final Map<String, Double> eventRates = new HashMap<String, Double>();

        /**
         * HLT output bandwidth (bytes per second) by stream name
         */
        private final Map<String, Double> bandwidths = new HashMap<String, Double>();

        private void setEventRate(String streamName, double rate) {
            eventRates.put(streamName, rate);
        }

        /**
         * @return the event rate (events per second) for the given stream
         * or null if not known
         */
        public Double getEventRate(String streamName) {
            return eventRates.get(streamName);
        }

        private void setBandwidth(String streamName, double bandwidth) {
            bandwidths.put(streamName, bandwidth);
        }

        /**
         * @return the bandwidth (bytes per second) for the given stream
         * or null if not known
         */
        public Double getBandwidth(String streamName) {
            return bandwidths.get(streamName);
        }

    }
}
