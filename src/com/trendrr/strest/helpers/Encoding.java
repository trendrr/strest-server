/**
 * 
 */
package com.trendrr.strest.helpers;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.base64.Base64;
import org.jboss.netty.handler.codec.base64.Base64Dialect;


/**
 * @author Dustin Norlander
 * @created Apr 5, 2011
 * 
 */
public class Encoding {

	protected static Log log = LogFactory.getLog(Encoding.class);
	
	/**
	 * Encodes as base64
	 * 
	 * @param bytes
	 * @return
	 */
	public static String base64(byte[] bytes) {
		
		ChannelBuffer buf = Base64.encode(ChannelBuffers.wrappedBuffer(bytes));
		try {
			return new String(buf.array(), "ASCII");
		} catch (UnsupportedEncodingException e) {
			log.error("Caught", e);
		}
		return null;
	}
	
	public static String base64(byte[] bytes, Base64Dialect dialect) {
		ChannelBuffer buf = Base64.encode(ChannelBuffers.wrappedBuffer(bytes), dialect);
		try {
			return new String(buf.array(), "ASCII");
		} catch (UnsupportedEncodingException e) {
			log.error("Caught", e);
		}
		return null;
	}
}
