/**
 * 
 */
package com.trendrr.strest.server.v2;

import java.nio.charset.Charset;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferFactory;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.util.CharsetUtil;

import com.trendrr.json.stream.JSONStreamParser;
import com.trendrr.oss.DynMap;
import com.trendrr.oss.FileHelper;


/**
 * @author Dustin Norlander
 * @created May 4, 2012
 * 
 */
public class JsonDecoder extends DelimiterBasedFrameDecoder {

	protected static Log log = LogFactory.getLog(JsonDecoder.class);

	JSONStreamParser parser = new JSONStreamParser();

	public JsonDecoder() {
		super((int)FileHelper.megsToBytes(1),  
				false,
				ChannelBuffers.wrappedBuffer(new byte[] { '}' }));
	}
	
	/* (non-Javadoc)
	 * @see org.jboss.netty.handler.codec.frame.FrameDecoder#decode(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.Channel, org.jboss.netty.buffer.ChannelBuffer)
	 */
	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			ChannelBuffer buf) throws Exception {
		Object result = super.decode(ctx, channel, buf);
		if (result != null) {
			StringCharacterIterator iter = new StringCharacterIterator(((ChannelBuffer)result).toString(CharsetUtil.UTF_8));
			for(char c = iter.first(); c != CharacterIterator.DONE; c = iter.next()) {
		         DynMap val = parser.addChar(c);
		         if (val != null)
		        	 return val;
		    }
		}
		return null;
	}
}
