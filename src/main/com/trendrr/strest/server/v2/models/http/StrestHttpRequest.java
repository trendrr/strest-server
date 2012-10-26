/**
 * 
 */
package com.trendrr.strest.server.v2.models.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.DynMapFactory;
import com.trendrr.strest.server.connections.StrestConnectionChannel;
import com.trendrr.strest.server.connections.StrestNettyConnectionChannel;
import com.trendrr.strest.server.v2.models.StrestRequest;
import com.trendrr.strest.server.v2.models.StrestHeader.Method;
import com.trendrr.strest.server.v2.models.StrestHeader.Name;
import com.trendrr.strest.server.v2.models.StrestHeader.TxnAccept;


/**
 * @author Dustin Norlander
 * @created May 1, 2012
 * 
 */
public class StrestHttpRequest extends StrestHttpBase implements StrestRequest {

	protected static Log log = LogFactory.getLog(StrestHttpRequest.class);
	
	protected StrestConnectionChannel channel;
		
	public StrestHttpRequest(HttpRequest request) {
		this.message = request;
	}
	
	public HttpRequest getRequest() {
		return (HttpRequest)this.message;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestRequest#getConnectionChannel()
	 */
	@Override
	public StrestConnectionChannel getConnectionChannel() {
		return this.channel;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestRequest#setConnectionChannel(com.trendrr.strest.server.connections.StrestConnectionChannel)
	 */
	@Override
	public void setConnectionChannel(StrestConnectionChannel channel) {
		this.channel = channel;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestRequest#setMethod(com.trendrr.strest.server.v2.models.StrestHeader.Method)
	 */
	@Override
	public void setMethod(Method method) {
		this.getRequest().setMethod(HttpMethod.valueOf(method.toString()));
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestRequest#getMethod()
	 */
	@Override
	public Method getMethod() {
		return Method.instance(this.getRequest().getMethod().toString());
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestRequest#setUri(java.lang.String)
	 */
	@Override
	public void setUri(String uri) {
		this.getRequest().setUri(uri);
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestRequest#getUri()
	 */
	@Override
	public String getUri() {
		return this.getRequest().getUri();
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestRequest#setTxnAccept(com.trendrr.strest.server.v2.models.StrestHeader.TxnAccept)
	 */
	@Override
	public void setTxnAccept(TxnAccept accept) {
		this.addHeader(Name.TXN_ACCEPT, accept.getHttp());
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestRequest#getTxnAccept()
	 */
	@Override
	public TxnAccept getTxnAccept() {
		return TxnAccept.instance(this.getHeader(Name.TXN_ACCEPT));
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestRequest#setParams(com.trendrr.oss.DynMap)
	 */
	@Override
	public void setParams(DynMap params) {
		String encodedParams = params.toURLString();
		if (encodedParams == null || encodedParams.isEmpty()) {
			return;
		}
		String uri = this.getUri();
		if (!uri.contains("?")) {
			uri = uri + "?";
		} else {
			uri = uri + "&";
		}
		this.setUri(uri + encodedParams);
	}

	protected DynMap params = null;
	protected DynMap paramsPOST = null;
	protected DynMap paramsGET = null;
	
	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestRequest#getParams()
	 */
	@Override
	public DynMap getParams() {
		if (params == null) {
			params = new DynMap();
			params.putAll(this.getParamsGET());
			params.putAll(this.getParamsPOST());
		}
		return params;
	}
	
	
	public DynMap getParamsPOST() {
		if (paramsPOST != null) {
			return this.paramsPOST;
		}
		
		 //parse any post params
        String contentType = this.getHeader(Name.CONTENT_TYPE);
        if(contentType != null){
        	String pms = this.getContentAsString();
        	if(pms != null){
        		if (contentType.contains("form-urlencoded")) {
        			this.paramsPOST = DynMapFactory.instanceFromURLEncoded(pms);
        		}else if (contentType.contains("json")){
        			this.paramsPOST = DynMapFactory.instanceFromJSON(pms);
        		}
        	}
        }
		if (this.paramsPOST == null)
			this.paramsPOST = new DynMap();
		return this.paramsPOST;
        
	}
	
	public DynMap getParamsGET() {
		if (this.paramsGET == null) {
			this.paramsGET = DynMapFactory.instanceFromURL(this.getUri());
		}
		return this.paramsGET;
	}
	/* (non-Javadoc)
	 * @see com.trendrr.strest.server.v2.models.StrestPacketBase#cleanup()
	 */
	@Override
	public void cleanup() {
		super.cleanup();
		this.channel = null;
	}
	
}
