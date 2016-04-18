package rcms.utilities.daqaggregator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonitorData {

	public Map<String, Map <Integer, Map<String, String>>> _ruDataByHM = new HashMap<String, Map <Integer, Map<String, String>>>();
	public Map<String, Map <Integer, Map<String, String>>> _evmDataByHM = new HashMap<String, Map <Integer, Map<String, String>>>();
	public Map<String, Map <Integer, Map<String, String>>> _buDataByHI = new HashMap<String, Map <Integer, Map<String, String>>>();
	public Map<String, Map <Integer, Map<String, String>>> _ferolConfigurationDataByHG = new HashMap<String, Map <Integer, Map<String, String>>>();
	public Map<String, Map <Integer, Map<String, String>>> _ferolStatusDataByHG = new HashMap<String, Map <Integer, Map<String, String>>>();
	public Map<String, Map <Integer, Map<Integer, Map<String, String>>>> _ferolInputStreamDataByHGI = new HashMap<String, Map <Integer, Map<Integer, Map<String, String>>>>();
	public Map<String, Map <Integer, Map<Integer, Map<String, String>>>> _ferolInputStreamDataByHGI_previous = new HashMap<String, Map <Integer, Map<Integer, Map<String, String>>>>();
	public Map<String, Map <Integer, Map<String, String>>> _fmmStatusDataByHG = new HashMap<String, Map <Integer, Map<String, String>>>();
	public Map<String, Map <Integer, Map<Integer, Map<String, String>>>> _fmmInputDataByHGI = new HashMap<String, Map <Integer, Map<Integer, Map<String, String>>>>();


	public Map<String, Map <Integer, Map<Integer, Map<String, String>>>> _frlLinkDataByHGI_previous = null;


	public Map<String, String> _jcMap = new HashMap<String, String>();




	public void clearCache() {
		_ruDataByHM = new HashMap<String, Map <Integer, Map<String, String>>>();
		_evmDataByHM = new HashMap<String, Map <Integer, Map<String, String>>>();
		_buDataByHI = new HashMap<String, Map <Integer, Map<String, String>>>();
		_ferolConfigurationDataByHG = new HashMap<String, Map <Integer, Map<String, String>>>();
		_ferolStatusDataByHG = new HashMap<String, Map <Integer, Map<String, String>>>();
		_ferolInputStreamDataByHGI_previous = _ferolInputStreamDataByHGI;
		_ferolInputStreamDataByHGI = new HashMap<String, Map <Integer, Map<Integer, Map<String, String>>>>();
		_fmmStatusDataByHG = new HashMap<String, Map <Integer, Map<String, String>>>();
		_fmmInputDataByHGI = new HashMap<String, Map <Integer, Map<Integer, Map<String, String>>>>();
		_jcMap = new HashMap<String, String>();
	}


	private String addToWarnStringIfNotNull(String warnString, String name, String value, String cssclass) {
		if (value == null)
			return warnString;

		if (Long.parseLong(value) == 0)
			return warnString;

		if (warnString.length() != 0)
			warnString += ", ";

		return warnString + (cssclass!=null?"<span class=\""+cssclass+"\">":"") +  name + value + (cssclass!=null?"</span>":"");
	}

	public String getJCWarnString(String context) {

		String warn = "";

		if ( _jcMap.containsKey(context) && ! _jcMap.get(context).equals("alive"))
			warn = "<font class=\"jctxt\">JobCrash:" + _jcMap.get(context) + "</font>";

		return warn;
	}

	private List<Integer> jsonToIntList(String s) {


		s = s.substring(s.indexOf('[') + 1, s.lastIndexOf(']'));

		String[] v = s.split(",");
		List<Integer> l = new ArrayList<Integer>();

		for (String strVal : v) {
			try {
				l.add( Integer.parseInt(strVal));
			}
			catch (NumberFormatException e) {

			}
		}
		return l;
	}

	public void calculateDerivedQuantities() {

		for (String hostname : _ruDataByHM.keySet()) {
			for (Integer myrId : _ruDataByHM.get(hostname).keySet()) {
				try {
					calculateReadoutUnitValues( _ruDataByHM.get(hostname).get(myrId) );
				}
				catch (Exception e) {

				}
			}
		}

		for (String hostname : _evmDataByHM.keySet()) {
			for (Integer myrId : _evmDataByHM.get(hostname).keySet()) {
				calculateReadoutUnitValues( _evmDataByHM.get(hostname).get(myrId) );
			}
		}

		for (String hostname : _ferolInputStreamDataByHGI.keySet()) {
			for (Integer geoSlot : _ferolInputStreamDataByHGI.get(hostname).keySet()) {
				for (Integer link : _ferolInputStreamDataByHGI.get(hostname).get(geoSlot).keySet()) {
					Map<String, String> fields = _ferolInputStreamDataByHGI.get(hostname).get(geoSlot).get(link);
					Map<String, String> fieldsPrevious = _ferolInputStreamDataByHGI_previous.get(hostname).get(geoSlot).get(link);
					String warn = "";

					if ( _ferolStatusDataByHG.containsKey(hostname) &&
							_ferolStatusDataByHG.get(hostname).containsKey(geoSlot) &&
							_ferolStatusDataByHG.get(hostname).get(geoSlot).containsKey("stateName")) {
						String stateName = _ferolStatusDataByHG.get(hostname).get(geoSlot).get("stateName");
						if ( stateName.equals("Failed") || stateName.equals("Error") )
							warn = "<span class=\"errtxt\">FRL:" + stateName+"</span>";
					}

					warn = addToWarnStringIfNotNull(warn, "#FCRC=", fields.get("FEDCRCError"), "fedcrc");
					warn = addToWarnStringIfNotNull(warn, "#SCRC=", fields.get("LinkCRCError"), "slinkcrc");

					fields.put("warn", warn);

					String bp = "";
					final String bpKey = "AccSlinkFullSecond";
					//					final String bpKey = "BackpressureCounter";
					final String timeKey = "timestamp";
					if ( fields.containsKey(bpKey) &&
							fieldsPrevious.containsKey(bpKey) &&
							fields.containsKey(timeKey) &&
							fieldsPrevious.containsKey(timeKey)
							) {
						double bpcount = Double.parseDouble(fields.get(bpKey));
						double bpcountPrevious = Double.parseDouble(fieldsPrevious.get(bpKey));
						//						long bpcount = Long.parseLong(fields.get(bpKey));
						//						long bpcountPrevious = Long.parseLong(fieldsPrevious.get(bpKey));
						long t = this.decodeTimestampToMillis(fields.get(timeKey));
						long tPrevious = this.decodeTimestampToMillis(fieldsPrevious.get(timeKey));
						long deltaT = t - tPrevious;
						//
						//						System.out.println(hostname+":"+geoSlot+":"+link+": "+deltaT+" "+bpcountPrevious+" "+bpcount+" "+(bpcount-bpcountPrevious));
						//
						if (deltaT > 0) {

							final Double percBP = (bpcount-bpcountPrevious)*1000. / (double)(deltaT) * 100.;

							if ( percBP > 0.) {
								bp = String.format("<span class=\"backpressure\">&lt;%2.1f%%</span>", percBP);
							}
						}
						else {
							bp = fieldsPrevious.get("bp");
						}
					}
					fields.put("bp", bp);
				}
			}
		}

		for (String hostname : _buDataByHI.keySet()) {
			for (Map<String, String> fields : _buDataByHI.get(hostname).values()) {

				double eventRate = Double.parseDouble( fields.get("eventRate") );
				double bandwidth = Double.parseDouble( fields.get("bandwidth") );
				double eventSize = Double.parseDouble( fields.get("eventSize") );
				double eventSizeStdDev = Double.parseDouble( fields.get("eventSizeStdDev") );

				String rate = String.format("<font color=\"%s\"><b>%7.3f</b></font>", (eventRate==0)?"880000":"666600" ,eventRate/1.e3);
				String throughPut = String.format("<font color=\"%s\"><b>%5.1f</b></font>",
						(eventRate==0)?"880000":"008800" ,bandwidth/1.e6);

				fields.put("rateFormatted", rate);
				fields.put("throughPutFormatted", throughPut);

				String fragSize = "";
				if (eventSize>1e3)
					fragSize = String.format("%5.1f&plusmn;%5.1f", eventSize/1.e3, eventSizeStdDev/1.e3);
				else
					fragSize = String.format("%4.3f&plusmn;%4.3f", eventSize/1.e3, eventSizeStdDev/1.e3);
				fields.put("eventSizeFormatted", fragSize);

				int priority = Integer.parseInt( fields.get("priority") );
				String priorityFormatted = String.format("<div class=\"%s\">%d</div>",
						(priority==0)?"":"warntxt",priority);
				fields.put("priorityFormatted", priorityFormatted);

				int nbTotalResources = Integer.parseInt( fields.get("nbTotalResources") );
				int nbBlockedResources = Integer.parseInt( fields.get("nbBlockedResources") );
				int outstandingRequests = Integer.parseInt( fields.get("outstandingRequests") );
				String nbUsedResources = String.format("%d", nbTotalResources - nbBlockedResources - outstandingRequests);
				String nbBlockedResourcesFormatted = String.format("<div class=\"%s\">%d</div>",
						(nbBlockedResources==0)?"cntCurr":"warntxt",nbBlockedResources);
				fields.put("nbUsedResources", nbUsedResources);
				fields.put("nbBlockedResourcesFormatted", nbBlockedResourcesFormatted);

				int fuSlotsStale = Integer.parseInt( fields.get("fuSlotsStale") );
				String fuSlotsStaleFormatted = String.format("<div class=\"%s\">%d</div>",
						(fuSlotsStale==0)?"":"errtxt",fuSlotsStale);
				fields.put("fuSlotsStaleFormatted", fuSlotsStaleFormatted);

				int fuSlotsQuarantined = Integer.parseInt( fields.get("fuSlotsQuarantined") );
				String fuSlotsQuarantinedFormatted = String.format("<div class=\"%s\">%d</div>",
						(fuSlotsQuarantined==0)?"":"errtxt",fuSlotsQuarantined);
				fields.put("fuSlotsQuarantinedFormatted", fuSlotsQuarantinedFormatted);

				int queuedLumiSections = Integer.parseInt( fields.get("queuedLumiSections") );
				String queuedLumiSectionsFormatted = String.format("<div class=\"%s\">%d</div>",
						(queuedLumiSections>5)?"errtxt":"",queuedLumiSections);
				fields.put("queuedLumiSectionsFormatted", queuedLumiSectionsFormatted);

				int queuedLumiSectionsOnFUs = Integer.parseInt( fields.get("queuedLumiSectionsOnFUs") );
				String queuedLumiSectionsOnFUsFormatted = String.format("<div class=\"%s\">%d</div>",
						(queuedLumiSectionsOnFUs>3)?"errtxt":"",queuedLumiSectionsOnFUs);
				fields.put("queuedLumiSectionsOnFUsFormatted", queuedLumiSectionsOnFUsFormatted);

				String ramDiskUsage = String.format("%3.1f%% of %3.0fGB",
						Double.parseDouble( fields.get("ramDiskUsed") ) * 100,
						Double.parseDouble( fields.get("ramDiskSizeInGB") ));
				fields.put("ramDiskUsage", ramDiskUsage);

				String warn="";
				if ( fields.containsKey("stateName") &&
						(! fields.get("stateName").equals("Halted")) &&
						(! fields.get("stateName").equals("Ready")) &&
						(! fields.get("stateName").equals("Enabled"))) {
					warn = "<span class=\"errtxt\">"+fields.get("stateName")+"</span>";
				}

				String errorString = "";
				if ( Integer.parseInt( fields.get("nbCorruptedEvents") ) > 0 ) {
					errorString = "#bad=" + fields.get("nbCorruptedEvents");
				}
				if ( Integer.parseInt( fields.get("nbEventsMissingData") ) > 0 ) {
					if ( !errorString.isEmpty() ) errorString += ",";
					errorString = "#partial=" + fields.get("nbEventsMissingData");
				}
				if ( Integer.parseInt( fields.get("nbEventsWithCRCerrors") ) > 0 ) {
					if ( !errorString.isEmpty() ) errorString += ",";
					errorString = "#CRC=" + fields.get("nbEventsWithCRCerrors");
				}
				if ( !errorString.isEmpty() ) {
					warn += " <span class=\"errtxt\">" + errorString + "</span>";
				}
				fields.put("warn", warn);
			}
		}
	}

	private void calculateReadoutUnitValues(Map<String, String> fields) {

		double eventRate = Double.parseDouble( fields.get("eventRate") );
		double superFragmentSize = Double.parseDouble( fields.get("superFragmentSize") );
		double superFragmentSizeStdDev = Double.parseDouble( fields.get("superFragmentSizeStdDev") );

		String rate = String.format("<font color=\"%s\"><b>%7.3f</b></font>", (eventRate==0)?"880000":"666600" ,eventRate/1.e3);
		String throughPut = String.format("<font color=\"%s\"><b>%5.1f</b></font>",
				(eventRate==0)?"880000":"008800" ,eventRate*superFragmentSize/1.e6);

		fields.put("rateFormatted", rate);
		fields.put("throughPut", "" + (eventRate*superFragmentSize));
		fields.put("throughPutFormatted", throughPut);

		String fragSize = "";
		if (superFragmentSize>1e3)
			fragSize = String.format("%5.1f&plusmn;%5.1f", superFragmentSize/1.e3, superFragmentSizeStdDev/1.e3);
		else
			fragSize = String.format("%4.3f&plusmn;%4.3f", superFragmentSize/1.e3, superFragmentSizeStdDev/1.e3);
		fields.put("superFragmentSizeFormatted", fragSize);

		String warn="";
		if ( fields.containsKey("stateName") &&
				(! fields.get("stateName").equals("Halted")) &&
				(! fields.get("stateName").equals("Ready")) &&
				(! fields.get("stateName").equals("Enabled"))) {
			warn = "<span class=\"errtxt\">"+fields.get("stateName")+"</span>";
		}
		else if ( fields.containsKey("fedIdsWithoutFragments") &&
				Integer.parseInt( fields.get("eventRate") ) == 0 &&
				Integer.parseInt( fields.get("incompleteSuperFragmentCount") ) > 0 ) {
			List<Integer> fedIds = jsonToIntList( fields.get("fedIdsWithoutFragments"));
			if ( ! fedIds.isEmpty() ) {
				warn = "<span class=\"errtxt\">" + fedIds.toString() + "</span>";
			}
		}
		if ( fields.containsKey("fedIdsWithErrors") ) {
			List<Integer> fedIds = jsonToIntList( fields.get("fedIdsWithErrors"));
			if ( ! fedIds.isEmpty() ) {
				List<Integer> fedCorrupt = jsonToIntList( fields.get("fedDataCorruption"));
				List<Integer> fedOutOfSync = jsonToIntList( fields.get("fedOutOfSync"));
				List<Integer> fedCRCs = jsonToIntList( fields.get("fedCRCerrors"));
				List<Integer> fedBXerrors = jsonToIntList( fields.get("fedBXerrors"));
				for (int i = 0; i < fedIds.size(); i++) {
					String errorString = "";
					if ( fedCorrupt.get(i) > 0 ) {
						errorString += "#bad=" + fedCorrupt.get(i);
					}
					if ( fedOutOfSync.get(i) > 0 ) {
						if ( !errorString.isEmpty() ) errorString += ",";
						errorString += "#OOS=" + fedOutOfSync.get(i);
					}
					if ( fedBXerrors.get(i) > 0 ) {
						if ( !errorString.isEmpty() ) errorString += ",";
						errorString += "#BX=" + fedBXerrors.get(i);
					}
					if ( fedCRCs.get(i) > 0 ) {
						if ( !errorString.isEmpty() ) errorString += ",";
						errorString += "#CRC=" + fedCRCs.get(i);
					}
					warn += " <span class=\"errtxt\">" + fedIds.get(i) + ":" + errorString + "</span>";
				}
			}
		}

		fields.put("warn", warn);
	}

	private long decodeTimestampToMillis(String ts) {
		ts = ts.substring(0, ts.length()-4) + " GMT";
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS z");
		Date d = null;
		try {
			d = df.parse( ts );
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return d.getTime();
	}
}
