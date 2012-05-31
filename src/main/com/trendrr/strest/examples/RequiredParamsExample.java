/**
 * 
 */
package com.trendrr.strest.examples;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.strest.ContentTypes;
import com.trendrr.strest.annotations.Strest;
import com.trendrr.strest.server.StrestController;


/**
 * @author Dustin Norlander
 * @created Mar 29, 2011
 * 
 */

/*//
 * 
 * @description This example demonstrates use of the requiredParams annotation.  
 * the 'what' param is required so if the request does not contain 'what' an error is returned. 
 * 
 */
@Strest(
		route ="/require",
		requiredParams = {"what"}
)
public class RequiredParamsExample extends StrestController {

	public void handleGET(DynMap params) throws Exception {
		this.setResponseUTF8(ContentTypes.TEXT, "WHAT! " + params.getString("what"));
	}
}
