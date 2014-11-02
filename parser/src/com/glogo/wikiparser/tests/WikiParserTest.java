package com.glogo.wikiparser.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.glogo.wikiparser.WikipediaRedirects;

/**
 * This class contains simple jUnit test for WikiParser
 * @author michael.gloger
 */
public class WikiParserTest {
	
	private static WikipediaRedirects parser = null;
	private static String absolutePath;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		parser = new WikipediaRedirects();
		absolutePath = new File("res/test_input.xml").getAbsolutePath();
	}
	
	@Test
	public void testFileShouldExist() {
		assertTrue("Test file could not be read.", absolutePath != null || absolutePath.length() != 0);
	}
	
	@Test
	public void pagesMapShouldNotBeEmpty() throws XMLStreamException, IOException {
		parser.readPages(absolutePath);
		assertTrue("Pages map is empty.", parser.getRedirectedPages().size() > 0);
	}
	
	@Test
	public void pageDogShouldHaveOneAlternativeTitle() throws XMLStreamException, IOException {
		parser.readPages(absolutePath);
		assertTrue("Page with title 'Dog' doesn't have one alternative title.", parser.getRedirectedPages().get("Dog").size() == 1);
	}
	
	@Test
	public void pageAnimalsShouldNotHaveAlternativeTitleAnimal() throws XMLStreamException, IOException {
		parser.readPages(absolutePath);
		assertTrue("Page with title 'Animals' shouldn't have alternative title 'animal' (ignoring case).", !parser.getRedirectedPages().get("Animals").contains("animal"));
	}
	
	@Test
	public void pageAnimalsShouldNotHaveAlternativeTitleAnimalIgnoreCase() throws XMLStreamException, IOException {
		parser.readPages(absolutePath);
		assertTrue("Page with title 'Animals' shouldn't have alternative title 'aNimAl' (ignoring case).", !parser.getRedirectedPages().get("Animals").contains("aNimAl"));
	}
	
	@Test
	public void pageAliensShouldNotHaveAlternativeTitles() throws XMLStreamException, IOException {
		parser.readPages(absolutePath);
		assertTrue("Page with title 'Aliens' has (and shouldn't) have any alternative titles becase link is to category.", parser.getRedirectedPages().get("Aliens").isEmpty());
	}
	
	@Test
	public void outputFileShouldBeCreated() throws IOException, XMLStreamException {
		parser.readPages(absolutePath);
		
		parser.exportToJSON("test_data_output.js");
		
		File f = new File("test_data_output.js");
		if(!f.exists()) {
			fail("Data file was not found");
		}
	}
}
