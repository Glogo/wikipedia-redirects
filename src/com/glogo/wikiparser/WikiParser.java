package com.glogo.wikiparser;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

import com.glogo.wikiparser.model.PageModel;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Main Wiki pages parser.
 * This class contains methods necessary to parse input XML doc, find alternative titles for articles (pages) and export them to readable format. <br /><br />
 * Terminology:<br />
 * 	<b>Redirect page</b> page which is redirecting to another page.<br />
 *  <b>Redirected page</b> page which was redirected to from redirect page page.<br />
 *  
 * TODO: stuff in loops could be merged together. It is currently not performance killer. Divided loops improve readability. 
 * 
 * @author Glogo
 */
public class WikiParser {

	/**
	 * Wikipedia dump XML reader
	 */
	private WikiReader wikiReader = new WikiReader();
	
	/**
	 * All non-redirect pages<br />
	 *     <b>key:</b> PageModel id<br />
	 *     <b>value:</b> PageModel instance
	 */
	private Map<Integer, PageModel> pages = new HashMap<Integer, PageModel>(1000000);
	
	/**
	 * All redirect pages<br />
	 *     <b>key:</b> PageModel title of redirected page (from XML redirect element & title attribute)<br />
	 *     <b>value:</b> PageModel title of redirect page (more values are possible for one key)<br />
	 * This is major (Carter) performance optimization, since we don't need to store
     * all pages in HashMap<String, PageModel> (problems with {@link String#hashCode()}) but only redirects in separate map.
     * In main cycle in {@link WikiParser#findAlternativeTitles()} step we will be iterating over all non-redirect pages and look
     * in this Multimap for alternative titles.
     * This needs to be done after all pages were read since we need pages in memory to add alternative titles.
	 */
	Multimap<String, String> redirectedPages = ArrayListMultimap.create(pages.size()/4, 4);
	
	/**
	 * Reads XML file as {@link InputStream} using {@link WikiReader} class, creates {@link PageModel} instances and stores them {@link WikiParser#pages} map
	 * @throws XMLStreamException 
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 */
	public void readPages(String filename) throws XMLStreamException, IOException {
		wikiReader.readFile(filename, pages, redirectedPages);
	}
	
	/**
	 * This method searches for alternative titles in pages stored in {@link WikiParser#pages} map.<br />
	 * Alternative titles will be then stored in each page property {@link PageModel#alternativeTitles}.<br />
	 * Alternative page titles are:
	 * <ul>
	 *     <li>Titles of auto redirect pages</li>
	 *     <li>Text content of links to another pages</li>
	 * </ul>
	 */
	public void findAlternativeTitles(){
		PageModel pageModel;
		
		Logger.info("Finding alternative titles");
		
		/*
		 * Loop through pages in map again & add alternative titles to pages
		 */
		for (Map.Entry<Integer, PageModel> entry : pages.entrySet()) {
			
			pageModel = entry.getValue();
			
			pageModel.getAlternativeTitles().clear();
			
			// Get all pages titles redirecting to current page (if any)
			Collection<String> redirectsFromPagesTitles = redirectedPages.get(pageModel.getTitle());
			
			// Add all redirect pages titles as alternative titles
			pageModel.getAlternativeTitles().addAll(redirectsFromPagesTitles);
		}
	}
	
	/**
	 * Adds following statistics to root of supplied json object: <br />
	 * <ul>
	 *     <li><b>totalPagesCnt: </b>Total pages count</li>
	 *     <li><b>nonRedirPagesCnt: </b>Total non-redirect pages count</li>
	 *     <li><b>redirPagesCnt: </b>Total redirect pages count</li>
	 *     <li><b>pagesWithAltCnt: </b>Total non-redirect pages with at least one alternative title from redirects</li>s
	 * </ul>
	 * 
	 * @param json
	 */
	private void addMetricsToJSON(Map<String, Object> json) {
		int pagesWithAltCnt = 0;
		
		PageModel pageModel;
		
		// Loop through all pages and increment appropriate metric
		for (Map.Entry<Integer, PageModel> entry : pages.entrySet()) {
			pageModel = entry.getValue();
			
			// If page has at least one alternative title
			if(pageModel.getAlternativeTitles().size() > 0) {
				pagesWithAltCnt++;
			}
		}
		
		// Store metrics values in json
		json.put("totalPagesCnt", pages.size() + redirectedPages.size());
		json.put("nonRedirPagesCnt", pages.size());
		json.put("redirPagesCnt", redirectedPages.size());
		json.put("pagesWithAltCnt", pagesWithAltCnt);
	}
	
	public void exportToJSON(String path) throws IOException{
		
		PageModel pageModel;
		
		// LinkedHashMap is used to preserve root attributes order
		Map<String, Object> json = new LinkedHashMap<String, Object>();
		
		// Info json element
		Map<String, Object> info = new LinkedHashMap<String, Object>();
		info.put("author", "Michael Gloger");
		addMetricsToJSON(info);
		
		json.put("info", info);
		
		// Initialize json array of all pages (it is list of pages maps)
		List<Map<String, Object>> pagesObjects = new ArrayList<Map<String, Object>>();
		json.put("pages", pagesObjects);
		
		Logger.info("Exporting pages with alternative titles to JSON");
		
		// Loop through pages, create & add element to pages element array value
		for(Map.Entry<Integer, PageModel> entry : pages.entrySet()) {
			
			pageModel = entry.getValue();
			
			// Skip page if does not have any alternative titles
			if(pageModel.getAlternativeTitles().size() == 0){
				continue;
			}
				
			// Create alternative titles json array
			List<String> alternativeTitles = new ArrayList<String>();

			// Add all alternative titles to array
			for(String alternativeTitle : pageModel.getAlternativeTitles()){
				alternativeTitles.add(alternativeTitle);
			}
			
			// Create page json object
			Map<String, Object> pageObject = new HashMap<String, Object>();
			pageObject.put("title", pageModel.getTitle());
			pageObject.put("alternative", alternativeTitles);
			
			// Add page object to json
			pagesObjects.add(pageObject);
		}
		
		FileWriter file = new FileWriter(path);
        try {
        	
        	// Create Google Gson to simplify json serializing
        	Gson gson = new GsonBuilder().setPrettyPrinting().create();
        	
        	// Create javascript compatible output to enable easy querying
            file.write("var pagesData = " + gson.toJson(json) + ";");
            Logger.info("Successfully saved JSON object to file: '%s'", path);
 
        } catch (IOException e) {
            e.printStackTrace();
 
        } finally {
            file.flush();
            file.close();
        }
	}
	
	public Map<Integer, PageModel> getPages(){
		return pages;
	}
}
