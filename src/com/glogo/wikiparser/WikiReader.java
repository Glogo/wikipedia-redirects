package com.glogo.wikiparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

import com.glogo.wikiparser.model.PageModel;
import com.google.common.collect.Multimap;

/**
 * Class used to read large wikipedia dumps XML files into pages map.
 * @see <a href="http://www.studytrails.com/java/xml/woodstox/java-xml-stax-woodstox-basic-parsing.jsp">Helpful Stax Woodstox parsing example</a>
 * @author Glogo
 */
public class WikiReader {

    /**
     * Indicates if title element started but not yet ended.
     */
    private boolean isTitle = false;
    
    /**
     * Indicates if id element started but not yet ended.
     */
    private boolean isId = false;
    
    /**
     * Indicates if current page is redirect (has redirect attribute)
     */
    private boolean isRedirect = false;
    
    /**
     * Indicates if revision element started but not yet ended (there are id elements inside revision we want to ommit)
     */
    private boolean isRevision = false;
    
    /**
     * Average bytes per one page, previously calculated from complete wikipedia dump "enwiki-latest-pages-articles.xml"
     * This file size is 50151236957 bytes and total pages element count is 14938386. 50151236957 / 14938386 = 3357.20585591 \approx \!\, 3357
     */
    private static final int AVERAGE_BYTES_PER_PAGE = 3357;
    
    /**
     *  Show progress status after how much % of total pages
     */
    private static final int SHOW_PROGRESS_EACH_PERCENT = 1;
    
    /*
     * XML elements and attributes strings
     */
    private static final String PAGE_ELEMENT = "page";
    private static final String ID_ELEMENT = "id";
    private static final String TITLE_ELEMENT = "title";
    private static final String REDIRECT_ELEMENT = "redirect";
    private static final String REVISION_ELEMENT = "revision";
    
    /**
     * @return approximated total pages count. This value is calculated from source file size in bytes divided by average bytes per one page.
     * Average bytes per one page was previously calculated from wikipedia dump "enwiki-latest-pages-articles10.xml"
     *  
     * @throws XMLStreamException 
     * @throws FileNotFoundException 
     */
    int getFastPagesCount(String filename) throws XMLStreamException, FileNotFoundException {
        File file = new File(filename);
        Logger.info("File size is %d bytes", file.length());

        return (int) (file.length() / AVERAGE_BYTES_PER_PAGE);
    }
    
    /**
     * Opens file with filename and parses wikipedia pages data into pages map.
     * @param filename 
     * @param pages
     * @param redirectedPages 
     * @throws XMLStreamException 
     * @throws IOException 
     */
    public void readFile(String filename, Map<Integer, PageModel> pages, Multimap<String, String> redirectedPages) throws XMLStreamException, IOException{
        /*
         * Try to open file
         */
        //filename = new File("res/test_input.xml").getAbsolutePath(); // test
        Logger.info("Reading file: '%s'", filename);
        InputStream xmlInputStream = new FileInputStream(filename);
        XMLInputFactory2 xmlInputFactory = (XMLInputFactory2)XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true); // Return whole element text altogether (we don't need to use StringBuilder/Buffer)
        XMLStreamReader2 xmlStreamReader = (XMLStreamReader2) xmlInputFactory.createXMLStreamReader(xmlInputStream);
        
        // Progress helpers
        int pagesCount;
        int currentPageIndex = 0;
        int showProgressEach; // How many pages needs to be read to display reading progress status
        float progress;
        
        // Current element name
        String elementName;
        
        // Page info
        Integer id = null; 
        String title = null;
        String redirectsTo = null;
        
        // Clear maps
        pages.clear();
        redirectedPages.clear();
        
        // Do a fast run over xml file to calculate pages count to enable showing reading progress
        Logger.info("Approximating total pages elements count");
        pagesCount = getFastPagesCount(filename);
        showProgressEach = pagesCount / (100 / SHOW_PROGRESS_EACH_PERCENT);
        Logger.info("Approximated total pages elements count: %d (used to calculate progress %%)", pagesCount);
        Logger.info("Displaying approximated progress each %d pages (%d %% of all calculated pages)", showProgressEach, SHOW_PROGRESS_EACH_PERCENT);
        
        /*
         * Read file contents and store in map. All nodes traversal conditions may not be all necessary, but it is also used as schema validation.
         */
        while(xmlStreamReader.hasNext()){
            
            switch (xmlStreamReader.next()) {
                case XMLEvent.START_ELEMENT:
                    elementName = xmlStreamReader.getName().getLocalPart();
                    
                    // Page element started
                    if(elementName.equals(PAGE_ELEMENT)){
                    	
                    	// Print progress each showProgressEach pages
                        if(showProgressEach != 0 && (currentPageIndex++ % showProgressEach == 0)) {
                            progress = (float)currentPageIndex * 100 / pagesCount;
                            Logger.info("Approx reading progress: %.0f%%. Pages processed: %d", progress >= 99 ? 99f : progress, currentPageIndex);
                        }
                        
                    // Id element started
                    }else if(elementName.equals(ID_ELEMENT)){
                        isId = true;
                        
                    // Title element started
                    }else if(elementName.equals(TITLE_ELEMENT)){
                        isTitle = true;

                    // Redirect element started
                    }else if(elementName.equals(REDIRECT_ELEMENT)){
                        
                        // Get redirects to page title (there is always only one attribute named "title")
                        redirectsTo = xmlStreamReader.getAttributeValue(0);
                        
                        // Is page redirected to another page?
                        if(redirectsTo != null){
                        	isRedirect = true;
                        }
                    
                    // Revision element started
                    }else if(elementName.equals(REVISION_ELEMENT)){
                    	isRevision = true;
                    }
                    
                    break;

                case XMLEvent.CHARACTERS:

                	// We are currently on id element
                    if(isId && !isRevision){
                    	id = Integer.parseInt(xmlStreamReader.getText());
                    
                    // We are currently on title element
                    }else if(isTitle){
                        title = xmlStreamReader.getText();
                    }
                    
                    break;

                case XMLEvent.END_ELEMENT:
                    elementName = xmlStreamReader.getName().getLocalPart();
                    
                    // Page element ended
                    if(elementName.equals(PAGE_ELEMENT)){
                    	
                    	// Redirect page: remember to which page it redirects
                    	if(isRedirect){
                    		redirectedPages.put(redirectsTo, title);
                    		isRedirect = false;
                    		
                    	// Non-redirect page: create PageModel & add to map
                    	}else{
                    		pages.put(id, new PageModel(id, title));
                    	}
                    
                    // Id element ended
                    }else if(elementName.equals(ID_ELEMENT)){
                    	isId = false;
                        
                    // Title element ended
                    }else if(elementName.equals(TITLE_ELEMENT)){
                    	isTitle = false;
                    
                    // Revision element ended
                    }else if(elementName.equals(REVISION_ELEMENT)){
                    	isRevision = false;
                    }
                    
                    break;
            }
        }

        Logger.info("%d non-redirect pages were read", pages.size());
        Logger.info("%d redirect pages were read", redirectedPages.size());
        Logger.info("%d total pages were read", pages.size() + redirectedPages.size());
        
        /*
         * Close input stream & reader
         */
        xmlInputStream.close();
        xmlStreamReader.closeCompletely();
    }

}
