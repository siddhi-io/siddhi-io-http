siddhi-io-http
======================================

The **siddhi-io-http extension** is an extension to <a target="_blank" href="https://wso2.github.io/siddhi">Siddhi</a> that allows you to receive and publish events via http and https transports and 
also allow you perform synchronous request and response. This extension works with WSO2 Stream Processor and with standalone Siddhi.

Find some useful links below:

* <a target="_blank" href="https://github.com/wso2-extensions/siddhi-io-http">Source code</a>
* <a target="_blank" href="https://github.com/wso2-extensions/siddhi-io-http/releases">Releases</a>
* <a target="_blank" href="https://github.com/wso2-extensions/siddhi-io-http/issues">Issue tracker</a>

## Latest API Docs 

Latest API Docs is <a target="_blank" href="https://wso2-extensions.github.io/siddhi-io-http/api/1.0.42">1.0.42</a>.

## How to use 

**Using the extension in <a target="_blank" href="https://github.com/wso2/product-sp">WSO2 Stream Processor</a>**

* You can use this extension with the latest <a target="_blank" href="https://github.com/wso2/product-sp/releases">WSO2 Stream Processor</a> that is a part of the <a target="_blank" href="http://wso2.com/analytics?utm_source=gitanalytics&utm_campaign=gitanalytics_Jul17">WSO2 Analytics</a> offering, with editor, debugger and simulation support. 

* This extension is shipped with WSO2 Stream Processor by default. If you need to use an alternative version of this extension you can replace the component <a target="_blank" href="https://github.com/wso2-extensions/siddhi-io-http/releases">jar</a> that can be found in the `<STREAM_PROCESSOR_HOME>/lib` directory with the component jar of the relevant version.

**Using the extension as a <a target="_blank" href="https://wso2.github.io/siddhi/documentation/running-as-a-java-library">java library</a>**

* This extension can be added as a maven dependency to your project together with other Siddhi dependencies.

```
     <dependency>
        <groupId>org.wso2.extension.siddhi.io.http</groupId>
        <artifactId>siddhi-io-http</artifactId>
        <version>x.x.x</version>
     </dependency>
```

## Jenkins Build Status

---

|  Branch | Build Status |
| :------ |:------------ | 
| master  | [![Build Status](https://wso2.org/jenkins/view/All%20Builds/job/siddhi/job/siddhi-io-http/badge/icon)](https://wso2.org/jenkins/view/All%20Builds/job/siddhi/job/siddhi-io-http/)|

---

## Features

* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-io-http/api/1.0.42/#http-sink">http</a> *<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#sink">(Sink)</a>*<br><div style="padding-left: 1em;"><p>This extension publish the HTTP events in any HTTP method  POST, GET, PUT, DELETE  via HTTP or https protocols. As the additional features this component can provide basic authentication as well as user can publish events using custom client truststore files when publishing events via https protocol. And also user can add any number of headers including HTTP_METHOD header for each event dynamically.<br>Following content types will be set by default according to the type of sink mapper used.<br>You can override them by setting the new content types in headers.<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- TEXT : text/plain<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- XML : application/xml<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- JSON : application/json<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- KEYVALUE : application/x-www-form-urlencoded</p></div>
* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-io-http/api/1.0.42/#http-request-sink">http-request</a> *<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#sink">(Sink)</a>*<br><div style="padding-left: 1em;"><p>This extension publish the HTTP events in any HTTP method  POST, GET, PUT, DELETE  via HTTP or https protocols. As the additional features this component can provide basic authentication as well as user can publish events using custom client truststore files when publishing events via https protocol. And also user can add any number of headers including HTTP_METHOD header for each event dynamically.<br>Following content types will be set by default according to the type of sink mapper used.<br>You can override them by setting the new content types in headers.<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- TEXT : text/plain<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- XML : application/xml<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- JSON : application/json<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- KEYVALUE : application/x-www-form-urlencoded<br><br>HTTP request sink is correlated with the The HTTP reponse source, through a unique <code>sink.id</code>.It sends the request to the defined url and the response is received by the response source which has the same 'sink.id'.</p></div>
* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-io-http/api/1.0.42/#http-response-sink">http-response</a> *<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#sink">(Sink)</a>*<br><div style="padding-left: 1em;"><p>HTTP response sink is correlated with the The HTTP request source, through a unique <code>source.id</code>, and it send a response to the HTTP request source having the same <code>source.id</code>. The response message can be formatted in <code>text</code>, <code>XML</code> or <code>JSON</code> and can be sent with appropriate headers.</p></div>
* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-io-http/api/1.0.42/#http-source">http</a> *<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#source">(Source)</a>*<br><div style="padding-left: 1em;"><p>The HTTP source receives POST requests via HTTP or HTTPS in format such as <code>text</code>, <code>XML</code> and <code>JSON</code>. In WSO2 SP, if required, you can enable basic authentication to ensure that events are received only from users who are authorized to access the service.</p></div>
* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-io-http/api/1.0.42/#http-request-source">http-request</a> *<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#source">(Source)</a>*<br><div style="padding-left: 1em;"><p>The HTTP request is correlated with the HTTP response sink, through a unique <code>source.id</code>, and for each POST requests it receives via HTTP or HTTPS in format such as <code>text</code>, <code>XML</code> and <code>JSON</code> it sends the response via the HTTP response sink. The individual request and response messages are correlated at the sink using the <code>message.id</code> of the events. If required, you can enable basic authentication at the source to ensure that events are received only from users who are authorized to access the service.</p></div>
* <a target="_blank" href="https://wso2-extensions.github.io/siddhi-io-http/api/1.0.42/#http-response-source">http-response</a> *<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#source">(Source)</a>*<br><div style="padding-left: 1em;"><p>The http-response source co-relates with http-request sink  with the parameter 'sink.id'.<br>This receives responses for the requests sent by the http-request sink which has the same sink id.<br>Response messages can be in formats such as TEXT, JSON and XML.<br>In order to handle the responses with different http status codes, user is allowed to defined the acceptable response source code using the parameter 'http.status.code'<br></p></div>

## How to Contribute
 
  * Report issues at <a target="_blank" href="https://github.com/wso2-extensions/siddhi-io-http/issues">GitHub Issue Tracker</a>.
  
  * Send your contributions as pull requests to the <a target="_blank" href="https://github.com/wso2-extensions/siddhi-io-http/tree/master">master branch</a>. 
 
## Contact us 

 * Post your questions with the <a target="_blank" href="http://stackoverflow.com/search?q=siddhi">"Siddhi"</a> tag in <a target="_blank" href="http://stackoverflow.com/search?q=siddhi">Stackoverflow</a>. 
 
 * Siddhi developers can be contacted via the following mailing lists:
 
    Developers List   : [dev@wso2.org](mailto:dev@wso2.org)
    
    Architecture List : [architecture@wso2.org](mailto:architecture@wso2.org)
 
## Support 

* We are committed to provide support for this extension in production. Our unique approach ensures that all support 
leverages our open development methodology, and is provided by the very same engineers who build the technology. 

* For more details and to take advantage of this unique opportunity contact us via <a target="_blank" href="http://wso2
.com/support?utm_source=gitanalytics&utm_campaign=gitanalytics_Jul17">http://wso2.com/support/</a>. 

