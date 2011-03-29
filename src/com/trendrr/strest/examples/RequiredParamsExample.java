/**
 * 
 */
package com.trendrr.strest.examples;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.strest.annotations.Strest;
import com.trendrr.strest.server.StrestController;


/**
 * @author Dustin Norlander
 * @created Mar 29, 2011
 * 
 */
@Strest(
		route ="/require",
		requiredParams = {"what"}
)
public class RequiredParamsExample extends StrestController {

	protected Log log = LogFactory.getLog(RequiredParamsExample.class);

	public void handleGET(DynMap params) throws Exception {
		this.setResponseBytes(("WHAT! " + params.getString("what")).getBytes("utf8"), "text/plain");
	}

}
