package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;

public class FED
{
  /** the parent FRL */
  private FRL frl;
  
  /** which FRL input: 0 or 1 */ 
  private int frlIO; 
  
  /** can be null */
  private FMM fmm;
          
  private int fmmIO;
  
  private int srcIdExpected;
  private int srcIdReceived;
  
  private float percentBackpressure;
  private float percentWarning;
  private float percentBusy;
  private String ttsState;
  
  private long numSCRCerrors;
  private long numFRCerrors;
  
  private long numTriggers;
  
  /** important for pseudofeds */
  private List<FED> mainFeds = new ArrayList<FED>();
  
}
