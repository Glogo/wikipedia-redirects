package com.glogo.wikiparser;

import java.util.ArrayList;
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
	 * Anchor text links on current page were obtained from direct parsing of the text element
	 */ 
	private List<AnchorTextLink> anchorTextLinks = new ArrayList<AnchorTextLink>();
	
	/**
	 * Final list of the related page terms from other pages anchor texts.<br />
	 * Anchor texts on current page were obtained from page referring to this page with anchor link
	 */ 
	private List<String> anchorTexts = new IgnoreCaseArrayList();
	
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
	
	public List<AnchorTextLink> getAnchorTextLinks() {
		return anchorTextLinks;
	}

	public void addAnchorTextLink(AnchorTextLink anchorTextLink) {
		anchorTextLinks.add(anchorTextLink);
	}

	public List<String> getAlternativeTitles() {
		return alternativeTitles;
	}

	public void addAlternativeTitle(String alternativeTitle) {
		this.alternativeTitles.add(alternativeTitle);
	}

	public List<String> getAnchorTexts() {
		return anchorTexts;
	}
	
	public void addAnchorText(String anchorText) {
		this.anchorTexts.add(anchorText);
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

	/**
	 * Returns true if at least one of following conditions is met
	 * 	- page is redirect
	 *  - have zero alternative titles and anchor texts
	 */
	public boolean isExcluded() {
		return redirectsToPageTitle != null || (alternativeTitles.size() == 0 && anchorTexts.size() == 0);
	}
	
}
