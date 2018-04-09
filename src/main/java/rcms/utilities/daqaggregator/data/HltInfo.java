package rcms.utilities.daqaggregator.data;

public class HltInfo  {

    private Integer crashes;

    /** average CPU load on FU nodes */
    private Float cpuLoad;

    public Integer getCrashes() {
        return crashes;
    }

    public void setCrashes(Integer crashes) {
        this.crashes = crashes;
    }

    public Float getCpuLoad() {
        return cpuLoad;
    }

    public void setCpuLoad(Float cpuLoad) {
        this.cpuLoad = cpuLoad;
    }
		
		
}
