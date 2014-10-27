package com.glogo.wikiparser.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.glogo.wikiparser.WikiParser;

/**
 * This class contains various jUnit test for WikiParser
 * @author michael.gloger
 */
public class WikiParserTest {
	
	private static WikiParser parser = null;
	private static String absolutePath;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		parser = new WikiParser();
		absolutePath = new File("res/test_input.xml").getAbsolutePath();
	}
	
	@Test
	public void testFileShouldExist() {
		assertTrue("Test file could not be read.", absolutePath != null || absolutePath.length() != 0);
	}
	
	@Test
	public void pagesMapShouldNotBeEmpty() throws XMLStreamException, IOException {
		parser.readPages(absolutePath);
		assertTrue("Pages map is empty.", parser.getPages().size() > 0);
	}
	
	@Test
	public void pageDogShouldHaveOneAlternativeTitle() throws XMLStreamException, IOException {
		parser.readPages(absolutePath);
		parser.findAlternativeTitles();
		assertTrue("Page with title 'Dog' doesn't have one alternative title.", parser.getPages().get("Dog").getAlternativeTitles().size() == 1);
	}
	
	@Test
	public void pageDogShouldHaveOneAnchorText() throws XMLStreamException, IOException {
		parser.readPages(absolutePath);
		parser.findAlternativeTitles();
		assertTrue("Page with title 'Dog' doesn't have one anchor text.", parser.getPages().get("Dog").getAnchorTexts().size() == 1);
	}
	
	@Test
	public void pageAnimalsShouldNotHaveAlternativeTitleAnimal() throws XMLStreamException, IOException {
		parser.readPages(absolutePath);
		parser.findAlternativeTitles();
		assertTrue("Page with title 'Animals' shouldn't have alternative title 'animal' (ignoring case).", !parser.getPages().get("Animals").getAlternativeTitles().contains("animal"));
	}
	
	@Test
	public void pageAnimalsShouldHaveAnchorTextAnimal() throws XMLStreamException, IOException {
		parser.readPages(absolutePath);
		parser.findAlternativeTitles();
		assertTrue("Page with title 'Animals' doesn't have anchor text 'animal' (ignoring case).", parser.getPages().get("Animals").getAnchorTexts().contains("animal"));
	}
	
	@Test
	public void pageAnimalsShouldNotHaveAlternativeTitleAnimalIgnoreCase() throws XMLStreamException, IOException {
		parser.readPages(absolutePath);
		parser.findAlternativeTitles();
		assertTrue("Page with title 'Animals' shouldn't have alternative title 'aNimAl' (ignoring case).", !parser.getPages().get("Animals").getAlternativeTitles().contains("aNimAl"));
	}
	
	@Test
	public void pageAnimalsShouldHaveAnchorTextAnimalIgnoreCase() throws XMLStreamException, IOException {
		parser.readPages(absolutePath);
		parser.findAlternativeTitles();
		assertTrue("Page with title 'Animals' doesn't have anchor text 'aNimAl' (ignoring case).", parser.getPages().get("Animals").getAnchorTexts().contains("aNimAl"));
	}
	
	@Test
	public void pageAliensShouldNotHaveAlternativeTitles() throws XMLStreamException, IOException {
		parser.readPages(absolutePath);
		parser.findAlternativeTitles();
		assertTrue("Page with title 'Aliens' has (and shouldn't) have any alternative titles becase link is to category.", parser.getPages().get("Aliens").getAlternativeTitles().isEmpty());
	}
	
	@Test
	public void outputFileShouldBeCreated() throws IOException, XMLStreamException {
		parser.readPages(absolutePath);
		parser.findAlternativeTitles();
		parser.exportToJSON("test_data_output.js");
		
		File f = new File("test_data_output.js");
		if(!f.exists()) {
			fail("Data file was not found");
		}
	}
}
