/**
 * 
 */
package com.trendrr.strest.doc.tags;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.strest.doc.AbstractDocTag;
import com.trendrr.strest.doc.StrestDocParser;


/**
 * @author Dustin Norlander
 * @created Feb 18, 2011
 * 
 */
public class ExampleTag extends AbstractDocTag {

	protected Log log = LogFactory.getLog(ExampleTag.class);

	/* (non-Javadoc)
	 * @see com.trendrr.strest.doc.AbstractDocTag#process(com.trendrr.strest.doc.StrestDocParser, java.lang.String)
	 */
	@Override
	public Object process(StrestDocParser parser, String value) {
		DynMap mp = new DynMap();
		mp.put("url", value);
		return mp;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.doc.AbstractDocTag#tagName()
	 */
	@Override
	public String tagName() {
		return "example";
	}
}
