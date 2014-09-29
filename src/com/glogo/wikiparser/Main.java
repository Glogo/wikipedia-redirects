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

public class Main {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		// Create document and read xml
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document doc = builder.parse("/Users/Glogo/Documents/School/VI/vi-wiki-parser/res/enwiki-latest-pages-articles1.xml");
		XPath xpath = XPathFactory.newInstance().newXPath();
		
		// Unescaped regex: \[\[([^\]\[:]+)\|([^\]\[:]+)\]\]
		// http://stackoverflow.com/questions/26010846/regex-match-wikipedia-internal-article-links/26010910#26010910
		// Captures wiki links between [[]] tags not beginning with any "Namespace:" and containing "|" delimiter (result is in 2 groups)
		Pattern wikiLinksPattern = Pattern.compile("\\[\\[([^\\]\\[:]+)\\|([^\\]\\[:]+)\\]\\]");
		
		// Helper data
		Element pageElement;
		Element titleElement;
		Element redirectElement;
		Element revisionElement;
		Element textElement;
		PageModel pageModel;
		Matcher matcher;
		String matchedArticleTitle;
		String matchedLinkText;
		
		// Start parsing
		System.out.printf("Starting parsing file: '%s'\n", doc.getBaseURI());
		long start = System.currentTimeMillis();

		// XPath Query for all page elements
		XPathExpression expr = xpath.compile("/mediawiki/page");
		
		// Map<PageModel.title, PageModel> of pages with case insensitive keys
		Map<String, PageModel> pages = new TreeMap<String, PageModel>(String.CASE_INSENSITIVE_ORDER);
		
		// Get all nodes from document
		// TODO use page id as key if it is faster than String keys if possible
		NodeList nodes = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
		
		System.out.printf("Nodes list of %d pages returned in %ds\n", nodes.getLength(), (System.currentTimeMillis() - start) / 1000);
		start = System.currentTimeMillis();
		
		/*
		 *  First processing: loop through nodes and create page models
		 */
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
		
		System.out.printf("First page process finished in %ds\n", (System.currentTimeMillis() - start) / 1000);
		start = System.currentTimeMillis();
		
		/*
		 *  Second processing: loop through pages
		 */
		for (Map.Entry<String, PageModel> entry : pages.entrySet()) {
			
			// Add redirectsToPage instance if page is redirecting to another page. If redirected page was not found then null will be returned automatically
			if(entry.getValue().getRedirectsToPageTitle() != null){
				entry.getValue().setRedirectsToPage(pages.get(entry.getValue().getRedirectsToPageTitle()));
			}
			
			/*
			 *  1. Add alternative title to redirected page
			 */
			if(entry.getValue().getRedirectsToPage() != null){
				entry.getValue().getRedirectsToPage().addAlternativeTitle(entry.getKey());
			}
			
			/*
			 *  2. Parse page text and extract links.
			 */
			// Check if page is not redirection
			if(entry.getValue().getRedirectsToPageTitle() == null){
//				System.out.println(entry.getValue().getTitle());
				
				// Find all links in page text
				matcher = wikiLinksPattern.matcher(entry.getValue().getText());
				
				// For each non-category link matches
				while(matcher.find()){
					matchedArticleTitle = matcher.group(1);
					matchedLinkText = matcher.group(2);
//					System.out.printf("%s|%s => %s \n", matchedArticleTitle, matchedLinkText, matcher.group());
					
					// Check if linked page exists in processed pages
					pageModel = pages.get(matchedArticleTitle);
					if(pageModel != null){
						// Add link text to alternative page title if not already exists
						if(!pageModel.getAlternativeTitles().contains(matchedLinkText)){
							// TODO distinguish between redirected and linked alternative names source
							pageModel.getAlternativeTitles().add(matchedLinkText);
						}
					}
				}
			}			
			
		}
		
		System.out.printf("Second page process finished in %ds\n", (System.currentTimeMillis() - start) / 1000);
		start = System.currentTimeMillis();
		
		// TODO store data in text file / xml
		
		/*
		 * Test loop. Loop through pages and print pages with alternative titles
		 */
//		System.out.printf("Printing alternative page titles in format: <PageTitle>|[AlternativeNamesArray]\n");
//		for (Map.Entry<String, PageModel> entry : pages.entrySet()) {
//			
//			if(entry.getValue().getAlternativeTitles().size() > 0){
//				System.out.printf("%s|%s\n", entry.getKey(), entry.getValue().getAlternativeTitles());
//			}
//		}
		
		/*
		 * Output parsed data to JSON
		 */
		
		JSONObject json = new JSONObject();
		// TODO add data info
//		json.put("Author", "Michael Gloger");
		
		JSONArray pagesObjects = new JSONArray();
		json.put("pages", pagesObjects);
		
		// Loop through pages and create + add element to root element
		System.out.println("Outputting pages with alternative titles to JSON");
		for (Map.Entry<String, PageModel> entry : pages.entrySet()) {
			
			// Output only pages which have alternative titles
			if(entry.getValue().getAlternativeTitles().size() > 0){
				
				// Create alternative titles json array
				JSONArray alternativeTitles = new JSONArray();
				
				// Add all alternative titles to array
				for(String alternativeTitle : entry.getValue().getAlternativeTitles()){
					alternativeTitles.add(alternativeTitle);
				}
				
				// Create page json object
				JSONObject pageObject = new JSONObject();
				pageObject.put("title", entry.getValue().getTitle());
				pageObject.put("alt", alternativeTitles);
				
				// Add page object to json
				pagesObjects.add(pageObject);
			}
		}
		
		FileWriter file = new FileWriter("data.js");
        try {
        	// Little hack to make my life easier
            file.write("var pagesData = " + JsonWriter.formatJson(json.toJSONString()) + ";");
            System.out.println("Successfully Copied JSON Object to File...");
 
        } catch (IOException e) {
            e.printStackTrace();
 
        } finally {
            file.flush();
            file.close();
        }
		
	}

}
