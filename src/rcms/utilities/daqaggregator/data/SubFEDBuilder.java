package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing one line in DAQView
 */
public class SubFEDBuilder
{
  /** the 'parent' FEDBuilder */
  private FEDBuilder fedBuilder;
  
  private TTCPartition ttcPartition;
  
  /** can be null */
  private FRLPc frlPc; 
  
  private long minTrig, maxTrig;
  
  private List<FRL> frls = new ArrayList<FRL>();
  
}
