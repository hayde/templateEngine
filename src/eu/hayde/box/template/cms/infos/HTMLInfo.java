/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.hayde.box.template.cms.infos;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author ArmenNalbandyan
 */
public class HTMLInfo extends FileInfo {

	private String parentFolderName;
	private String raw;
	private String absolutePathPrefix;
	private HashMap<String, String> metaTags = new HashMap<String, String>();

	public String getRaw() {
		return raw;
	}

	public void setAbsolutePath(String parPath) {
		this.absolutePathPrefix = parPath;
	}

	public void getAbsolutePath(String parPath) {
		this.absolutePathPrefix = parPath;
	}

	public String getAbsoluteFileName() {
		return this.absolutePathPrefix + "/" + this.parentFolderName + "/"
				+ this.getFileName();
	}

	public void setRaw(String parRaw) {
		raw = parRaw;
		this.processHTML();
	}

	public String getTag(String parTagName) {
		return this.loadTag(parTagName);
	}

	public String getMetaTag(String parMetaTagName) {
		if (metaTags.containsKey(parMetaTagName.toLowerCase())) {
			return metaTags.get(parMetaTagName.toLowerCase());
		} else {
			return "";
		}
	}

	public HashMap<String, String> getMetaTags() {
		return this.metaTags;
	}

	public String getParentFolderName() {
		return parentFolderName;
	}

	public void setParentFolderName(String parentFolderName) {
		if (parentFolderName != null && !("".equals(parentFolderName)) && parentFolderName.charAt(0) == '/') {
			parentFolderName = parentFolderName.substring(1);
		}
		this.parentFolderName = parentFolderName;
	}

	public String getSubFolder() {
		return getParentFolderName();
	}

	private void processHTML() {
		/*
		 * load all meta tags
		 */
		this._loadMetaTags();
	}

	/**
	 * will load all meta tags of the header into the hashMap
	 */
	private void _loadMetaTags() {

		Pattern pattern = Pattern.compile("(?i)<META NAME=\"([^\"]+)\" CONTENT=\"([^\"]*)\"[^>]*>");
		Matcher matcher = pattern.matcher(raw);
		while (matcher.find()) {
			if (matcher.group(1) != null && !matcher.group(1).equals("")) {
				metaTags.put(matcher.group(1).toLowerCase(), matcher.group(2));
			}
		}

	}

	/**
	 * load all tags, that are important for the template system
	 */
	private String loadTag(String tagName) {
		Pattern pattern = Pattern.compile("(?i)<" + tagName + "[^>]*>((?s).*)</" + tagName + "[^>]*>");
		Matcher matcher = pattern.matcher(raw);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			return "";
		}
	}

	/**
	 * does load a image info, for further processing.
	 *
	 * @param description
	 * @return
	 */
	public ImageInfo loadImageInfo(String description) {
		ImageInfo imageInfo = null;
		Pattern matchingPattern = Pattern.compile("(?i)<img[^>]+alt=\"" + description + "\"[^>]*>");
		Matcher matcher = matchingPattern.matcher(raw);
		if (matcher.find()) {
			imageInfo = new ImageInfo();
			String imgTag = matcher.group(0);
			imageInfo = new ImageInfo();
			String height = getHtmlAttributeValue(imgTag, "HEIGHT", false);
			String width = getHtmlAttributeValue(imgTag, "WIDTH", false);
			imageInfo.setSrc(getHtmlAttributeValue(imgTag, "SRC", true));
			if (height != null) {
				imageInfo.setHeight(Integer.valueOf(height));
			}
			if (width != null) {
				imageInfo.setWidth(Integer.valueOf(width));

			}
		}

		return imageInfo;
	}

	private String getHtmlAttributeValue(String tag, String name, boolean haveQuotes) {

		Pattern matchingPattern = Pattern.compile("(?i)" + name + "\\s*=\\s*\"?([^\\s\"]*)\"?.*");
		Matcher matcher = matchingPattern.matcher(tag);
		if (matcher.find()) {
			return matcher.group(1);

		}
		return null;
	}
}
