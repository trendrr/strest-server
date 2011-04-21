/**
 * Javascript client for STREST protocol.  
 * 
 * Assumes a websocket is available to a server supporting STREST requests. 
 * 
 * 
 * 
 * @param url
 * @param onopen
 * @param onclose
 * @return
 */


function Strest(url, onopen, onclose) {
	//callbacks keyed by transactionId
	this.callbacks = {};
	
	var self = this;
	var _onmessage = function(event) {		
		//console.log(event.data);
		var response = new StrestResponse(event.data);
		var txnId = response.getHeader('Strest-Txn-Id');
		var cb = self.callbacks[txnId];
		if (!cb) {
			console.log("ERROR: No callback registered for : " + response);
			return;
		}
		if (cb['onmessage']) {
			cb.onmessage(response);
		}
		if ('complete' == response.getHeader('Strest-Txn-Status')) {
			if (cb['ontxncomplete']) {
				cb.ontxncomplete(txnId);
			}
			delete self.callbacks[txnId];
		}
	};
	
	var _onclose = function(event) {
		//iterate over all the waiting callbacks and send the error
		for (var k in self.callbacks) {
			var cb = self.callbacks[k];
			if (cb.onerror) {
				cb.onerror('Connection closed');
			}
		}
		self.callbacks = {};
		if (onclose) {
			onclose(event);
		}
	};
	
	var _onopen = function(event) {
		if (onopen) {
			onopen(event);
		}
	};
	
	var _onerror = function(event) {
		self.close();
	};
	
	//initialize the websocket
	if (window.WebSocket) {
		this.socket = new WebSocket(url);
		this.socket.onmessage = _onmessage;
		this.socket.onopen = _onopen;
		this.socket.onclose = _onclose;
	} else {
	  alert("Your browser does not support Web Socket.");
	}	
	
}

/*
 * Globals
 * 
 */
Strest._txnId = 0;
Strest.userAgent = "JsStrest 1.0";
Strest.protocol = "STREST/0.1";
Strest.txnId = function() {
	return Strest._txnId++;
};

/**
 * Sends a request.  
 * 
 * 
 * 
 * @param request
 * @param message_callback
 * @param txn_end_callback
 * @param error_callback
 * @return
 */
Strest.prototype.sendRequest = function(request, message_callback, txn_complete_callback, error_callback) {
	if (!this.connected()) {
		throw "Not yet connected!";
	}
	//set up the headers.
	if (!(request instanceof StrestRequest)) {
		request = new StrestRequest(request);
	}
	
	
	request.setHeaderIfAbsent('User-Agent', Strest.userAgent);
	request.setHeaderIfAbsent('Strest-Txn-id', Strest.txnId());
	request.setHeaderIfAbsent('Strest-Txn-Accept', 'multi');
	if (request.content) {
		request.setHeaderIfAbsent('Content-Length', request.content.length);
	}
	//register the callbacks
	this.callbacks[request.getHeader('Strest-Txn-Id')] = {
			'onmessage' : message_callback,
			'ontxncomplete' : txn_complete_callback,
			'onerror' : error_callback
	};
	
	var msg = request.toString();
	this.socket.send(msg);
	return request;
};

Strest.prototype.close = function() {
	if (this.connected()) {
		this.socket.close();
	}
};

Strest.prototype.connected = function() {
	if (!this.socket) {
		return false;
	}
	return this.socket.readyState == WebSocket.OPEN;	
};

function StrestMessage() {
	this.headers = {};
	this.content = null;
};

/**
 * Base class for both StrestRequest and StrestResponse
 * @param key
 * @param value
 * @return
 */
StrestMessage.prototype.setHeader = function(key, value) {
	this.headers[StrestMessage.normalizeHeaderName(key)] = Strest.trim(value);
};
StrestMessage.prototype.setHeaderIfAbsent = function(key, value) {
	var nm = StrestMessage.normalizeHeaderName(key);
	if (this.headers[nm]) {
		return;
	}
	this.headers[nm] = Strest.trim(value);
};

StrestMessage.prototype.getHeader = function(key) {
	return this.headers[key]
};

/**
 * Converts a name to Http-Header-Case.
 * @param name
 * @return
 */
StrestMessage.normalizeHeaderName = function(name) {
	return Strest.trim(name.replace(/\-/g, ' ').replace( /(^|\s)([a-z])/g , function(m,p1,p2){ return p1+p2.toUpperCase(); } ).replace(/\s+/g, '-'));
};


StrestRequest.prototype = new StrestMessage;
StrestRequest.prototype.constructor = StrestRequest;
/**
 * A strest request.  
 * 
 * example:
 * var request = new StrestRequest({ method : 'GET', uri : '/firehose'})
 * 
 * 
 * 
 * @param options 
 * uri
 * method : one of 'GET', 'POST', 'PUT', 'DELETE'
 * params : these will get added to the uri
 * 
 */
function StrestRequest(options) {
	StrestMessage.call(this);
	this.method = "GET";
	this.headers = {};
	this.uri = "/";
	this.content = null;
	this.params = null;
	if (options['method']) {
		this.method = options.method;
	}
	if (options['headers']) {
		this.headers = options.headers;
	}
	if (options['uri']) {
		this.uri = options.uri;
	}
	if (options['content']) {
		this.content = options.content;
	}
	if (options['params']) {
		this.params = options['params'];
	}
};
StrestRequest.prototype.setUri = function(uri) {
	this.uri = uri;
};
StrestRequest.prototype.setMethod = function(method) {
	this.method = method;
};

StrestRequest.prototype.toString = function() {
//	GET /firehose STREST/0.1
//	Strest-Txn-Accept: multi
//	Content-Length: 0
//	Strest-Txn-Id: 0
	
	//add params to the uri\
	var uri = this.uri;
	if (this.params) {
		if (uri.indexOf('?') == -1) {
			uri += '?';
		} else {
			uri += '&';
		}
		uri += Strest.urlEncodeParams(this.params);
	}
	
	var val = this.method + ' ' + uri + ' ' + Strest.protocol + '\r\n';
	
	for (key in this.headers) {
		val += key + ": " + this.headers[key] + '\r\n';
	}
	val += '\r\n';
	if (this.content) {
		val += content;
	}
	return val;
};


StrestResponse.prototype = new StrestMessage;
StrestResponse.prototype.constructor = StrestResponse;
/**
 * A strest response.  
 * 
 * @param options
 */
function StrestResponse(msg) {
	StrestMessage.call(this);
	var ind = msg.indexOf('\r\n\r\n');
	var headerStr = msg.substring(0, ind);
	var content = msg.substring(ind + 4, msg.length);
	
	//parse the headers.
//	STREST/0.1 200 OK
//	Strest-Txn-Id: 1
//	Content-Type: text/plain
//	Content-Length: 11
//	Strest-Txn-Status: complete
//
//	Hello JERK!
	var lines = headerStr.split('\r\n');
	var line1 = lines.shift().split(' ');
	this.protocol = line1.shift();
	this.code = line1.shift();
	this.message = line1.join(' ');
	
	for (var i=0; i < lines.length; i++) {
		var ln = lines[i].split(':');
		this.setHeader(ln[0], ln[1]);
	}
	
	this.content = content;
	this.originalText = msg;
};

StrestResponse.prototype.toString = function() {
	return this.originalText;
};

Strest.isString= function(obj) {
	if (!obj) {
		return false;
	}
	if (obj instanceof String) {
		return true;
	}
	if (typeof(obj) == 'string') {
		return true;
	}
	return false;
};

Strest.trim = function(str) {
	if (!Strest.isString(str)) {
		return str;
	}
//	http://forum.jquery.com/topic/faster-jquery-trimjavascript to
	str = str.replace(/^\s+/, '');
	for (var i = str.length - 1; i >= 0; i--) {
		if (/\S/.test(str.charAt(i))) {
			str = str.substring(0, i + 1);
			break;
		}
	}
	return str;
};


Strest.urlEncodeParams = function(params) {
	//urlEncode the params..
	if (!params) {
		return params;
	}
	
	var str = '';
	for (param in params) {
		str += encodeURIComponent(param) + '=' + encodeURIComponent(params[param]) + '&';
	}
	//strip the trailing &
	str = str.substring(0, str.length-1);
	return str;
};