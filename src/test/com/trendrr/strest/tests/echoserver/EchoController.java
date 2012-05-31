/**
 * 
 */
package com.trendrr.strest.tests.echoserver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.strest.annotations.Strest;
import com.trendrr.strest.server.StrestController;


/**
 * @author Dustin Norlander
 * @created May 31, 2012
 * 
 */
@Strest (
	route = "/echo"
)
public class EchoController extends StrestController {

	protected static Log log = LogFactory.getLog(EchoController.class);
	
	public void handleGET(DynMap params) throws Exception {
		this.getResponseAsBuilder().contentUTF8("text/plain", params.getString("echo"));
	}
}
