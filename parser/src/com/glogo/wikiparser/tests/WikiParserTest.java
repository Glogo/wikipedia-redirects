package com.glogo.wikiparser.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;

import javax.swing.filechooser.FileSystemView;
import javax.xml.stream.XMLStreamException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.glogo.wikiparser.WikipediaRedirects;

/**
 * This class contains simple jUnit tests for WikiParser.
 * For testing is used input data file named "sample_input_enwiki-latest-pages-articles1_alt.xml" and output is saved in user home directory
 * For detailed information please see <a href="https://github.com/Glogo/wikipedia-redirects">project repository</a>
 * @author michael.gloger
 */
public class WikiParserTest {
	
	private static final String INPUT_FILENAME = "sample_input_enwiki-latest-pages-articles1_alt.xml";
	private static final String OUTPUT_FILENAME = "sample_output_enwiki-latest-pages-articles1_alt.csv";
	private static WikipediaRedirects parser = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		// Initialize parser
		parser = new WikipediaRedirects();
		
		// Get absolute input resource url
		URL testInputUrl = WikiParserTest.class.getClassLoader().getResource(INPUT_FILENAME);
		
		// Check if file exists
		if(testInputUrl == null){
			fail("Could not read resource file '" + INPUT_FILENAME + "'");
			return;
		}
		
		// Decode url to absolute format
		String absoluteInputFilename = URLDecoder.decode(testInputUrl.getFile(), "UTF-8");
		
		// Read pages
		parser.readPages(absoluteInputFilename);
	}
	
	@Test
	public void pagesMapShouldNotBeEmpty() throws XMLStreamException, IOException {
		assertTrue("Pages map is empty.", parser.getRedirectedPages().size() > 0);
	}
	
	@Test
	public void pageComputeraAcessibilityShouldHaveTwoAlternativeTitles() throws XMLStreamException, IOException {
		assertTrue("Page with title 'Computer accessibility' doesn't have two alternative titles.", parser.getRedirectedPages().get("Computer accessibility").size() == 2);
	}
	
	@Test
	public void pageComputerAcessibilityShouldHaveAlternativeTitleAccessibleComputing() throws XMLStreamException, IOException {
		assertTrue("Page with title 'ComputerAcessibility' doesn't have alternative title 'AccessibleComputing'.", parser.getRedirectedPages().get("Computer accessibility").contains("AccessibleComputing"));
	}
	
	@Test
	public void pageRoadBicycleShouldHaveOneAlternativeTitle() throws XMLStreamException, IOException {
		assertTrue("Page with title 'Road bicycle' doesn't have one alternative title.", parser.getRedirectedPages().get("Road bicycle").size() == 1);
	}
	
	@Test
	public void pageCarnaticShouldNotHaveAlternativeTitles() throws XMLStreamException, IOException {
		assertTrue("Page with title 'Carnatic' shouldn't have alternative titles.", parser.getRedirectedPages().get("Carnatic").size() == 0);
	}
	
	@Test
	public void pageCortezShouldNotHaveAlternativeTitles() throws XMLStreamException, IOException {
		assertTrue("Page with title 'Cortez' shouldn't have alternative titles.", parser.getRedirectedPages().get("Cortez").size() == 0);
	}
	
	@Test
	public void outputFileShouldBeCreated() throws IOException, XMLStreamException {
		
		// Save test output in default user home directory
		String testOutputPath = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath() + "/" + OUTPUT_FILENAME;		
		parser.saveToCSV(testOutputPath);
		
		File f = new File(testOutputPath);
		if(!f.exists()) {
			fail("Data file was not found");
		}
	}
}
