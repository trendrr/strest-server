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
import com.trendrr.strest.doc.renderer.FileRenderer;
import com.trendrr.strest.doc.renderer.JSONFileRenderer;


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
	
	protected Set<String> annotationNames = new HashSet<String>();
	
	public StrestDocParser() {
		this.addTags("com.trendrr.strest.doc.tags", true);
		this.annotationNames.add("Strest");
		this.addTemplateRenderer(new JSONFileRenderer());
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
	
	public void addAnnotationName(String ann) {
		this.annotationNames.add(ann);
	}
	
	/**
	 * parses the given src directories and saves the resulting files in the save directory.
	 * @param srcDirectories
	 * @param saveDirectory
	 */
	public void parseAndSave(List<String> srcDirectories, String saveDirectory) {
		List<DynMap> routes = new ArrayList<DynMap>();
		for (String dir : srcDirectories) {
			routes.addAll(this.parseDirectory(dir));
		}
		List<DynMap> indexes = this.createIndexes(routes);
		//save the index.
		for (DynMap index : indexes) {
			for (TemplateRenderer rend : this.renderers) {
				if (rend instanceof FileRenderer) {
					((FileRenderer)rend).setSaveDirectory(saveDirectory);
				}
				rend.renderIndex(index);
			}
		}
		
		for (DynMap route : routes) {
			String r = route.get(String.class, "route");
			if (r.isEmpty())
				r = "/index";

			for (TemplateRenderer rend : this.renderers) {	
				rend.renderPage(r, route);
			}
		}
	}
	
	/**
	 * parses the given src directory and saves the resulting files in the save directory.
	 * @param docDirectory
	 * @param saveDirectory
	 */
	public void parseAndSave(String srcDirectory, String saveDirectory) {
		List<String> dirs = new ArrayList<String>();
		dirs.add(srcDirectory);
		this.parseAndSave(dirs, saveDirectory);
	}
	
	
	/**
	 * creates a indexes
	 * 
	 * where each entry looks like:
	 * {
	 * 	"name" : "default", //the default index
	 * 	"categories" : [
	 * 		{
	 * 			"category" : "admin", //name of the category
	 * 			"routes" : [
	 * 				{"route" : "/" , "abstract" : "blah blah"}
	 * 			]
	 * 		}
	 *  ]
	 * }
	 * 
	 * 
	 * 
	 * 
	 * individual entries are created by the createIndexEntry method.
	 * 
	 * @param routes
	 * @return
	 */
	public List<DynMap> createIndexes(List<DynMap> routes) {
		
		DynMap indexes = new DynMap();
		
		
		
		
		for (DynMap route : routes) {
			route.putIfAbsent("index", "default");
			for (String indexName : route.getList(String.class, "index", ",")) {
				indexes.putIfAbsent(indexName, new DynMap("name", indexName));
				DynMap index = indexes.getMap(indexName);
				
				
				
				String category = route.getString("category", "default");
				
				DynMap mp = this.createIndexEntry(route);
				if (mp == null)
					continue;
				
				index.addToListWithDot("categories." + category, mp);
			}
		}
		List<DynMap> inds = new ArrayList<DynMap>();
				
		//	now need to sort the categories
		for(String ind : indexes.keySet()) {
			DynMap cats = indexes.getMap(ind + ".categories");
			List<DynMap> catList = new ArrayList<DynMap>();
			
			for (String c : cats.keySet()) {
				DynMap ct = this.createIndexCategory(c, cats.getList(DynMap.class, "routes"));
				if (ct != null) {
					catList.add(ct);
				}
			}
			//sort the categories
			Collections.sort(catList, new Comparator<DynMap>() {
				@Override
				public int compare(DynMap o1, DynMap o2) {
					return o1.getString("category").compareTo(o2.getString("category"));
				}
			});
			
			//add back to the index.
			indexes.getMap(ind).put("categories", catList);
			inds.add(indexes.getMap(ind));
		}
		return inds;
	}
	
	public DynMap createIndexCategory(String category, List<DynMap> routes) {
		if (routes == null || routes.isEmpty()) {
			return null;
		}
		Collections.sort(routes, new Comparator<DynMap>(){
			@Override
			public int compare(DynMap o1, DynMap o2) {
				String r1 = o1.getString("route", "");
				String r2 = o2.getString("route", "");
				return r1.compareToIgnoreCase(r2);
			}
		});
		DynMap cat = new DynMap();
		cat.put("category", category);
		cat.put("routes", routes);
		return cat;
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
			List<String> routes = m.getList(String.class, "route");
			if (routes != null) {
				for(String route : routes) {
					DynMap v1 = new DynMap();
					v1.putAll(m);
					v1.put("route", route);
					v1 = this.cleanUpRoute(v1);
					vals.add(v1);
				}
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
		
		DynMap mp = new DynMap();
		for (String annotationName : this.annotationNames) {
			String ann = Regex.matchFirst(java, "\\@" + annotationName + "\\s*\\([^\\)]+\\)", false);
			
			if (ann == null) {
				continue;
			}
			ann = ann.replaceFirst("\\@" + annotationName + "\\s*\\(", "");
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
			if (mp.get("method") == null) {
				mp.put("method", this.parseMethod(java));
			} else {
				mp.put("method", mp.getList(String.class, "method", ","));
			}
			results.add(mp);
		}
		
		return results;
	}
	
	/**
	 * attempts to figure out if the route is GET, POST, DELETE, ect.
	 * @param java
	 * @return
	 */
	protected List<String> parseMethod(String java) {
		List<String> methods = new ArrayList<String>();
		if (java.contains("handleGET")) {
			methods.add("GET");
		}
		if (java.contains("handlePOST")) {
			methods.add("POST");
		}
		if (java.contains("handleDELETE")) {
			methods.add("DELETE");
		}
		
		if (java.contains("handlePUT")) {
			methods.add("PUT");
		}
		return methods;
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
