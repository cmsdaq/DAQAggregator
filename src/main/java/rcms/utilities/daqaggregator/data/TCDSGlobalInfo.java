package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.datasource.FlashlistType;
import rcms.utilities.daqaggregator.mappers.FlashlistUpdatable;

public class TCDSGlobalInfo implements FlashlistUpdatable{

	/**
	 * Container of global information from the TCDS flashlists
	 *
	 * @author Michail Vougioukas (michail.vougioukas@cern.ch)
	 */
	
	//pm channel global tts states
	private Map<String, GlobalTTSState> globalTtsStates;// = new HashMap<String, GlobalTTSState>();
	
	//cpm counts
	private int sup_trg_cnt_beamactive_total;
	private int sup_trg_cnt_total;
	private int trg_cnt_beamactive_total;
	private int trg_cnt_total;
	
	private List<Integer> sup_trg_cnt_beamactive_tt_values;
	private List<Integer> sup_trg_cnt_tt_values;
	private List<Integer> trg_cnt_beamactive_tt_values;
	private List<Integer> trg_cnt_tt_values;
	
	//cpm deadtimes
	private int fillNumber;
	private Map<String, Double> deadTimes;
	
	//cpm rates
	private double sup_trg_rate_beamactive_total;
	private double sup_trg_rate_total;
	private double trg_rate_beamactive_total;
	private double trg_rate_total;
	
	private List<Double> sup_trg_rate_beamactive_tt_values;
	private List<Double> sup_trg_rate_tt_values;
	private List<Double> trg_rate_beamactive_tt_values;
	private List<Double> trg_rate_tt_values;
	
	
	//pm action counts
	private List<Integer> actionCounts;
	
	public TCDSGlobalInfo() {
		super();
		globalTtsStates = new HashMap<String, GlobalTTSState>();
		sup_trg_cnt_beamactive_tt_values = new ArrayList<Integer>();
		sup_trg_cnt_tt_values = new ArrayList<Integer>();
		trg_cnt_beamactive_tt_values = new ArrayList<Integer>();
		trg_cnt_tt_values = new ArrayList<Integer>();
		sup_trg_rate_beamactive_tt_values = new ArrayList<Double>();
		sup_trg_rate_tt_values = new ArrayList<Double>();
		trg_rate_beamactive_tt_values = new ArrayList<Double>();
		trg_rate_tt_values = new ArrayList<Double>();
	}

	public Map<String, GlobalTTSState> getGlobalTtsStates() {
		return globalTtsStates;
	}

	public void setGlobalTtsStates(Map<String, GlobalTTSState> globalTtsStates) {
		this.globalTtsStates = globalTtsStates;
	}

	public int getSup_trg_cnt_beamactive_total() {
		return sup_trg_cnt_beamactive_total;
	}

	public void setSup_trg_cnt_beamactive_total(int sup_trg_cnt_beamactive_total) {
		this.sup_trg_cnt_beamactive_total = sup_trg_cnt_beamactive_total;
	}

	public int getSup_trg_cnt_total() {
		return sup_trg_cnt_total;
	}

	public void setSup_trg_cnt_total(int sup_trg_cnt_total) {
		this.sup_trg_cnt_total = sup_trg_cnt_total;
	}

	public int getTrg_cnt_beamactive_total() {
		return trg_cnt_beamactive_total;
	}

	public void setTrg_cnt_beamactive_total(int trg_cnt_beamactive_total) {
		this.trg_cnt_beamactive_total = trg_cnt_beamactive_total;
	}

	public int getTrg_cnt_total() {
		return trg_cnt_total;
	}

	public void setTrg_cnt_total(int trg_cnt_total) {
		this.trg_cnt_total = trg_cnt_total;
	}

	public List<Integer> getSup_trg_cnt_beamactive_tt_values() {
		return sup_trg_cnt_beamactive_tt_values;
	}

	public void setSup_trg_cnt_beamactive_tt_values(List<Integer> sup_trg_cnt_beamactive_tt_values) {
		this.sup_trg_cnt_beamactive_tt_values = sup_trg_cnt_beamactive_tt_values;
	}

	public List<Integer> getSup_trg_cnt_tt_values() {
		return sup_trg_cnt_tt_values;
	}

	public void setSup_trg_cnt_tt_values(List<Integer> sup_trg_cnt_tt_values) {
		this.sup_trg_cnt_tt_values = sup_trg_cnt_tt_values;
	}

	public List<Integer> getTrg_cnt_beamactive_tt_values() {
		return trg_cnt_beamactive_tt_values;
	}

	public void setTrg_cnt_beamactive_tt_values(List<Integer> trg_cnt_beamactive_tt_values) {
		this.trg_cnt_beamactive_tt_values = trg_cnt_beamactive_tt_values;
	}

	public List<Integer> getTrg_cnt_tt_values() {
		return trg_cnt_tt_values;
	}

	public void setTrg_cnt_tt_values(List<Integer> trg_cnt_tt_values) {
		this.trg_cnt_tt_values = trg_cnt_tt_values;
	}

	public int getFillNumber() {
		return fillNumber;
	}

	public void setFillNumber(int fillNumber) {
		this.fillNumber = fillNumber;
	}

	public Map<String, Double> getDeadTimes() {
		return deadTimes;
	}

	public void setDeadTimes(Map<String, Double> deadTimes) {
		this.deadTimes = deadTimes;
	}

	public double getSup_trg_rate_beamactive_total() {
		return sup_trg_rate_beamactive_total;
	}

	public void setSup_trg_rate_beamactive_total(double sup_trg_rate_beamactive_total) {
		this.sup_trg_rate_beamactive_total = sup_trg_rate_beamactive_total;
	}

	public double getSup_trg_rate_total() {
		return sup_trg_rate_total;
	}

	public void setSup_trg_rate_total(double sup_trg_rate_total) {
		this.sup_trg_rate_total = sup_trg_rate_total;
	}

	public double getTrg_rate_beamactive_total() {
		return trg_rate_beamactive_total;
	}

	public void setTrg_rate_beamactive_total(double trg_rate_beamactive_total) {
		this.trg_rate_beamactive_total = trg_rate_beamactive_total;
	}

	public double getTrg_rate_total() {
		return trg_rate_total;
	}

	public void setTrg_rate_total(double trg_rate_total) {
		this.trg_rate_total = trg_rate_total;
	}

	public List<Double> getSup_trg_rate_beamactive_tt_values() {
		return sup_trg_rate_beamactive_tt_values;
	}

	public void setSup_trg_rate_beamactive_tt_values(List<Double> sup_trg_rate_beamactive_tt_values) {
		this.sup_trg_rate_beamactive_tt_values = sup_trg_rate_beamactive_tt_values;
	}

	public List<Double> getSup_trg_rate_tt_values() {
		return sup_trg_rate_tt_values;
	}

	public void setSup_trg_rate_tt_values(List<Double> sup_trg_rate_tt_values) {
		this.sup_trg_rate_tt_values = sup_trg_rate_tt_values;
	}

	public List<Double> getTrg_rate_beamactive_tt_values() {
		return trg_rate_beamactive_tt_values;
	}

	public void setTrg_rate_beamactive_tt_values(List<Double> trg_rate_beamactive_tt_values) {
		this.trg_rate_beamactive_tt_values = trg_rate_beamactive_tt_values;
	}

	public List<Double> getTrg_rate_tt_values() {
		return trg_rate_tt_values;
	}

	public void setTrg_rate_tt_values(List<Double> trg_rate_tt_values) {
		this.trg_rate_tt_values = trg_rate_tt_values;
	}

	public List<Integer> getActionCounts() {
		return actionCounts;
	}

	public void setActionCounts(List<Integer> actionCounts) {
		this.actionCounts = actionCounts;
	}

	@Override
	public void updateFromFlashlist(FlashlistType flashlistType, JsonNode flashlistRow) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clean() {
		// TODO Auto-generated method stub
		
	}
	
	
}
