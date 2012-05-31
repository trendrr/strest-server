/**
 * 
 */
package com.trendrr.strest.server.callbacks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.strest.server.connections.StrestConnectionGroup;


/**
 * Used to alert when a connection group is empty.
 * 
 * 
 * @author Dustin Norlander
 * @created Apr 26, 2011
 * 
 */
public interface ConnectionGroupEmptyCallback {
	public void connectionGroupEmpty(StrestConnectionGroup group);
}
