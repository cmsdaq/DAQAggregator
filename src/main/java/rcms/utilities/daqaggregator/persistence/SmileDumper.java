/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rcms.utilities.daqaggregator.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import java.io.File;
import java.io.IOException;

/**
 * Helper utility to dump smile files to json on the command line.
 * 
 * use e.g. as 
 * 
 *   java -cp target/DAQAggregator-...-jar-with-dependencies.jar \
 *     rcms.utilities.daqaggregator.persistence.SmileDumper \
 *     myfile.smile
 * 
 * @author holzner
 */
public class SmileDumper {
  
  //----------------------------------------------------------------------
    
  public static void main(String argv[]) throws IOException {
    if (argv.length != 1)
    {
      System.err.println("must specify exactly one input file");
      System.err.println();
      usage();
    } 
    
    ObjectMapper inputMapper = new ObjectMapper(new SmileFactory());
    ObjectMapper outputMapper = new ObjectMapper();
    
    // enable pretty printing
    outputMapper.enable(SerializationFeature.INDENT_OUTPUT);
    
    JsonNode data = inputMapper.readTree(new File(argv[0]));
    outputMapper.writeValue(System.out, data);
  }
  
  //----------------------------------------------------------------------
  
  protected static void usage() {
    System.err.println();
    System.err.println("usage: SmileDumper inputFile.smile");
    System.err.println();
    System.err.println("       reads the given flash list dump in .smile format and");
    System.err.println("       prints the corresponding data in json format");
    System.err.println("       on stdout");

    System.exit(1);
  }
    
  //----------------------------------------------------------------------
  
}
