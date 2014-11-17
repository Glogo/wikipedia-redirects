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
 * This class contains simple jUnit test for WikiParser
 * @author michael.gloger
 */
public class WikiParserTest {
	
	private static WikipediaRedirects parser = null;
	private static String absolutePath;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		parser = new WikipediaRedirects();
		
		URL testInputUrl = WikiParserTest.class.getClassLoader().getResource("test_input.xml");
		
		// Decode url to remove %20 space sepparators
		absolutePath = URLDecoder.decode(testInputUrl.getFile(), "UTF-8");
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
	public void pageDogShouldHaveThreeAlternativeTitle() throws XMLStreamException, IOException {
		parser.readPages(absolutePath);
		assertTrue("Page with title 'Dog' doesn't have three alternative title.", parser.getRedirectedPages().get("Dog").size() == 3);
	}
	
	@Test
	public void pageFootballShouldHaveOneAlternativeTitleSoccer() throws XMLStreamException, IOException {
		parser.readPages(absolutePath);
		assertTrue("Page with title 'Football' doesn't have alternative title 'Soccer' (ignoring case).", parser.getRedirectedPages().get("Football").contains("Soccer"));
	}
	
	@Test
	public void pageSoccerShouldNotHaveAlternativeTitles() throws XMLStreamException, IOException {
		parser.readPages(absolutePath);
		assertTrue("Page with title 'Soccer' shouldn't have alternative titles.", parser.getRedirectedPages().get("Soccer") != null);
	}
	
	@Test
	public void outputFileShouldBeCreated() throws IOException, XMLStreamException {
		parser.readPages(absolutePath);

		// Save test output in default user home directory
		String testOutputPath = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath() + "/test_output.xml";		
		parser.saveToCSV(testOutputPath);
		
		File f = new File(testOutputPath);
		if(!f.exists()) {
			fail("Data file was not found");
		}
	}
}
