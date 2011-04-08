/**
 * 
 */
package com.trendrr.strest.examples;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.strest.annotations.Strest;
import com.trendrr.strest.contrib.templating.TemplateRenderer;
import com.trendrr.strest.server.StrestController;


/**
 * @author Dustin Norlander
 * @created Apr 7, 2011
 * 
 */
@Strest(
		route = "/template/:name"
)
public class Template extends StrestController{

	protected Log log = LogFactory.getLog(Template.class);


	public void handleGET(DynMap params) throws Exception {
		String html = TemplateRenderer.render("/test.html", params);
		this.setResponseBytes(html.getBytes("utf8"), "text/html");
	}
}
