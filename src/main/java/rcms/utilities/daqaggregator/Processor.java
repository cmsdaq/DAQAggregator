package rcms.utilities.daqaggregator;

import java.io.IOException;

import rcms.utilities.daqaggregator.persistence.PersistorManager;

public class Processor {

	public static void main (String[] args){
		convertToJson();

		
	}
	

	public static void convertToJson(){
		PersistorManager persistorManager = new PersistorManager("/tmp/mgladki/persistence/");
		try {
			persistorManager.convertSnapshots("/tmp/mgladki/daq-24h-json/");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		System.out.println("Done!");
	}
	
	public static void convertFromJson(){
		
	}
	
}
