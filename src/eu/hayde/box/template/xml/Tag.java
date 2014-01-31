package eu.hayde.box.template.xml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author can.senturk
 */
public class Tag {

	public String tag;
	public int start;
	public int end;
	public String name;

	/**
	 * contructor for the tag
	 *
	 * @param tag -> the complete tag itself with all attributes
	 * @param name -> the name of the tag "tagName"
	 * @param start -> absolute start position inside the original text
	 * @param end -> absolute end position inside the original text
	 */
	public Tag(String tag, String name, int start, int end) {
		this.tag = tag;
		this.name = name;
		this.start = start;
		this.end = end;
	}

	public Attribute getAttribute(String name) throws XMLException {
		Attribute returnValue = null;
		/*
		 * regular express explenation:
		 * group 1: ( + name + ) -> the tag name
		 *			\b*=\b* and none word char
		 * group 2: (['\"]) the delimiter single quote or double quote
		 * group 3: Everything up to the second appearance of the 'group 2'
		 *          quote.
		 */
		Pattern pattern = Pattern.compile("(" + name + ")\\s*=\\s*(['\\\"])(.*?)\\2", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(tag);

		if (matcher.find()) {
			returnValue = new Attribute();
			returnValue.name = matcher.group(1);
			if (matcher.groupCount() < 2) {
				throw new XMLException("Couldn't get the value for attribute '" + name + "' from Tag " + tag + " at starting at position " + start);
			}
			returnValue.value = matcher.group(3);
		}

		return returnValue;
	}

	@Override
	public String toString() {
		return "Tag '" + this.tag + "' at " + this.start + " to " + this.end;
	}

	public String removeAttribute(Attribute attribute) {
		tag = tag.replaceAll("\\s*" + attribute.name + "\\s*=\\s*(['\\\"])(.*?)\\1", "");
		return tag;
	}

	String addAttribute(Attribute attribute) {
		String attributeText = null;
		if (attribute.name.endsWith("[noval]")) {
			attributeText = " " + attribute.name.replaceAll("\\[noval\\]", "");
		} else {
			attributeText = " " + attribute.name + "='" + attribute.value + "'";
		}

		if (tag.endsWith("/>")) {
			tag = tag.substring(0, tag.length() - 2)
					+ attributeText
					+ "/>";
		} else {
			tag = tag.substring(0, tag.length() - 1)
					+ attributeText
					+ ">";
		}
		return tag;
	}
}
