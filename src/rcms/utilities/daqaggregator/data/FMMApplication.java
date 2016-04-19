package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;

public class FMMApplication
{
  private DAQ daq;
  
  private String hostname;
  
  private String url;
  
  private boolean crashed;
  
  private List<FMM> fmms = new ArrayList<FMM>();
  
}
