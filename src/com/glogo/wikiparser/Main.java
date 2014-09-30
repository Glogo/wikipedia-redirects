package com.glogo.wikiparser;

/**
 * Main class responsible for reading command line arguments and running WikiParser
 * @author Glogo
 */
public class Main {

	public static void main(String[] args) throws Exception{
		WikiParser parser = new WikiParser("C:\\Users\\michael.gloger\\Documents\\spring-workspace\\vi-wiki-parser\\res\\enwiki-latest-pages-articles1.xml");
		parser.readPages();
		parser.findAlternativeTitles();
		parser.exportToJSON("data.js");
	}

}
