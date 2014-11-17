package com.glogo.wikiparser;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class responsible for reading command line arguments and running {@link WikipediaRedirects}.
 * @author Glogo
 */
public class Main {
	
	private static Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args){
		if(args == null || args.length != 2){
			logger.error("Program must have two arguments:");
			logger.error("	[0] : input file name");
			logger.error("	[1] : output file name");
			System.exit(1);
		}
		
		String input = args[0];
		String output = args[1];
		
		long start = System.currentTimeMillis();
		
		/*
		 * Initialize WikiParser
		 */
		WikipediaRedirects parser = new WikipediaRedirects();
		
		/*
		 * Open file & read pages from xml file
		 */
		try {
			parser.readPages(input);

		} catch (IOException e) {
			logger.error("Input file does not exists or input stream was already closed.");
			logger.error(e.getMessage());
			System.exit(1);

		} catch (XMLStreamException e) {
			logger.error("Input file is not valid XML.");
			logger.error(e.getMessage());
			System.exit(1);
			
		}
		
		/*
		 * Save result to output file
		 */
		try{
			parser.exportToJSON(output);
		}catch(Exception e){
			logger.error("Could not write to file '{}'", output);
			logger.error(e.getMessage());
			System.exit(1);
		}
		
		long end = System.currentTimeMillis();
		
		logger.info("Job done in {} s", ((float)(end - start) / 1000));
		logger.info("Heap size is {} MB", (float)Runtime.getRuntime().totalMemory() / (1024 * 1024));
	}

}
