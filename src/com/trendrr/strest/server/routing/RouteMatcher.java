/**
 * 
 */
package com.trendrr.strest.server.routing;

import java.util.ArrayList;
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

	protected Log log = LogFactory.getLog(RouteMatcher.class);
	
	TreeNode tree = new TreeNode();
	
	
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
		List<UriMapping> found = new ArrayList<UriMapping>();
		
		List<String> words = new ArrayList<String>();
		
		String word = null;
		for (String w : StringHelper.trim(uri, "/").split("\\/")) {
			if (word != null)
				words.add(word);
			word = w;
		}
		
//		get the extension if it exists
		String extension = null;
		int index = word.indexOf('.');
		if (index != -1) {
			extension = word.substring(index+1);
			word = word.substring(0, index);
		}
		words.add(word);
		
		
		tree.find(found, words);
		
		if (found.isEmpty()) {
			return null;
		}
		UriMapping route = found.get(0);
		
		if (found.size() > 1) {
//			log.warn("More then one route matched: " + found);
			for (UriMapping mp : found) {
				if (!mp.isWildCard()) {
					route = mp;
				}
			}
		}
		
		MatchedRoute m = new MatchedRoute();
		m.setMapping(route);
		m.setParams(m.getMapping().getWildCardMatches(words));
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
		if (mapping.getRoute().isEmpty()) {
			tree.setMapping(mapping);
		} else {
			tree.addChildNode(mapping, mapping.getTokens());
		}
	}
	
}
