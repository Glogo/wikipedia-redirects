package com.glogo.wikiparser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

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
		System.out.println(String.format("Reading file: '%s'", filename));
		InputStream xmlInputStream = new FileInputStream(filename);
        XMLInputFactory2 xmlInputFactory = (XMLInputFactory2)XMLInputFactory.newInstance();
        XMLStreamReader2 xmlStreamReader = (XMLStreamReader2) xmlInputFactory.createXMLStreamReader(xmlInputStream);
        
        PageModel pageModel = null;
        int eventType;
        String elementName;
        String redirectsTo;
        
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
	            		pageModel = new PageModel();
	            		
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
	            		pageModel.setText(xmlStreamReader.getText());
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
	            	}
	                
	                break;
            }
        }
        
        System.out.println(String.format("%d pages were successfully read into map", pages.size()));
        
        /*
         * Close input stream & reader
         */
        xmlInputStream.close();
        xmlStreamReader.closeCompletely();
	}

}
