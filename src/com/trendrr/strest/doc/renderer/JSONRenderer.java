/**
 * 
 */
package com.trendrr.strest.doc.renderer;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.strest.doc.TemplateRenderer;


/**
 * @author Dustin Norlander
 * @created Mar 31, 2011
 * 
 */
public class JSONRenderer implements TemplateRenderer {

	protected Log log = LogFactory.getLog(JSONRenderer.class);

	/* (non-Javadoc)
	 * @see com.trendrr.strest.doc.TemplateRenderer#renderIndex(com.trendrr.oss.DynMap)
	 */
	@Override
	public byte[] renderIndex(DynMap index) {
		try {
			return index.toJSONString().getBytes("utf8");
		} catch (UnsupportedEncodingException e) {
			log.error("Caught", e);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.doc.TemplateRenderer#renderPage(com.trendrr.oss.DynMap)
	 */
	@Override
	public byte[] renderPage(DynMap page) {
		try {
			return page.toJSONString().getBytes("utf8");
		} catch (UnsupportedEncodingException e) {
			log.error("Caught", e);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.doc.TemplateRenderer#getFileExtension()
	 */
	@Override
	public String getFileExtension() {
		return ".json";
	}
}
