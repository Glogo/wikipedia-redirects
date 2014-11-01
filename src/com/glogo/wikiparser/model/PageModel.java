package com.glogo.wikiparser.model;

import java.util.ArrayList;
import java.util.List;

/**
 * PageModel entity representing one page in Wikipedia. Data is read from Wikipedia articles XML dump.
 * @see <a href="http://dumps.wikimedia.org/enwiki/">http://dumps.wikimedia.org/enwiki/</a>
 * @author Glogo
 */
public class PageModel {
	
	/**
	 * ID of the page
	 */
	private Integer id;
	
	/**
	 * Title of the page
	 */
	private String title;
	
	/**
	 * Final list of the alternative page titles from redirects
	 */ 
	private List<String> alternativeTitles = new ArrayList<String>();

	public PageModel(Integer id, String title) {
		this.id = id;
		this.title = title;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<String> getAlternativeTitles() {
		return alternativeTitles;
	}
}
