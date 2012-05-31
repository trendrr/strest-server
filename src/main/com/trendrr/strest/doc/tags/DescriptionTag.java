/**
 * 
 */
package com.trendrr.strest.doc.tags;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.strest.doc.AbstractDocTag;
import com.trendrr.strest.doc.StrestDocParser;


/**
 * @author Dustin Norlander
 * @created Feb 18, 2011
 * 
 */
public class DescriptionTag extends AbstractDocTag {

	protected Log log = LogFactory.getLog(DescriptionTag.class);

	/* (non-Javadoc)
	 * @see com.trendrr.strest.doc.AbstractDocTag#process(java.lang.String)
	 */
	@Override
	public Object process(StrestDocParser parser, String value) {
		return value;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.doc.AbstractDocTag#tagName()
	 */
	@Override
	public String tagName() {
		return "description";
	}
}
