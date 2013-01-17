/**
 * 
 */
package com.trendrr.strest.server.routing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.StringHelper;


/**
 * 
 * Creates a tree of uri mappings for fast lookup.
 * 
 * will handle named wildcards in the rails style:
 * 
 * /object/:name/:id
 * 
 * will match 
 * /object/my_object/2009
 * 
 * Wildcards will only match on / boundaries.  so /my_object:id will not work.
 * 
 * 
 * lookup should be constant time, no matter how many routes are in the system
 * 
 * 
 * @author Dustin Norlander
 * @created Jan 14, 2011
 * 
 */
public class RouteMatcher {

	protected static Log log = LogFactory.getLog(RouteMatcher.class);
	
	protected TreeNode tree = new TreeNode();
	protected HashMap<String, UriMapping> nonWildcardRoutes = new HashMap<String, UriMapping>();
	
	protected HashSet<UriMapping> all = new HashSet<UriMapping>();
	
	public static void main(String ...strings) {
		
		RouteMatcher tree = new RouteMatcher();
//		
//		tree.addMapping(new UriMapping("/", DummyController.class));
//		tree.addMapping(new UriMapping("/dustin", DummyController.class));
//		tree.addMapping(new UriMapping("/billie", DummyController.class));
//		tree.addMapping(new UriMapping("/dustin/:id", DummyController.class));
//		tree.addMapping(new UriMapping("/dustin/:name/:id", DummyController.class));
//		tree.addMapping(new UriMapping("/", DummyController.class));
//		
//		System.out.println(tree.find("/dustin/blah"));
//		System.out.println(tree.find("/dustin/blah/asdf"));
//		System.out.println(tree.find("/"));
//		
		
		
	}

	/**
	 * Finds the matched route.
	 * 
	 * returns null if no match is found.
	 * 
	 * if multiple matches are found, first one is returned (no guarentee on ordering).
	 * 
	 * if uri ends with .extension then that is trimmed and put as return_type into the params
	 * @param uri
	 * @return
	 */
	public MatchedRoute find(String uri) {
		
		MatchedRoute m = new MatchedRoute();
		String u = StringHelper.trim(uri, "/");
		String extension = null;
		{
			String tmp[] = u.split("\\.");
			if (tmp.length == 2) {
				u = tmp[0];
				extension = tmp[1];
			}
		}
		UriMapping route = this.nonWildcardRoutes.get(u);
		if (route == null) {
			//check wildcard matches.
			List<UriMapping> found = new ArrayList<UriMapping>();
			List<String> words = new ArrayList<String>();
			for (String w : u.split("\\/")) {
				words.add(w);
			}
			
			tree.find(found, words);
			if (found.isEmpty()) {
				return null;
			}
			route = found.get(found.size()-1);
			m.setParams(route.getWildCardMatches(words));
		}
		
		m.setMapping(route);
		
		if (extension != null) {
			m.getParams().put("return_type", extension);
		}
		
		return m;
	}
	
	/**
	 * adds a route mapping.
	 * 
	 * TODO: this is synchronized, but not technically threadsafe, as a concurrent find could be happening. Should fix 
	 * 
	 * @param mapping
	 */
	public synchronized void addMapping(UriMapping mapping) {
		if (!mapping.isWildCard()) {
			this.nonWildcardRoutes.put(mapping.getRoute(), mapping);
		}else if (mapping.getRoute().isEmpty()) {
			tree.setMapping(mapping);
		} else {
			tree.addChildNode(mapping, mapping.getTokens());
		}
		this.all.add(mapping);
	}

	/**
	 * returns a list of all the mapped uris
	 * @return
	 */
	public Collection<UriMapping> getAll() {
		return this.all;
	}
}
