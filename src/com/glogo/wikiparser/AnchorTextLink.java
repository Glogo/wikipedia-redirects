package com.glogo.wikiparser;

/**
 * Anchor text link holds information about anchor text displayed on current page and anchor link reffering to another page
 * @author Glogo
 */
public class AnchorTextLink {
	/**
	 * String displayed on page text anchor link
	 */
	private String anchorText;
	
	/**
	 * Page title to which anchor text reffers
	 */
	private String anchorLink;
	
	public AnchorTextLink(String anchorText, String anchorLink) {
		this.anchorText = anchorText;
		this.anchorLink = anchorLink;
	}

	public String getAnchorText() {
		return anchorText;
	}

	public void setAnchorText(String anchorText) {
		this.anchorText = anchorText;
	}

	public String getAnchorLink() {
		return anchorLink;
	}

	public void setAnchorLink(String anchorLink) {
		this.anchorLink = anchorLink;
	}
	
	
}
