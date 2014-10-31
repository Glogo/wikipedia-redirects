package com.glogo.wikiparser.model;

import java.util.List;

/**
 * PageModel entity representing one page in Wikipedia. Data is read from Wikipedia articles XML dump.
 * @see <a href="http://dumps.wikimedia.org/enwiki/">http://dumps.wikimedia.org/enwiki/</a>
 * @author Glogo
 */
public class PageModel {
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
	
	/**
	 * Page to which this page redirects to.
	 * This page could redirect to another page while this value would still be null.
	 * This happens when redirected page was not in parsed data.
	 * See: {@link #redirectsToPageTitle}
	 */
	private PageModel redirectsToPage;

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

	public PageModel getRedirectsToPage() {
		return redirectsToPage;
	}

	public void setRedirectsToPage(PageModel redirectsToPage) {
		this.redirectsToPage = redirectsToPage;
	}
	
}
