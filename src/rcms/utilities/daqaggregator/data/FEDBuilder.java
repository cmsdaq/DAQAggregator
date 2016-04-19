package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;

public class FEDBuilder
{
  private DAQ daq;
  
  private List<SubFEDBuilder> subFedbuilders = new ArrayList<SubFEDBuilder>();
  
  /** the RU in this FEDBuilder */
  private RU ru;
}
