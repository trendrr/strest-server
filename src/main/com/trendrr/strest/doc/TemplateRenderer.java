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

	public void renderIndex(DynMap index);
	
	public void renderPage(String route, DynMap page);
}
