package com.glogo.wikiparser;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

/**
 * Main class responsible for reading command line arguments and running WikiParser
 * @author Glogo
 */
public class Main {

	public static void main(String[] args){
		if(args == null || args.length != 2){
			System.err.println("Program must have two arguments:");
			System.err.println("	[0] : input file name");
			System.err.println("	[1] : output file name");
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
			System.err.println("Input file does not exists or input stream was already closed.");
			System.err.println(e.getMessage());

		} catch (XMLStreamException e) {
			System.err.println("Input file is not valid XML.");
			System.err.println(e.getMessage());
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
			System.err.printf("Could not write to file '%'\n", output);
			System.err.println(e.getMessage());
			System.exit(1);
		}
		
		long end = System.currentTimeMillis();
		
		System.out.printf("Job done in %.2fs\n", ((float)(end - start) / 1000));
	}

}
