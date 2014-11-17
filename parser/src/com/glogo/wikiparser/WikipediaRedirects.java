package com.glogo.wikiparser;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Wiki redirect pages parser.
 * This class contains methods necessary to parse input XML doc, find alternative titles for pages from redirects and export them to readable format.
 * For detailed information please see <a href="https://github.com/Glogo/wikipedia-redirects">project repository</a>
 * @author Glogo
 */
public class WikipediaRedirects {
	
	private static Logger logger = LoggerFactory.getLogger(WikipediaRedirects.class);

	/**
	 * Wikipedia dump XML reader
	 */
	private WikipediaRedirectsReader wikiReader = new WikipediaRedirectsReader();
	
	/**
	 * Guava {@link Multimap} of all redirect pages<br />
	 *     <b>key:</b> Title of redirected page (from XML redirect element & title attribute)<br>
	 *     <b>value:</b> Title of redirect page (more values are possible for one key)<br>
     * Expected keys & values per keys were added after calculating total redirect pages of complete Wikipedia(en) dump.<br>
     * <br>
     * Terminology:<br />
	 * 	<b>Redirect page</b> page which is redirecting to another page.<br />
	 *  <b>Redirected page</b> page which was redirected to from redirect page page.<br />
	 */
	Multimap<String, String> redirectedPages = ArrayListMultimap.create(7000000, 4);
	
	/**
	 * Reads XML file as {@link InputStream} using {@link WikipediaRedirectsReader} class and stores redirected pages in redirectedPages Multimap
	 * @throws XMLStreamException 
	 * @throws IOException
	 */
	public void readPages(String filename) throws XMLStreamException, IOException {
		wikiReader.readFile(filename, redirectedPages);
	}
	
	/**
	 * Exports pages with alternative titles to csv file<br>
	 * and adds following statistics (in specified order) to first record: <br>
	 * <ul>
	 *     <li><b>totalPagesCnt:</b> Total pages count</li>
	 *     <li><b>nonRedirPagesCnt:</b> Total non-redirect pages count</li>
	 *     <li><b>redirPagesCnt:</b> Total redirect pages count</li>
	 *     <li><b>pagesWithAltCnt:</b> Total non-redirect pages with at least one alternative title from redirects</li>
	 * </ul>
	 */
	public void saveToCSV(String path) throws IOException{
		
		/*
		 *  Sepparate info file
		 */
		
		// Add suffix to path filename
		String extension = FilenameUtils.getExtension(path);
		String infoFilePath = FilenameUtils.getFullPathNoEndSeparator(path) + "/" + FilenameUtils.getBaseName(path) + "_info" + ((extension == null || extension.length() == 0) ? "" : "." + extension);
		
		logger.info("Saving info to CSV file: '{}'", infoFilePath);
		CSVWriter infoWriter = new CSVWriter(new FileWriter(infoFilePath, false));
		infoWriter.writeNext(new String[]{
			"author",
			"totalPagesCnt",
			"nonRedirPagesCnt",
			"redirPagesCnt",
			"pagesWithAltCnt"
		});
		infoWriter.writeNext(new String[]{
			"Michael Gloger",
			
			// Info row statistics
			String.valueOf(wikiReader.getTotalPagesCount()),
			String.valueOf(wikiReader.getTotalPagesCount() - redirectedPages.size()),
			String.valueOf(redirectedPages.size()),
			String.valueOf(redirectedPages.keySet().size())
		});
		infoWriter.close();
		
		/*
		 *  Alternative titles file
		 */
		logger.info("Saving pages with alternative titles to CSV file: '{}'", path);
		CSVWriter writer = new CSVWriter(new FileWriter(path));
		
		// Iterate over all alternative titles
		for(Entry<String, Collection<String>> entry : redirectedPages.asMap().entrySet()){
			String pageTitle = entry.getKey();
			Collection<String> alternativeTitles = entry.getValue();
			
			// Write to csv file with mega arrays overkill. Consider it hardcore experiment
			// Page title is in first column and other columns contains alternative titles
			writer.writeNext(Arrays.copyOf(ArrayUtils.addAll(new String[]{pageTitle}, alternativeTitles.toArray()), alternativeTitles.size() + 1, String[].class));
		}
		writer.close();
	}
	
	public Multimap<String, String> getRedirectedPages(){
		return redirectedPages;
	}
}
