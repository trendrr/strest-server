/**
 * 
 */
package com.trendrr.strest.server;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

import java.nio.channels.ClosedChannelException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;


import com.trendrr.oss.DynMapFactory;
import com.trendrr.oss.Reflection;
import com.trendrr.strest.StrestException;
import com.trendrr.strest.StrestHttpException;
import com.trendrr.strest.StrestUtil;
import com.trendrr.strest.annotations.AnnotationHelper;
import com.trendrr.strest.annotations.Async;
import com.trendrr.strest.server.connections.StrestNettyConnectionChannel;
import com.trendrr.strest.server.v2.models.*;
import com.trendrr.strest.server.v2.models.StrestHeader.TxnAccept;
import com.trendrr.strest.server.v2.models.StrestHeader.TxnStatus;


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

//	protected ConcurrentHashMap<Channel, StrestNettyConnectionChannel> connections = new ConcurrentHashMap<Channel, StrestNettyConnectionChannel>();
	
	protected HashMap<String, List<StrestControllerFilter>> defaultFilters = new HashMap<String,List<StrestControllerFilter>> ();
	protected ConcurrentHashMap<Class, StrestControllerFilter> filtersByClass = new ConcurrentHashMap<Class,StrestControllerFilter>();
	
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
		List<StrestController> ctrls = Reflection.instances(StrestController.class, packageName, true);
		if (ctrls != null) {
			controllers.addAll(ctrls);
		}
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
	 * gets all the filters for this controller. Will return an empty list, never null.
	 * @param controller
	 * @return
	 */
	protected List<StrestControllerFilter> getFilters(StrestController controller) {
		//TODO: implement a local cache of this? or overkill?
		List<StrestControllerFilter> retList = new ArrayList<StrestControllerFilter>();
		if (controller == null)
			return retList;
        Class[] filters = controller.filters();
        if (filters != null) {
            for (Class f : filters) {
            	StrestControllerFilter filter = this.getFilter(f);
            	retList.add(filter);
            }
        }
        retList.addAll(this.getNamespaceFilters(controller.getControllerNamespace()));
        return retList;
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
	public void setFilters(String namespace, List<String> defaultFilters) {
		if (namespace == null) {
			namespace = "default";
		}
		
		List<StrestControllerFilter> filters = new ArrayList<StrestControllerFilter> ();
		 for (String d : defaultFilters) {
			 try {
				StrestControllerFilter f = Reflection.defaultInstance(StrestControllerFilter.class, d);
				filters.add(f);
			} catch (Exception x) {
				log.warn("Unable to load filter: " + d, x);
			}
		 }
		 this.defaultFilters.put(namespace, filters);
	}
	/**
	 * returns the list of filters for the given namespace or empty list
	 * @param namespace
	 * @return
	 */
	protected List<StrestControllerFilter> getNamespaceFilters(String namespace) {
		List<StrestControllerFilter> filters = this.defaultFilters.get(namespace);
		if (filters == null)
			filters = new ArrayList<StrestControllerFilter>();
		return filters;
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
//	public void removeChannel(Channel channel) {
//		StrestNettyConnectionChannel con = this.connections.remove(channel);
//		if (con == null)
//			return;
//		con.cleanup();
//	}
	
	private StrestControllerFilter getFilter(Class cls) {
		StrestControllerFilter f = this.filtersByClass.get(cls);
		if (f != null)
			return f;
		try {
			f = (StrestControllerFilter)Reflection.defaultInstance(cls);
			this.filtersByClass.putIfAbsent(cls, f);
		} catch (Exception x) {
			log.warn("Unable to load filter: " + cls, x);
		}
		return f;
	}
	
	private boolean isAsync(StrestController controller, StrestHeader.Method method) {
		return AnnotationHelper.hasMethodAnnotation(Async.class, controller, "handle" + method.toString());
	}
	
	
	public void incoming(StrestRequest request) {
		boolean isStrest = StrestUtil.isStrest(request);
		// Build the response object.
		//throw an illegal exception here?
		//TODO:throws a problem if the packet is invalid..
		try {
			StrestUtil.validateRequest(request);
		} catch (StrestException x) {
			//TODO: send exception response..
			log.error("caught", x);
			if (request.getConnectionChannel() != null) {
				request.getConnectionChannel().cleanup();
			}
			return;
		}
        ResponseBuilder response = new ResponseBuilder(request);
        
//        this.connections.putIfAbsent(channel, new StrestNettyConnectionChannel(channel));
//        StrestNettyConnectionChannel con = this.connections.get(channel);
//        request.setConnectionChannel(con);
  
        String txnId = request.getTxnId();
        request.getConnectionChannel().incoming(request);
        log.info("HERE! " + request);
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
	            //TODO: this should be handled by the request objects
	            
	            controller.setParamsGET(DynMapFactory.instanceFromURL(request.getUri())); 
	            controller.getParams().putAll(request.getParams());
	            
	            //parse any post params
	            //TODO: don't think this is really correct.
	            String contentType = request.getHeader(CONTENT_TYPE);
	            if(contentType != null){
	            	String pms = request.getContent().toString();
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
	            
	            //before filters
	            for (StrestControllerFilter f : this.getFilters(controller)) {
	            	f.before(controller);
	            }
	            
	            //now execution the appropriate action.
	            if (!controller.isSkipExecution()) {
		            if (request.getMethod() == StrestHeader.Method.GET) {
						controller.handleGET(controller.getParams());
		            } else if (request.getMethod() == StrestHeader.Method.POST) {
		            	controller.handlePOST(controller.getParams());	
		            } else if (request.getMethod() == StrestHeader.Method.PUT) {
		            	controller.handlePUT(controller.getParams());	
		            } else if (request.getMethod() == StrestHeader.Method.DELETE) {
		            	controller.handleDELETE(controller.getParams());	
		            } else {
		            	throw StrestHttpException.METHOD_NOT_ALLOWED();
		            }
		            if (this.isAsync(controller, request.getMethod())) {
		            	//user is responsable to complete the request.
//		            	return; 
					}
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
			response.txnStatus(TxnStatus.COMPLETED);
			//run the error filters
			if (controller != null) {
				for (StrestControllerFilter f : this.getFilters(controller)) {
					f.error(controller, response.getResponse(), e);
	            }
			}
			try {
				this.sendResponse(request, response);
			} catch (Exception e1) {
				log.error("Caught", e);
			}
			return;
		}
		this.finishResponse(controller, response);
	}
	
	/**
	 * actually does the send, and handles the txn.
	 * @param controller
	 * @param response
	 */
	protected void sendResponse(StrestRequest request, ResponseBuilder response) throws Exception{
		StrestHeader.TxnStatus txnStatus = response.getResponse().getTxnStatus();
		if (txnStatus == null) {
			txnStatus = StrestHeader.TxnStatus.COMPLETED;
		}
		
		//client only accepts single transactions.
		if (request.getTxnAccept() == TxnAccept.SINGLE) {
			txnStatus = StrestHeader.TxnStatus.COMPLETED;
		}
		
		//now set the status
		response.txnStatus(txnStatus);
        
		
		
		
        // Write the response.
//			System.out.println(response.getResponse());
//			System.out.println("*****");
//			System.out.println(response.getResponse().getContent().toString());
		Object future = request.getConnectionChannel().sendMessage(response);
		
		 // Close the non-keep-alive connection after the write operation is done.
        if (!StrestUtil.isStrest(request) && future instanceof ChannelFuture) {
//		        	log.info("CLOSING NON STREST CONNECTION");
        	((ChannelFuture)future).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        	((ChannelFuture)future).addListener(ChannelFutureListener.CLOSE);
        }
		
	}
	/**
	 * completes a response.  
	 * @param controller
	 * @param response
	 */
	public void finishResponse(StrestController controller, ResponseBuilder response) {
		try {
			try {
				//execute final filters
				for (StrestControllerFilter f : this.getFilters(controller)) {
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
			response.getResponse().setTxnStatus(TxnStatus.COMPLETED);
			//run the error filters
			if (controller != null) {
				for (StrestControllerFilter f : this.getFilters(controller)) {
					f.error(controller, response.getResponse(), e);
	            }
			}
		}
        try {
			this.sendResponse(controller.getRequest(), response);
		} catch (Exception e) {
			log.error("Caught", e);
		}
	}
}
