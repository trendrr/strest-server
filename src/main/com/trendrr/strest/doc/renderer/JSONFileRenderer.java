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
public class JSONFileRenderer extends FileRenderer {

	protected Log log = LogFactory.getLog(JSONFileRenderer.class);

	/* (non-Javadoc)
	 * @see com.trendrr.strest.doc.TemplateRenderer#renderIndex(com.trendrr.oss.DynMap)
	 */
	@Override
	public void renderIndex(DynMap index) {
		this.save("strestdoc_index_" + index.getString("name") + ".json", index.toJSONString());
	}

	/* (non-Javadoc)
	 * @see com.trendrr.strest.doc.TemplateRenderer#renderPage(com.trendrr.oss.DynMap)
	 */
	@Override
	public void renderPage(String route, DynMap page) {
		this.save(route + ".json", page.toJSONString());
	}
}
