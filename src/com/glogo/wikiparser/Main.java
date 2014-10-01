package com.glogo.wikiparser;

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
		
		/*
		 * Open file
		 */
		WikiParser parser = null;
		try{
			parser = new WikiParser(input);
		}catch(Exception e){
			System.err.println("Input file does not exists or is not valid XML.");
			System.err.println(e.getMessage());
			System.exit(1);
		}
		
		/*
		 * Read pages from xml file
		 */
		parser.readPages();
		
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
	}

}
