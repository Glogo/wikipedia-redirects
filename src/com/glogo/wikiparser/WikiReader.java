package com.glogo.wikiparser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

/**
 * Class used to read large wikipedia dumps XML files into pages map.
 * @see <a href="http://www.studytrails.com/java/xml/woodstox/java-xml-stax-woodstox-basic-parsing.jsp">Helpful Stax Woodstox parsing example</a>
 * @author Glogo
 */
public class WikiReader {
	
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
	 * Reader states determining current XML reading position
	 */
	private enum State {
		NOPE,
		PAGE_ELEMENT,
		TITLE_ELEMENT,
		REDIRECT_ELEMENT,
		REVISION_ELEMENT,
		TEXT_ELEMENT
	};
	
	/*
	 * XML elements and attributes strings
	 */
	private static final String PAGE_ELEMENT = "page";
	private static final String TITLE_ELEMENT = "title";
	private static final String REDIRECT_ELEMENT = "redirect";
	private static final String REVISION_ELEMENT = "revision";
	private static final String TEXT_ELEMENT = "text";
	
	private State state = State.NOPE;
	
	/**
	 * Opens file with filename and parses wikipedia pages data into pages map.
	 * @param filename 
	 * @param pages
	 * @throws XMLStreamException 
	 * @throws IOException 
	 */
	public void readFile(String filename, Map<String, PageModel> pages) throws XMLStreamException, IOException{
		/*
		 * Try to open file
		 */
		//test
		//filename = new File("res/test_input.xml").getAbsolutePath();
		System.out.printf("Reading file: '%s'\n", filename);
		InputStream xmlInputStream = new FileInputStream(filename);
        XMLInputFactory2 xmlInputFactory = (XMLInputFactory2)XMLInputFactory.newInstance();
        XMLStreamReader2 xmlStreamReader = (XMLStreamReader2) xmlInputFactory.createXMLStreamReader(xmlInputStream);
        
        PageModel pageModel = null;
        int eventType;
        String elementName;
        String redirectsTo;
        StringBuffer bufferedText = new StringBuffer(); // Used to append text since characters return only portion of very long texts
        
		// Clear pages map
		pages.clear();
        
        /*
         * Read file contents and store in map. All nodes traversal conditions may not be all necessary, but it is also used as schema validation.
         */
        while(xmlStreamReader.hasNext()){
        	
            eventType = xmlStreamReader.next();
            
            switch (eventType) {
	            case XMLEvent.START_ELEMENT:
	            	elementName = xmlStreamReader.getName().getLocalPart();
	            	
	            	// We are currently on no element & page element started (we are ignoring root mediawiki element)
	            	if(state == State.NOPE && elementName.equals(PAGE_ELEMENT)){
	            		state = State.PAGE_ELEMENT;
	            		
	            		// Create new page model
	            		pageModel = new PageModel();
	            		
	            		// Clear text buffer
	            		bufferedText.setLength(0);
	            		
	            	// We are currently on page element & title element started
	            	}else if(state == State.PAGE_ELEMENT && elementName.equals(TITLE_ELEMENT)){
	            		state = State.TITLE_ELEMENT;

	            	// We are currently on page element & redirect element started
	            	}else if(state == State.PAGE_ELEMENT && elementName.equals(REDIRECT_ELEMENT)){
	            		state = State.REDIRECT_ELEMENT;
	            		
	            		// Get redirects to page title (there is always only one attribute named "titles")
	            		redirectsTo = xmlStreamReader.getAttributeValue(0);
	            		
	            		// Is page redirected to another page?
	        			if(redirectsTo != null && redirectsTo.length() != 0){
	        				pageModel.setRedirectsToPageTitle(redirectsTo);
	        				// System.out.println(pageModel.getTitle() + " -> " + pageModel.getRedirectsToPageTitle());
	        			}
	            		
	            	// We are currently on page element & revision element started
	            	}else if(state == State.PAGE_ELEMENT && elementName.equals(REVISION_ELEMENT)){
	            		state = State.REVISION_ELEMENT;
	            	
	            	// We are currently on revision element & text element started
	            	}else if(state == State.REVISION_ELEMENT && elementName.equals(TEXT_ELEMENT)){
	            		state = State.TEXT_ELEMENT;
	            	}
	            	
	                break;

	            case XMLEvent.CHARACTERS:
	            	
	            	// We are currently on title element
	            	if(state == State.TITLE_ELEMENT){
	            		pageModel.setTitle(xmlStreamReader.getText());
	            	
            		// We are currently on text element
	            	}else if(state == State.TEXT_ELEMENT){
	            		bufferedText.append(xmlStreamReader.getText());
	            	}
	            	
	                break;

	            case XMLEvent.END_ELEMENT:
	            	elementName = xmlStreamReader.getName().getLocalPart();
	            	
	            	// We are on page element which ended
	            	if(state == State.PAGE_ELEMENT && elementName.equals(PAGE_ELEMENT)){
	            		state = State.NOPE;
	            		
	            		// Add page model to pages map
	            		pages.put(pageModel.getTitle(), pageModel);
	            		
	            		pageModel = null;
	            		
	            	// We are on title element which ended
	            	}else if(state == State.TITLE_ELEMENT && elementName.equals(TITLE_ELEMENT)){
	            		state = State.PAGE_ELEMENT;
	            		
	            	// We are on redirect element which ended
	            	}else if(state == State.REDIRECT_ELEMENT && elementName.equals(REDIRECT_ELEMENT)){
	            		state = State.PAGE_ELEMENT;
	            	
	            	// We are on revision element which ended
	            	}else if(state == State.REVISION_ELEMENT && elementName.equals(REVISION_ELEMENT)){
	            		state = State.PAGE_ELEMENT;
	            	
	            	// We are on text element which ended
	            	}else if(state == State.TEXT_ELEMENT && elementName.equals(TEXT_ELEMENT)){
	            		state = State.REVISION_ELEMENT;
	            		
	            		// Instead of remembering whole page text, parse text for anchor texts links here and process later
	            		readAnchorTextLinks(pageModel, bufferedText.toString());
	            	}
	                
	                break;
            }
        }
        
        System.out.printf("%d pages were successfully read into map\n", pages.size());
        
        /*
         * Close input stream & reader
         */
        xmlInputStream.close();
        xmlStreamReader.closeCompletely();
	}

	/**
	 * Reads anchor links from page text and remembers anchor links in page model
	 */
	private void readAnchorTextLinks(PageModel pageModel, String text){
		Matcher matcher;
		String anchorLink;
		String anchorText;
		
		/*
		 * Parse page text and extract anchor texts from links
		 */
		// Check if page is not redirection & has not null text
		if(pageModel.getRedirectsToPageTitle() == null && text != null && text.length() != 0){
			// System.out.println(pageModel.getTitle());
			
			// Find all anchor texts links in page text
			matcher = WIKI_LINKS_PATTERN.matcher(text);
			
			// For each non-category link matches
			while(matcher.find()){

				// Read link info
				anchorLink = matcher.group(1);
				anchorText = matcher.group(2);
				
				// Store link info (will be processed later)
				pageModel.addAnchorTextLink(new AnchorTextLink(anchorText, anchorLink));
				
				//System.out.printf("%s|%s => %s\n", matchedArticleTitle, matchedLinkText, matcher.group());
			}
		}
	}

}
