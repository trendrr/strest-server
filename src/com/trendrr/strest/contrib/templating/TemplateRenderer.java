/**
 * 
 */
package com.trendrr.strest.contrib.templating;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;


/**
 * Interface for TemplateRendering
 * 
 * 
 * @author Dustin Norlander
 * @created May 3, 2011
 * 
 */
public interface TemplateRenderer {

	public void init(DynMap config);
	public String renderTemplateString(String template, DynMap params);
	public String renderTemplateFile(String filename, DynMap params);
}
