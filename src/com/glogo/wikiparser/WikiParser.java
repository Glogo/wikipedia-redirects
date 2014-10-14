package com.glogo.wikiparser;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.cedarsoftware.util.io.JsonWriter;

/**
 * Main Wiki pages parser.\n
 * This class contains methods necessary to parse input XML doc, find alternative titles for articles (pages) and export them to readable format.
 * @author Glogo
 */
public class WikiParser {

	/**
	 * This regular expression pattern is used to match links in wiki article text content.<br />
	 * Only links with following rules are matched:
	 * <ul>
	 *     <li>Match only links to internal articles</li>
	 *     <li>Match only links having delimiter "|"</li>
	 *     <li>Match links in two groups</li>
	 * </ul>
	 * Unescaped pattern: "\[\[([^\]\[:]+)\|([^\]\[:]+)\]\]"<br />
	 * Short pattern description: Captures wiki links between [[]] tags not beginning with any "Namespace:" and containing "|" delimiter (result is in 2 groups) 
	 * @see <a href="http://stackoverflow.com/questions/26010846/regex-match-wikipedia-internal-article-links/26010910#26010910">Related stackoverflow question</a>
	 */
	private static final Pattern WIKI_LINKS_PATTERN = Pattern.compile("\\[\\[([^\\]\\[:]+)\\|([^\\]\\[:]+)\\]\\]");
	
	/**
	 * Main {@link Document}, which will be read and parsed.
	 */
	private Document doc;
	
	/**
	 * All pages stored in TreeMap with case insensitive keys.<br />
	 *     <b>key:</b> PageModel title<br />
	 *     <b>value:</b> PageModel instance
	 */
	private Map<String, PageModel> pages = new TreeMap<String, PageModel>(String.CASE_INSENSITIVE_ORDER);
	
	/**
	 * Main constructor which is supposed to open XML file in specified absolute path 
	 * @param path
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public WikiParser(String path) throws ParserConfigurationException, SAXException, IOException {
		System.out.printf("Opening file: '%s'\n", path);
		
		// Open file and parse Document
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		doc = builder.parse(path);
		
		System.out.println("File successfully opened.");
	}
	
	/**
	 * Reads XML {@link Document} nodes, creates {@link PageModel} instances and stores them {@link WikiParser#pages} map 
	 * @throws XPathExpressionException 
	 */
	public void readPages() {
		Element pageElement;
		Element titleElement;
		Element redirectElement;
		Element revisionElement;
		Element textElement;
		PageModel pageModel;
		
		// XPath instance used to query nodes in XML document.
		XPath xpath = XPathFactory.newInstance().newXPath();
	
		// Compiled XPath expression 
		XPathExpression expr = null;
		
		// Read nodes from Document
		NodeList nodes = null;
		
		try{
			expr = xpath.compile("/mediawiki/page");
			nodes = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
		}catch(XPathExpressionException e){
			System.err.println(e.getMessage());
			System.exit(1);
		}
		
		// Clear pages map
		pages.clear();
		
		// Loop through all found nodes and create page models
		for(int i = 0; i < nodes.getLength(); i++) {
			pageElement = (Element)nodes.item(i);
			titleElement = (Element)pageElement.getElementsByTagName("title").item(0);
			redirectElement = (Element)pageElement.getElementsByTagName("redirect").item(0);
			revisionElement = (Element)pageElement.getElementsByTagName("revision").item(0);
			textElement = (Element)revisionElement.getElementsByTagName("text").item(0);
			
			// Create pageModel
			pageModel = new PageModel(titleElement.getTextContent(), textElement.getTextContent());
			
			// Is page redirected to another page?
			if(redirectElement != null){
				pageModel.setRedirectsToPageTitle(redirectElement.getAttribute("title"));
				// System.out.println(pageModel.getTitle() + " -> " + pageModel.getRedirectsToPageTitle());
			}
			
			pages.put(pageModel.getTitle(), pageModel);
		}
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
		Matcher matcher;
		String matchedArticleTitle;
		String matchedLinkText;
		PageModel pageModel;
		PageModel tmpPageModel;
		
		// Clear all alternative titles & anchor texts
		for (Map.Entry<String, PageModel> entry : pages.entrySet()) {
			entry.getValue().getAlternativeTitles().clear();
			entry.getValue().getAnchorTexts().clear();
		}
		
		// Loop through pages in map
		for (Map.Entry<String, PageModel> entry : pages.entrySet()) {
			pageModel = entry.getValue();
			
			// Add redirectsToPage instance if page is redirecting to another page. If redirected page was not found then null will be returned automatically
			if(pageModel.getRedirectsToPageTitle() != null){
				pageModel.setRedirectsToPage(pages.get(pageModel.getRedirectsToPageTitle()));
			}
			
			/*
			 *  1. Add alternative title to redirected page
			 */
			if(pageModel.getRedirectsToPage() != null){
				pageModel.getRedirectsToPage().addAlternativeTitle(entry.getKey());
				continue;
			}
			
			/*
			 *  2. Parse page text and extract anchor texts from links.
			 */
			// Check if page is not redirection
			if(pageModel.getRedirectsToPageTitle() == null){
				// System.out.println(pageModel.getTitle());
				
				// Find all links in page text
				matcher = WIKI_LINKS_PATTERN.matcher(pageModel.getText());
				
				// For each non-category link matches
				while(matcher.find()){
					matchedArticleTitle = matcher.group(1);
					matchedLinkText = matcher.group(2);
					// System.out.printf("%s|%s => %s \n", matchedArticleTitle, matchedLinkText, matcher.group());
					
					// Check if linked page exists in processed pages
					tmpPageModel = pages.get(matchedArticleTitle);
					if(tmpPageModel != null){
						
						// Add anchor text to anchor texts if not already exists
						if(!tmpPageModel.getAnchorTexts().contains(matchedLinkText)){
							tmpPageModel.addAnchorText(matchedLinkText);
						}
					}
				}
			}			
			
		}
	}
	
	/**
	 * Adds following statistics to root of supplied json object: <br />
	 * <ul>
	 *     <li><b>pagesCnt: </b>Total pages count</li>
	 *     <li><b>redirPagesCnt: </b>Total pages count with at least one alternative title</li>
	 *     <li><b>pagesAltCnt: </b>Total pages count with at least one alternative title</li>
	 *     <li><b>pagesAnchCnt: </b>Total pages count with at least one anchor text</li>
	 *     <li><b>altTitlesCnt: </b>Total count of all alternative titles</li>
	 *     <li><b>anchTextsCnt: </b>Total count of all anchor texts</li>
	 * </ul>
	 * 
	 * @param json
	 */
	@SuppressWarnings("unchecked")
	private void addMetricsToJSON(JSONObject json) {
		json.put("pagesCnt", pages.size());
		
		int redirPagesCnt = 0;
		int pagesAltCnt = 0;
		int pagesAnchCnt = 0;
		int altTitlesCnt = 0;
		int anchTextsCnt = 0;
		
		PageModel pageModel;
		
		// Loop through all pages and increment appropriate metric
		for (Map.Entry<String, PageModel> entry : pages.entrySet()) {
			pageModel = entry.getValue();
			
			// If page is redirect
			if(pageModel.getRedirectsToPageTitle() != null){
				redirPagesCnt++;
				continue;
			}
			
			// If page has at least one alternative title
			if(pageModel.getAlternativeTitles().size() > 0){
				pagesAltCnt++;
				altTitlesCnt += pageModel.getAlternativeTitles().size();
			}
			
			// If page has at least one anchor text
			if(pageModel.getAnchorTexts().size() > 0){
				pagesAnchCnt++;
				anchTextsCnt += pageModel.getAnchorTexts().size();
			}
		}
		
		// Store metrics values in json
		json.put("redirPagesCnt", redirPagesCnt);
		json.put("pagesAltCnt", pagesAltCnt);
		json.put("pagesAnchCnt", pagesAnchCnt);
		json.put("altTitlesCnt", altTitlesCnt);
		json.put("anchTextsCnt", anchTextsCnt);
	}
	
	@SuppressWarnings("unchecked")
	public void exportToJSON(String path) throws IOException{
		JSONObject json = new JSONObject();
		json.put("author", "Michael Gloger");
		
		addMetricsToJSON(json);
		
		JSONArray pagesObjects = new JSONArray();
		json.put("pages", pagesObjects);
		
		// Loop through pages and create + add element to root element
		System.out.println("Outputting pages with alternative titles to JSON");
		for (Map.Entry<String, PageModel> entry : pages.entrySet()) {
			
			/*
			 * Ouput pages which:
			 * 	- are not redirects
			 *  - ...
			 */
			if(entry.getValue().getRedirectsToPageTitle() == null /* || entry.getValue().getAlternativeTitles().size() > 0 || entry.getValue().getAnchorTexts().size() > 0 */) {
				
				// Create alternative titles json array
				JSONArray alternativeTitles = new JSONArray();
	
				// Add all alternative titles to array
				for(String alternativeTitle : entry.getValue().getAlternativeTitles()){
					alternativeTitles.add(alternativeTitle);
				}
				
				// Create anchor texts json array
				JSONArray anchorTexts = new JSONArray();
				
				// Add all anchor texts to array
				for(String anchorText : entry.getValue().getAnchorTexts()){
					anchorTexts.add(anchorText);
				}
				
				// Create page json object
				JSONObject pageObject = new JSONObject();
				pageObject.put("title", entry.getValue().getTitle());
				pageObject.put("alternative", alternativeTitles);
				pageObject.put("anchor", anchorTexts);
				
				// Add page object to json
				pagesObjects.add(pageObject);
			
			}
		}
		
		FileWriter file = new FileWriter(path);
        try {
        	// Little hack to make my life easier
            file.write("var pagesData = " + JsonWriter.formatJson(json.toJSONString()) + ";");
            System.out.println("Successfully saved JSON object to file...");
 
        } catch (IOException e) {
            e.printStackTrace();
 
        } finally {
            file.flush();
            file.close();
        }
	}
	
	public Document getDocument() {
		return doc;
	}
	
	public Map<String, PageModel> getPages(){
		return pages;
	}
}
