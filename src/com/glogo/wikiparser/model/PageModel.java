package com.glogo.wikiparser.model;

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
	private List<String> alternativeTitles = new IgnoreCaseArrayList();
	
	/**
	 * Title of the page this page redirects to
	 */
	private String redirectsToPageTitle;

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

	public void addAlternativeTitle(String alternativeTitle) {
		this.alternativeTitles.add(alternativeTitle);
	}
	
	public String getRedirectsToPageTitle() {
		return redirectsToPageTitle;
	}

	public void setRedirectsToPageTitle(String redirectsToPageTitle) {
		this.redirectsToPageTitle = redirectsToPageTitle;
	}
	
}
