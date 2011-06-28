/**
 * 
 */
package com.trendrr.strest.tests;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.trendrr.strest.examples.HelloWorld;
import com.trendrr.strest.server.routing.MatchedRoute;
import com.trendrr.strest.server.routing.RouteMatcher;
import com.trendrr.strest.server.routing.UriMapping;


/**
 * @author Dustin Norlander
 * @created Jun 28, 2011
 * 
 */
public class RouteMatchingTests {

	protected Log log = LogFactory.getLog(RouteMatchingTests.class);
	
	
	@Test
	public void testRoutes() {
		
		RouteMatcher tree = new RouteMatcher();
		
		
		tree.addMapping(new UriMapping("/", HelloWorld.class));
		tree.addMapping(new UriMapping("/test", HelloWorld.class));
		tree.addMapping(new UriMapping("/test/*filenames", HelloWorld.class));
		tree.addMapping(new UriMapping("/test/:id", HelloWorld.class));
		tree.addMapping(new UriMapping("/test/:name/:id", HelloWorld.class));
		tree.addMapping(new UriMapping("/test/idmatch/:id", HelloWorld.class));
		tree.addMapping(new UriMapping("/test/idmatch/namematch", HelloWorld.class));
		
		/*
		 * Notes on matching:
		 * 
		 * matched route allways strips leading /
		 * 
		 * Will always return the MOST specific match, ie exact matches take precidence over wildcards
		 * 
		 */
		
		Assert.assertTrue(match(tree, "hello", null));
		Assert.assertTrue(match(tree, "/", "")); 
		
		Assert.assertTrue(match(tree, "/test/1/2/3/4", "test/*filenames"));
		Assert.assertTrue(match(tree, "/test/1", "test/:id"));
		Assert.assertTrue(match(tree, "/test/name/1", "test/:name/:id"));
		
		Assert.assertTrue(match(tree, "/test/idmatch/1", "test/idmatch/:id"));
		Assert.assertTrue(match(tree, "/test/idmatch/namematch", "test/idmatch/namematch"));
		
//		System.out.println(tree.find("/dustin/blah"));
//		System.out.println(tree.find("/dustin/blah/asdf"));
//		System.out.println(tree.find("/"));
		
	}
	
	protected boolean match(RouteMatcher tree, String route, String expected) {
		MatchedRoute rt = tree.find(route);
		if (rt == null && expected == null)
			return true;
		if (rt == null) {
			log.warn("Route matched was null: " + route);
			return false;
		}
		if (rt.getMapping().getRoute().equals(expected)) {
			return true;
		}
		
		log.warn("Route didn't match: " + route + " GOT: " + rt.getMapping().getRoute() + " EXPECTED: " + expected);
		
		return false;
	}
}
