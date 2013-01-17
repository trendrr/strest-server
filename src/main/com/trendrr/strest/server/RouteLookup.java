/**
 * 
 */
package com.trendrr.strest.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.Reflection;
import com.trendrr.strest.server.routing.MatchedRoute;
import com.trendrr.strest.server.routing.RouteMatcher;
import com.trendrr.strest.server.routing.UriMapping;

/**
 * 
 * handles the conversion from a route to a class.
 * 
 * @author dustin
 *
 */
public class RouteLookup {

	protected Log log = LogFactory.getLog(RouteLookup.class);
	
	
	RouteMatcher matcher = new RouteMatcher();
	
	public void addRoute(String route, Class<StrestController> cls) {
		matcher.addMapping(new UriMapping(route, cls));
	}
	
	/**
	 * Returns all the controllers registered controllers.
	 * 
	 * This copies and instantiates all registered controllers into a new list
	 * so if there are many this could be a heavy operation.
	 * 
	 */
	public List<StrestController> getAllControllers() {
		Collection<UriMapping> mps = this.matcher.getAll();
		ArrayList<StrestController> controllers = new ArrayList<StrestController>();
		for (UriMapping m : mps) {
			Class cls = m.getCls();
			if (cls == null)
				continue;
			try {
				StrestController controller = (StrestController)Reflection.defaultInstance(cls);
				controllers.add(controller);
			} catch (Exception e) {
				log.error("Caught" ,e);
			}
		}
		return controllers;
	}
	
	/**
	 * Finds the controller based on the URI param.
	 * 
	 * Also parses any params on the uri 
	 * @param uri
	 * @return
	 */
	public StrestController find(String uri) {
		if (uri.startsWith("http://")) {
			//sometimes the host shows up in the uri
			uri.replaceFirst("http\\:\\/\\/[^\\/]+\\/", "/");
		}
		int queryIndex = uri.indexOf('?');
		
		if (queryIndex != -1) {
			uri = uri.substring(0, queryIndex);
		}
		
		
		MatchedRoute route = matcher.find(uri);
		if (route == null)
			return null;
		
		Class cls = route.getMapping().getCls();

		if (cls == null)
			return null;
		try {
			StrestController controller = (StrestController)Reflection.defaultInstance(cls);
			controller.params.putAll(route.getParams());
			return controller;
		} catch (Exception e) {
			log.error("Caught" ,e);
		}
		return null;
	}
}
