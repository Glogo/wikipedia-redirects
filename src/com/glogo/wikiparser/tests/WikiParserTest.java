package com.glogo.wikiparser.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.glogo.wikiparser.WikiParser;

/**
 * This class contains various jUnit test for WikiParser
 * @author michael.gloger
 */
public class WikiParserTest {
	
	private static WikiParser parser = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		parser = new WikiParser(new File("res/test_input.xml").getAbsolutePath());
	}
	
	@Test
	public void documentShouldBeRead() {
		assertTrue("Document is null.", parser.getDocument() != null);
	}
	
	@Test
	public void pagesMapShouldNotBeEmpty() {
		parser.readPages();
		assertTrue("Pages map is empty.", parser.getPages().size() > 0);
	}
	
	@Test
	public void pageDogShouldHaveTwoAlternativeTitles() {
		parser.readPages();
		parser.findAlternativeTitles();
		assertTrue("Page with title 'Dog' doesn't have two alternative titles.", parser.getPages().get("Dog").getAlternativeTitles().size() == 2);
	}
	
	@Test
	public void pageAnimalsShouldHaveAlternativeTitleAnimal() {
		parser.readPages();
		parser.findAlternativeTitles();
		assertTrue("Page with title 'Animals' doesn't have alternative title 'animal' (ignoring case).", parser.getPages().get("Animals").getAlternativeTitles().contains("animal"));
	}
	
	@Test
	public void pageAnimalsShouldHaveAlternativeTitleAnimalIgnoreCase() {
		parser.readPages();
		parser.findAlternativeTitles();
		assertTrue("Page with title 'Animals' doesn't have alternative title 'aNimAl' (ignoring case).", parser.getPages().get("Animals").getAlternativeTitles().contains("aNimAl"));
	}
	
	@Test
	public void pageAliensShouldNotHaveAlternativeTitles() {
		parser.readPages();
		parser.findAlternativeTitles();
		assertTrue("Page with title 'Aliens' has (and shouldn't) have any alternative titles becase link is to category.", parser.getPages().get("Aliens").getAlternativeTitles().isEmpty());
	}
	
	@Test
	public void outputFileShouldBeCreated() throws IOException {
		parser.readPages();
		parser.findAlternativeTitles();
		parser.exportToJSON("test_data_output.js");
		
		File f = new File("test_data_output.js");
		if(!f.exists()) {
			fail("Data file was not found");
		}
	}
}
