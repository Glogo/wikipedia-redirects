package com.glogo.wikiparser;

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
	 * Page text content
	 */
	private String text;
	
	/**
	 * Final list of the alternative page titles
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
	
	/**
	 * Main constructor 
	 * @param title
	 */
	public PageModel(String title, String text){
		this.title = title;
		this.text = text;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
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
