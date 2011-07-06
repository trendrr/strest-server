/**
 * 
 */
package com.trendrr.strest.contrib.filters;

import java.io.UnsupportedEncodingException;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.trendrr.oss.ListHelper;
import com.trendrr.oss.TypeCast;
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
	
	private static Collection<String> params = ListHelper.toTypedList(String.class, "jsonp,callback", ",");
	
	/**
	 * returns the possible names for the jsonp param.  
	 * defaults to ['jsonp','callback']
	 * @return
	 */
	protected Collection<String> getParamNames() {
		return params;
	}
	
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
		String param = null;
		for (String p : this.getParamNames()) {
			param = controller.getParams().getString(p);
			if (param != null)
				break;
		}
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
