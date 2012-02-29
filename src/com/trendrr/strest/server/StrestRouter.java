/**
 * 
 */
package com.trendrr.strest.server;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

import java.nio.channels.ClosedChannelException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;


import com.trendrr.oss.DynMapFactory;
import com.trendrr.oss.Reflection;
import com.trendrr.strest.StrestHttpException;
import com.trendrr.strest.StrestUtil;
import com.trendrr.strest.server.connections.StrestConnectionChannel;


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

	protected ConcurrentHashMap<Channel, StrestConnectionChannel> connections = new ConcurrentHashMap<Channel, StrestConnectionChannel>();
	
	protected List<StrestControllerFilter> defaultFilters = new ArrayList<StrestControllerFilter> ();
	protected ConcurrentHashMap<Class, StrestControllerFilter> filters = new ConcurrentHashMap<Class,StrestControllerFilter>();
	
	//the server that this router belongs to .
	protected StrestServer server = null; 
	
	public StrestServer getServer() {
		return server;
	}

	public void setServer(StrestServer server) {
		this.server = server;
	}

	/**
	 * will search this package (and all subpackages) for controllers.
	 * will also work for fully qualified classnames
	 * 
	 * 
	 * @param packageName
	 */
	public void addControllerPackage(String packageName) {
		List<StrestController> controllers = new ArrayList<StrestController>();
		if (!packageName.toLowerCase().equals(packageName)) {
			//see if this is a classname
			StrestController controller;
			try {
				controller = Reflection.defaultInstance(StrestController.class, packageName);
				if (controller != null)
					controllers.add(controller);
			} catch (Exception e) {
				//do nothing.
			}
		}
		
		controllers.addAll(Reflection.instances(StrestController.class, packageName, true));
		
		if (controllers.isEmpty()) {
			log.warn("No controllers found in package: " + packageName);
			return;
		}
		for (StrestController c : controllers) {
			String[] routes = c.routes();
			if (routes == null) {
				this.log.warn("Controller: " + c.getClass().getCanonicalName() + " has no routes.  Skipping");
				continue;
			}
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
		StrestConnectionChannel con = this.connections.remove(channel);
		if (con == null)
			return;
		con.cleanup();
	}
	
	private StrestControllerFilter getFilter(Class cls) {
		StrestControllerFilter f = this.filters.get(cls);
		if (f != null)
			return f;
		try {
			f = (StrestControllerFilter)Reflection.defaultInstance(cls);
			this.filters.putIfAbsent(cls, f);
		} catch (Exception x) {
			log.warn("Unable to load filter: " + cls, x);
		}
		return f;
	}
	
	public void incoming(Channel channel, HttpRequest request) {
		boolean isStrest = StrestUtil.isStrest(request);
		// Build the response object.
        ResponseBuilder response = new ResponseBuilder(request);
        
        this.connections.putIfAbsent(channel, new StrestConnectionChannel(channel));
        StrestConnectionChannel con = this.connections.get(channel);
      
        
        String txnId = request.getHeader(StrestUtil.HEADERS.TXN_ID);
        con.incoming(request);
        StrestController controller = null;
        try {
        	try {
	            controller = this.getRouteLookup().find(request.getUri());
	            if (controller == null) {
	            	throw StrestHttpException.NOT_FOUND();
	            }
	            controller.setRouter(this);
	            controller.setStrest(isStrest);
	            if (isStrest) {
	            	controller.setStrestTxnId(txnId);	
	            }
	            
	            //parse the get string params
	            controller.setParamsGET(DynMapFactory.instanceFromURL(request.getUri()));
	            
	            //parse any post params
	            String contentType = request.getHeader(CONTENT_TYPE);
	            if(contentType != null){
	            	String pms = request.getContent().toString(Charset.forName("utf8"));
	            	if(pms != null){
	            		if (contentType.contains("form-urlencoded")) {
	            			controller.setParamsPOST(DynMapFactory.instanceFromURLEncoded(pms));
	            		}else if (contentType.contains("json")){
	            			controller.setParamsPOST(DynMapFactory.instanceFromJSON(pms));
	            		}
	            	}
	            }

	            
	            controller.getParams().putAll(controller.getParamsGET());
	            controller.getParams().putAll(controller.getParamsPOST());
	            
	            controller.setRequest(request);
	            controller.setResponse(response.getResponse());
	            controller.setChannelConnection(con);
	            
	            //before filters
	            for (Class f : controller.filters()) {
	            	this.getFilter(f).before(controller);
	            }
	            for (StrestControllerFilter f : this.defaultFilters) {
	            	f.before(controller);
	            }
	            
	            //now execution the appropriate action.
	            if (!controller.isSkipExecution()) {
		            if (request.getMethod() == HttpMethod.GET) {
						controller.handleGET(controller.getParams());	
		            } else if (request.getMethod() == HttpMethod.POST) {
		            	controller.handlePOST(controller.getParams());	
		            } else if (request.getMethod() == HttpMethod.PUT) {
		            	controller.handlePUT(controller.getParams());	
		            } else if (request.getMethod() == HttpMethod.DELETE) {
		            	controller.handleDELETE(controller.getParams());	
		            } else {
		            	throw StrestHttpException.METHOD_NOT_ALLOWED();
		            }
	            }
	            for (Class f : controller.filters()) {
	            	this.getFilter(f).after(controller);
	            }
				for (StrestControllerFilter f : this.defaultFilters) {
	            	f.after(controller);
	            }
				
				response.setResponse(controller.getResponse());
				if (!controller.isSendResponse()) {
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
			//run the error filters
			if (controller != null) {
				for (Class fcls : controller.filters()) {
					StrestControllerFilter f = this.getFilter(fcls);
					if (f != null)
						f.error(controller, response.getResponse(), e);
	            }
			}
			for (StrestControllerFilter f : this.defaultFilters) {
				f.error(controller, response.getResponse(), e);
            }
		}
		
        String txnStatus = response.getTxnStatus();
		if (txnStatus == null) {
			txnStatus = StrestUtil.HEADERS.TXN_STATUS_VALUES.COMPLETE;
		}
		
		//client only accepts single transactions.
		if (StrestUtil.HEADERS.TXN_ACCEPT_VALUES.SINGLE.equalsIgnoreCase(request.getHeader(StrestUtil.HEADERS.TXN_ACCEPT))) {
			txnStatus = StrestUtil.HEADERS.TXN_STATUS_VALUES.COMPLETE;
		}
		
		//now set the status
		response.txnStatus(txnStatus);
        
		
		
		
        // Write the response.
//		System.out.println(response.getResponse());
//		System.out.println("*****");
//		System.out.println(response.getResponse().getContent().toString());
		ChannelFuture future = con.sendMessage(response);
		if (future == null) {
			this.removeChannel(channel);
			return;
		}
		
		 // Close the non-keep-alive connection after the write operation is done.
        if (!isStrest) {
//	        	log.info("CLOSING NON STREST CONNECTION");
        	future.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            future.addListener(ChannelFutureListener.CLOSE);
            this.removeChannel(channel);
        }
	}
}
