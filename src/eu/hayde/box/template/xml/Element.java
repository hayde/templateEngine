package eu.hayde.box.template.xml;

/**
 *
 * @author can.senturk
 */
public class Element {

	private Tag startTag;
	private Tag endTag;

	public Element() {
	}

	public Tag getStartTag() {
		return startTag;
	}

	public void setStartTag(Tag startTag) {
		this.startTag = startTag;
	}

	public Tag getEndTag() {
		return endTag;
	}

	public void setEndTag(Tag endTag) {
		this.endTag = endTag;
	}

	public boolean isSingleTag() {
		boolean returnValue = true;

		if (startTag != null) {
			if (!startTag.tag.endsWith("/>")) {
				returnValue = false;
			}
		}
		return returnValue;
	}

	public Attribute getAttribute(String name) throws XMLException {
		if (startTag != null) {
			return startTag.getAttribute(name);
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		if (startTag != null && endTag != null) {
			return "XMLTag with start" + startTag.toString() + " and end" + endTag.toString();
		} else if (startTag != null) {
			return "XMLTag with start" + startTag.toString() + " and endTag==null";
		} else {
			return "XMLTag with startTag==null and end" + endTag.toString();
		}
	}
}
