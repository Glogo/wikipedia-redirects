package com.glogo.wikiparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;

/**
 * Class used to read alternative pages titles from redirects.<br />
 * Can be used for large wikipedia dumps XML files.
 * For detailed information please see <a href="https://github.com/Glogo/wikipedia-redirects">project repository</a>
 * @see <a href="http://www.studytrails.com/java/xml/woodstox/java-xml-stax-woodstox-basic-parsing.jsp">Helpful Stax Woodstox parsing example</a><br />
 * @author Glogo
 */
public class WikipediaRedirectsReader {
	
	private static Logger logger = LoggerFactory.getLogger(WikipediaRedirectsReader.class);

    /**
     * Indicates if title element started but not yet ended.
     */
    private boolean isTitle = false;
    
    /**
     * Indicates if current page is redirect (if page has redirect attribute)
     */
    private boolean isRedirect = false;
    
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
     * XML elements strings
     */
    private static final String PAGE_ELEMENT = "page"; // used only to display progress
    private static final String TITLE_ELEMENT = "title";
    private static final String REDIRECT_ELEMENT = "redirect";
    
    /**
     * Total pages count
     */
    private int totalPagesCount;
    
    /**
     * @return approximated total pages count. This value is calculated from source file size in bytes divided by average bytes per one page.
     * Average bytes per one page was previously calculated from complete Wikipedia(en) dump "enwiki-latest-pages-articles.xml"
     *  
     * @throws XMLStreamException 
     * @throws FileNotFoundException 
     */
    int getFastPagesCount(String filename) throws XMLStreamException, FileNotFoundException {
        File file = new File(filename);
        logger.info("File size is {} bytes", file.length());

        return (int) (file.length() / AVERAGE_BYTES_PER_PAGE);
    }
    
    /**
     * Opens a file and parses wikipedia redirects data into redirectedPages map.
     * @param filename 
     * @param pages
     * @param redirectedPages 
     * @throws XMLStreamException 
     * @throws IOException 
     */
    public void readFile(String filename, Multimap<String, String> redirectedPages) throws XMLStreamException, IOException{
        /*
         * Try to open file
         */
        //filename = new File("res/test_input.xml").getAbsolutePath(); // test
    	logger.info("Reading file: '{}'", filename);
        InputStream xmlInputStream = new FileInputStream(filename);
        XMLInputFactory2 xmlInputFactory = (XMLInputFactory2)XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true); // Force long texts to come concatenated together in CHARACTERS
        XMLStreamReader2 xmlStreamReader = (XMLStreamReader2) xmlInputFactory.createXMLStreamReader(xmlInputStream);
        
        // Progress helpers
        int approxPagesCount;
        int showProgressEach; // How many pages needs to be read to display reading progress status
        int progress;
        
        // Current element name
        String elementName;
        
        // Page info
        String title = null;
        String redirectsTo = null;
        
        // Clear redirectedPages & total pages count
        redirectedPages.clear();
        totalPagesCount = 0;
        
        // Calculate approximate pages count to enable showing reading progress
        logger.info("Approximating total pages elements count");
        approxPagesCount = getFastPagesCount(filename);
        showProgressEach = approxPagesCount / (100 / SHOW_PROGRESS_EACH_PERCENT);
        logger.info("Approximated total pages elements count: {} (used to calculate progress %)", approxPagesCount);
        logger.info("Displaying approximated progress each {} pages ({} % of all calculated pages)", showProgressEach, SHOW_PROGRESS_EACH_PERCENT);
        
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
                        if(showProgressEach != 0 && (totalPagesCount % showProgressEach == 0)) {
                            progress = totalPagesCount * 100 / approxPagesCount;
                            logger.info("Approx reading progress: {}%. Pages processed: {}", progress >= 99 ? 99 : progress, totalPagesCount);
                        }
                        
                        totalPagesCount++;
                        
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
                    }
                    
                    break;

                case XMLEvent.CHARACTERS:

                    // We are currently on title element
                    if(isTitle){
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
                    	}
                        
                    // Title element ended
                    }else if(elementName.equals(TITLE_ELEMENT)){
                    	isTitle = false;
                    }
                    
                    break;
            }
        }

        logger.info("{} total pages were read", totalPagesCount);
        logger.info("{} non-redirect pages were read", totalPagesCount - redirectedPages.size());
        logger.info("{} redirect pages were read", redirectedPages.size());
        logger.info("{} total pages with at least one alternative title", redirectedPages.keySet().size());
        
        /*
         * Close input stream & reader
         */
        xmlInputStream.close();
        xmlStreamReader.closeCompletely();
    }
    
    public int getTotalPagesCount(){
    	return totalPagesCount;
    }

}
