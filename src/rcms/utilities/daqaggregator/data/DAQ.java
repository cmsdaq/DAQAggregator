package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;

public class DAQ
{
  private List<TTCPartition> ttcPartitions = new ArrayList<>();
  
  private List<FRLPc> frlPc = new ArrayList<>();
  
  private List<BU> bus = new ArrayList<>();

  private List<FMMApplication> fmmApplications = new ArrayList<>();

  private int runNumber;
  
  private int sessionId;
  
  /** timestamp */
  private long lastUpdate;
  
  private String daqState;
  
  private String dpsetPath;
  
  private FEDBuilderSummary fedBuilderSummary;
  
  private BUSummary BUSummary;
  
}
