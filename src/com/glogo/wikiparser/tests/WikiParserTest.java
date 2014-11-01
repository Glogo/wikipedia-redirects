package com.glogo.wikiparser.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.glogo.wikiparser.WikiParser;
import com.glogo.wikiparser.model.PageModel;

/**
 * This class contains various jUnit test for WikiParser
 * @author michael.gloger
 */
public class WikiParserTest {
	
	private static WikiParser parser = null;
	private static String absolutePath;
	private static Map<String, PageModel> pagesMap = new HashMap<String, PageModel>();
	
	/**
	 * Clears pagesMap and maps all pages to this map with title as key.
	 */
	private void readPagesMap() {
		pagesMap.clear();
		PageModel pageModel;
		
		for(Map.Entry<Integer, PageModel> entry : parser.getPages().entrySet()){
			pageModel = entry.getValue();
			pagesMap.put(pageModel.getTitle(), pageModel);
		}
	}

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
		readPagesMap();
		assertTrue("Page with title 'Dog' doesn't have one alternative title.", pagesMap.get("Dog").getAlternativeTitles().size() == 1);
	}
	
	@Test
	public void pageAnimalsShouldNotHaveAlternativeTitleAnimal() throws XMLStreamException, IOException {
		parser.readPages(absolutePath);
		parser.findAlternativeTitles();
		readPagesMap();
		assertTrue("Page with title 'Animals' shouldn't have alternative title 'animal' (ignoring case).", !pagesMap.get("Animals").getAlternativeTitles().contains("animal"));
	}
	
	@Test
	public void pageAnimalsShouldNotHaveAlternativeTitleAnimalIgnoreCase() throws XMLStreamException, IOException {
		parser.readPages(absolutePath);
		parser.findAlternativeTitles();
		readPagesMap();
		assertTrue("Page with title 'Animals' shouldn't have alternative title 'aNimAl' (ignoring case).", !pagesMap.get("Animals").getAlternativeTitles().contains("aNimAl"));
	}
	
	@Test
	public void pageAliensShouldNotHaveAlternativeTitles() throws XMLStreamException, IOException {
		parser.readPages(absolutePath);
		parser.findAlternativeTitles();
		readPagesMap();
		assertTrue("Page with title 'Aliens' has (and shouldn't) have any alternative titles becase link is to category.", pagesMap.get("Aliens").getAlternativeTitles().isEmpty());
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
