/**
 * 
 */
package com.trendrr.strest.doc.renderer;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.FileHelper;
import com.trendrr.oss.StringHelper;
import com.trendrr.strest.doc.TemplateRenderer;


/**
 * @author Dustin Norlander
 * @created Mar 5, 2012
 * 
 */
public abstract class FileRenderer implements TemplateRenderer {

	protected Log log = LogFactory.getLog(FileRenderer.class);

	protected String docDirectory="strestdoc";
	
	public void setSaveDirectory(String dir) {
		this.docDirectory = dir;
	}
	
	protected void save(String filename, String content) {
		try {
			this.save(filename, content.getBytes("utf8"));
		} catch (UnsupportedEncodingException e) {
			log.error("Caught", e);
		}
	}
	protected void save(String filename, byte[] bytes) {
		try {
			String f = docDirectory + "/" + StringHelper.trim(filename, "/");
			FileHelper.saveBytes(FileHelper.toWindowsFilename(f), bytes);
		} catch (Exception e) {
			log.error("Caught", e);
		}
	}
	
}
