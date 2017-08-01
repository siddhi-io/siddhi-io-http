siddhi-io-http
======================================
---
|  Branch | Build Status |
| :------ |:------------ | 
| master  | [![Build Status](https://wso2.org/jenkins/view/All%20Builds/job/siddhi/job/siddhi-io-http/badge/icon)](https://wso2.org/jenkins/view/All%20Builds/job/siddhi/job/siddhi-io-http/) |
---
##### New version of Siddhi v4.0.0 is built in Java 8.

This is a http input and output extension for siddhi source and sink implementation. This extension only works inside 
the WSO2 Data Analytic Server and cannot be run with standalone siddhi.
This component allow us to receive and publish events through http and https transports.

Features Supported
------------------
 - http source
   -- Multiple sources can be defined and receive http messages to the DAS server.
 - https source
   -- Multiple sources can be defined and receive https messages to the DAS server.
 - http sink 
   -- Multiple sinks can be defined and publish event to the http endpoints.
 - https sink 
   -- Multiple sinks can be defined and publish event to the https endpoints using custom client trusts-store.
 - basic authentication
   - component able provide basic authentication.
 - process event with order preserving
   -- component capable of preserving the event order in http source
     
Prerequisites for using the feature
------------------
 - Siddhi Stream should be defined
 - If user need to put custom configurations parameters he/she can put those parameters in deployment yml
 
  siddhi:
  
     extensions:
     
       -extension:
        name: 'http'
        namespace: 'source'
        properties:
          latency.metrics.enabled: true
          server.bootstrap.socket.timeout: 15
          client.bootstrap.socket.timeout: 15
          server.bootstrap.worker.group.size: 8
          server.bootstrap.boss.group.size: 4
          default.host: 0.0.0.0
          default.port: 9763
          default.scheme: http
          default.keyStoreFile: ''
          default.keyStorePass: ''
          default.certPass: ''
          https.host: 0.0.0.0
          https.port: 9763
          https.scheme: https
          https.keystore.file: ${carbon.home}/resources/security/wso2carbon.jks
          https.keyStore.pass: wso2carbon
          
       -extension:
        name: 'http'
        namespace: 'sink'
        properties:
          latency.metrics.enabled: true
          server.bootstrap.socket.timeout: 15
          client.bootstrap.socket.timeout: 15
          server.bootstrap.worker.group.size: 8
          server.bootstrap.boss.group.size: 4
          default.host: 0.0.0.0
          default.port: 9763
          default.scheme: http
          default.key.store.file: ''
          default.key.store.pass: ''
          default.cert.pass: ''
          https.host: 0.0.0.0
          https.port: 9763
          https.scheme: https
          https.truststore.file: ${carbon.home}/resources/security/client-truststore.jks
          https.truststore.pass: wso2carbon
          https.certPass: wso2carbon
 
Deploying the feature
------------------
 Feature can be deploy as a OSGI bundle by putting jar file of component to DAS_HOME/lib directory of DAS 4.0.0 pack. 
 
Example Siddhi Queries
------------------ 
#### Event Source
 
     @source(type='http', @map(type='text'),
     receiver.url='http://localhost:8080/streamName', is.basic.auth.enabled='true')
     define stream inputStream (name string, age int, country string);

#### Event Sink
 
     @sink(type='http',publisher.url='http://localhost:8009', method='{{method}}',headers='{{headers}}', 
     @map(type='xml' , @payload('{{payloadBody}}')))
     define stream FooStream (method string, headers string);"

Documentation 
------------------
  * https://docs.wso2.com/display/DAS400/Configuring+HTTP+Event+Sinks
  * https://docs.wso2.com/display/DAS400/Configuring+HTTP+Event+Sources

How to Contribute
------------------
* Send your bug fixes pull requests to [master branch] (https://github.com/wso2-extensions/siddhi-io-http/tree/master) 

Contact us 
----------
Siddhi developers can be contacted via the mailing lists:
  * Carbon Developers List : dev@wso2.org
  * Carbon Architecture List : architecture@wso2.org

We welcome your feedback and contribution.
------------------
WSO2 Smart Analytics Team.

## API Docs:

1. <a href="./api/1.0.1-SNAPSHOT">1.0.1-SNAPSHOT</a>
