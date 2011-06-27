/**
 * 
 */
package com.trendrr.strest.server;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.Reflection;
import com.trendrr.oss.concurrent.LazyInit;
import com.trendrr.strest.ContentTypes;
import com.trendrr.strest.StrestHttpException;
import com.trendrr.strest.annotations.Strest;
import com.trendrr.strest.contrib.templating.TemplateLookup;
import com.trendrr.strest.contrib.templating.TemplateRenderer;
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
	protected DynMap paramsGET = new DynMap();
	protected DynMap paramsPOST = new DynMap();
	
	protected StrestRouter router = null;
	
	
	/**
	 * returns the config params for the server.  typically this is the parsed yaml config file.
	 */
	public DynMap getServerConfig() {
		return this.getRouter().getServer().getConfig();
	}
	
	/**
	 * This is the StrestRouter instance that initialized the controller.
	 * 
	 * This is only useful in rare instances. 
	 * 
	 * @return
	 */
	public StrestRouter getRouter() {
		return router;
	}

	public void setRouter(StrestRouter router) {
		this.router = router;
	}

	/**
	 * These params appear in the URI. I.E regular GET string params.
	 * 
	 * These params also appear in the params that are passed to the controller method, so unless you 
	 * need to know that specific params are GET vs POST then use the regular params.
	 * 
	 * @return the GET params
	 */
	public DynMap getParamsGET() {
		return paramsGET;
	}

	public void setParamsGET(DynMap paramsGET) {
		this.paramsGET = paramsGET;
	}

	/**
	 * The params from a url encoded POST.
	 * 
	 * These params also appear in the params that are passed to the controller method, so unless you 
	 * need to know that specific params are GET vs POST then use the regular params.
	 * 	 
	 * @return the POST params
	 */
	public DynMap getParamsPOST() {
		return paramsPOST;
	}

	public void setParamsPOST(DynMap paramsPOST) {
		this.paramsPOST = paramsPOST;
	}

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

	/**
	 * Txn storage is a way to keep state associated with a specific transaction.
	 * The underlying map is threadsafe.  All data will be deleted as soon the txn is complete.
	 * 
	 * 
	 * @return
	 */
	private ConcurrentHashMap<String,Object> nonstrestTxnStorage = new ConcurrentHashMap<String,Object>();
	public Map<String,Object> getTxnStorage() {
		if (!this.isStrest())
			return this.nonstrestTxnStorage;
		
		return this.connection.getTxnConnection(this.strestTxnId).getStorage();
	}
	
	/**
	 * Connection storage is a way to keep any state associated to the connection.
	 * 
	 * The underlying map is threadsafe.  All data will be deleted as soon the connection is complete.
	 * @return
	 */
	public Map<String,Object> getConnectionStorage() {
		return this.connection.getStorage();
	}
	
	
	/**
	 * gets the session storage.  
	 * sessions must be enabled for http connections. otherwise this is
	 * similar to connection storage or txn storage.
	 * 
	 * This is not threadsafe, and should not be used in STREST connections if avoidable.
	 * 
	 * @return
	 */
	public Map<String,Object> getSessionStorage() {
		Map<String, Object> session = (Map<String, Object>)this.getConnectionStorage().get("session");
		if (session == null) {
			session = new HashMap<String,Object>();
			this.getConnectionStorage().put("session", session);
		}
		return session;
	}
	
	/**
	 * deletes the session (if sessions are not enabled, does nothing)
	 */
	public void destroySession() {
		this.getConnectionStorage().put("session_destroy", true);
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
	
	public ResponseBuilder getResponseAsBuilder() {
		return ResponseBuilder.instance(this.getRequest());
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
	
	public void setResponseBytes(String mimeType, byte[] bytes) {
		ResponseBuilder.instance(this.response).content(mimeType, bytes);
	}
	
	public void setResponseUTF8(String mimeType, String val) {
		ResponseBuilder.instance(this.response).contentUTF8(mimeType, val);
	}
	
	public void setResponseJSON(String json) {
		ResponseBuilder.instance(this.response).contentUTF8(ContentTypes.JSON, json);
	}
	
	public void setResponseJSON(DynMap json) {
		ResponseBuilder.instance(this.response).contentJSON(json);
	}
	
	/**
	 * returns the content of the request as a UTF8 encoded string
	 * @return
	 */
	public String getRequestContentUTF8() {
		return request.getContent().toString(Charset.forName("utf8"));
	}
	
	/**
	 * Renders a template using the default TemplateRenderer.
	 * @param filename
	 * @param params
	 * @return
	 */
	public String renderTemplate(String filename, DynMap params) {
		TemplateRenderer renderer = TemplateLookup.getRenderer(this.getServerConfig());
		return renderer.renderTemplateFile(filename, params);
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
	
	public String[] requiredParams() {	
		if (this.getClass().isAnnotationPresent(Strest.class)) {	
			return this.getClass().getAnnotation(Strest.class).requiredParams();
		}
		return null;
	}
	

	private List<StrestControllerFilter> _getFilters() {
		Class[] filterClss = this.filters();
		List<StrestControllerFilter> filters = new ArrayList<StrestControllerFilter>();
        if (filterClss == null || filterClss.length < 1) {
        	return filters;
        }
    	for (Class f : filterClss) {   		
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
	private List<StrestControllerFilter> _filters = null;
	/**
	 * gets any filters associated with this controller.
	 * 
	 * @return filters or empty list, never null
	 */
	public List<StrestControllerFilter> getFilters() {
		if (_filters == null)
			this._filters = this._getFilters();
		return this._filters;
	}
}
