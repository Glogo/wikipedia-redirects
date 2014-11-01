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
     * Average bytes per one page, previously calculated from wikipedia dump "enwiki-latest-pages-articles10.xml"
     * This file size is 1073236112 bytes and total pages element count is 214750. 1073236112 / 214750 = 4997.60704074505239 \approx \!\, 5000
     */
    private static final int AVERAGE_BYTES_PER_PAGE = 5000;
    
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
     * @throws XMLStreamException 
     * @throws IOException 
     */
    public void readFile(String filename, Map<Integer, PageModel> pages) throws XMLStreamException, IOException{
        /*
         * Try to open file
         */
        //test
        //filename = new File("res/test_input.xml").getAbsolutePath();
        Logger.info("Reading file: '%s'", filename);
        InputStream xmlInputStream = new FileInputStream(filename);
        XMLInputFactory2 xmlInputFactory = (XMLInputFactory2)XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true); // Return whole element text altogether (we don't need to use StringBuilder/Buffer)
        XMLStreamReader2 xmlStreamReader = (XMLStreamReader2) xmlInputFactory.createXMLStreamReader(xmlInputStream);
        
        int pagesCount;
        int currentPageIndex = 0;
        int showProgressEach; // How many pages needs to be read to display reading progress status
        PageModel pageModel = null;
        int eventType;
        String elementName;
        String redirectsTo;
        float progress;
        
        // Clear pages map
        pages.clear();
        
        // Do a fast run over xml file to calculate pages count to enable showing reading progress
        Logger.info("Approximating total pages elements count");
        pagesCount = getFastPagesCount(filename);
        showProgressEach = pagesCount / (100 / SHOW_PROGRESS_EACH_PERCENT);
        Logger.info("Aproximated total pages elements count: %d (used to calculate progress %%)", pagesCount);
        
        /*
         * Read file contents and store in map. All nodes traversal conditions may not be all necessary, but it is also used as schema validation.
         */
        while(xmlStreamReader.hasNext()){
            
            eventType = xmlStreamReader.next();
            
            switch (eventType) {
                case XMLEvent.START_ELEMENT:
                    elementName = xmlStreamReader.getName().getLocalPart();
                    
                    // Page element started
                    if(elementName.equals(PAGE_ELEMENT)){
                        
                    	// Print progress each showProgressEach pages
                        if(currentPageIndex++ % showProgressEach == 0){
                            progress = (float)currentPageIndex * 100 / pagesCount;
                            Logger.info("Approx reading progress: %.0f%%", progress >= 99 ? 99f : progress);
                        }
                        
                        // Create new page model
                        pageModel = new PageModel();
                        
                    // Id element started
                    }else if(elementName.equals(ID_ELEMENT)){
                        isId = true;
                        
                    // Title element started
                    }else if(elementName.equals(TITLE_ELEMENT)){
                        isTitle = true;
                        

                    // Redirect element started
                    }else if(elementName.equals(REDIRECT_ELEMENT)){
                        
                        // Get redirects to page title (there is always only one attribute named "titles")
                        redirectsTo = xmlStreamReader.getAttributeValue(0);
                        
                        // Is page redirected to another page?
                        if(redirectsTo != null && redirectsTo.length() != 0){
                            pageModel.setRedirectsToPageTitle(redirectsTo);
                            // Logger.info(pageModel.getTitle() + " -> " + pageModel.getRedirectsToPageTitle());
                        }
                    }
                    
                    break;

                case XMLEvent.CHARACTERS:
                    
                	// We are currently on id element
                    if(isId){
                        pageModel.setId(Integer.parseInt(xmlStreamReader.getText()));
                    
                    // We are currently on title element
                    }else if(isTitle){
                        pageModel.setTitle(xmlStreamReader.getText());
                    }
                    
                    break;

                case XMLEvent.END_ELEMENT:
                    elementName = xmlStreamReader.getName().getLocalPart();
                    
                    // Page element ended
                    if(elementName.equals(PAGE_ELEMENT)){
                        pages.put(pageModel.getId(), pageModel);
                        pageModel = null;
                    
                    // Id element ended
                    }else if(elementName.equals(ID_ELEMENT)){
                    	isId = false;
                        
                    // Title element ended
                    }else if(elementName.equals(TITLE_ELEMENT)){
                    	isTitle = false;
                    }
                    
                    break;
            }
        }
        
        Logger.info("%d pages were successfully read into map", pages.size());
        
        /*
         * Close input stream & reader
         */
        xmlInputStream.close();
        xmlStreamReader.closeCompletely();
    }

}
