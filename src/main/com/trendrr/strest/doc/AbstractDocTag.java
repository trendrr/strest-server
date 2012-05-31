/**
 * 
 */
package com.trendrr.strest.doc;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.Regex;
import com.trendrr.oss.StringHelper;


/**
 * @author Dustin Norlander
 * @created Feb 17, 2011
 * 
 */
public abstract class AbstractDocTag {

	protected Log log = LogFactory.getLog(AbstractDocTag.class);
	
	/**
	 * java fragment is expected to start with this tag.
	 * return the rest of the java to process by other tags.
	 * 
	 * @param javaFragment
	 * @return
	 */
	public abstract Object process(StrestDocParser parser, String value);
	
	public abstract String tagName();
	
	
	/**
	 * returns true if the text contains the given tag.
	 * @param tag
	 * @param value
	 * @return
	 */
	protected boolean containsTag(String tag, String value) {
		String regex = "\\@" + tag + "\\s+";
		String d = Regex.matchFirst(value, regex, false);
		return d != null;
	}
	
	/**
	 * for tags of the form 
	 * tag(value)
	 * 
	 * returns the value portion
	 * @param tag
	 * @param value
	 * @return
	 */
	protected String tagValue(String tag, String text) {
		String regex = "\\@" + tag + "\\s*\\([^\\)]+\\)";
		String d = Regex.matchFirst(text, regex, false);
		if (d == null)
			return null;
		
		d = d.replaceAll("\\@" + tag + "\\s*\\(", "");
		d = StringHelper.trim(d, ")");
		return d.trim();
	}
	
	/**
	 * removes the tag from value and returns the new value.
	 * will also remove tags of the form tag(value)
	 * 
	 * @param tag
	 * @param value
	 * @return
	 */
	protected String removeTag(String tag, String value) {
		String regex = "\\@" + tag + "\\s+";
		value = value.replaceFirst(regex, "").trim();
		regex = "\\@" + tag + "\\s*\\([^\\)]+\\)";
		value = value.replaceFirst(regex, "").trim();
		return value;
	}
}
