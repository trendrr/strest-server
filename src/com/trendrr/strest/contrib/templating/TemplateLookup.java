/**
 * 
 */
package com.trendrr.strest.contrib.templating;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.Reflection;
import com.trendrr.oss.concurrent.LazyInit;


/**
 * @author Dustin Norlander
 * @created May 3, 2011
 * 
 */
public class TemplateLookup {

	protected static Log log = LogFactory.getLog(TemplateLookup.class);
	
	private static ConcurrentHashMap<String, Class> renderers = new ConcurrentHashMap<String, Class>();
	
	/**
	 * gets the specified renderer, looks for classname in 
	 * templating.renderer.
	 * 
	 * if no classname is found, then returns the default renderer (STTemplateRenderer)
	 * @param config
	 * @return
	 */
	public static TemplateRenderer getRenderer(DynMap config) {
		Class cls = null;
		String classname = config.getString("templating.renderer");
		if (classname != null) {
			cls = renderers.get(classname);
			if (cls == null) {
				TemplateRenderer r = null;
				try {
					r = Reflection.defaultInstance(TemplateRenderer.class, classname);
				} catch (Exception e) {
					log.warn("Caught", e);
				}
				if (r != null) {
					renderers.put(classname, r.getClass());
				}
				cls = renderers.get(classname);
			}
		}
		
		TemplateRenderer rend = new STTemplateRenderer();
		if (cls != null) {
			try {
				rend = Reflection.defaultInstance(cls);
			} catch (Exception e) {
				log.warn("Caught", e);
			}
		}
		rend.init(config);
		return rend;
	}
}
