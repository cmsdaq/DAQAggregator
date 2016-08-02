package rcms.utilities.daqaggregator.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;

import rcms.utilities.daqaggregator.data.BU;
import rcms.utilities.daqaggregator.data.BUSummary;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.data.FEDBuilderSummary;
import rcms.utilities.daqaggregator.data.FMM;
import rcms.utilities.daqaggregator.data.FMMApplication;
import rcms.utilities.daqaggregator.data.FRL;
import rcms.utilities.daqaggregator.data.FRLPc;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.data.SubFEDBuilder;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqaggregator.data.mixin.BUMixIn;
import rcms.utilities.daqaggregator.data.mixin.BUSummaryMixIn;
import rcms.utilities.daqaggregator.data.mixin.DAQMixIn;
import rcms.utilities.daqaggregator.data.mixin.FEDBuilderMixIn;
import rcms.utilities.daqaggregator.data.mixin.FEDBuilderSummaryMixIn;
import rcms.utilities.daqaggregator.data.mixin.FEDMixIn;
import rcms.utilities.daqaggregator.data.mixin.FMMApplicationMixIn;
import rcms.utilities.daqaggregator.data.mixin.FMMMixIn;
import rcms.utilities.daqaggregator.data.mixin.FRLMixIn;
import rcms.utilities.daqaggregator.data.mixin.FRLPcMixIn;
import rcms.utilities.daqaggregator.data.mixin.RUMixIn;
import rcms.utilities.daqaggregator.data.mixin.SubFEDBuilderMixIn;
import rcms.utilities.daqaggregator.data.mixin.SubSystemMixIn;
import rcms.utilities.daqaggregator.data.mixin.TTCPartitionMixIn;

/**
 * Persists DAQ structure in multiple formats format
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class StructureSerializer {

	private static final Logger logger = Logger.getLogger(StructureSerializer.class);

	
	private void addRefMixins(ObjectMapper objectMapper){
		objectMapper.addMixIn(BU.class, rcms.utilities.daqaggregator.data.mixin.ref.BUMixIn.class);
		objectMapper.addMixIn(BUSummary.class, rcms.utilities.daqaggregator.data.mixin.ref.BUSummaryMixIn.class);
		objectMapper.addMixIn(DAQ.class, rcms.utilities.daqaggregator.data.mixin.ref.DAQMixIn.class);
		objectMapper.addMixIn(FED.class, rcms.utilities.daqaggregator.data.mixin.ref.FEDMixIn.class);
		objectMapper.addMixIn(FEDBuilder.class, rcms.utilities.daqaggregator.data.mixin.ref.FEDBuilderMixIn.class);
		objectMapper.addMixIn(FEDBuilderSummary.class, rcms.utilities.daqaggregator.data.mixin.ref.FEDBuilderSummaryMixIn.class);
		objectMapper.addMixIn(FMM.class, rcms.utilities.daqaggregator.data.mixin.ref.FMMMixIn.class);
		objectMapper.addMixIn(FMMApplication.class, rcms.utilities.daqaggregator.data.mixin.ref.FMMApplicationMixIn.class);
		objectMapper.addMixIn(FRL.class, rcms.utilities.daqaggregator.data.mixin.ref.FRLMixIn.class);
		objectMapper.addMixIn(FRLPc.class, rcms.utilities.daqaggregator.data.mixin.ref.FRLPcMixIn.class);
		objectMapper.addMixIn(RU.class, rcms.utilities.daqaggregator.data.mixin.ref.RUMixIn.class);
		objectMapper.addMixIn(SubFEDBuilder.class, rcms.utilities.daqaggregator.data.mixin.ref.SubFEDBuilderMixIn.class);
		objectMapper.addMixIn(SubSystem.class, rcms.utilities.daqaggregator.data.mixin.ref.SubSystemMixIn.class);
		objectMapper.addMixIn(TTCPartition.class, rcms.utilities.daqaggregator.data.mixin.ref.TTCPartitionMixIn.class);
	}
	
	private void addMixins(ObjectMapper objectMapper){
		objectMapper.addMixIn(BU.class, BUMixIn.class);
		objectMapper.addMixIn(BUSummary.class, BUSummaryMixIn.class);
		objectMapper.addMixIn(DAQ.class, DAQMixIn.class);
		objectMapper.addMixIn(FED.class, FEDMixIn.class);
		objectMapper.addMixIn(FEDBuilder.class, FEDBuilderMixIn.class);
		objectMapper.addMixIn(FEDBuilderSummary.class, FEDBuilderSummaryMixIn.class);
		objectMapper.addMixIn(FMM.class, FMMMixIn.class);
		objectMapper.addMixIn(FMMApplication.class, FMMApplicationMixIn.class);
		objectMapper.addMixIn(FRL.class, FRLMixIn.class);
		objectMapper.addMixIn(FRLPc.class, FRLPcMixIn.class);
		objectMapper.addMixIn(RU.class, RUMixIn.class);
		objectMapper.addMixIn(SubFEDBuilder.class, SubFEDBuilderMixIn.class);
		objectMapper.addMixIn(SubSystem.class, SubSystemMixIn.class);
		objectMapper.addMixIn(TTCPartition.class, TTCPartitionMixIn.class);
	}


	public String serializeToSmile(DAQ daq, String name, String folder)
			throws JsonGenerationException, JsonMappingException, IOException {
		File file = new File(folder + name + ".smile");
		ObjectMapper objectMapper = new ObjectMapper(new SmileFactory());

		addMixins(objectMapper);

		objectMapper.writeValue(file, daq);
		return file.getAbsolutePath();
	}

	public String serializeToJSON(DAQ daqSnapshot, String name, String folder)
			throws JsonGenerationException, JsonMappingException, IOException {
		File file = new File(folder + name + ".json");
		ObjectMapper mapper = new ObjectMapper();

		addMixins(mapper);
		mapper.writerWithDefaultPrettyPrinter().writeValue(file, daqSnapshot);

		return file.getAbsolutePath();
	}

	public String serializeToRefJSON(DAQ daqSnapshot, String name, String folder)
			throws JsonGenerationException, JsonMappingException, IOException {
		File file = new File(folder + name + ".json");
		ObjectMapper mapper = new ObjectMapper();

		addRefMixins(mapper);
		mapper.writerWithDefaultPrettyPrinter().writeValue(file, daqSnapshot);

		return file.getAbsolutePath();
	}
	
	public DAQ deserializeFromSmile(String filepath) {
		return mapperDeserialiser(filepath, new SmileFactory());
	}

	public DAQ deserializeFromJSON(String filepath) {
		return mapperDeserialiser(filepath, new JsonFactory());
	}

	private DAQ mapperDeserialiser(String filepath, JsonFactory factory) {

		DAQ daq = null;
		/* read from smile */
		ObjectMapper mapper = new ObjectMapper(factory);
		addMixins(mapper);

		ObjectInputStream in = null;
		FileInputStream fileIn = null;
		try {
			daq = mapper.readValue(new File(filepath), DAQ.class);
			return daq;
		} catch (IOException i) {
			logger.error("File incompatible: " + filepath);
			i.printStackTrace();
			return null;
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e1) {
				}
			if (fileIn != null)
				try {
					fileIn.close();
				} catch (IOException e1) {
				}
		}
	}
}
