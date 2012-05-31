/**
 * 
 */
package com.trendrr.strest.examples;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.strest.StrestServerBuilder;
import com.trendrr.strest.server.StrestServer;


/**
 * @author Dustin Norlander
 * @created Mar 16, 2011
 * 
 */
public class Main {

	protected static Log log = LogFactory.getLog(Main.class);
	
	public static void main(String[] args) throws Exception {
		StrestServer server = new StrestServerBuilder().addConfigFile("example_config.yaml").build();
		System.out.println("!!! WELCOME TO STREST !!!");
		System.out.println("Strest-Server examples started");
		server.start();
    }
}
