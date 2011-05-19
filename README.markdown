# Welcome to Strest-Server
Strest-Server is a highly scalable STREST and HTTP server, specifically designed for creating api's

Take a look at the wiki and javadocs for more info.

### Main Features

* wicked fast
* easy to create data streams (ex firehoses)
* asynchronous 
* controllers automatically work with both HTTP and (faster)STREST
* websockets integration out of the box


### Getting started:

The best way to get started is to clone the repo and try out the examples.
<pre>
git clone git@github.com:trendrr/strest-server.git
cd strest-server
java -jar strest.jar
</pre>

Then point your browser at:

<pre>
http://localhost:8000/hello/world
</pre>

Then to see the streaming / websocket demo point your browser at:
<pre>
http://localhost:8000/static/examples/example.html
</pre>  


To learn more, please check out:

* JavaDocs -- http://trendrr.github.com/strest-server/javadoc/
* STREST Protocol Spec -- https://github.com/trendrr/strest-server/wiki/STREST-Protocol-Spec
* Strest Server introduction -- https://github.com/trendrr/strest-server/wiki/Strest-Server-introduction
* Serverside Examples -- https://github.com/trendrr/strest-server/tree/master/src/com/trendrr/strest/examples
* Websocket Example -- https://github.com/trendrr/strest-server/wiki/Websocket-Example
* Client Drivers -- https://github.com/trendrr/strest-server/wiki/Strest-Drivers
