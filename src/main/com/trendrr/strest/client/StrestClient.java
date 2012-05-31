/**
 * 
 */
package com.trendrr.strest.client;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpVersion;

import com.trendrr.oss.DynMapFactory;
import com.trendrr.oss.concurrent.LazyInit;
import com.trendrr.oss.concurrent.TrendrrLock;
import com.trendrr.strest.StrestUtil;


/**
 * @author Dustin Norlander
 * @created Jan 10, 2011
 * 
 */
public class StrestClient {

	protected Log log = LogFactory.getLog(StrestClient.class);
	
	Channel channel = null;
	ClientBootstrap bootstrap = null;
	String host = null;
	
	ConcurrentHashMap<String, StrestCallback> callbacks = new ConcurrentHashMap<String,StrestCallback>();
	
	public static void main(String...strings) throws Exception{
		
		final StrestClient client = new StrestClient("http://localhost:8008", Executors.newCachedThreadPool());
		{
			RequestBuilder builder = new RequestBuilder();
			builder.uri("/echo");
		}
		Date start = new Date();
		for (int i=0; i < 10000; i++) {
			RequestBuilder builder = new RequestBuilder();
			builder.uri("/echo");
			System.out.println(client.send(builder.getRequest()));
		}
		long millis = new Date().getTime() - start.getTime();
		System.out.println("FINISHED IN " + millis);
//		
		
		//firehose request..
		
		
		client.sendRequest(new RequestBuilder().uri("/firehose").getRequest(), new StrestCallback() {

			@Override
			public void messageRecieved(HttpResponse response) {
				// TODO Auto-generated method stub
				System.out.println("***************");
				System.out.println(response);
				System.out.println("***************");
			}

			@Override
			public void txnComplete() {
				// TODO Auto-generated method stub
				System.out.println("TRANSACTION COMPLETE!");
			}

			@Override
			public void error(Throwable x) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	public StrestClient(String host, ExecutorService threadPool) {
		this.host = host;
        bootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                		threadPool,
                        threadPool));
		
	}
	
	
	private LazyInit lock = new LazyInit();
	
	/**
	 * initializes the connection
	 */
	private void init() {
		if (lock.start()) {
			try {
				URI uri = new URI(host);
		        String scheme = uri.getScheme() == null? "http" : uri.getScheme();
		        host = uri.getHost() == null? "localhost" : uri.getHost();
		        int port = uri.getPort();
		        if (port == -1) {
		        	port = 80;
		            if (scheme.endsWith("s")) {
		                port = 443;
		            }
		        }
		
		        boolean ssl = scheme.endsWith("s");
		        // Set up the event pipeline factory.
		        bootstrap.setPipelineFactory(new HttpClientPipelineFactory(this, ssl));
		
		        // Start the connection attempt.
		        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
		
		        // Wait until the connection attempt succeeds or fails.
		        channel = future.awaitUninterruptibly().getChannel();
		        if (!future.isSuccess()) {
		            future.getCause().printStackTrace();
		            bootstrap.releaseExternalResources();
		            return;
		        }	
			} catch (Exception x) {
				x.printStackTrace();
			} finally {
				lock.end();
			}
		}
	}
	
	public void close() {
		channel.close().awaitUninterruptibly();
        bootstrap.releaseExternalResources();
	}
	
	/**
	 * sends the request asynchrounously. 
	 * 
	 * this method returns immediately, response is sent to the callback.
	 * 
	 * @param request
	 * @param callback
	 * @throws Exception
	 */
	public synchronized void sendRequest(HttpRequest request, StrestCallback callback){
		this.init();
		RequestHelper.addTxnId(request);
		if (callback != null)
			this.callbacks.put(request.getHeader("Strest-Txn-Id"), callback);
		channel.write(request);
	}
	
	/**
	 * sends a synchronious request, will wait for the result.
	 * @param request
	 * @return
	 */
	public HttpResponse send(HttpRequest request) {
		SynchronousRequest sr = new SynchronousRequest();
		this.sendRequest(request, sr);
		return sr.awaitResponse();
	}
	
	public void responseReceived(HttpResponse response) {
		String txnId = response.getHeader("Strest-Txn-Id");
		String txnStatus = response.getHeader("Strest-Txn-Status");
		StrestCallback cb = this.callbacks.get(txnId);
		try {
			cb.messageRecieved(response);
		} catch (Exception x) {
			log.error("Caught", x);
		}
		if (!"continue".equalsIgnoreCase(txnStatus)) {
			this.callbacks.remove(txnId);
			cb.txnComplete();
		}
	}
}
