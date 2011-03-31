/**
 * 
 */
package com.trendrr.strest.doc.tags;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.Regex;
import com.trendrr.oss.StringHelper;
import com.trendrr.strest.doc.AbstractDocTag;
import com.trendrr.strest.doc.StrestDocParser;


/**
 * @author Dustin Norlander
 * @created Feb 18, 2011
 * 
 */
public class ParamTag extends AbstractDocTag {

	protected Log log = LogFactory.getLog(ParamTag.class);

	/* (non-Javadoc)
	 * @see com.trendrr.strest.doc.AbstractDocTag#process(com.trendrr.strest.doc.StrestDocParser, java.lang.String)
	 */
	@Override
	public Object process(StrestDocParser parser, String value) {
		DynMap mp = new DynMap();
		
		int ind = value.indexOf(' ');
		if (ind == -1) {
			//only the param in this one.
			mp.put("param", value);
			return mp;
		}
		String param = value.substring(0, ind).trim();
		mp.put("param", param);
		value = value.substring(ind).trim();
		
		//look for @default
		String defval = this.tagValue("default", value);
		if (defval != null) {
			mp.put("default", defval);
			value = this.removeTag("default", value);
		}
		
		mp.put("required", this.containsTag("required", value));
		value = this.removeTag("required", value);
		
		mp.put("description", value);
		return mp;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.doc.AbstractDocTag#tagName()
	 */
	@Override
	public String tagName() {
		return "param";
	}
}
