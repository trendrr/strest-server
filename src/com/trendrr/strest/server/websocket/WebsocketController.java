/**
 * 
 */
package com.trendrr.strest.server.websocket;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.ORIGIN;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SEC_WEBSOCKET_KEY1;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SEC_WEBSOCKET_KEY2;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SEC_WEBSOCKET_LOCATION;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SEC_WEBSOCKET_ORIGIN;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SEC_WEBSOCKET_PROTOCOL;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.WEBSOCKET_LOCATION;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.WEBSOCKET_ORIGIN;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.WEBSOCKET_PROTOCOL;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Values.WEBSOCKET;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpHeaders.Values;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameDecoder;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameEncoder;

import com.trendrr.oss.DynMap;
import com.trendrr.strest.StrestException;
import com.trendrr.strest.StrestHttpException;
import com.trendrr.strest.annotations.Strest;
import com.trendrr.strest.server.StrestController;


/**
 * @author Dustin Norlander
 * @created Mar 18, 2011
 * 
 */
@Strest(
		route="/websocket"
)
public class WebsocketController extends StrestController {

	protected Log log = LogFactory.getLog(WebsocketController.class);

	public void handleGET(DynMap params) throws StrestException {
		if (!Values.UPGRADE.equalsIgnoreCase(request.getHeader(CONNECTION)) ||
				!WEBSOCKET.equalsIgnoreCase(request.getHeader(Names.UPGRADE))) {
			log.info("WEBSOCKET NOT AN UPGRADE REQUEST");
			throw new StrestHttpException(400, "Websocket upgrade path, no upgrade headers present");
		}
		
		/*
		 * This logic is cut and pasted from the netty websocket handler
		 *  
		 *  
		 */
		// Create the WebSocket handshake response.
        HttpResponse res = new DefaultHttpResponse(
                HTTP_1_1,
                new HttpResponseStatus(101, "Web Socket Protocol Handshake"));
        res.addHeader(Names.UPGRADE, WEBSOCKET);
        res.addHeader(CONNECTION, Values.UPGRADE);

        // Fill in the headers and contents depending on handshake method.
        if (request.containsHeader(SEC_WEBSOCKET_KEY1) &&
            request.containsHeader(SEC_WEBSOCKET_KEY2)) {
            // New handshake method with a challenge:
            res.addHeader(SEC_WEBSOCKET_ORIGIN, request.getHeader(ORIGIN));
            res.addHeader(SEC_WEBSOCKET_LOCATION, getWebSocketLocation(request));
            String protocol = request.getHeader(SEC_WEBSOCKET_PROTOCOL);
            if (protocol != null) {
                res.addHeader(SEC_WEBSOCKET_PROTOCOL, protocol);
            }

            // Calculate the answer of the challenge.
            String key1 = request.getHeader(SEC_WEBSOCKET_KEY1);
            String key2 = request.getHeader(SEC_WEBSOCKET_KEY2);
            int a = (int) (Long.parseLong(key1.replaceAll("[^0-9]", "")) / key1.replaceAll("[^ ]", "").length());
            int b = (int) (Long.parseLong(key2.replaceAll("[^0-9]", "")) / key2.replaceAll("[^ ]", "").length());
            long c = request.getContent().readLong();
            ChannelBuffer input = ChannelBuffers.buffer(16);
            input.writeInt(a);
            input.writeInt(b);
            input.writeLong(c);
            ChannelBuffer output;
			try {
				output = ChannelBuffers.wrappedBuffer(
				        MessageDigest.getInstance("MD5").digest(input.array()));
				 res.setContent(output);
			} catch (NoSuchAlgorithmException e) {
				log.error("Caught", e); //should never ever happen
			}
           
        } else {
            // Old handshake method with no challenge:
            res.addHeader(WEBSOCKET_ORIGIN, request.getHeader(ORIGIN));
            res.addHeader(WEBSOCKET_LOCATION, getWebSocketLocation(request));
            String protocol = request.getHeader(WEBSOCKET_PROTOCOL);
            if (protocol != null) {
                res.addHeader(WEBSOCKET_PROTOCOL, protocol);
            }
        }

        // Upgrade the connection and send the handshake response.
        ChannelPipeline p = this.getChannelConnection().getChannel().getPipeline();
        p.addFirst("wsstrestdecoder", new WebsocketDecoder());
        p.addFirst("wsdecoder", new WebSocketFrameDecoder());  
        this.getChannelConnection().sendMessage(res);
        this.setSendResponse(false);
        
        //Downstream handlers execute in reverse order.
        p.addFirst("wsstrestencoder", new WebsocketEncoder());
        p.addFirst("wsencoder", new WebSocketFrameEncoder());
	}

	 private String getWebSocketLocation(HttpRequest req) {
        return "ws://" + req.getHeader(HttpHeaders.Names.HOST) + this.routes()[0];
    }

}
