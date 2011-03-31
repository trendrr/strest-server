/**
 * 
 */
package com.trendrr.strest.doc;

import com.trendrr.oss.DynMap;


/**
 * @author Dustin Norlander
 * @created Mar 31, 2011
 * 
 */
public interface TemplateRenderer {

	public byte[] renderIndex(DynMap index);
	
	public byte[] renderPage(DynMap page);
	
	/**
	 * returns what filename extension should be used.
	 * EX ".json" or ".html"
	 * @return
	 */
	public String getFileExtension();
}
