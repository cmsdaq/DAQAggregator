package rcms.utilities.daqaggregator.data;

public class RU 
{
  /** the FEDbuilder this RU corresponds to */
  private FEDBuilder fedBuilder;
  
  private String hostname;
  
  private boolean isEVM;
  
  private boolean masked;
  
  private String errorMsg;
  
  private String warnMsg;
  
  private String infoMsg;
  
  /** events rate in kHz ? */
  private float rate;
  
  /** MByte per second ? */
  private float throughput;
  
  /** mean superfragment size in kByte ? */
  private float superFragmentSizeMean;
  
  /** spread of superfragment size in kByte ? */
  private float superFragmentSizeStddev;
  
  private int fragmentsInRU;
  
  private int eventsInRU;
  
  /** requests from BUs ? */
  private int requests;
  
}
