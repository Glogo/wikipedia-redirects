package com.glogo.wikiserver;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.gson.GsonBuilder;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("getRedirects")
public class MyResource {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Path("/{query}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getIt(@PathParam("query") String query) {
    	Map<String, List<String>> found = LuceneService.getInstance().searchPages(query);
    	System.out.println(found.toString());
        return new GsonBuilder().setPrettyPrinting().create().toJson(found);
    }
}
