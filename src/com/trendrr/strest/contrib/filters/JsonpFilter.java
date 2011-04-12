/**
 * 
 */
package com.trendrr.strest.contrib.filters;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.trendrr.strest.ContentTypes;
import com.trendrr.strest.StrestException;
import com.trendrr.strest.StrestHttpException;
import com.trendrr.strest.StrestUtil.HEADERS;
import com.trendrr.strest.server.ResponseBuilder;
import com.trendrr.strest.server.StrestController;
import com.trendrr.strest.server.StrestControllerFilter;


/**
 * 
 * Simple filter to handle jsonp requests.
 * 
 * 
 * @author Dustin Norlander
 * @created Apr 12, 2011
 * 
 */
public class JsonpFilter implements StrestControllerFilter {

	protected Log log = LogFactory.getLog(JsonpFilter.class);

	String jsonpParam = "jsonp";
	
	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.StrestControllerFilter#before(com.trendrr.strest.server.StrestController)
	 */
	@Override
	public void before(StrestController controller) throws StrestException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.StrestControllerFilter#after(com.trendrr.strest.server.StrestController)
	 */
	@Override
	public void after(StrestController controller) throws StrestException {
		HttpResponse response = controller.getResponse();
		if (!ContentTypes.JSON.equals(response.getHeader(HttpHeaders.Names.CONTENT_TYPE))) {
			return;
		}
		String param = controller.getParams().getString(this.jsonpParam);
		if (param == null)
			return;
		try {
			String jsonp = param +"(" + new String(response.getContent().array(), "utf8") + ");";
			ResponseBuilder.instance(response).content(ContentTypes.JAVASCRIPT, jsonp.getBytes("utf8"));
		} catch (UnsupportedEncodingException e) {
			throw StrestHttpException.INTERNAL_SERVER_ERROR(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.StrestControllerFilter#error(com.trendrr.strest.server.StrestController, org.jboss.netty.handler.codec.http.HttpResponse, java.lang.Exception)
	 */
	@Override
	public void error(StrestController controller, HttpResponse response,
			Exception exception) {
		// TODO Auto-generated method stub

	}
}
