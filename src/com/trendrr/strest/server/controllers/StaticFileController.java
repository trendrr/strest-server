/**
 * 
 */
package com.trendrr.strest.server.controllers;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.FileHelper;
import com.trendrr.oss.concurrent.ReinitObject;
import com.trendrr.strest.ContentTypes;
import com.trendrr.strest.StrestHttpException;
import com.trendrr.strest.annotations.Strest;
import com.trendrr.strest.server.StrestController;


/*//
 * 
 * @description static files. typically cached
 * 
 * @json(example) 
 * {
 *    "var" : "poop"
 * }
 * 
 * 
 * 
 */

/**
 * 
 * Controller for serving static files.
 * 
 * will give access to anything the static directory so be careful.
 * 
 * @author Dustin Norlander
 * @created Apr 1, 2011
 * 
 */
@Strest(
		route = "/static/*filename"
)
public class StaticFileController extends StrestController {

	protected Log log = LogFactory.getLog(StaticFileController.class);

	//TODO: these should be in the config file.
	public static String baseDir = "static/";
	//cache times out after 20 seconds.  long enough to help in case of 
	//a torrent of traffic, short enough to develop.  
	public static long cacheTimeout = 20*1000l;
	
	private static ConcurrentHashMap<String, ReinitObject<byte[]>> cache = new ConcurrentHashMap<String, ReinitObject<byte[]>> ();
	
	
	protected ConcurrentHashMap<String, ReinitObject<byte[]>> getCache() {
		return cache;
	}
	protected String getBaseDir() {
		return baseDir;
	}
	protected long getCacheTimeout() {
		return cacheTimeout;
	}
	
	@Override
	public void handleGET(DynMap params) throws Exception {
		String filename = this.getBaseDir() + params.getString("filename");
		if (filename.contains("/.")) {
			throw StrestHttpException.BAD_REQUEST("Bad bad bad");
		}
		String returnType = params.getString("return_type");
		if (returnType != null) {
			filename += "." + returnType;
		}
		System.out.println(filename);
		
		final String f = filename;
		
		//fancy timeout cache. 
		cache.putIfAbsent(filename, new ReinitObject<byte[]>(cacheTimeout) {
			@Override
			public byte[] init() {
				try {
					System.out.println("LOADING FROM FILE SYSTEM!");
					return FileHelper.loadBytes(f);
				} catch (Exception e) {
				}
				return null;
			}
		});
		
		
		byte[] bytes = cache.get(filename).get();
		if (bytes == null) {
			throw StrestHttpException.NOT_FOUND();
		}
		this.setResponseBytes(ContentTypes.fromFileExtension(returnType), bytes);
	}

}
