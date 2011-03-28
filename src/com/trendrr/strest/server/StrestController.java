/**
 * 
 */
package com.trendrr.strest.server;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.util.CharsetUtil;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.Reflection;
import com.trendrr.strest.StrestHttpException;
import com.trendrr.strest.annotations.Strest;
import com.trendrr.strest.server.connections.StrestConnectionChannel;
import com.trendrr.strest.server.connections.StrestConnectionTxn;


/**
 * 
 * Base controller class. 
 * 
 * 
 * 
 * @author Dustin Norlander
 * @created Jan 13, 2011
 * 
 */
public abstract class StrestController {

	protected Log log = LogFactory.getLog(StrestController.class);
	
	protected HttpRequest request;
	protected HttpResponse response = null;
	protected DynMap params = new DynMap();
	protected boolean sendResponse = true;
	
	/**
	 * If false then the this.response item will not automatically be sent to the client
	 * @return
	 */
	public boolean isSendResponse() {
		return sendResponse;
	}

	/**
	 * set to false if you would like to send responses outside the normal request response
	 * 
	 * @param sendResponse
	 */
	public void setSendResponse(boolean sendResponse) {
		this.sendResponse = sendResponse;
	}
	
	/**
	 * these are the params, either parsed from the get string, or from a form encoded post.
	 * will also include any named params from the url string.
	 * 
	 * @return
	 */
	public DynMap getParams() {
		return params;
	}

	public void setParams(DynMap params) {
		this.params = params;
	}

	protected boolean strest = false;
	protected String strestTxnId = null;
	protected StrestConnectionChannel connection = null;
	
	/**
	 * default constructor is manditory.  Other constructors will not be used.
	 */
	public StrestController() {
		
		
	}
	
	public StrestConnectionChannel getChannelConnection() {
		return this.connection;
	}

	public void setChannelConnection(StrestConnectionChannel connection) {
		this.connection = connection;
	}
	
	public StrestConnectionTxn getTxnConnection() {
		return this.connection.getTxnConnection(this.strestTxnId);
	}

	public Map<String,Object> getTransactionStorage() {
		return this.connection.getTxnConnection(this.strestTxnId).getStorage();
	}
	
	
	public void handleGET(DynMap params) throws Exception {
		throw StrestHttpException.METHOD_NOT_ALLOWED();
	}
	
	public void handlePOST(DynMap params) throws Exception {
		throw StrestHttpException.METHOD_NOT_ALLOWED();
	}
	
	/**
	 * overrideable for the lesser used methods
	 * @param params
	 * @throws Exception
	 */
	public void handlePUT(DynMap params) throws Exception {
		throw StrestHttpException.METHOD_NOT_ALLOWED();
	}
	
	/**
	 * overrideable for the lesser used methods
	 * @param params
	 * @throws Exception
	 */
	public void handleDELETE(DynMap params) throws Exception {
		throw StrestHttpException.METHOD_NOT_ALLOWED();
	}
	
	public HttpRequest getRequest() {
		return request;
	}

	public void setRequest(HttpRequest request) {
		this.request = request;
	}

	public HttpResponse getResponse() {
		return response;
	}

	public void setResponse(HttpResponse response) {
		this.response = response;
	}

	public boolean isStrest() {
		return strest;
	}
	
	public void setResponseStatus(HttpResponseStatus status_code){
		this.response.setStatus(status_code);
	}
	
	public void setResponseStatus(int code, String message){
		this.response.setStatus(new HttpResponseStatus(code,message));
	}
	
	public void setStrest(boolean strest) {
		this.strest = strest;
	}

	public String getStrestTxnId() {
		return strestTxnId;
	}

	public void setStrestTxnId(String strestTxnId) {
		this.strestTxnId = strestTxnId;
	}
	
	public void setResponseBytes(byte[] bytes, String mimeType) {
		this.response.setContent(ChannelBuffers.wrappedBuffer(bytes));
		this.response.setHeader("Content-Type", mimeType);
		response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);
	}
	
	public void setResponseJson(String json) {
		this.setResponseBytes(json.getBytes(Charset.forName("utf8")), "json");
	}
	
	public void setResponseJson(DynMap json) {
		this.setResponseJson(json.toJSONString());			
	}
	
	
	public String[] routes() {
		if (this.getClass().isAnnotationPresent(Strest.class)) {
			return this.getClass().getAnnotation(Strest.class).route();
		}
		return null;
	}

	public Class[] filters() {
		if (this.getClass().isAnnotationPresent(Strest.class)) {
			return this.getClass().getAnnotation(Strest.class).filters();
		}
		return null;
	}
	
	/**
	 * gets any filters associated with this controller.
	 * 
	 * @return filters or empty list, never null
	 */
	public List<StrestControllerFilter> getFilters() {
		Class[] filterClss = this.filters();

		
		List<StrestControllerFilter> filters = new ArrayList<StrestControllerFilter>();
        if (filterClss == null || filterClss.length < 1) {
        	return filters;
        }
    	for (Class f : filterClss) {    
    		if (f.equals(Strest.class)) {
    			//Cheap hack..  this is the default...
    			continue;
    		}
    		
    		Object filter;
			try {
				filter = Reflection.defaultInstance(f);
				if (!(filter instanceof StrestControllerFilter)) {
	    			log.warn("Filters must implement the StrestControllerFilter interface (" + filter + "  does not)");
	    			continue;
	    		}	
				filters.add((StrestControllerFilter)filter);
			} catch (Exception e) {
				log.warn("Caught", e);
			}
    	}
    	return filters;
	}
}
