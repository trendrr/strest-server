/**
 * 
 */
package com.trendrr.strest.doc.renderer;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.FileHelper;
import com.trendrr.strest.doc.TemplateRenderer;


/**
 * @author Dustin Norlander
 * @created Mar 31, 2011
 * 
 */
public class JSONFileRenderer implements TemplateRenderer {

	protected Log log = LogFactory.getLog(JSONFileRenderer.class);
	
	protected String docDirectory="strestdoc";
	
	
	/* (non-Javadoc)
	 * @see com.trendrr.strest.doc.TemplateRenderer#renderIndex(com.trendrr.oss.DynMap)
	 */
	@Override
	public void renderIndex(DynMap index) {
		//save the index.
		try {
			String filename = docDirectory + "/strestdoc_index.json";
			byte[] json = index.toJSONString().getBytes("utf8");
			
			FileHelper.saveBytes(FileHelper.toWindowsFilename(filename), json);
		} catch (Exception e) {
			log.error("Caught", e);
		}
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.doc.TemplateRenderer#renderPage(com.trendrr.oss.DynMap)
	 */
	@Override
	public void renderPage(String route, DynMap page) {
		try {
			String filename = docDirectory + route + ".json";
			byte[] json = page.toJSONString().getBytes("utf8");
			
			FileHelper.saveBytes(FileHelper.toWindowsFilename(filename), json);
		} catch (Exception e) {
			log.error("Caught", e);
		}
	}

	
}
