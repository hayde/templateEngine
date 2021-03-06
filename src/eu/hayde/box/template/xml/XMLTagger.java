package eu.hayde.box.template.xml;

import eu.hayde.box.template.util.StringBufferExt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author can.senturk
 */
public class XMLTagger {

	private StringBufferExt xml;

	public XMLTagger(String xml) {
		this.xml = new StringBufferExt(xml);
	}

	public String toString() {
		return xml.toString();
	}

	public String getNextTag(String prefix) {
		String returnValue = null;
		Pattern pattern = Pattern.compile("<(\\w+)\\b+[^\\<\\>]((?!hyd:\\w+|[<>]).)*" + prefix + "\\:(\\w+)\\s*=\\s*([^>])*>", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(xml.get_charsequence());
		if (matcher.find()) {
			returnValue = matcher.group(3);
		}
		return returnValue;
	}

	public Element getByAttribute(String attribute) throws XMLException {
		Element returnValue = null;
		Pattern pattern = Pattern.compile("<(\\w+)\\b+[^\\<\\>]*" + attribute + "\\s*=\\s*(((\"[^\"]*\")|(\'[^\']*\'))[^>]*)>", Pattern.CASE_INSENSITIVE);
		//Pattern pattern = Pattern.compile("<(\\w+)\\b+[^\\<\\>]*" + attribute + "\\s*=\\s*([^>])*>", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(xml.get_charsequence());
		if (matcher.find()) {
			Tag tmpOpenTag = new Tag(matcher.group(0), matcher.group(1), matcher.start(), matcher.end());
			returnValue = _getByOpenTag(tmpOpenTag);
		}

		return returnValue;
	}

	private Tag _findOpenTag(String tagName, int startPosition) {
		Tag returnValue = null;
		Pattern pattern = Pattern.compile("<(" + tagName + ")[^>]*>", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(xml.get_charsequence());
		if (matcher.find(startPosition)) {
			returnValue = new Tag(matcher.group(0), matcher.group(1), matcher.start(), matcher.end());
		}
		return returnValue;
	}

	private Tag _findCloseTag(Tag openTag) throws XMLException {
		Tag returnValue = null;
		Pattern pattern = Pattern.compile("</(" + openTag.name + ")\\b*>", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(xml.get_charsequence());
		int startPosition = openTag.end;

		/*
		 * repeat this loop, until there is no tag inside this level
		 */
		while (startPosition > 0) {

			/*
			 * get the next close tag
			 */
			if (!matcher.find(startPosition - 1)) {
				/*
				 * no close tag found for this position
				 */
				startPosition = -1;
				throw new XMLException("Close tag not found for: '" + openTag.tag + "'");
			} else {

				/*
				 * now check, if there is another open tag up to this close-tag
				 * position
				 */
				int nextOpenTag = _searchOpenTag(openTag.name, startPosition);
				if (nextOpenTag > -1 && nextOpenTag < matcher.start()) {
					/*
					 * yes, there is something inside, so get that tag
					 */
					Tag tmpOpen = _findOpenTag(openTag.name, startPosition);
					if (!tmpOpen.tag.endsWith("/>")) {
						Tag tmpClose = _findCloseTag(tmpOpen);
						startPosition = tmpClose.end;
					} else {
						startPosition = tmpOpen.end;
					}

				} else {
					/*
					 * no open tag in between, so remember now this close position
					 */
					returnValue = new Tag(matcher.group(0), matcher.group(1), matcher.start(), matcher.end());
					startPosition = -1;
				}
			}
		}
		return returnValue;
	}

	private Element _getByOpenTag(Tag openTag) throws XMLException {
		Element returnValue = new Element();

		returnValue.setStartTag(openTag);

		// now, if we have to look for a closing tag
		if (!returnValue.isSingleTag()) {
			Tag closeTag = _findCloseTag(returnValue.getStartTag());
			if (closeTag == null) {
				throw new XMLException("Close tag not found for: " + returnValue.getStartTag());
			}
			returnValue.setEndTag(closeTag);
		} else {
		}

		return returnValue;
	}

	private int _searchOpenTag(String name, int searchStart) {
		return xml.indexOf("<" + name, searchStart);
	}

	public void replace(Element element, String content) {
		if (element.isSingleTag()) {
			xml.replace(element.getStartTag().start, element.getStartTag().end, content);
//			xml = xml.substring(0, element.getStartTag().start)
//					+ content
//					+ xml.substring(element.getStartTag().end, xml.length());
		} else {
//			xml = xml.substring(0, element.getStartTag().start)
//					+ content
//					+ xml.substring(element.getEndTag().end, xml.length());
			xml.replace(element.getStartTag().start, element.getEndTag().end, content);
		}
	}

	public void setContent(Element element, String content) throws XMLException {
		if (element.isSingleTag()) {
			throw new XMLException("command hyd:content can only put content to a <x></x>; tag. but this is a empty element tag <x />;. only valid with hyd:replace: " + element.getStartTag());
		} else {
			xml.replace(element.getStartTag().end, element.getEndTag().start, content);
//			xml = xml.substring(0, element.getStartTag().end)
//					+ content
//					+ xml.substring(element.getEndTag().start, xml.length());
		}
	}

	public void removeAttribute(Element element, Attribute attribute) {
		int beforeSize = element.getStartTag().tag.length();
		String newStartTag = element.getStartTag().removeAttribute(attribute);
		int afterSize = element.getStartTag().tag.length();
		xml.replace(element.getStartTag().start, element.getStartTag().end, newStartTag);
//		xml = xml.substring(0, element.getStartTag().start)
//				+ newStartTag
//				+ xml.substring(element.getStartTag().end, xml.length());
		element.getStartTag().end -= beforeSize - afterSize;
		if (element.getEndTag() != null) {
			element.getEndTag().start -= beforeSize - afterSize;
			element.getEndTag().end -= beforeSize - afterSize;
		}
	}

	public String getContent(Element element) {
		return xml.substring(element.getStartTag().end, element.getEndTag().start);
	}

	public String getTag(Element element) {
		return xml.substring(element.getStartTag().start, element.getEndTag().end);
	}

	public void addAttribute(Element element, Attribute attribute) {
		int beforeSize = element.getStartTag().tag.length();
		String newStartTag = element.getStartTag().addAttribute(attribute);
		int afterSize = element.getStartTag().tag.length();
		xml.replace(element.getStartTag().start, element.getStartTag().end, newStartTag );
//		xml = xml.substring(0, element.getStartTag().start)
//				+ newStartTag
//				+ xml.substring(element.getStartTag().end, xml.length());
		element.getStartTag().end -= beforeSize - afterSize;
		if (element.getEndTag() != null) {
			element.getEndTag().start -= beforeSize - afterSize;
			element.getEndTag().end -= beforeSize - afterSize;
		}
	}

	public void removeTag(Element element) {
		if (element.getEndTag() != null) {
			// reverse order to keep the order
			xml.replace(element.getEndTag().start,element.getEndTag().end, "" );
			xml.replace(element.getStartTag().start, element.getStartTag().end, "");
//			xml = xml.substring(0, element.getStartTag().start)
//					+ xml.substring(element.getStartTag().end, element.getEndTag().start)
//					+ xml.substring(element.getEndTag().end, xml.length());
		} else {
			xml.replace(element.getStartTag().start, element.getStartTag().end, "");
//			xml = xml.substring(0, element.getStartTag().start)
//					+ xml.substring(element.getStartTag().end, xml.length());
		}
	}
}
