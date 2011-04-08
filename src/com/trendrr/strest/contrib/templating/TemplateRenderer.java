/**
 * 
 */
package com.trendrr.strest.contrib.templating;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stringtemplate.v4.ST;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.FileCache;
import com.trendrr.strest.server.StrestServer;


/**
 * A simple template renderer based on StringTemplate
 * 
 * All StringTemplate functionality is present, just that we use 
 * ${varname} instead of <varname>
 * 
 * see http://www.stringtemplate.org/
 * 
 * @author Dustin Norlander
 * @created Apr 7, 2011
 * 
 */
public class TemplateRenderer {

	protected Log log = LogFactory.getLog(TemplateRenderer.class);
	
	protected FileCache cache = new FileCache();
	
	protected long timeout = 10*1000; //default to 10 seconds.
	protected String templateDirectory = "templates";
	
	protected static TemplateRenderer instance = new TemplateRenderer();
	
	public TemplateRenderer() {
		String base = StrestServer.config.getString("templates.base_dir");
		if (base != null) {
			this.templateDirectory = base;
		}
		
		Long timeout = StrestServer.config.getLong("templates.cache_timeout");
		if (timeout != null)
			this.timeout = timeout;
	}
	
	/**
	 * initializes the singleton renderer instance.  This should be called <b>before</b> 
	 * any calls to instance() as this method is not threadsafe. 
	 * 
	 * @param templateDirectory
	 * @param cacheTimeout
	 */
	public static void init(String templateDirectory, long cacheTimeout) {
		TemplateRenderer rend = new TemplateRenderer();
		rend.templateDirectory = templateDirectory;
		rend.timeout = cacheTimeout;
		instance = rend;
	}
	
	public static TemplateRenderer instance() {
		return instance;
	}
	
	/**
	 * renders a template based on the singleton TemplateRenderer instance.
	 * 
	 * shortcut to TemplateRenderer.instance().render(filename, params);
	 * 
	 * @param filename
	 * @param params
	 */
	public static String render(String filename, DynMap params) {
		return instance().renderTemplate(filename, params);
	}
	
	/**
	 * renders the specified template.
	 * @param filename filename of the template to render
	 * @param params
	 * @return
	 */
	public String renderTemplate(String filename, DynMap params) {
		byte bytes[] = cache.getFileBytes(this.templateDirectory + filename, this.timeout);
		if (bytes == null) {
			return null;
		}
		String templateString = "";
		try {
			templateString = new String(bytes, "utf8");
		} catch (UnsupportedEncodingException e) {
			log.warn("Caught", e);
			return null;
		}
		templateString = templateString.replace("${", "{");
		ST template = new ST(templateString, '{', '}');
		for (String key : params.keySet()) {
			template.add(key, params.get(key));
		}
		return template.render();
	}
}
