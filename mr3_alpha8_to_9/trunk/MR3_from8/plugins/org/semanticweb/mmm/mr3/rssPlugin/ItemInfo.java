package org.semanticweb.mmm.mr3.rssPlugin;
import java.net.*;

/*
 * Created on 2003/08/11
 *
 * @author takeshi morita
 */
public class ItemInfo {
	
	private URI uri;
	private String title;
	private String link;
	private String description;
	private String language;

	public ItemInfo(URI uri) {
		this.uri = uri;
		this.link = uri.toString();
		this.description = "";
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getLink() {
		return link;
	}

	public URI getURI() {
		return uri;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getLanguage() {
		return language;
	}
}
