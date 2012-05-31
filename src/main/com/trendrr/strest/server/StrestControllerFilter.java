/**
 * 
 */
package com.trendrr.strest.server;

import org.jboss.netty.handler.codec.http.HttpResponse;

import com.trendrr.strest.StrestException;
import com.trendrr.strest.server.v2.models.StrestResponse;


/**
 * 
 * Defines filters for before the controller runs and after.
 * 
 * 
 * 
 * 
 * @author Dustin Norlander
 * @created Jan 19, 2011
 * 
 */
public interface StrestControllerFilter {
	
	/**
	 * Runs before the controller.action method is executed.
	 * 
	 * if the filter throws an exception, execution is halted and an error response is 
	 * sent to the user.
	 * 
	 * @param controller
	 * @throws StrestException
	 */
	public void before(StrestController controller) throws StrestException;
	
	
	/**
	 * Runs after the controller.action method is executed, but before the response is written to the socket.
	 * 
	 * @param controller
	 * @throws StrestException
	 */
	public void after(StrestController controller) throws StrestException;
	
	/**
	 * this runs if the controller throws an exception.
	 * 
	 * 
	 * @param controller The controller that issued the exception.  This might be null
	 * @param response The response packet to send to the end user.  Mutable
	 * @param exception The exception
	 */
	public void error(StrestController controller, StrestResponse response, Exception exception);
}
