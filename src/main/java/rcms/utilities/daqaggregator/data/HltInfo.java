package rcms.utilities.daqaggregator.data;

public class HltInfo  {

    private Integer crashes;

    /** average CPU load on FU nodes */
    private float cpuLoad;

    public Integer getCrashes() {
        return crashes;
    }

    public void setCrashes(Integer crashes) {
        this.crashes = crashes;
    }

    public float getCpuLoad() {
        return cpuLoad;
    }

    public void setCpuLoad(float cpuLoad) {
        this.cpuLoad = cpuLoad;
    }
		
		
}
