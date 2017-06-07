siddhi-io-http
======================================
---
##### New version of Siddhi v4.0.0 is built in Java 8.
##### Latest Released Version v4.0.0-m4.

This is an extension for siddhi source and sink implentation. This extension only works inside the WSO2 Data Analityc Server and does not works in any other siddhi formats such as vanila siddhi. 

Siddhi HTTP input and output transport is mainly responsile of handling the http or https transports from and to the Data Analytic Server using http cabon trasport including following features.

Features Supported
------------------
 - http source
   - Multiple sources can be defined and recevive http messages to the DAS server.
 - https source
   -  Multiple sources can be defined and recevive https messages to the DAS server.
 - http sink 
   - Multiple sinks can be defined and publish event to the http endpoints.
 - https sink 
   - Multiple sinks can be defined and publish event to the https endpoints using custom client trusts-store.
 - basic authentication
   - component able provide basic authentication.
 - process event with order preserving
   - component capable of preserving the event order in http source
     
 #### Prerequisites for using the feature
 - Siddhi Stream should be defined
 - If user need to put custom configrations parameters he/she can put those parameters in deployment yml
 
        extension:
        name: 'http'
        namespace: 'source'
        properties:
          latency.metrics.enabled: true
          server.bootstrap.socket.timeout: 15
          client.bootstrap.socket.timeout: 15
          default.host: 0.0.0.0
          default.port: 9763
          default.scheme: http
          default.keyStoreFile: ''
          default.keyStorePass: ''
          default.certPass: ''
          https.host: 0.0.0.0
          https.port: 9763
          https.scheme: https
          https.keystore.file: ${carbon.home}/conf/security/wso2carbon.jks
          https.keyStore.pass: wso2carbon
          
        extension:
        name: 'http'
        namespace: 'sink'
        properties:
          latency.metrics.enabled: true
          server.bootstrap.socket.timeout: 15
          client.bootstrap.socket.timeout: 15
          default.host: 0.0.0.0
          default.port: 9763
          default.scheme: http
          default.keyStoreFile: ''
          default.keyStorePass: ''
          default.certPass: ''
          https.host: 0.0.0.0
          https.port: 9763
          https.scheme: https
          https.truststore.file: ${carbon.home}/conf/security/client-truststore.jks
          https.truststore.pass: wso2carbon
          https.certPass: wso2carbon
 
 #### Deploying the feature
 Feture can be deploy as a OSGI bundle by putting jar file of component to DAS_HOME/lib directory of DAS 4.0.0 pack. 
 #### Example Siddhi Queries
 ##### Event Source
     @source(type='http', @map(type='text'),
     receiver.url='http://localhost:8080/streamName', is.basic.auth.enabled='true')
     define stream inputStream (name string, age int, country string);

 ##### Event Sink
     @sink(type='http',publisher.url='http://localhost:8009', method='{{method}}',headers='{{headers}}', 
     @map(type='xml' , @payload('{{payloadBody}}')))
     define stream FooStream (method string, headers string);"

#### Documentation 

  * https://docs.wso2.com/display/DAS400/Configuring+HTTP+Event+Sinks
  * https://docs.wso2.com/display/DAS400/Configuring+HTTP+Event+Sources

## How to Contribute
* Please report issues at [Siddhi JIRA] (https://wso2.org/jira/browse/SIDDHI)
* Send your bug fixes pull requests to [master branch] (https://github.com/wso2-extensions/siddhi-io-http/tree/master) 

## Contact us 
Siddhi developers can be contacted via the mailing lists:
  * Carbon Developers List : dev@wso2.org
  * Carbon Architecture List : architecture@wso2.org

### We welcome your feedback and contribution.

Siddhi DAS Team

