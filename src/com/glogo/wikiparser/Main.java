package com.glogo.wikiparser;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

/**
 * Main class responsible for reading command line arguments and running WikiParser
 * @author Glogo
 */
public class Main {

	public static void main(String[] args){
		if(args == null || args.length != 2){
			Logger.error("Program must have two arguments:");
			Logger.error("	[0] : input file name");
			Logger.error("	[1] : output file name");
			System.exit(1);
		}
		
		String input = args[0];
		String output = args[1];
		
		long start = System.currentTimeMillis();
		
		/*
		 * Initialize WikiParser
		 */
		WikiParser parser = new WikiParser();
		
		/*
		 * Open file & read pages from xml file
		 */
		try {
			parser.readPages(input);

		} catch (IOException e) {
			Logger.error("Input file does not exists or input stream was already closed.");
			Logger.error(e.getMessage());
			System.exit(1);

		} catch (XMLStreamException e) {
			Logger.error("Input file is not valid XML.");
			Logger.error(e.getMessage());
			System.exit(1);
			
		} catch (SAXException e) {
			Logger.error(e.getMessage());
			System.exit(1);
			
		} catch (ParserConfigurationException e) {
			Logger.error(e.getMessage());
			System.exit(1);
		}
		
		/*
		 * Find alternative titles
		 */
		parser.findAlternativeTitles();
		
		/*
		 * Save result to output file
		 */
		try{
			parser.exportToJSON(output);
		}catch(Exception e){
			Logger.error("Could not write to file '%'", output);
			Logger.error(e.getMessage());
			System.exit(1);
		}
		
		long end = System.currentTimeMillis();
		
		Logger.info("Job done in %.2fs", ((float)(end - start) / 1000));
		Logger.info("Heap size is %.2f MB", (float)Runtime.getRuntime().totalMemory() / (1024 * 1024));
	}

}
