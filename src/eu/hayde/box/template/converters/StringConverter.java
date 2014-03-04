/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.hayde.box.template.converters;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 *
 * @author can.senturk
 */
public class StringConverter {

	private static Locale locale = null;

	public StringConverter(String localeLanguage) {
		if (localeLanguage == null) {
			localeLanguage = "en";
		}
		locale = new Locale(localeLanguage);
	}

	public static String toUpperCase(String string) {
		return string.toUpperCase();
	}

	public static String toLowerCase(String string) {
		return string.toLowerCase();
	}

	public static String toURL(String string) {
		try {
			return URLEncoder.encode(string, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			return "haydeTemplate engine: Couldn't encode '" + string + "'!";
		}
	}

	public static String toHTML(String string) {
		StringBuilder sb = new StringBuilder(string.length());
		// true if last char was blank
		boolean lastWasBlankChar = false;
		int len = string.length();
		char c;

		for (int i = 0; i < len; i++) {
			c = string.charAt(i);
			if (c == ' ') {
				// blank gets extra work,
				// this solves the problem you get if you replace all
				// blanks with &nbsp;, if you do that you loss
				// word breaking
				if (lastWasBlankChar) {
					lastWasBlankChar = false;
					sb.append("&nbsp;");
				} else {
					lastWasBlankChar = true;
					sb.append(' ');
				}
			} else {
				lastWasBlankChar = false;
				//
				// HTML Special Chars

				if (c == '"') {
					sb.append("&quot;");
				} else if (c == 'ä') {
					sb.append("&auml;");
				} else if (c == 'Ä') {
					sb.append("&Auml;");
				} else if (c == 'ü') {
					sb.append("&uuml;");
				} else if (c == 'Ü') {
					sb.append("&Uuml;");
				} else if (c == 'ö') {
					sb.append("&ouml;");
				} else if (c == 'Ö') {
					sb.append("&Ouml;");
				} else if (c == 'ß') {
					sb.append("&szlig;");
				} else if (c == '§') {
					sb.append("&sect;");
				} else if (c == '€') {
					sb.append("&euro;");
				} else if (c == '&') {
					sb.append("&amp;");
				} else if (c == '<') {
					sb.append("&lt;");
				} else if (c == '>') {
					sb.append("&gt;");
				} else if (c == '\n') // Handle Newline
				{
					sb.append("&lt;br/&gt;");
				} else {
					int ci = 0xffff & c;
					if (ci < 160) // nothing special only 7 Bit
					{
						sb.append(c);
					} else {
						// Not 7 Bit use the unicode system
						sb.append("&#");
						sb.append(new Integer(ci).toString());
						sb.append(';');
					}
				}
			}
		}
		return sb.toString();
	}

	/**
	 * converts a date to the following format: "yyyy-MM-dd HH:mm:ss"
	 *
	 * @param date to be formated
	 * @return the String in format "yyyy-MM-dd HH:mm:ss"
	 */
	public static String DateToString(Date date) {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale).format(date);
	}

	/**
	 * converts a string of the format "yyyy-MM-dd HH:mm:ss" to a date
	 *
	 * @param the string in the format "yyyy-MM-dd HH:mm:ss"
	 * @return a date
	 */
	public static Date StringToDate(String dateString) {

		Date returnValue = null;
		try {
			if (dateString.matches("[0-9]{2,4}-[0-9]{1,2}-[0-9]{1,2}\\s+[0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}")) {
				returnValue = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS", locale).parse(dateString);
			} else if (dateString.matches("[0-9]{2,4}-[0-9]{1,2}-[0-9]{1,2}\\s+[0-9]{1,2}:[0-9]{1,2}")) {
				returnValue = new SimpleDateFormat("yyyy-MM-dd HH:mm", locale).parse(dateString);
			} else if (dateString.matches("[0-9]{2,4}-[0-9]{1,2}-[0-9]{1,2}\\s*")) {
				returnValue = new SimpleDateFormat("yyyy-MM-dd", locale).parse(dateString);
			} else if (dateString.matches("[0-9]{8}\\s*")) {
				returnValue = new SimpleDateFormat("yyyyMMdd", locale).parse(dateString);
			} else if (dateString.matches("[0-9]{1,2}\\.[0-9]{1,2}\\.[0-9]{2,4}\\s*")) {
				returnValue = new SimpleDateFormat("dd.MM.yyyy", locale).parse(dateString);
			}
		} catch (ParseException ex) {
		}
		return returnValue;
	}

	public static String DateToString(XMLGregorianCalendar date) {
		return DateToString(date.toGregorianCalendar().getTime());
	}

	public static String DateToString(Date date, String format) {
		return new SimpleDateFormat(format, locale).format(date);
	}

	public static String DateToString(XMLGregorianCalendar date, String format) {
		if (date == null) {
			return null;
		} else {
			return DateToString(date.toGregorianCalendar().getTime(), format);
		}
	}

	public static String DateToString(String date, String format) {
		return DateToString(StringToDate(date), format);
	}

	public static String NumberToFormat(Double val, String format) {
		if (val == null) {
			return null;
		} else {
			DecimalFormat formated = new DecimalFormat(format);
			return formated.format(val);
		}
	}

	public static String NumberToFormat(Integer val, String format) {
		return StringConverter.NumberToFormat((double) (val.intValue()), format);
	}

	public static String NumberToFormat(Long val, String format) {
		return StringConverter.NumberToFormat((double) (val.longValue()), format);
	}

	public static String Right(String val, Integer endIndex) {
		if (val.length() > endIndex) {
			return val;
		} else {
			return val.substring(val.length() - endIndex, val.length() - 1);
		}
	}

	public static String Left(String val, Integer endIndex) {
		return val.substring(0, endIndex);
	}
    public static String LeftSmooth(String val, Integer endIndex) {
        String returnValue = "";
        if (endIndex > val.length()) {
            returnValue = val;
        } else {
            returnValue  = val.substring(0, endIndex);
            for (int i = endIndex; i < val.length(); i++) {
                char currentChar = val.charAt(i);
                if (currentChar == '.'
                        || currentChar == ' '
                        || currentChar == ','
                        || currentChar == ';'
                        || currentChar == '('
                        || currentChar == ')') {
                    // quit it here
                    i = val.length();
                }
                returnValue += currentChar;
            }
        }

        return returnValue;
    }
}
