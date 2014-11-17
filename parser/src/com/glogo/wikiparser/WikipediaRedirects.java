package com.glogo.wikiparser;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Wiki redirect pages parser.
 * This class contains methods necessary to parse input XML doc, find alternative titles for pages from redirects and export them to readable format.
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
	 * Exports pages with alternative titles to json file<br>
	 * and adds following statistics to info parameter of json object: <br>
	 * <ul>
	 *     <li><b>totalPagesCnt:</b> Total pages count</li>
	 *     <li><b>nonRedirPagesCnt:</b> Total non-redirect pages count</li>
	 *     <li><b>redirPagesCnt:</b> Total redirect pages count</li>
	 *     <li><b>pagesWithAltCnt:</b> Total non-redirect pages with at least one alternative title from redirects</li>
	 * </ul>
	 * 
	 * @param json
	 */
	public void exportToJSON(String path) throws IOException{
		logger.info("Exporting pages with alternative titles to JSON");
		
		// LinkedHashMap is used to preserve order
		Map<String, Object> json = new LinkedHashMap<String, Object>();
		
		// Info json element
		Map<String, Object> info = new LinkedHashMap<String, Object>();
		info.put("author", "Michael Gloger");
		info.put("totalPagesCnt", wikiReader.getTotalPagesCount());
		info.put("nonRedirPagesCnt", wikiReader.getTotalPagesCount() - redirectedPages.size());
		info.put("redirPagesCnt", redirectedPages.size());
		info.put("pagesWithAltCnt", redirectedPages.keySet().size());
		json.put("info", info);
		
		// Add redirected pages to json. Once again: key = title of page & array of values = alternative titles (titles of pages redirecting to current page)
		json.put("pages", redirectedPages.asMap());
		
		// Write json to file
		FileWriter file = new FileWriter(path);
        try {
        	
        	// Create Google Gson to simplify json serializing
        	Gson gson = new GsonBuilder().create();
            file.write(gson.toJson(json));
            logger.info("Successfully saved JSON object to file: '{}'", path);
 
        } catch (IOException e) {
            e.printStackTrace();
 
        } finally {
            file.flush();
            file.close();
        }
	}
	
	public Multimap<String, String> getRedirectedPages(){
		return redirectedPages;
	}
}
