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
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import com.trendrr.oss.DynMap;
import com.trendrr.oss.Reflection;
import com.trendrr.oss.TypeCast;

import com.trendrr.strest.ContentTypes;
import com.trendrr.strest.StrestHttpException;
import com.trendrr.strest.annotations.AnnotationHelper;
import com.trendrr.strest.annotations.Strest;
import com.trendrr.strest.server.connections.StrestConnectionChannel;
import com.trendrr.strest.server.connections.StrestNettyConnectionChannel;
import com.trendrr.strest.server.connections.StrestConnectionTxn;
import com.trendrr.strest.server.v2.models.StrestRequest;
import com.trendrr.strest.server.v2.models.StrestResponse;


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

	protected static Log log = LogFactory.getLog(StrestController.class);
	
	protected StrestRequest request;
	protected StrestResponse response = null;
	protected DynMap params = new DynMap();
	protected DynMap paramsGET = new DynMap();
	protected DynMap paramsPOST = new DynMap();
	
	protected StrestRouter router = null;
	
	protected boolean skipExecution = false;
	
	/**
	 * Should we skip running the GET POST ect methods?
	 * @return
	 */
	public boolean isSkipExecution() {
		return skipExecution;
	}

	/**
	 * This flag will indicate that the get,post,delete,put methods should NOT be executed.
	 * 
	 * This is useful for filters to indicate that no error has occurred but the business logic should
	 * not be run (i.e. for caching implementations).
	 * 
	 * @param skipExecution
	 */
	public void setSkipExecution(boolean skipExecution) {
		this.skipExecution = skipExecution;
	}

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
	
	/**
	 * default constructor is manditory.  Other constructors will not be used.
	 */
	public StrestController() {
		
		
	}
	
	public StrestConnectionChannel getChannelConnection() {
		return this.request.getConnectionChannel();
	}

	public StrestConnectionTxn getTxnConnection() {
		return  this.request.getConnectionChannel().getTxnConnection(this.strestTxnId);
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
		try {
			return this.getChannelConnection().getTxnConnection(this.strestTxnId).getStorage();
		} catch (NullPointerException x) {
			log.warn("Unable to get txn storage.  The connection was likely broken, returning dummy");
			return new HashMap<String,Object>();
		}
	}
	
	/**
	 * Connection storage is a way to keep any state associated to the connection.
	 * 
	 * The underlying map is threadsafe.  All data will be deleted as soon the connection is complete.
	 * @return
	 */
	public Map<String,Object> getConnectionStorage() {
		return this.getChannelConnection().getStorage();
	}
	
	
	/**
	 * gets the session storage.  
	 * sessions must be enabled for http connections. otherwise this is
	 * similar to connection storage or txn storage.
	 * 
	 * This is not threadsafe, and should not be used in STREST connections if avoidable.
	 * @deprecated functionality moved to cheshire html controller.
	 * @return
	 */
	@Deprecated
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
	@Deprecated
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
	
	public StrestRequest getRequest() {
		return request;
	}

	public void setRequest(StrestRequest request) {
		this.request = request;
	}

	public StrestResponse getResponse() {
		return response;
	}
	
	public ResponseBuilder getResponseAsBuilder() {
		return ResponseBuilder.instance(this.getResponse());
	}

	public void setResponse(StrestResponse response) {
		this.response = response;
	}

	public boolean isStrest() {
		return strest;
	}
	
	public void setResponseStatus(int code, String message){
		this.response.setStatus(code, message);
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
		return request.getContent().toString();
	}
	
	public String[] routes() {
		if (this.isAnnotationPresent()) {
			return this.getAnnotationVal(String[].class, "route");
		}
		return null;
	}

	/**
	 * gets any additional filters that should run for this controller.  
	 * can be override by subclass, else takes the 'filters' value from the 
	 * annotation.
	 * @return
	 */
	public Class[] filters() {
		if (this.isAnnotationPresent()) {
			return this.getAnnotationVal(Class[].class, "filters");
		}
		return null;
	}
	
	public String[] requiredParams() {	
		if (this.isAnnotationPresent()) {	
			return this.getAnnotationVal(String[].class, "requiredParams");
		}
		return null;
	}
	
	/**
	 * gets the namespace for the filters in
	 * @return
	 */
	public String getControllerNamespace() {
		return "default";
	}
	
	/**
	 * gets a value from the annotation
	 * @param cls
	 * @param name
	 * @return
	 */
	protected <T> T getAnnotationVal(Class<T> cls, String name) {
		return AnnotationHelper.getAnnotationVal(this.getAnnotationClass(), this, cls, name);
	}
	
	protected Class getAnnotationClass() {
		return Strest.class;
	}
	
	protected boolean isAnnotationPresent() {
		return this.getClass().isAnnotationPresent(this.getAnnotationClass());
	}
}
