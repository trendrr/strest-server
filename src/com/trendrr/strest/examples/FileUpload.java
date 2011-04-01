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
 * @created Apr 1, 2011
 * 
 */
@Strest(
		route = "/upload"
)
public class FileUpload extends StrestController {

	protected Log log = LogFactory.getLog(FileUpload.class);

	public void handlePOST(DynMap params) throws Exception {
		System.out.println(
		new String(this.request.getContent().array()));
	}
}
