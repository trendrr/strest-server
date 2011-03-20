/**
 * 
 */
package com.trendrr.strest.server;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameEncoder;
import org.jboss.netty.util.CharsetUtil;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.DynMapFactory;
import com.trendrr.oss.Reflection;
import com.trendrr.strest.StrestHttpException;
import com.trendrr.strest.StrestUtil;
import com.trendrr.strest.server.websocket.WebsocketEncoder;


/**
 * Handles creating and routing to the controllers. 
 * There is one instance of this class per server, so everything 
 * must be extremely threadsafe.
 * 
 * 
 * @author Dustin Norlander
 * @created Jan 12, 2011
 * 
 */
public class StrestRouter {

	protected Log log = LogFactory.getLog(StrestRouter.class);
	
	protected RouteLookup routeLookup = new RouteLookup();

	protected ConcurrentHashMap<Channel, StrestConnection> connections = new ConcurrentHashMap<Channel, StrestConnection>();
	
	protected List<StrestControllerFilter> defaultFilters = new ArrayList<StrestControllerFilter> ();
	
	/**
	 * will search this package (and all subpackages) for controllers.
	 * 
	 * 
	 * @param packageName
	 */
	public void addControllerPackage(String packageName) {
		List<StrestController> controllers = Reflection.instances(StrestController.class, packageName, true);
		for (StrestController c : controllers) {
			for (String route : c.routes()) {
				this.addRoute(route, c.getClass());
			}
		}
	}
	
	/**
	 * sets the default filters that will be run on all controllers.
	 * 
	 * List should be full class names of the filters.
	 * 
	 * TODO: this is not currently threadsafe.  it is meant to be executed before starting up, but even so, should be safe.
	 * 
	 * @param defaultFilters
	 */
	public void setDefaultFilters(List<String> defaultFilters) {
 
		
		this.defaultFilters = new ArrayList<StrestControllerFilter> ();
		 for (String d : defaultFilters) {
			 try {
				StrestControllerFilter f = Reflection.defaultInstance(StrestControllerFilter.class, d);
				this.defaultFilters.add(f);
			} catch (Exception x) {
				log.warn("Unable to load filter: " + d, x);
			}
		 }
	}
	
	public void addRoute(String route, Class cls) {
		this.getRouteLookup().addRoute(route, cls);
	}
	
	public RouteLookup getRouteLookup() {
		return routeLookup;
	}

	public void setRouteLookup(RouteLookup lookup) {
		this.routeLookup = lookup;
	}

	/**
	 * removes any state associated with this channel. 
	 * does not do anything to the channel itself. 
	 * @param channel
	 */
	public void removeChannel(Channel channel) {
		StrestConnection con = this.connections.remove(channel);
		if (con == null)
			return;
		con.finished();
	}
	
	public void incoming(Channel channel, HttpRequest request) {
		
//		System.out.println("NEW CONNECTION!");
//		System.out.println("TOTAL CONNECTIONS: " + this.connections.size());
		boolean isStrest = "STREST".equalsIgnoreCase(request.getProtocolVersion().getProtocolName());
		// Build the response object.
        ResponseBuilder response = new ResponseBuilder(request);
        
        this.connections.putIfAbsent(channel, new StrestConnection());
        StrestConnection con = this.connections.get(channel);
        con.channel = channel;
        
        String txnId = request.getHeader(StrestUtil.HEADERS.TXN_ID);
        con.transactionStarted(txnId);
        
        try {
        	try {
	        	log.info("Looking up:" + request.getUri());

	            StrestController controller = this.getRouteLookup().find(request.getUri());
	            if (controller == null) {
	            	throw StrestHttpException.NOT_FOUND();
	            }
	            
	            controller.setStrest(isStrest);
	            if (isStrest) {
	            	controller.setStrestTxnId(txnId);	
	            }
	            
	            //parse the get string params
	            DynMap params = DynMapFactory.instanceFromURL(request.getUri());
	            
	            //parse any post params
	            String contentType = request.getHeader(CONTENT_TYPE);
	            if (contentType != null && contentType.contains("form-urlencoded")) {
	            	String pms = request.getContent().toString(Charset.forName("utf8"));
	            	if (pms != null) {
	            		controller.getParams().putAll(DynMapFactory.instanceFromURL(pms));
	            	}
	            }
	            
	            controller.getParams().putAll(params);
	            
	            controller.setRequest(request);
	            controller.setResponse(response.getResponse());
	            controller.setConnection(con);
	            
	            //before filters
	            for (StrestControllerFilter f : controller.getFilters()) {
	            	f.before(controller);
	            }
	            for (StrestControllerFilter f : this.defaultFilters) {
	            	f.before(controller);
	            }
	            
	            //now execution the appropriate action.
	            if (request.getMethod() == HttpMethod.GET) {
					controller.handleGET(controller.getParams());	
	            } else if (request.getMethod() == HttpMethod.POST) {
	            	controller.handlePOST(controller.getParams());	
	            } else if (request.getMethod() == HttpMethod.PUT) {
	            	controller.handlePUT(controller.getParams());	
	            } else if (request.getMethod() == HttpMethod.DELETE) {
	            	controller.handleDELETE(controller.getParams());	
	            } else {
	            	log.warn("UNSUPPORTED METHOD: " + request.getMethod());
	            }
	            
	            for (StrestControllerFilter f : controller.getFilters()) {
	            	f.after(controller);
	            }
				for (StrestControllerFilter f : this.defaultFilters) {
	            	f.after(controller);
	            }
				
				response.setResponse(controller.getResponse());
				if (!controller.isSendResponse()) {
					log.info("controller requested we not send a response.");
					return;
				}
				
				
	        } catch (StrestHttpException e) {
	        	throw e;
	        } catch (Exception x) {
	        	StrestHttpException e = StrestHttpException.INTERNAL_SERVER_ERROR();
	        	e.setCause(x);
	        	log.error("Caught", x);
	        	throw e;
	        }
		} catch (StrestHttpException e) {
			response.status(e.getCode(), e.getMessage());
			response.txnStatus(StrestUtil.HEADERS.TXN_STATUS_VALUES.COMPLETE);
		}
		
        String txnStatus = response.getTxnStatus();
		if (txnStatus == null) {
			txnStatus = StrestUtil.HEADERS.TXN_STATUS_VALUES.COMPLETE;
		}
		
		//client only accepts single transactions.
		if (StrestUtil.HEADERS.TXN_ACCEPT_VALUES.SINGLE.equalsIgnoreCase(request.getHeader(StrestUtil.HEADERS.TXN_ACCEPT))) {
			txnStatus = StrestUtil.HEADERS.TXN_STATUS_VALUES.COMPLETE;
		}
		
		
		if (txnStatus.equalsIgnoreCase(StrestUtil.HEADERS.TXN_STATUS_VALUES.COMPLETE)) {
			//remove any transaction cache.
			con.transactionComplete(txnId);
		}
		//now set the status
		response.txnStatus(txnStatus);
        
		
		
		
        // Write the response.
//		System.out.println(response.getResponse());
//		System.out.println("*****");
//		System.out.println(response.getResponse().getContent().toString());
		
        ChannelFuture future = con.sendMessage(response.getResponse());

        // Close the non-keep-alive connection after the write operation is done.
        if (!isStrest) {
//        	log.info("CLOSING NON STREST CONNECTION");
            future.addListener(ChannelFutureListener.CLOSE);
            this.removeChannel(channel);
        }
	}
}
