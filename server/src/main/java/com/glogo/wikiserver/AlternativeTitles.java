package com.glogo.wikiserver;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;

/**
 * Alternative titles REST resource providing methods for searching for pages with alternative titles.
 * For detailed information please see <a href="https://github.com/Glogo/wikipedia-redirects">project repository</a>
 */
@Path("/alt")
public class AlternativeTitles {
	
	private static Logger logger = LoggerFactory.getLogger(AlternativeTitles.class);
	
	/**
     * @return json string with data info
     */
    @GET
    @Path("/info")
    @Produces(MediaType.APPLICATION_JSON)
    public String info() {
    	logger.info("Info resource called");
    	
    	// Call lucene service with query string to find pages
    	Map<String, Object> info = LuceneService.getInstance().getDataInfoMap();
    	
    	// Convert map to json and return it
        return new GsonBuilder().setPrettyPrinting().create().toJson(info);
    }

    /**
     * @return json string of pages titles fitered with specified query
     * and alternative titles for current page.
     */
    @GET
    @Path("/search/{query}")
    @Produces(MediaType.APPLICATION_JSON)
    public String search(@PathParam("query") String query) {
    	
    	logger.info("Search resource called with query: '{}'", query);
    	
    	// Return empty object if query was not specified
    	if(query == null || query.length() == 0){
    		logger.error("Query was not specified.");
    		return "{}";
    	}
    	
    	// Call lucene service with query string to find pages
    	Map<String, String[]> found = LuceneService.getInstance().searchPages(query);
    	
    	// Convert map to json and return it
        return new GsonBuilder().setPrettyPrinting().create().toJson(found);
    }
}
