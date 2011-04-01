/**
 * 
 */
package com.trendrr.strest;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.StringHelper;


/**
 * @author Dustin Norlander
 * @created Apr 1, 2011
 * 
 */
public class ContentTypes {

	protected Log log = LogFactory.getLog(ContentTypes.class);
	
	public static final String TEXT = "text/plain";
	public static final String JSON = "application/json";
	public static final String JAVASCRIPT = "text/javascript";
	public static final String BINARY = "application/octet-stream";
	public static final String HTML = "text/html";
	public static final String CSV = "text/csv";
	public static final String CSS = "text/css";
	public static final String XML = "text/xml";
	
	
	private static HashMap<String,String> mappings = new HashMap<String,String>();
	static {
		mappings.put("txt", TEXT);
		mappings.put("json", JSON);
		mappings.put("js", JAVASCRIPT);
		mappings.put("html", HTML);
		mappings.put("csv", CSV);
		mappings.put("css", CSS);
		mappings.put("xml", XML);
	}
	
	
	/**
	 * attempts to find a mime type based on the file extension
	 * ex:
	 * csv would return "text/csv"
	 * 
	 * @param ext the extension to search
	 * @return the mime type or null
	 */
	public static String fromFileExtension(String ext) {
		if (ext == null)
			return null;
		String e = StringHelper.trim(ext.toLowerCase(), ".");
		return mappings.get(e);
	}
	
}
