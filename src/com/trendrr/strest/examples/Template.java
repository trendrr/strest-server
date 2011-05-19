/**
 * 
 */
package com.trendrr.strest.examples;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.strest.ContentTypes;
import com.trendrr.strest.annotations.Strest;
import com.trendrr.strest.contrib.templating.TemplateRenderer;
import com.trendrr.strest.server.StrestController;


/**
 * Example showing the use of the Templating Contrib
 * 
 * 
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
		String html = this.renderTemplate("/template.html", params);
		this.setResponseUTF8(ContentTypes.HTML, html);
	}
}
