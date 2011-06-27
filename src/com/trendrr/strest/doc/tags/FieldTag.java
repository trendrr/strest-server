/**
 * 
 */
package com.trendrr.strest.doc.tags;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Dustin Norlander
 * @created Jun 27, 2011
 * 
 */
public class FieldTag extends ParamTag {

	protected Log log = LogFactory.getLog(FieldTag.class);
	
	/* (non-Javadoc)
	 * @see com.trendrr.strest.doc.AbstractDocTag#tagName()
	 */
	@Override
	public String tagName() {
		return "field";
	}
}
