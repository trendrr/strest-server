/**
 * 
 */
package com.trendrr.strest.contrib.templating;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupString;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.FileCache;
import com.trendrr.oss.FileHelper;
import com.trendrr.oss.Regex;
import com.trendrr.oss.concurrent.LazyInit;
import com.trendrr.oss.concurrent.ReinitObject;
import com.trendrr.strest.server.StrestServer;


/**
 * 
 * The default template renderer, uses StringTemplate.org lib to handle the rendering 
 * 
 * A few modifications on top of StringTemplate:
 * 
 * uses ${xxxxx} as field delimiter.
 * 
 * to define regions:
 * <%def name="methodName()">
 * 
 * </%def>
 *  
 * to extend another template use:
 * 
 * <%template extends="/template.html" />
 *  
 * @author Dustin Norlander
 * @created May 2, 2011
 * 
 */
public class STTemplateRenderer implements TemplateRenderer{

	protected Log log = LogFactory.getLog(STTemplateRenderer.class);

	
	public static void main (String ...args) {
		
		STTemplateRenderer rend = new STTemplateRenderer();
		
		
		
		rend.init("/java/trendrr-api/templates", 10);
		
		DynMap params = new DynMap();
		params.put("test", "NOTHING");
		System.out.println(rend.renderTemplateFile("/docs/index.html", null));
	
		
	}
	
	
	
	
	public STGroup loadGroup(String filename) {
		final String f = filename;
		final STTemplateRenderer self = this;
		cache.putIfAbsent(filename, new ReinitObject<STGroup>(timeout){
			@Override
			public STGroup init() {
				try {
					String templateString = FileHelper.loadString(templateDirectory + f);
					StringBuilder page = new StringBuilder();
					
					StringBuilder template = new StringBuilder();
					
	//				<%template extends="/layouts/htmlPage.html" />
					
					STGroup parent = null;
					String extnds = Regex.matchFirst(templateString, "\\<\\%\\s*template\\s+extends[^\\>]+\\/\\>", true);
					if (extnds != null) {
						String file = Regex.matchFirst(extnds, "extends\\=\\\"[^\\\"]+", true).replaceFirst("extends\\=\\\"", "");
						parent = self.loadGroup(file);
//						System.out.println(parent);
						templateString = templateString.replaceFirst("\\<\\%\\s*template\\s+extends[^\\>]+\\/\\>", "");
					}
					
					templateString = templateString.replaceAll("\\<\\%def\\s+name\\=\\\"([^\\\"]+)\\\"\\s*\\>", "||SPLIT||\n$1 ::= <<\n");
					
					templateString = templateString.replaceAll("\\<\\s*\\/\\%def\\s*\\>", "\n>>\n||SPLIT||");
					
					String tmp[] = templateString.split("\\|\\|SPLIT\\|\\|");
					
					
					for (String frag : tmp) {
						if (frag.endsWith("\n>>\n")) {
							template.append(frag);
						} else {
							page.append(frag);
							page.append("\n");
						}
					}
					if (parent == null) {
						template.insert(0, "page(params) ::= <<\n" + page.toString() + "\n>>\n");
					}
					
					
					
					System.out.println("##### TEMPLATE : " + f);
					System.out.println(template.toString());
					System.out.println("##### END TEMPLATE : " + f);
					STGroup group = new STGroupString("page", template.toString(), '$', '$');
					if (parent != null)
						group.importTemplates(parent);
					return group;
				} catch (Exception x) {
					log.warn("Caught", x);
				}
				return null;
			}
		});
		return cache.get(filename).get();
	}
	
	
	
	private static long timeout = 10*1000; //default to 10 seconds.
	private static String templateDirectory = "templates";
	
	private static LazyInit lock = new LazyInit();
	private static ConcurrentHashMap<String, ReinitObject<STGroup>> cache = new ConcurrentHashMap<String, ReinitObject<STGroup>> ();
	
	public STTemplateRenderer() {
		
	}
	
	/**
	 * initializes the renderer.
	 * 
	 * @param templateDirectory
	 * @param cacheTimeout
	 */
	@Override
	public void init(DynMap config) {
		if (lock.start()) {
			try {
				String base = config.getString("templating.base_dir");
				if (base != null) {
					templateDirectory = base;
				}

				Long timout = config.getLong("templating.cache_timeout");
				if (timout != null)
					timeout = timout;
			} finally {
				lock.end();
			}
		}
	}
	
	public void init(String templateDir, long timeout) {
		DynMap config = new DynMap();
		config.put("templating.base_dir", templateDir);
		config.put("templating.cache_timeout", timeout);
		init(config);
	}
	

	/* (non-Javadoc)
	 * @see com.trendrr.strest.contrib.templating.TemplateRenderer#renderTemplateString(java.lang.String, com.trendrr.oss.DynMap)
	 */
	@Override
	public String renderTemplateString(String template, DynMap params) {
		log.warn("Sorry can't use " + this.getClass() + " to render a template string right now.");
		return null;
	}




	/* (non-Javadoc)
	 * @see com.trendrr.strest.contrib.templating.TemplateRenderer#renderTemplateFile(java.lang.String, com.trendrr.oss.DynMap)
	 */
	@Override
	public String renderTemplateFile(String filename, DynMap params) {
		System.out.println("LOADING GROUP: " + filename);
		STGroup group = this.loadGroup(filename);
		System.out.println("GOT GROUP: " + group);
		if (group == null) {
			log.warn("template: " + filename + " could not be loaded");
			return null;
		}
		ST st = group.getInstanceOf("page");
		if (params != null) {
			st.add("params", params);
//			for (String key : params.keySet()) {
//				st.add(key, params.get(key));
//			}
		}
		return st.render();
	}
}
