/**
 * 
 */
package com.trendrr.strest.server;

import com.trendrr.strest.StrestException;


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
}
