/**
 * 
 */
package com.trendrr.strest.doc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.DynMapFactory;
import com.trendrr.oss.FileHelper;
import com.trendrr.oss.Reflection;
import com.trendrr.oss.Regex;
import com.trendrr.oss.StringHelper;
import com.trendrr.strest.doc.renderer.JSONRenderer;


/**
 * @author Dustin Norlander
 * @created Feb 18, 2011
 * 
 */
public class StrestDocParser {

	protected Log log = LogFactory.getLog(StrestDocParser.class);
	
	protected HashMap<String, AbstractDocTag> tags = new HashMap<String, AbstractDocTag>();
	
	protected Set<TemplateRenderer> renderers = new HashSet<TemplateRenderer>();
	
	protected int abstractLength = 256;
	
	protected String annotationName = "Strest";
	
	public StrestDocParser() {
		this.addTags("com.trendrr.strest.doc.tags", true);
		this.addTemplateRenderer(new JSONRenderer());
	}
	
	public static void main(String ...strings) {
		StrestDocParser parser = new StrestDocParser();
//		List<DynMap> routes = parser.parseDirectory("src");
//		System.out.println("*****************************");
//		for(DynMap route : routes) {
//			System.out.println(route.toJSONString());
//			
//		}
//		System.out.println(parser.createIndex(routes));
//		
//		
		parser.parseAndSave("src", "strestdoc");
		
	}
	
	public void parseAndSave(String docDirectory, String saveDirectory) {
		List<DynMap> routes = this.parseDirectory(docDirectory);
		DynMap index = this.createIndex(routes);
		//save the index.
		try {
			String filename = saveDirectory + "/strestdoc_index";
			for (TemplateRenderer rend : this.renderers) {
				FileHelper.saveBytes(filename + rend.getFileExtension(), rend.renderIndex(index));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for (DynMap route : routes) {
			try {
				String r = route.get(String.class, "route");
				if (r.isEmpty())
					r = "/index";
				String filename = saveDirectory + r;
				
				for (TemplateRenderer rend : this.renderers) {					
					FileHelper.saveBytes(filename + rend.getFileExtension(), rend.renderPage(route));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * creates an index map.
	 * index is keyed by categories
	 * 
	 * {
	 * 	 "default" : [
	 * 					{"route" : "/" , "abstract" : "blah blah"}
	 * 				],
	 * 	 "admin" : [ ... ] 
	 * }
	 * 
	 * individual entries are created by the createIndexEntry method.
	 * 
	 * @param routes
	 * @return
	 */
	public DynMap createIndex(List<DynMap> routes) {
		DynMap index = new DynMap();
		for (DynMap route : routes) {
			String category = route.get(String.class, "category", "default");
			index.putIfAbsent(category, new ArrayList<DynMap>());
			DynMap mp = this.createIndexEntry(route);
			if (mp == null)
				continue;
			index.get(List.class, category).add(mp);
		}
		//now sort the lists.
		for (String cat : index.keySet()) {
			List<DynMap> list = index.getList(DynMap.class, cat);
			if (list == null || list.isEmpty()) {
				index.remove(cat);
				continue;
			}
			Collections.sort(list, new Comparator<DynMap>(){
				@Override
				public int compare(DynMap o1, DynMap o2) {
					String r1 = o1.getString("route", "");
					String r2 = o2.getString("route", "");
					return r1.compareToIgnoreCase(r2);
				}
			});
			
			index.put(cat, list);
		}
		return index;
	}
	
	/**
	 * creates the entry for the index page based on the map from the route.
	 * 
	 * return null to skip this entry in the index.
	 * @param route
	 * @return
	 */
	public DynMap createIndexEntry(DynMap route) {
		DynMap mp = new DynMap();
		mp.put("route", route.get("route"));
		String abs = route.get(String.class, "abstract");
		if (abs == null) {
			abs = route.get(String.class, "description", "");
			System.out.println(abs);
			abs = abs.substring(0, Math.min(this.abstractLength, abs.length()));
		}
		mp.put("abstract", abs);
		
		if (route.containsKey("method")) {
			mp.put("method", route.get("method"));
		}
		return mp;
	}
	
	/**
	 * recursively parses this directory and all others below it.
	 * 
	 * Each member of the returned list contains a single route. List is sorted by route.
	 * 
	 * @param dir
	 * @throws Exception 
	 */
	public List<DynMap> parseDirectory(String dir) {
		try {
			List<String> filenames = FileHelper.listDirectory(dir, true);
			List<DynMap> routes = new ArrayList<DynMap>();
			for (String filename : filenames) {
				if (!filename.endsWith(".java")) {
					continue;
				}
				String java = FileHelper.loadString(filename);
				routes.addAll(this.parse(java));
			}
			
			Collections.sort(routes, new Comparator<DynMap>() {
				@Override
				public int compare(DynMap o1, DynMap o2) {
					String r1 = o1.get(String.class, "route") + "_" + o1.get(String.class, "method", "");
					String r2 = o1.get(String.class, "route") + "_" + o2.get(String.class, "method", "");
					return r1.compareTo(r2);
				}
			});
			return routes;
		} catch (Exception x) {
			log.error("Caught", x);
		}
		return null;
	}
	
	/**
	 * adds a new template renderer
	 * @param renderer
	 */
	public void addTemplateRenderer(TemplateRenderer renderer) {
		this.renderers.add(renderer);
	}
	
	/**
	 * sets the list of template renderers
	 * @param renderers
	 */
	public void setTemplateRenderers(Collection<TemplateRenderer> renderers) {
		this.renderers.clear();
		this.renderers.addAll(renderers);
	}
	
	public void addTag(AbstractDocTag tag) {
		tags.put(tag.tagName(), tag);
	}
	
	public void addTags(String packageName, boolean recure) {
		try {
			List<AbstractDocTag> tags = Reflection.defaultInstances(AbstractDocTag.class, packageName, recure);
			for (AbstractDocTag t : tags) {
				this.addTag(t);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * returns a list of maps,
	 * 
	 * Each map represents a single route.
	 * 
	 * returns empty list if the java contains no useful documentation
	 * @param java
	 * @return
	 */
	public List<DynMap> parse(String java) {
		
		
		DynMap mp = this.parseAnnotation(java);
		
		List<DynMap> docs = this.getStrestDoc(java);
		if (mp.isEmpty() && docs.isEmpty()) {
			return docs;
		}
		if (docs.isEmpty()) {
			docs.add(mp);
		}
		List<DynMap> vals = new ArrayList<DynMap>();
		for(DynMap v : docs) {
			
			DynMap m = new DynMap().extend(mp,v);
//			System.out.println(m.toJSONString());
			for(String route : m.getList(String.class, "route")) {
				DynMap v1 = new DynMap();
				v1.putAll(m);
				v1.put("route", route);
				v1 = this.cleanUpRoute(v1);
				vals.add(v1);
			}
		}
		return vals;
		
	}
	
	/**
	 * does final processing on a route map (the data associated with a single route)
	 * 
	 * 
	 * 
	 * @param route
	 * @return
	 */
	protected DynMap cleanUpRoute(DynMap route) {
		List<DynMap> params = route.getList(DynMap.class, "param");
		if (params == null) 
			return route;

		//Make unify the requiredParams annotation with the markup.
		List<String> requiredParams = route.getList(String.class, "requiredParams");
		if (requiredParams != null) {
			HashMap<String, DynMap> pmsMap = new HashMap<String,DynMap>();		
			for (DynMap p : params) {
				pmsMap.put(p.getString("param"), p);
			}
			
			for (String rp : requiredParams) {
				if (pmsMap.containsKey(rp)) {
					pmsMap.get(rp).put("required", true);
				} else {
					DynMap p = new DynMap();
					p.put("param", rp);
					p.put("required", true);
					p.put("description", "");
					params.add(p);
				}
			}
		}
		Collections.sort(params, new Comparator<DynMap>(){
			@Override
			public int compare(DynMap o1, DynMap o2) {
				
				if (o1.getBoolean("required", false) != o2.getBoolean("required", false)) {
					if (o1.getBoolean("required", false)) {
						return -1;
					} else {
						return 1;
					}
				}
				return o1.getString("param").compareTo(o1.getString("param"));
			}
		});
		
		
		
		System.out.println(params);
		route.put("params", params);
		route.removeAll("requiredParams", "param");
		return route;
	}
	
	/**
	 * parses the Strest annotation into a map.
	 * @param java
	 * @return
	 */
	public DynMap parseAnnotation(String java) {
		/*
		 * This is all ugly as hell, and pretty slow, but it works :)
		 */
		
		String ann = Regex.matchFirst(java, "\\@" + this.annotationName + "\\s*\\([^\\)]+\\)", false);
		DynMap mp = new DynMap();
		if (ann == null) {
			return mp;
		}
		ann = ann.replaceFirst("\\@" + this.annotationName + "\\s*\\(", "");
		ann = ann.replaceAll("\\)$", "");
		
		
		String[] tokens = ann.split("\\s*\\=\\s*");
		String key = null;
		String value = null;
		for (String t : tokens) {
			if (key == null) {
				key = t;
				continue;
			}
			//else parse the value.
			String nextKey = Regex.matchFirst(t, "[\\n\\r]+\\s*[^\\s]+\\s*$", false);
			value = t.replaceFirst("[\\n\\r]+\\s*[^\\s]+\\s*$", "").trim();
			boolean isList = value.startsWith("{");
			
			
			value = StringHelper.trim(value, ",");
			value = StringHelper.trim(value, "{");
			value = StringHelper.trim(value, "}");
			value = StringHelper.trim(value.trim(), "\"");
			if (isList) {
				List<String> values = new ArrayList<String>();
				for (String v : value.split(",")) {
					values.add(StringHelper.trim(v.trim(), "\""));
				}
				mp.put(key.trim(), values);
			} else {
				mp.put(key.trim(), value);
			}
			
			if (nextKey != null) {
				key = nextKey;
			}
		}
		
		
		
		System.out.println(mp.toJSONString());
		
		return mp;
	}
	
	public List<DynMap> getStrestDoc(String java) {
//		String doc = Regex.matchFirst(java, "\\/\\*\\/\\/(?s).*?\\*\\/", false);
		List<DynMap> results = new ArrayList<DynMap>();
		for (String doc : Regex.matchAll(java, "\\/\\*\\/\\/(?s).*?\\*\\/", false)) {
		
			
			//now clean it up.
			doc = doc.replaceFirst("^\\/\\*\\/\\/", "");
			doc = doc.replaceAll("(?m)^\\s*\\*", "");
			doc = doc.replaceAll("\\/$", "");
			doc = doc.trim();
			DynMap mp = new DynMap();
			//now split into key values
			for (String tmp : doc.split("[\\r\\n]\\s*\\@")) {
				
				tmp = StringHelper.trim(tmp, "@");
				int ind = tmp.indexOf(' ');
				if (ind < 0)
					continue;
				String key = tmp.substring(0, ind).trim();
				String value = tmp.substring(ind).trim();
				if (value.isEmpty()) {
					log.warn("TAG: " + key + " IS EMPTY!");
					continue;
				}
				
				Object v = this.processTag(key, value);
				if (v == null) {
					log.warn("TAG: " + key + " IS EMPTY!");
					continue;
				}
				
				if (mp.containsKey(key)) {
					List values = mp.get(List.class, key);
					values.add(v);
					mp.put(key, values);
				} else {
					mp.put(key, v);
				}
			}
			results.add(mp);
		}
		
		return results;
	}
	
	/**
	 * processes the text associated with a tag.
	 * 
	 * by default just returns the value.
	 * 
	 * @param tag
	 * @param value
	 * @return
	 */
	public Object processTag(String tag, String value) {
		AbstractDocTag t = this.tags.get(tag);
		if (t == null)
			return value;
		return t.process(this, value);
	}
}
