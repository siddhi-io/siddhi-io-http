# API Docs - v2.3.7

!!! Info "Tested Siddhi Core version: *<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/">5.1.23</a>*"
    It could also support other Siddhi Core minor versions.

## Sink

### http *<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#sink">(Sink)</a>*
<p></p>
<p style="word-wrap: break-word;margin: 0;">HTTP sink publishes messages via HTTP or HTTPS protocols using methods such as POST, GET, PUT, and DELETE on formats <code>text</code>, <code>XML</code> and <code>JSON</code>. It can also publish to endpoints protected by basic authentication or OAuth 2.0.</p>
<p></p>
<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>

```
@sink(type="http", publisher.url="<STRING>", basic.auth.username="<STRING>", basic.auth.password="<STRING>", https.truststore.file="<STRING>", https.truststore.password="<STRING>", https.keystore.file="<STRING>", https.keystore.password="<STRING>", https.keystore.key.password="<STRING>", oauth.username="<STRING>", oauth.password="<STRING>", consumer.key="<STRING>", consumer.secret="<STRING>", body.consumer.key="<STRING>", body.consumer.secret="<STRING>", token.url="<STRING>", refresh.token="<STRING>", oauth.scope="<STRING>", headers="<STRING>", method="<STRING>", socket.idle.timeout="<INT>", chunk.disabled="<BOOL>", ssl.protocol="<STRING>", ssl.verification.disabled="<BOOL>", tls.store.type="<STRING>", ssl.configurations="<STRING>", proxy.host="<STRING>", proxy.port="<STRING>", proxy.username="<STRING>", proxy.password="<STRING>", client.bootstrap.configurations="<STRING>", max.pool.active.connections="<INT>", min.pool.idle.connections="<INT>", max.pool.idle.connections="<INT>", executor.service.threads="<INT>", min.evictable.idle.time="<STRING>", time.between.eviction.runs="<STRING>", max.wait.time="<STRING>", test.on.borrow="<BOOL>", test.while.idle="<BOOL>", exhausted.action="<INT>", hostname.verification.enabled="<BOOL>", @map(...)))
```

<span id="query-parameters" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">QUERY PARAMETERS</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <tr>
        <td style="vertical-align: top">publisher.url</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The URL to which the outgoing events should be published.<br>Examples:<br><code>http://localhost:8080/endpoint</code>,<br><code>https://localhost:8080/endpoint</code></p></td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">basic.auth.username</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The username to be included in the authentication header when calling endpoints protected by basic authentication. <code>basic.auth.password</code> property should be also set when using this property.</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">basic.auth.password</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The password to be included in the authentication header when calling endpoints protected by basic authentication. <code>basic.auth.username</code> property should be also set when using this property.</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">https.truststore.file</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The file path of the client truststore when sending messages through <code>https</code> protocol.</p></td>
        <td style="vertical-align: top">`${carbon.home}/resources/security/client-truststore.jks`</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">https.truststore.password</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The password for the client-truststore.</p></td>
        <td style="vertical-align: top">wso2carbon</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">https.keystore.file</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The file path of the keystore when sending messages through <code>https</code> protocol.</p></td>
        <td style="vertical-align: top">`${carbon.home}/resources/security/wso2carbon.jks`</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">https.keystore.password</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The password for the keystore.</p></td>
        <td style="vertical-align: top">wso2carbon</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">https.keystore.key.password</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The password for the keys in the path of the keystore when sending messages through <code>https</code> protocol.</p></td>
        <td style="vertical-align: top">wso2carbon</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">oauth.username</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The username to be included in the authentication header when calling endpoints protected by OAuth 2.0. <code>oauth.password</code> property should be also set when using this property.</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">oauth.password</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The password to be included in the authentication header when calling endpoints protected by OAuth 2.0. <code>oauth.username</code> property should be also set when using this property.</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">consumer.key</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Consumer key used for calling endpoints protected by OAuth 2.0</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">consumer.secret</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Consumer secret used for calling endpoints protected by OAuth 2.0</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">body.consumer.key</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Consumer key used for calling endpoints protected by OAuth 2.0 if it's required to be sent in token request body</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">body.consumer.secret</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Consumer secret used for calling endpoints protected by OAuth 2.0</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">token.url</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Token URL to generate a new access tokens when calling endpoints protected by OAuth 2.0</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">refresh.token</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Refresh token used for generating new access tokens when calling endpoints protected by OAuth 2.0</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">oauth.scope</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Standard OAuth 2.0 scope parameter</p></td>
        <td style="vertical-align: top">default</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">headers</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">HTTP request headers in format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>When <code>Content-Type</code> header is not provided the system derives the Content-Type based on the provided sink mapper as following: <br>&nbsp;- <code>@map(type='xml')</code>: <code>application/xml</code><br>&nbsp;- <code>@map(type='json')</code>: <code>application/json</code><br>&nbsp;- <code>@map(type='text')</code>: <code>plain/text</code><br>&nbsp;- <code>@map(type='keyvalue')</code>: <code>application/x-www-form-urlencoded</code><br>&nbsp;- For all other cases system defaults to <code>plain/text</code><br>Also the <code>Content-Length</code> header need not to be provided, as the system automatically defines it by calculating the size of the payload.</p></td>
        <td style="vertical-align: top">Content-Type and Content-Length headers</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">method</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The HTTP method used for calling the endpoint.</p></td>
        <td style="vertical-align: top">POST</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">socket.idle.timeout</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Socket timeout in millis.</p></td>
        <td style="vertical-align: top">6000</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">chunk.disabled</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Disable chunked transfer encoding.</p></td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">ssl.protocol</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">SSL/TLS protocol.</p></td>
        <td style="vertical-align: top">TLS</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">ssl.verification.disabled</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Disable SSL verification.</p></td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">tls.store.type</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">TLS store type.</p></td>
        <td style="vertical-align: top">JKS</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">ssl.configurations</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">SSL/TSL configurations in format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>Some supported parameters:<br>&nbsp;- SSL/TLS protocols: <code>'sslEnabledProtocols:TLSv1.1,TLSv1.2'</code><br>&nbsp;- List of ciphers: <code>'ciphers:TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256'</code><br>&nbsp;- Enable session creation: <code>'client.enable.session.creation:true'</code><br>&nbsp;- Supported server names: <code>'server.suported.server.names:server'</code><br>&nbsp;- Add HTTP SNIMatcher: <code>'server.supported.snimatchers:SNIMatcher'</code></p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">proxy.host</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Proxy server host</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">proxy.port</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Proxy server port</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">proxy.username</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Proxy server username</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">proxy.password</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Proxy server password</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">client.bootstrap.configurations</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Client bootstrap configurations in format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>Some supported configurations :<br>&nbsp;- Client connect timeout in millis: <code>'client.bootstrap.connect.timeout:15000'</code><br>&nbsp;- Client socket timeout in seconds: <code>'client.bootstrap.socket.timeout:15'</code><br>&nbsp;- Client socket reuse: <code>'client.bootstrap.socket.reuse:true'</code><br>&nbsp;- Enable TCP no delay: <code>'client.bootstrap.nodelay:true'</code><br>&nbsp;- Enable client keep alive: <code>'client.bootstrap.keepalive:true'</code><br>&nbsp;- Send buffer size: <code>'client.bootstrap.sendbuffersize:1048576'</code><br>&nbsp;- Receive buffer size: <code>'client.bootstrap.recievebuffersize:1048576'</code></p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.pool.active.connections</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Maximum possible number of active connection per client pool.</p></td>
        <td style="vertical-align: top">-1</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">min.pool.idle.connections</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Minimum number of idle connections that can exist per client pool.</p></td>
        <td style="vertical-align: top">0</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.pool.idle.connections</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Maximum number of idle connections that can exist per client pool.</p></td>
        <td style="vertical-align: top">100</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">executor.service.threads</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Thread count for the executor service.</p></td>
        <td style="vertical-align: top">20</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">min.evictable.idle.time</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Minimum time (in millis) a connection may sit idle in the client pool before it become eligible for eviction.</p></td>
        <td style="vertical-align: top">300000</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">time.between.eviction.runs</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Time between two eviction operations (in millis) on the client pool.</p></td>
        <td style="vertical-align: top">30000</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.wait.time</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The maximum time (in millis) the pool will wait (when there are no available connections) for a connection to be returned to the pool.</p></td>
        <td style="vertical-align: top">60000</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">test.on.borrow</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Enable connections to be validated before being borrowed from the client pool.</p></td>
        <td style="vertical-align: top">true</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">test.while.idle</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Enable connections to be validated during the eviction operation (if any).</p></td>
        <td style="vertical-align: top">true</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">exhausted.action</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Action that should be taken when the maximum number of active connections are being used. This action should be indicated as an int and possible action values are following.<br>0 - Fail the request.<br>1 - Block the request, until a connection returns to the pool.<br>2 - Grow the connection pool size.</p></td>
        <td style="vertical-align: top">1 (Block when exhausted)</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">hostname.verification.enabled</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Enable hostname verification.</p></td>
        <td style="vertical-align: top">true</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="system-parameters" class="md-typeset" style="display: block; font-weight: bold;">System Parameters</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Parameters</th>
    </tr>
    <tr>
        <td style="vertical-align: top">clientBootstrapClientGroupSize</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">Number of client threads to perform non-blocking read and write to one or more channels.</p></td>
        <td style="vertical-align: top">(Number of available processors) * 2</td>
        <td style="vertical-align: top">Any positive integer</td>
    </tr>
    <tr>
        <td style="vertical-align: top">clientBootstrapBossGroupSize</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">Number of boss threads to accept incoming connections.</p></td>
        <td style="vertical-align: top">Number of available processors</td>
        <td style="vertical-align: top">Any positive integer</td>
    </tr>
    <tr>
        <td style="vertical-align: top">clientBootstrapWorkerGroupSize</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">Number of worker threads to accept the connections from boss threads and perform non-blocking read and write from one or more channels.</p></td>
        <td style="vertical-align: top">(Number of available processors) * 2</td>
        <td style="vertical-align: top">Any positive integer</td>
    </tr>
    <tr>
        <td style="vertical-align: top">trustStoreLocation</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default truststore file path.</p></td>
        <td style="vertical-align: top">`${carbon.home}/resources/security/client-truststore.jks`</td>
        <td style="vertical-align: top">Path to client truststore `.jks` file</td>
    </tr>
    <tr>
        <td style="vertical-align: top">trustStorePassword</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default truststore password.</p></td>
        <td style="vertical-align: top">wso2carbon</td>
        <td style="vertical-align: top">Truststore password as string</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keyStoreLocation</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default keystore file path.</p></td>
        <td style="vertical-align: top">${carbon.home}/resources/security/wso2carbon.jks</td>
        <td style="vertical-align: top">Path to client keystore `.jks` file</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keyStorePassword</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default keystore password.</p></td>
        <td style="vertical-align: top">wso2carbon</td>
        <td style="vertical-align: top">Keystore password as string</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keyPassword</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default keystore key password.</p></td>
        <td style="vertical-align: top">wso2carbon</td>
        <td style="vertical-align: top">Keystore key password as string</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@sink(type = 'http', publisher.url = 'http://stocks.com/stocks',
      @map(type = 'json'))
define stream StockStream (symbol string, price float, volume long);
```
<p></p>
<p style="word-wrap: break-word;margin: 0;">Events arriving on the StockStream will be published to the HTTP endpoint <code>http://stocks.com/stocks</code> using <code>POST</code> method with Content-Type <code>application/json</code> by converting those events to the default JSON format as following:<br></p><pre>{
  "event": {
    "symbol": "FB",
    "price": 24.5,
    "volume": 5000
  }
}</pre><p style="word-wrap: break-word;margin: 0;"></p>
<p></p>
<span id="example-2" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 2</span>
```
@sink(type='http', publisher.url = 'http://localhost:8009/foo',
      client.bootstrap.configurations = "'client.bootstrap.socket.timeout:20'",
      max.pool.active.connections = '1', headers = "{{headers}}",
      @map(type='xml', @payload("""<stock>
{{payloadBody}}
</stock>""")))
define stream FooStream (payloadBody String, headers string);
```
<p></p>
<p style="word-wrap: break-word;margin: 0;">Events arriving on FooStream will be published to the HTTP endpoint <code>http://localhost:8009/foo</code> using <code>POST</code> method with Content-Type <code>application/xml</code> and setting <code>payloadBody</code> and <code>header</code> attribute values.<br>If the <code>payloadBody</code> contains<br></p><pre>&lt;symbol&gt;WSO2&lt;/symbol&gt;
&lt;price&gt;55.6&lt;/price&gt;
&lt;volume&gt;100&lt;/volume&gt;</pre><p style="word-wrap: break-word;margin: 0;">and <code>header</code> contains <code>'topic:foobar'</code> values, then the system will generate an output with the body:<br></p><pre>&lt;stock&gt;
&lt;symbol&gt;WSO2&lt;/symbol&gt;
&lt;price&gt;55.6&lt;/price&gt;
&lt;volume&gt;100&lt;/volume&gt;
&lt;/stock&gt;</pre><p style="word-wrap: break-word;margin: 0;">and HTTP headers:<br><code>Content-Length:xxx</code>,<br><code>Content-Location:'xxx'</code>,<br><code>Content-Type:'application/xml'</code>,<br><code>HTTP_METHOD:'POST'</code></p>
<p></p>
### http-call *<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#sink">(Sink)</a>*
<p></p>
<p style="word-wrap: break-word;margin: 0;">The http-call sink publishes messages to endpoints via HTTP or HTTPS protocols using methods such as POST, GET, PUT, and DELETE on formats <code>text</code>, <code>XML</code> or <code>JSON</code> and consume responses through its corresponding http-call-response source. It also supports calling endpoints protected with basic authentication or OAuth 2.0.</p>
<p></p>
<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>

```
@sink(type="http-call", publisher.url="<STRING>", sink.id="<STRING>", basic.auth.username="<STRING>", basic.auth.password="<STRING>", https.truststore.file="<STRING>", https.truststore.password="<STRING>", oauth.username="<STRING>", oauth.password="<STRING>", consumer.key="<STRING>", consumer.secret="<STRING>", token.url="<STRING>", refresh.token="<STRING>", headers="<STRING>", method="<STRING>", downloading.enabled="<BOOL>", download.path="<STRING>", blocking.io="<BOOL>", socket.idle.timeout="<INT>", chunk.disabled="<BOOL>", ssl.protocol="<STRING>", ssl.verification.disabled="<BOOL>", ssl.configurations="<STRING>", proxy.host="<STRING>", proxy.port="<STRING>", proxy.username="<STRING>", proxy.password="<STRING>", client.bootstrap.configurations="<STRING>", max.pool.active.connections="<INT>", min.pool.idle.connections="<INT>", max.pool.idle.connections="<INT>", min.evictable.idle.time="<STRING>", time.between.eviction.runs="<STRING>", max.wait.time="<STRING>", test.on.borrow="<BOOL>", test.while.idle="<BOOL>", exhausted.action="<INT>", hostname.verification.enabled="<BOOL>", @map(...)))
```

<span id="query-parameters" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">QUERY PARAMETERS</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <tr>
        <td style="vertical-align: top">publisher.url</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The URL which should be called.<br>Examples:<br><code>http://localhost:8080/endpoint</code>,<br><code>https://localhost:8080/endpoint</code></p></td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">sink.id</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Identifier to correlate the http-call sink to its corresponding http-call-response sources to retrieved the responses.</p></td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">basic.auth.username</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The username to be included in the authentication header when calling endpoints protected by basic authentication. <code>basic.auth.password</code> property should be also set when using this property.</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">basic.auth.password</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The password to be included in the authentication header when calling endpoints protected by basic authentication. <code>basic.auth.username</code> property should be also set when using this property.</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">https.truststore.file</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The file path of the client truststore when sending messages through <code>https</code> protocol.</p></td>
        <td style="vertical-align: top">`${carbon.home}/resources/security/client-truststore.jks`</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">https.truststore.password</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The password for the client-truststore.</p></td>
        <td style="vertical-align: top">wso2carbon</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">oauth.username</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The username to be included in the authentication header when calling endpoints protected by OAuth 2.0. <code>oauth.password</code> property should be also set when using this property.</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">oauth.password</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The password to be included in the authentication header when calling endpoints protected by OAuth 2.0. <code>oauth.username</code> property should be also set when using this property.</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">consumer.key</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Consumer key used for calling endpoints protected by OAuth 2.0</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">consumer.secret</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Consumer secret used for calling endpoints protected by OAuth 2.0</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">token.url</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Token URL to generate a new access tokens when calling endpoints protected by OAuth 2.0</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">refresh.token</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Refresh token used for generating new access tokens when calling endpoints protected by OAuth 2.0</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">headers</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">HTTP request headers in format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>When the <code>Content-Type</code> header is not provided the system decides the Content-Type based on the provided sink mapper as following: <br>&nbsp;- <code>@map(type='xml')</code>: <code>application/xml</code><br>&nbsp;- <code>@map(type='json')</code>: <code>application/json</code><br>&nbsp;- <code>@map(type='text')</code>: <code>plain/text</code><br>&nbsp;- <code>@map(type='keyvalue')</code>: <code>application/x-www-form-urlencoded</code><br>&nbsp;- For all other cases system defaults to <code>plain/text</code><br>Also the <code>Content-Length</code> header need not to be provided, as the system automatically defines it by calculating the size of the payload.</p></td>
        <td style="vertical-align: top">Content-Type and Content-Length headers</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">method</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The HTTP method used for calling the endpoint.</p></td>
        <td style="vertical-align: top">POST</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">downloading.enabled</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Enable response received by the http-call-response source to be written to a file. When this is enabled the <code>download.path</code> property should be also set.</p></td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">download.path</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The absolute file path along with the file name where the downloads should be saved.</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">Yes</td>
    </tr>
    <tr>
        <td style="vertical-align: top">blocking.io</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Blocks the request thread until a response it received from HTTP call-response source before sending any other request.</p></td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">socket.idle.timeout</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Socket timeout in millis.</p></td>
        <td style="vertical-align: top">6000</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">chunk.disabled</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Disable chunked transfer encoding.</p></td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">ssl.protocol</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">SSL/TLS protocol.</p></td>
        <td style="vertical-align: top">TLS</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">ssl.verification.disabled</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Disable SSL verification.</p></td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">ssl.configurations</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">SSL/TSL configurations.<br>Expected format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>Some supported parameters:<br>&nbsp;- SSL/TLS protocols: <code>'sslEnabledProtocols:TLSv1.1,TLSv1.2'</code><br>&nbsp;- List of ciphers: <code>'ciphers:TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256'</code><br>&nbsp;- Enable session creation: <code>'client.enable.session.creation:true'</code><br>&nbsp;- Supported server names: <code>'server.suported.server.names:server'</code><br>&nbsp;- Add HTTP SNIMatcher: <code>'server.supported.snimatchers:SNIMatcher'</code></p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">proxy.host</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Proxy server host</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">proxy.port</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Proxy server port</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">proxy.username</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Proxy server username</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">proxy.password</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Proxy server password</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">client.bootstrap.configurations</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Client bootstrap configurations in format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>Some supported configurations :<br>&nbsp;- Client connect timeout in millis: <code>'client.bootstrap.connect.timeout:15000'</code><br>&nbsp;- Client socket timeout in seconds: <code>'client.bootstrap.socket.timeout:15'</code><br>&nbsp;- Client socket reuse: <code>'client.bootstrap.socket.reuse:true'</code><br>&nbsp;- Enable TCP no delay: <code>'client.bootstrap.nodelay:true'</code><br>&nbsp;- Enable client keep alive: <code>'client.bootstrap.keepalive:true'</code><br>&nbsp;- Send buffer size: <code>'client.bootstrap.sendbuffersize:1048576'</code><br>&nbsp;- Receive buffer size: <code>'client.bootstrap.recievebuffersize:1048576'</code></p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.pool.active.connections</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Maximum possible number of active connection per client pool.</p></td>
        <td style="vertical-align: top">-1</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">min.pool.idle.connections</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Minimum number of idle connections that can exist per client pool.</p></td>
        <td style="vertical-align: top">0</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.pool.idle.connections</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Maximum number of idle connections that can exist per client pool.</p></td>
        <td style="vertical-align: top">100</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">min.evictable.idle.time</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Minimum time (in millis) a connection may sit idle in the client pool before it become eligible for eviction.</p></td>
        <td style="vertical-align: top">300000</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">time.between.eviction.runs</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Time between two eviction operations (in millis) on the client pool.</p></td>
        <td style="vertical-align: top">30000</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.wait.time</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The maximum time (in millis) the pool will wait (when there are no available connections) for a connection to be returned to the pool.</p></td>
        <td style="vertical-align: top">60000</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">test.on.borrow</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Enable connections to be validated before being borrowed from the client pool.</p></td>
        <td style="vertical-align: top">true</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">test.while.idle</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Enable connections to be validated during the eviction operation (if any).</p></td>
        <td style="vertical-align: top">true</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">exhausted.action</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Action that should be taken when the maximum number of active connections are being used. This action should be indicated as an int and possible action values are following.<br>0 - Fail the request.<br>1 - Block the request, until a connection returns to the pool.<br>2 - Grow the connection pool size.</p></td>
        <td style="vertical-align: top">1 (Block when exhausted)</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">hostname.verification.enabled</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Enable hostname verification</p></td>
        <td style="vertical-align: top">true</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="system-parameters" class="md-typeset" style="display: block; font-weight: bold;">System Parameters</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Parameters</th>
    </tr>
    <tr>
        <td style="vertical-align: top">clientBootstrapClientGroupSize</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">Number of client threads to perform non-blocking read and write to one or more channels.</p></td>
        <td style="vertical-align: top">(Number of available processors) * 2</td>
        <td style="vertical-align: top">Any positive integer</td>
    </tr>
    <tr>
        <td style="vertical-align: top">clientBootstrapBossGroupSize</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">Number of boss threads to accept incoming connections.</p></td>
        <td style="vertical-align: top">Number of available processors</td>
        <td style="vertical-align: top">Any positive integer</td>
    </tr>
    <tr>
        <td style="vertical-align: top">clientBootstrapWorkerGroupSize</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">Number of worker threads to accept the connections from boss threads and perform non-blocking read and write from one or more channels.</p></td>
        <td style="vertical-align: top">(Number of available processors) * 2</td>
        <td style="vertical-align: top">Any positive integer</td>
    </tr>
    <tr>
        <td style="vertical-align: top">trustStoreLocation</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default truststore file path.</p></td>
        <td style="vertical-align: top">`${carbon.home}/resources/security/client-truststore.jks`</td>
        <td style="vertical-align: top">Path to client truststore `.jks` file</td>
    </tr>
    <tr>
        <td style="vertical-align: top">trustStorePassword</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default truststore password.</p></td>
        <td style="vertical-align: top">wso2carbon</td>
        <td style="vertical-align: top">Truststore password as string</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@sink(type='http-call', sink.id='foo',
      publisher.url='http://localhost:8009/foo',
      @map(type='xml', @payload('{{payloadBody}}')))
define stream FooStream (payloadBody string);

@source(type='http-call-response', sink.id='foo',
        @map(type='text', regex.A='((.|\n)*)',
             @attributes(headers='trp:headers', message='A[1]')))
define stream ResponseStream(message string, headers string);
```
<p></p>
<p style="word-wrap: break-word;margin: 0;">When events arrive in <code>FooStream</code>, http-call sink makes calls to endpoint on url <code>http://localhost:8009/foo</code> with <code>POST</code> method and Content-Type <code>application/xml</code>.<br>If the event <code>payloadBody</code> attribute contains following XML:<br></p><pre>&lt;item&gt;
    &lt;name&gt;apple&lt;/name&gt;
    &lt;price&gt;55&lt;/price&gt;
    &lt;quantity&gt;5&lt;/quantity&gt;
&lt;/item&gt;</pre><p style="word-wrap: break-word;margin: 0;">the http-call sink maps that and sends it to the endpoint.<br>When endpoint sends a response it will be consumed by the corresponding http-call-response source correlated via the same <code>sink.id</code> <code>foo</code> and that will map the response message and send it via <code>ResponseStream</code> steam by assigning the message body as <code>message</code> attribute and response headers as <code>headers</code> attribute of the event.</p>
<p></p>
<span id="example-2" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 2</span>
```
@sink(type='http-call', publisher.url='http://localhost:8005/files/{{name}}'
      downloading.enabled='true', download.path='{{downloadPath}}{{name}}',
      method='GET', sink.id='download', @map(type='json'))
define stream DownloadRequestStream(name String, id int, downloadPath string);

@source(type='http-call-response', sink.id='download',
        http.status.code='2\\d+',
        @map(type='text', regex.A='((.|\n)*)',
             @attributes(name='trp:name', id='trp:id', file='A[1]')))
define stream ResponseStream2xx(name string, id string, file string);

@source(type='http-call-response', sink.id='download',
        http.status.code='4\\d+',
        @map(type='text', regex.A='((.|\n)*)', @attributes(errorMsg='A[1]')))
define stream ResponseStream4xx(errorMsg string);
```
<p></p>
<p style="word-wrap: break-word;margin: 0;">When events arrive in <code>DownloadRequestStream</code> with <code>name</code>:<code>foo.txt</code>, <code>id</code>:<code>75</code> and <code>downloadPath</code>:<code>/user/download/</code> the http-call sink sends a GET request to the url <code>http://localhost:8005/files/foo.txt</code> to download the file to the given path <code>/user/download/foo.txt</code> and capture the response via its corresponding http-call-response source based on the response status code.<br>If the response status code is in the range of 200 the message will be received by the http-call-response source associated with the <code>ResponseStream2xx</code> stream which expects <code>http.status.code</code> with regex <code>2\\d+</code> while downloading the file to the local file system on the path <code>/user/download/foo.txt</code> and mapping the response message having the absolute file path to event's <code>file</code> attribute.<br>If the response status code is in the range of 400 then the message will be received by the http-call-response source associated with the <code>ResponseStream4xx</code> stream which expects <code>http.status.code</code> with regex <code>4\\d+</code> while mapping the error response to the <code>errorMsg</code> attribute of the event.</p>
<p></p>
### <s>http-request *<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#sink">(Sink)</a>*</s>
<p><i>Deprecated</i></p>
<p></p>
<p style="word-wrap: break-word;margin: 0;">_(Use http-call sink instead)._<br>The http-request sink publishes messages to endpoints via HTTP or HTTPS protocols using methods such as POST, GET, PUT, and DELETE on formats <code>text</code>, <code>XML</code> or <code>JSON</code> and consume responses through its corresponding http-response source. It also supports calling endpoints protected with basic authentication or OAuth 2.0.</p>
<p></p>
<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>

```
@sink(type="http-request", publisher.url="<STRING>", sink.id="<STRING>", basic.auth.username="<STRING>", basic.auth.password="<STRING>", https.truststore.file="<STRING>", https.truststore.password="<STRING>", oauth.username="<STRING>", oauth.password="<STRING>", consumer.key="<STRING>", consumer.secret="<STRING>", token.url="<STRING>", refresh.token="<STRING>", headers="<STRING>", method="<STRING>", downloading.enabled="<BOOL>", download.path="<STRING>", blocking.io="<BOOL>", socket.idle.timeout="<INT>", chunk.disabled="<BOOL>", ssl.protocol="<STRING>", ssl.verification.disabled="<BOOL>", ssl.configurations="<STRING>", proxy.host="<STRING>", proxy.port="<STRING>", proxy.username="<STRING>", proxy.password="<STRING>", client.bootstrap.configurations="<STRING>", max.pool.active.connections="<INT>", min.pool.idle.connections="<INT>", max.pool.idle.connections="<INT>", min.evictable.idle.time="<STRING>", time.between.eviction.runs="<STRING>", max.wait.time="<STRING>", test.on.borrow="<BOOL>", test.while.idle="<BOOL>", exhausted.action="<INT>", hostname.verification.enabled="<BOOL>", @map(...)))
```

<span id="query-parameters" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">QUERY PARAMETERS</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <tr>
        <td style="vertical-align: top">publisher.url</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The URL which should be called.<br>Examples:<br><code>http://localhost:8080/endpoint</code>,<br><code>https://localhost:8080/endpoint</code></p></td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">sink.id</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Identifier to correlate the http-request sink to its corresponding http-response sources to retrieved the responses.</p></td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">basic.auth.username</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The username to be included in the authentication header when calling endpoints protected by basic authentication. <code>basic.auth.password</code> property should be also set when using this property.</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">basic.auth.password</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The password to be included in the authentication header when calling endpoints protected by basic authentication. <code>basic.auth.username</code> property should be also set when using this property.</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">https.truststore.file</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The file path of the client truststore when sending messages through <code>https</code> protocol.</p></td>
        <td style="vertical-align: top">`${carbon.home}/resources/security/client-truststore.jks`</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">https.truststore.password</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The password for the client-truststore.</p></td>
        <td style="vertical-align: top">wso2carbon</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">oauth.username</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The username to be included in the authentication header when calling endpoints protected by OAuth 2.0. <code>oauth.password</code> property should be also set when using this property.</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">oauth.password</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The password to be included in the authentication header when calling endpoints protected by OAuth 2.0. <code>oauth.username</code> property should be also set when using this property.</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">consumer.key</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Consumer key used for calling endpoints protected by OAuth 2.0</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">consumer.secret</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Consumer secret used for calling endpoints protected by OAuth 2.0</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">token.url</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Token URL to generate a new access tokens when calling endpoints protected by OAuth 2.0</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">refresh.token</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Refresh token used for generating new access tokens when calling endpoints protected by OAuth 2.0</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">headers</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">HTTP request headers in format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>When the <code>Content-Type</code> header is not provided the system decides the Content-Type based on the provided sink mapper as following: <br>&nbsp;- <code>@map(type='xml')</code>: <code>application/xml</code><br>&nbsp;- <code>@map(type='json')</code>: <code>application/json</code><br>&nbsp;- <code>@map(type='text')</code>: <code>plain/text</code><br>&nbsp;- <code>@map(type='keyvalue')</code>: <code>application/x-www-form-urlencoded</code><br>&nbsp;- For all other cases system defaults to <code>plain/text</code><br>Also the <code>Content-Length</code> header need not to be provided, as the system automatically defines it by calculating the size of the payload.</p></td>
        <td style="vertical-align: top">Content-Type and Content-Length headers</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">method</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The HTTP method used for calling the endpoint.</p></td>
        <td style="vertical-align: top">POST</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">downloading.enabled</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Enable response received by the http-response source to be written to a file. When this is enabled the <code>download.path</code> property should be also set.</p></td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">download.path</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The absolute file path along with the file name where the downloads should be saved.</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">Yes</td>
    </tr>
    <tr>
        <td style="vertical-align: top">blocking.io</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Blocks the request thread until a response it received from HTTP call-response source before sending any other request.</p></td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">socket.idle.timeout</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Socket timeout in millis.</p></td>
        <td style="vertical-align: top">6000</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">chunk.disabled</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Disable chunked transfer encoding.</p></td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">ssl.protocol</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">SSL/TLS protocol.</p></td>
        <td style="vertical-align: top">TLS</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">ssl.verification.disabled</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Disable SSL verification.</p></td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">ssl.configurations</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">SSL/TSL configurations in format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>Some supported parameters:<br>&nbsp;- SSL/TLS protocols: <code>'sslEnabledProtocols:TLSv1.1,TLSv1.2'</code><br>&nbsp;- List of ciphers: <code>'ciphers:TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256'</code><br>&nbsp;- Enable session creation: <code>'client.enable.session.creation:true'</code><br>&nbsp;- Supported server names: <code>'server.suported.server.names:server'</code><br>&nbsp;- Add HTTP SNIMatcher: <code>'server.supported.snimatchers:SNIMatcher'</code></p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">proxy.host</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Proxy server host</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">proxy.port</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Proxy server port</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">proxy.username</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Proxy server username</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">proxy.password</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Proxy server password</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">client.bootstrap.configurations</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Client bootstrap configurations in format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>Some supported configurations :<br>&nbsp;- Client connect timeout in millis: <code>'client.bootstrap.connect.timeout:15000'</code><br>&nbsp;- Client socket timeout in seconds: <code>'client.bootstrap.socket.timeout:15'</code><br>&nbsp;- Client socket reuse: <code>'client.bootstrap.socket.reuse:true'</code><br>&nbsp;- Enable TCP no delay: <code>'client.bootstrap.nodelay:true'</code><br>&nbsp;- Enable client keep alive: <code>'client.bootstrap.keepalive:true'</code><br>&nbsp;- Send buffer size: <code>'client.bootstrap.sendbuffersize:1048576'</code><br>&nbsp;- Receive buffer size: <code>'client.bootstrap.recievebuffersize:1048576'</code></p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.pool.active.connections</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Maximum possible number of active connection per client pool.</p></td>
        <td style="vertical-align: top">-1</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">min.pool.idle.connections</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Minimum number of idle connections that can exist per client pool.</p></td>
        <td style="vertical-align: top">0</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.pool.idle.connections</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Maximum number of idle connections that can exist per client pool.</p></td>
        <td style="vertical-align: top">100</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">min.evictable.idle.time</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Minimum time (in millis) a connection may sit idle in the client pool before it become eligible for eviction.</p></td>
        <td style="vertical-align: top">300000</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">time.between.eviction.runs</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Time between two eviction operations (in millis) on the client pool.</p></td>
        <td style="vertical-align: top">30000</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.wait.time</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The maximum time (in millis) the pool will wait (when there are no available connections) for a connection to be returned to the pool.</p></td>
        <td style="vertical-align: top">60000</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">test.on.borrow</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Enable connections to be validated before being borrowed from the client pool.</p></td>
        <td style="vertical-align: top">true</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">test.while.idle</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Enable connections to be validated during the eviction operation (if any).</p></td>
        <td style="vertical-align: top">true</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">exhausted.action</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Action that should be taken when the maximum number of active connections are being used. This action should be indicated as an int and possible action values are following.<br>0 - Fail the request.<br>1 - Block the request, until a connection returns to the pool.<br>2 - Grow the connection pool size.</p></td>
        <td style="vertical-align: top">1 (Block when exhausted)</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">hostname.verification.enabled</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Enable hostname verification</p></td>
        <td style="vertical-align: top">true</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="system-parameters" class="md-typeset" style="display: block; font-weight: bold;">System Parameters</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Parameters</th>
    </tr>
    <tr>
        <td style="vertical-align: top">clientBootstrapClientGroupSize</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">Number of client threads to perform non-blocking read and write to one or more channels.</p></td>
        <td style="vertical-align: top">(Number of available processors) * 2</td>
        <td style="vertical-align: top">Any positive integer</td>
    </tr>
    <tr>
        <td style="vertical-align: top">clientBootstrapBossGroupSize</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">Number of boss threads to accept incoming connections.</p></td>
        <td style="vertical-align: top">Number of available processors</td>
        <td style="vertical-align: top">Any positive integer</td>
    </tr>
    <tr>
        <td style="vertical-align: top">clientBootstrapWorkerGroupSize</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">Number of worker threads to accept the connections from boss threads and perform non-blocking read and write from one or more channels.</p></td>
        <td style="vertical-align: top">(Number of available processors) * 2</td>
        <td style="vertical-align: top">Any positive integer</td>
    </tr>
    <tr>
        <td style="vertical-align: top">trustStoreLocation</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default truststore file path.</p></td>
        <td style="vertical-align: top">`${carbon.home}/resources/security/client-truststore.jks`</td>
        <td style="vertical-align: top">Path to client truststore `.jks` file</td>
    </tr>
    <tr>
        <td style="vertical-align: top">trustStorePassword</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default truststore password.</p></td>
        <td style="vertical-align: top">wso2carbon</td>
        <td style="vertical-align: top">Truststore password as string</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@sink(type='http-request', sink.id='foo',
      publisher.url='http://localhost:8009/foo',
      @map(type='xml', @payload('{{payloadBody}}')))
define stream FooStream (payloadBody string);

@source(type='http-response', sink.id='foo',
        @map(type='text', regex.A='((.|\n)*)',
             @attributes(headers='trp:headers', message='A[1]')))
define stream ResponseStream(message string, headers string);
```
<p></p>
<p style="word-wrap: break-word;margin: 0;">When events arrive in <code>FooStream</code>, http-request sink makes calls to endpoint on url <code>http://localhost:8009/foo</code> with <code>POST</code> method and Content-Type <code>application/xml</code>.<br>If the event <code>payloadBody</code> attribute contains following XML:<br></p><pre>&lt;item&gt;
    &lt;name&gt;apple&lt;/name&gt;
    &lt;price&gt;55&lt;/price&gt;
    &lt;quantity&gt;5&lt;/quantity&gt;
&lt;/item&gt;</pre><p style="word-wrap: break-word;margin: 0;">the http-request sink maps that and sends it to the endpoint.<br>When endpoint sends a response it will be consumed by the corresponding http-response source correlated via the same <code>sink.id</code> <code>foo</code> and that will map the response message and send it via <code>ResponseStream</code> steam by assigning the message body as <code>message</code> attribute and response headers as <code>headers</code> attribute of the event.</p>
<p></p>
<span id="example-2" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 2</span>
```
@sink(type='http-request', publisher.url='http://localhost:8005/files/{{name}}'
      downloading.enabled='true', download.path='{{downloadPath}}{{name}}',
      method='GET', sink.id='download', @map(type='json'))
define stream DownloadRequestStream(name String, id int, downloadPath string);

@source(type='http-response', sink.id='download',
        http.status.code='2\\d+',
        @map(type='text', regex.A='((.|\n)*)',
             @attributes(name='trp:name', id='trp:id', file='A[1]')))
define stream ResponseStream2xx(name string, id string, file string);

@source(type='http-response', sink.id='download',
        http.status.code='4\\d+',
        @map(type='text', regex.A='((.|\n)*)', @attributes(errorMsg='A[1]')))
define stream ResponseStream4xx(errorMsg string);
```
<p></p>
<p style="word-wrap: break-word;margin: 0;">When events arrive in <code>DownloadRequestStream</code> with <code>name</code>:<code>foo.txt</code>, <code>id</code>:<code>75</code> and <code>downloadPath</code>:<code>/user/download/</code> the http-request sink sends a GET request to the url <code>http://localhost:8005/files/foo.txt</code> to download the file to the given path <code>/user/download/foo.txt</code> and capture the response via its corresponding http-response source based on the response status code.<br>If the response status code is in the range of 200 the message will be received by the http-response source associated with the <code>ResponseStream2xx</code> stream which expects <code>http.status.code</code> with regex <code>2\\d+</code> while downloading the file to the local file system on the path <code>/user/download/foo.txt</code> and mapping the response message having the absolute file path to event's <code>file</code> attribute.<br>If the response status code is in the range of 400 then the message will be received by the http-response source associated with the <code>ResponseStream4xx</code> stream which expects <code>http.status.code</code> with regex <code>4\\d+</code> while mapping the error response to the <code>errorMsg</code> attribute of the event.</p>
<p></p>
### <s>http-response *<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#sink">(Sink)</a>*</s>
<p><i>Deprecated</i></p>
<p></p>
<p style="word-wrap: break-word;margin: 0;">_(Use http-service-response sink instead)._<br>The http-response sink send responses of the requests consumed by its corresponding http-request source, by mapping the response messages to formats such as <code>text</code>, <code>XML</code> and <code>JSON</code>.</p>
<p></p>
<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>

```
@sink(type="http-response", source.id="<STRING>", message.id="<STRING>", headers="<STRING>", @map(...)))
```

<span id="query-parameters" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">QUERY PARAMETERS</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <tr>
        <td style="vertical-align: top">source.id</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Identifier to correlate the http-response sink to its corresponding http-request source which consumed the request.</p></td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">message.id</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Identifier to correlate the response with the request received by http-request source.</p></td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">Yes</td>
    </tr>
    <tr>
        <td style="vertical-align: top">headers</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">HTTP request headers in format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>When the <code>Content-Type</code> header is not provided the system decides the Content-Type based on the provided sink mapper as following: <br>&nbsp;- <code>@map(type='xml')</code>: <code>application/xml</code><br>&nbsp;- <code>@map(type='json')</code>: <code>application/json</code><br>&nbsp;- <code>@map(type='text')</code>: <code>plain/text</code><br>&nbsp;- <code>@map(type='keyvalue')</code>: <code>application/x-www-form-urlencoded</code><br>&nbsp;- For all other cases system defaults to <code>plain/text</code><br>Also the <code>Content-Length</code> header need not to be provided, as the system automatically defines it by calculating the size of the payload.</p></td>
        <td style="vertical-align: top">Content-Type and Content-Length headers</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@source(type='http-request', receiver.url='http://localhost:5005/add',
        source.id='adder',
        @map(type='json, @attributes(messageId='trp:messageId',
                                     value1='$.event.value1',
                                     value2='$.event.value2')))
define stream AddStream (messageId string, value1 long, value2 long);

@sink(type='http-response', source.id='adder',
      message.id='{{messageId}}', @map(type = 'json'))
define stream ResultStream (messageId string, results long);

@info(name = 'query1')
from AddStream 
select messageId, value1 + value2 as results 
insert into ResultStream;
```
<p></p>
<p style="word-wrap: break-word;margin: 0;">The http-request source on stream <code>AddStream</code> listens on url <code>http://localhost:5005/stocks</code> for JSON messages with format:<br></p><pre>{
  "event": {
    "value1": 3,
    "value2": 4
  }
}</pre><p style="word-wrap: break-word;margin: 0;"><br>and when events arrive it maps to <code>AddStream</code> events and pass them to query <code>query1</code> for processing. The query results produced on <code>ResultStream</code> are sent as a response via http-response sink with format:</p><pre>{
  "event": {
    "results": 7
  }
}</pre><p style="word-wrap: break-word;margin: 0;">Here the request and response are correlated by passing the <code>messageId</code> produced by the http-request to the respective http-response sink.</p>
<p></p>
### http-service-response *<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#sink">(Sink)</a>*
<p></p>
<p style="word-wrap: break-word;margin: 0;">The http-service-response sink send responses of the requests consumed by its corresponding http-service source, by mapping the response messages to formats such as <code>text</code>, <code>XML</code> and <code>JSON</code>.</p>
<p></p>
<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>

```
@sink(type="http-service-response", source.id="<STRING>", message.id="<STRING>", headers="<STRING>", @map(...)))
```

<span id="query-parameters" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">QUERY PARAMETERS</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <tr>
        <td style="vertical-align: top">source.id</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Identifier to correlate the http-service-response sink to its corresponding http-service source which consumed the request.</p></td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">message.id</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Identifier to correlate the response with the request received by http-service source.</p></td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">Yes</td>
    </tr>
    <tr>
        <td style="vertical-align: top">headers</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">HTTP request headers in format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>When the <code>Content-Type</code> header is not provided the system decides the Content-Type based on the provided sink mapper as following: <br>&nbsp;- <code>@map(type='xml')</code>: <code>application/xml</code><br>&nbsp;- <code>@map(type='json')</code>: <code>application/json</code><br>&nbsp;- <code>@map(type='text')</code>: <code>plain/text</code><br>&nbsp;- <code>@map(type='keyvalue')</code>: <code>application/x-www-form-urlencoded</code><br>&nbsp;- For all other cases system defaults to <code>plain/text</code><br>Also the <code>Content-Length</code> header need not to be provided, as the system automatically defines it by calculating the size of the payload.</p></td>
        <td style="vertical-align: top">Content-Type and Content-Length headers</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@source(type='http-service', receiver.url='http://localhost:5005/add',
        source.id='adder',
        @map(type='json, @attributes(messageId='trp:messageId',
                                     value1='$.event.value1',
                                     value2='$.event.value2')))
define stream AddStream (messageId string, value1 long, value2 long);

@sink(type='http-service-response', source.id='adder',
      message.id='{{messageId}}', @map(type = 'json'))
define stream ResultStream (messageId string, results long);

@info(name = 'query1')
from AddStream 
select messageId, value1 + value2 as results 
insert into ResultStream;
```
<p></p>
<p style="word-wrap: break-word;margin: 0;">The http-service source on stream <code>AddStream</code> listens on url <code>http://localhost:5005/stocks</code> for JSON messages with format:<br></p><pre>{
  "event": {
    "value1": 3,
    "value2": 4
  }
}</pre><p style="word-wrap: break-word;margin: 0;"><br>and when events arrive it maps to <code>AddStream</code> events and pass them to query <code>query1</code> for processing. The query results produced on <code>ResultStream</code> are sent as a response via http-service-response sink with format:</p><pre>{
  "event": {
    "results": 7
  }
}</pre><p style="word-wrap: break-word;margin: 0;">Here the request and response are correlated by passing the <code>messageId</code> produced by the http-service to the respective http-service-response sink.</p>
<p></p>
### sse-server *<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#sink">(Sink)</a>*
<p></p>
<p style="word-wrap: break-word;margin: 0;">HTTP SSE sink sends events to all subscribers.</p>
<p></p>
<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>

```
@sink(type="sse-server", server.url="<STRING>", worker.count="<INT>", headers="<STRING>", https.truststore.file="<STRING>", https.truststore.password="<STRING>", client.bootstrap.configurations="<STRING>", @map(...)))
```

<span id="query-parameters" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">QUERY PARAMETERS</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <tr>
        <td style="vertical-align: top">server.url</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The listening URL of the SSE server which clients need to connect to receive events. If not provided url will be constructed using siddhi app name and stream name as the context by default with port 8280. eg :- http://0.0.0.0:8280/{app_name}/{stream_name}</p></td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">worker.count</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The number of active worker threads to serve the incoming events. By default the value is set to <code>1</code> to ensure events are processed in the same order they arrived. By increasing this value, higher performance can be achieved in the expense of loosing event ordering.</p></td>
        <td style="vertical-align: top">1</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">headers</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">HTTP request headers in format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>When the <code>Content-Type</code> header is not provided the system decides the Content-Type based on the provided sink mapper as following: <br>&nbsp;- <code>@map(type='xml')</code>: <code>application/xml</code><br>&nbsp;- <code>@map(type='json')</code>: <code>application/json</code><br>&nbsp;- <code>@map(type='text')</code>: <code>plain/text</code><br>&nbsp;- <code>@map(type='keyvalue')</code>: <code>application/x-www-form-urlencoded</code><br>&nbsp;- For all other cases system defaults to <code>plain/text</code><br>Also the <code>Content-Length</code> header need not to be provided, as the system automatically defines it by calculating the size of the payload.</p></td>
        <td style="vertical-align: top">Content-Type and Content-Length headers</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">https.truststore.file</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The file path of the client truststore when sending messages through <code>https</code> protocol.</p></td>
        <td style="vertical-align: top">`${carbon.home}/resources/security/client-truststore.jks`</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">https.truststore.password</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The password for the client-truststore.</p></td>
        <td style="vertical-align: top">wso2carbon</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">client.bootstrap.configurations</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Client bootstrap configurations in format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>Some supported configurations :<br>&nbsp;- Client connect timeout in millis: <code>'client.bootstrap.connect.timeout:15000'</code><br>&nbsp;- Client socket timeout in seconds: <code>'client.bootstrap.socket.timeout:15'</code><br>&nbsp;- Client socket reuse: <code>'client.bootstrap.socket.reuse:true'</code><br>&nbsp;- Enable TCP no delay: <code>'client.bootstrap.nodelay:true'</code><br>&nbsp;- Enable client keep alive: <code>'client.bootstrap.keepalive:true'</code><br>&nbsp;- Send buffer size: <code>'client.bootstrap.sendbuffersize:1048576'</code><br>&nbsp;- Receive buffer size: <code>'client.bootstrap.recievebuffersize:1048576'</code></p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="system-parameters" class="md-typeset" style="display: block; font-weight: bold;">System Parameters</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Parameters</th>
    </tr>
    <tr>
        <td style="vertical-align: top">defaultScheme</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default protocol.</p></td>
        <td style="vertical-align: top">http</td>
        <td style="vertical-align: top">http<br>https</td>
    </tr>
    <tr>
        <td style="vertical-align: top">defaultHttpPort</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default HTTP port when default scheme is <code>http</code>.</p></td>
        <td style="vertical-align: top">8280</td>
        <td style="vertical-align: top">Any valid port</td>
    </tr>
    <tr>
        <td style="vertical-align: top">defaultHost</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default host of the transport.</p></td>
        <td style="vertical-align: top">0.0.0.0</td>
        <td style="vertical-align: top">Any valid host</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@Source(type='sse-server', server.url='http://localhost:8080/sse', @map(type='json')) define stream PublishingStream (param1 string);
```
<p></p>
<p style="word-wrap: break-word;margin: 0;">External clients can listen to the server.url</p>
<p></p>
### websubhub *<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#sink">(Sink)</a>*
<p></p>
<p style="word-wrap: break-word;margin: 0;">WebSubHubEventPublisher publishes messages via HTTP/HTTP according to the provided URL when subscribe to the WebSub hub. The table.name, hub.id are mandatory when defining the websubhub source </p>
<p></p>
<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>

```
@sink(type="websubhub", hub.id="<STRING>", table.name="<STRING>", https.truststore.file="<STRING>", https.truststore.password="<STRING>", consumer.key="<STRING>", consumer.secret="<STRING>", token.url="<STRING>", refresh.token="<STRING>", headers="<STRING>", method="<STRING>", socket.idle.timeout="<INT>", chunk.disabled="<BOOL>", ssl.protocol="<STRING>", ssl.verification.disabled="<BOOL>", tls.store.type="<STRING>", ssl.configurations="<STRING>", proxy.host="<STRING>", proxy.port="<STRING>", proxy.username="<STRING>", proxy.password="<STRING>", client.bootstrap.configurations="<STRING>", max.pool.active.connections="<INT>", min.pool.idle.connections="<INT>", max.pool.idle.connections="<INT>", executor.service.threads="<INT>", min.evictable.idle.time="<STRING>", time.between.eviction.runs="<STRING>", max.wait.time="<STRING>", test.on.borrow="<BOOL>", test.while.idle="<BOOL>", exhausted.action="<INT>", hostname.verification.enabled="<BOOL>", @map(...)))
```

<span id="query-parameters" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">QUERY PARAMETERS</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <tr>
        <td style="vertical-align: top">hub.id</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Id of the hub that the messages needed to process </p></td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">table.name</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Name of the table which subscription data holds related to the hub  </p></td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">https.truststore.file</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The file path of the client truststore when sending messages through <code>https</code> protocol.</p></td>
        <td style="vertical-align: top">`${carbon.home}/resources/security/client-truststore.jks`</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">https.truststore.password</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The password for the client-truststore.</p></td>
        <td style="vertical-align: top">wso2carbon</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">consumer.key</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Consumer key used for calling endpoints protected by OAuth 2.0</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">consumer.secret</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Consumer secret used for calling endpoints protected by OAuth 2.0</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">token.url</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Token URL to generate a new access tokens when calling endpoints protected by OAuth 2.0</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">refresh.token</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Refresh token used for generating new access tokens when calling endpoints protected by OAuth 2.0</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">headers</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">HTTP request headers in format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>When <code>Content-Type</code> header is not provided the system derives the Content-Type based on the provided sink mapper as following: <br>&nbsp;- <code>@map(type='xml')</code>: <code>application/xml</code><br>&nbsp;- <code>@map(type='json')</code>: <code>application/json</code><br>&nbsp;- <code>@map(type='text')</code>: <code>plain/text</code><br>&nbsp;- <code>@map(type='keyvalue')</code>: <code>application/x-www-form-urlencoded</code><br>&nbsp;- For all other cases system defaults to <code>plain/text</code><br>Also the <code>Content-Length</code> header need not to be provided, as the system automatically defines it by calculating the size of the payload.</p></td>
        <td style="vertical-align: top">Content-Type and Content-Length headers</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">method</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The HTTP method used for calling the endpoint.</p></td>
        <td style="vertical-align: top">POST</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">socket.idle.timeout</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Socket timeout in millis.</p></td>
        <td style="vertical-align: top">6000</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">chunk.disabled</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Disable chunked transfer encoding.</p></td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">ssl.protocol</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">SSL/TLS protocol.</p></td>
        <td style="vertical-align: top">TLS</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">ssl.verification.disabled</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Disable SSL verification.</p></td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">tls.store.type</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">TLS store type.</p></td>
        <td style="vertical-align: top">JKS</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">ssl.configurations</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">SSL/TSL configurations in format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>Some supported parameters:<br>&nbsp;- SSL/TLS protocols: <code>'sslEnabledProtocols:TLSv1.1,TLSv1.2'</code><br>&nbsp;- List of ciphers: <code>'ciphers:TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256'</code><br>&nbsp;- Enable session creation: <code>'client.enable.session.creation:true'</code><br>&nbsp;- Supported server names: <code>'server.suported.server.names:server'</code><br>&nbsp;- Add HTTP SNIMatcher: <code>'server.supported.snimatchers:SNIMatcher'</code></p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">proxy.host</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Proxy server host</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">proxy.port</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Proxy server port</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">proxy.username</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Proxy server username</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">proxy.password</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Proxy server password</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">client.bootstrap.configurations</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Client bootstrap configurations in format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>Some supported configurations :<br>&nbsp;- Client connect timeout in millis: <code>'client.bootstrap.connect.timeout:15000'</code><br>&nbsp;- Client socket timeout in seconds: <code>'client.bootstrap.socket.timeout:15'</code><br>&nbsp;- Client socket reuse: <code>'client.bootstrap.socket.reuse:true'</code><br>&nbsp;- Enable TCP no delay: <code>'client.bootstrap.nodelay:true'</code><br>&nbsp;- Enable client keep alive: <code>'client.bootstrap.keepalive:true'</code><br>&nbsp;- Send buffer size: <code>'client.bootstrap.sendbuffersize:1048576'</code><br>&nbsp;- Receive buffer size: <code>'client.bootstrap.recievebuffersize:1048576'</code></p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.pool.active.connections</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Maximum possible number of active connection per client pool.</p></td>
        <td style="vertical-align: top">-1</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">min.pool.idle.connections</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Minimum number of idle connections that can exist per client pool.</p></td>
        <td style="vertical-align: top">0</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.pool.idle.connections</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Maximum number of idle connections that can exist per client pool.</p></td>
        <td style="vertical-align: top">100</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">executor.service.threads</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Thread count for the executor service.</p></td>
        <td style="vertical-align: top">20</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">min.evictable.idle.time</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Minimum time (in millis) a connection may sit idle in the client pool before it become eligible for eviction.</p></td>
        <td style="vertical-align: top">300000</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">time.between.eviction.runs</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Time between two eviction operations (in millis) on the client pool.</p></td>
        <td style="vertical-align: top">30000</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.wait.time</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The maximum time (in millis) the pool will wait (when there are no available connections) for a connection to be returned to the pool.</p></td>
        <td style="vertical-align: top">60000</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">test.on.borrow</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Enable connections to be validated before being borrowed from the client pool.</p></td>
        <td style="vertical-align: top">true</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">test.while.idle</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Enable connections to be validated during the eviction operation (if any).</p></td>
        <td style="vertical-align: top">true</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">exhausted.action</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Action that should be taken when the maximum number of active connections are being used. This action should be indicated as an int and possible action values are following.<br>0 - Fail the request.<br>1 - Block the request, until a connection returns to the pool.<br>2 - Grow the connection pool size.</p></td>
        <td style="vertical-align: top">1 (Block when exhausted)</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">hostname.verification.enabled</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Enable hostname verification.</p></td>
        <td style="vertical-align: top">true</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="system-parameters" class="md-typeset" style="display: block; font-weight: bold;">System Parameters</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Parameters</th>
    </tr>
    <tr>
        <td style="vertical-align: top">clientBootstrapClientGroupSize</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">Number of client threads to perform non-blocking read and write to one or more channels.</p></td>
        <td style="vertical-align: top">(Number of available processors) * 2</td>
        <td style="vertical-align: top">Any positive integer</td>
    </tr>
    <tr>
        <td style="vertical-align: top">clientBootstrapBossGroupSize</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">Number of boss threads to accept incoming connections.</p></td>
        <td style="vertical-align: top">Number of available processors</td>
        <td style="vertical-align: top">Any positive integer</td>
    </tr>
    <tr>
        <td style="vertical-align: top">clientBootstrapWorkerGroupSize</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">Number of worker threads to accept the connections from boss threads and perform non-blocking read and write from one or more channels.</p></td>
        <td style="vertical-align: top">(Number of available processors) * 2</td>
        <td style="vertical-align: top">Any positive integer</td>
    </tr>
    <tr>
        <td style="vertical-align: top">trustStoreLocation</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default truststore file path.</p></td>
        <td style="vertical-align: top">`${carbon.home}/resources/security/client-truststore.jks`</td>
        <td style="vertical-align: top">Path to client truststore `.jks` file</td>
    </tr>
    <tr>
        <td style="vertical-align: top">trustStorePassword</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default truststore password.</p></td>
        <td style="vertical-align: top">wso2carbon</td>
        <td style="vertical-align: top">Truststore password as string</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@store(type='rdbms' , jdbc.url='jdbc:mysql://localhost:3306/production?useSSL=false', username='root', password='root', jdbc.driver.name='com.mysql.jdbc.Driver') 
@sink(type='websubhubeventpublisher', hub.id="anu_123" , table.name='SessionTable',publisher.url="mysql://localhost:3306/production?useSSL=false",
@map(type='keyvalue',implicit.cast.enable='true'))
define stream LowProductionAlertStream (topic string, payload string);
```
<p></p>
<p style="word-wrap: break-word;margin: 0;">Subscribed users will received the messages generated through the hub and will publish to the callback url when subscribe. </p>
<p></p>
## Source

### http *<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#source">(Source)</a>*
<p></p>
<p style="word-wrap: break-word;margin: 0;">HTTP source receives POST requests via HTTP and HTTPS protocols in format such as <code>text</code>, <code>XML</code> and <code>JSON</code>. It also supports basic authentication to ensure events are received from authorized users/systems.<br>The request headers and properties can be accessed via transport properties in the format <code>trp:&lt;header&gt;</code>.</p>
<p></p>
<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>

```
@source(type="http", receiver.url="<STRING>", basic.auth.enabled="<STRING>", worker.count="<INT>", socket.idle.timeout="<INT>", ssl.verify.client="<STRING>", ssl.protocol="<STRING>", tls.store.type="<STRING>", ssl.configurations="<STRING>", request.size.validation.configurations="<STRING>", header.validation.configurations="<STRING>", server.bootstrap.configurations="<STRING>", trace.log.enabled="<BOOL>", @map(...)))
```

<span id="query-parameters" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">QUERY PARAMETERS</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <tr>
        <td style="vertical-align: top">receiver.url</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The URL on which events should be received. To enable SSL use <code>https</code> protocol in the url.</p></td>
        <td style="vertical-align: top">`http://0.0.0.0:9763/<appNAme>/<streamName>`</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">basic.auth.enabled</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">This only works in VM, Docker and Kubernetes.<br>Where when enabled it authenticates each request using the <code>Authorization:'Basic encodeBase64(username:Password)'</code> header.</p></td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">worker.count</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The number of active worker threads to serve the incoming events. By default the value is set to <code>1</code> to ensure events are processed in the same order they arrived. By increasing this value, higher performance can be achieved in the expense of loosing event ordering.</p></td>
        <td style="vertical-align: top">1</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">socket.idle.timeout</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Idle timeout for HTTP connection in millis.</p></td>
        <td style="vertical-align: top">120000</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">ssl.verify.client</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The type of client certificate verification. Supported values are <code>require</code>, <code>optional</code>.</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">ssl.protocol</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">SSL/TLS protocol.</p></td>
        <td style="vertical-align: top">TLS</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">tls.store.type</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">TLS store type.</p></td>
        <td style="vertical-align: top">JKS</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">ssl.configurations</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">SSL/TSL configurations in format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>Some supported parameters:<br>&nbsp;- SSL/TLS protocols: <code>'sslEnabledProtocols:TLSv1.1,TLSv1.2'</code><br>&nbsp;- List of ciphers: <code>'ciphers:TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256'</code><br>&nbsp;- Enable session creation: <code>'client.enable.session.creation:true'</code><br>&nbsp;- Supported server names: <code>'server.suported.server.names:server'</code><br>&nbsp;- Add HTTP SNIMatcher: <code>'server.supported.snimatchers:SNIMatcher'</code></p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">request.size.validation.configurations</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Configurations to validate the HTTP request size.<br>Expected format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>Some supported configurations :<br>&nbsp;- Enable request size validation: <code>'request.size.validation:true'</code><br>&nbsp;If request size is validated<br>&nbsp;- Maximum request size: <code>'request.size.validation.maximum.value:2048'</code><br>&nbsp;- Response status code when request size validation fails: <code>'request.size.validation.reject.status.code:401'</code><br>&nbsp;- Response message when request size validation fails: <code>'request.size.validation.reject.message:Message is bigger than the valid size'</code><br>&nbsp;- Response Content-Type when request size validation fails: <code>'request.size.validation.reject.message.content.type:plain/text'</code></p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">header.validation.configurations</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Configurations to validate HTTP headers.<br>Expected format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>Some supported configurations :<br>&nbsp;- Enable header size validation: <code>'header.size.validation:true'</code><br>&nbsp;If header size is validated<br>&nbsp;- Maximum length of initial line: <code>'header.validation.maximum.request.line:4096'</code><br>&nbsp;- Maximum length of all headers: <code>'header.validation.maximum.size:8192'</code><br>&nbsp;- Maximum length of the content or each chunk: <code>'header.validation.maximum.chunk.size:8192'</code><br>&nbsp;- Response status code when header validation fails: <code>'header.validation.reject.status.code:401'</code><br>&nbsp;- Response message when header validation fails: <code>'header.validation.reject.message:Message header is bigger than the valid size'</code><br>&nbsp;- Response Content-Type when header validation fails: <code>'header.validation.reject.message.content.type:plain/text'</code></p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">server.bootstrap.configurations</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Server bootstrap configurations in format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>Some supported configurations :<br>&nbsp;- Server connect timeout in millis: <code>'server.bootstrap.connect.timeout:15000'</code><br>&nbsp;- Server socket timeout in seconds: <code>'server.bootstrap.socket.timeout:15'</code><br>&nbsp;- Enable TCP no delay: <code>'server.bootstrap.nodelay:true'</code><br>&nbsp;- Enable server keep alive: <code>'server.bootstrap.keepalive:true'</code><br>&nbsp;- Send buffer size: <code>'server.bootstrap.sendbuffersize:1048576'</code><br>&nbsp;- Receive buffer size: <code>'server.bootstrap.recievebuffersize:1048576'</code><br>&nbsp;- Number of connections queued: <code>'server.bootstrap.socket.backlog:100'</code></p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">trace.log.enabled</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Enable trace log for traffic monitoring.</p></td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="system-parameters" class="md-typeset" style="display: block; font-weight: bold;">System Parameters</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Parameters</th>
    </tr>
    <tr>
        <td style="vertical-align: top">serverBootstrapBossGroupSize</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">Number of boss threads to accept incoming connections.</p></td>
        <td style="vertical-align: top">Number of available processors</td>
        <td style="vertical-align: top">Any positive integer</td>
    </tr>
    <tr>
        <td style="vertical-align: top">serverBootstrapWorkerGroupSize</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">Number of worker threads to accept the connections from boss threads and perform non-blocking read and write from one or more channels.</p></td>
        <td style="vertical-align: top">(Number of available processors) * 2</td>
        <td style="vertical-align: top">Any positive integer</td>
    </tr>
    <tr>
        <td style="vertical-align: top">serverBootstrapClientGroupSize</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">Number of client threads to perform non-blocking read and write to one or more channels.</p></td>
        <td style="vertical-align: top">(Number of available processors) * 2</td>
        <td style="vertical-align: top">Any positive integer</td>
    </tr>
    <tr>
        <td style="vertical-align: top">defaultHost</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default host of the transport.</p></td>
        <td style="vertical-align: top">0.0.0.0</td>
        <td style="vertical-align: top">Any valid host</td>
    </tr>
    <tr>
        <td style="vertical-align: top">defaultScheme</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default protocol.</p></td>
        <td style="vertical-align: top">http</td>
        <td style="vertical-align: top">http<br>https</td>
    </tr>
    <tr>
        <td style="vertical-align: top">defaultHttpPort</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default HTTP port when default scheme is <code>http</code>.</p></td>
        <td style="vertical-align: top">8280</td>
        <td style="vertical-align: top">Any valid port</td>
    </tr>
    <tr>
        <td style="vertical-align: top">defaultHttpsPort</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default HTTPS port when default scheme is <code>https</code>.</p></td>
        <td style="vertical-align: top">8243</td>
        <td style="vertical-align: top">Any valid port</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keyStoreLocation</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default keystore file path.</p></td>
        <td style="vertical-align: top">`${carbon.home}/resources/security/wso2carbon.jks`</td>
        <td style="vertical-align: top">Path to `.jks` file</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keyStorePassword</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default keystore password.</p></td>
        <td style="vertical-align: top">wso2carbon</td>
        <td style="vertical-align: top">Keystore password as string</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@app.name('StockProcessor')

@source(type='http', @map(type = 'json'))
define stream StockStream (symbol string, price float, volume long);

```
<p></p>
<p style="word-wrap: break-word;margin: 0;">Above HTTP source listeners on url <code>http://0.0.0.0:9763/StockProcessor/StockStream</code> for JSON messages on the format:<br></p><pre>{
  "event": {
    "symbol": "FB",
    "price": 24.5,
    "volume": 5000
  }
}</pre><p style="word-wrap: break-word;margin: 0;">It maps the incoming messages and sends them to <code>StockStream</code> for processing.</p>
<p></p>
<span id="example-2" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 2</span>
```
@source(type='http', receiver.url='http://localhost:5005/stocks',
        @map(type = 'xml'))
define stream StockStream (symbol string, price float, volume long);

```
<p></p>
<p style="word-wrap: break-word;margin: 0;">Above HTTP source listeners on url <code>http://localhost:5005/stocks</code> for JSON messages on the format:<br></p><pre>&lt;events&gt;
    &lt;event&gt;
        &lt;symbol&gt;Fb&lt;/symbol&gt;
        &lt;price&gt;55.6&lt;/price&gt;
        &lt;volume&gt;100&lt;/volume&gt;
    &lt;/event&gt;
&lt;/events&gt;</pre><p style="word-wrap: break-word;margin: 0;"><br>It maps the incoming messages and sends them to <code>StockStream</code> for processing.</p>
<p></p>
### http-call-response *<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#source">(Source)</a>*
<p></p>
<p style="word-wrap: break-word;margin: 0;">The http-call-response source receives the responses for the calls made by its corresponding http-call sink, and maps them from formats such as <code>text</code>, <code>XML</code> and <code>JSON</code>.<br>To handle messages with different http status codes having different formats, multiple http-call-response sources are allowed to associate with a single http-call sink.<br>It allows accessing the attributes of the event that initiated the call, and the response headers and properties via transport properties in the format <code>trp:&lt;attribute name&gt;</code> and <code>trp:&lt;header/property&gt;</code> respectively.</p>
<p></p>
<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>

```
@source(type="http-call-response", sink.id="<STRING>", http.status.code="<STRING>", allow.streaming.responses="<BOOL>", @map(...)))
```

<span id="query-parameters" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">QUERY PARAMETERS</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <tr>
        <td style="vertical-align: top">sink.id</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Identifier to correlate the http-call-response source with its corresponding http-call sink that published the messages.</p></td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">http.status.code</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The matching http responses status code regex, that is used to filter the the messages which will be processed by the source.Eg: <code>http.status.code = '200'</code>,<br><code>http.status.code = '4\\d+'</code></p></td>
        <td style="vertical-align: top">200</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">allow.streaming.responses</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Enable consuming responses on a streaming manner.</p></td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@sink(type='http-call', method='POST',
      publisher.url='http://localhost:8005/registry/employee',
      sink.id='employee-info', @map(type='json')) 
define stream EmployeeRequestStream (name string, id int);

@source(type='http-call-response', sink.id='employee-info',
        http.status.code='2\\d+',
        @map(type='json',
             @attributes(name='trp:name', id='trp:id',
                         location='$.town', age='$.age')))
define stream EmployeeResponseStream(name string, id int,
                                     location string, age int);

@source(type='http-call-response', sink.id='employee-info',
        http.status.code='4\\d+',
        @map(type='text', regex.A='((.|\n)*)',
             @attributes(error='A[1]')))
define stream EmployeeErrorStream(error string);
```
<p></p>
<p style="word-wrap: break-word;margin: 0;">When events arrive in <code>EmployeeRequestStream</code>, http-call sink makes calls to endpoint on url <code>http://localhost:8005/registry/employee</code> with <code>POST</code> method and Content-Type <code>application/json</code>.<br>If the arriving event has attributes <code>name</code>:<code>John</code> and <code>id</code>:<code>1423</code> it will send a message with default JSON mapping as follows:<br></p><pre>{
  "event": {
    "name": "John",
    "id": 1423
  }
}</pre><p style="word-wrap: break-word;margin: 0;">When the endpoint responds with status code in the range of 200 the message will be received by the http-call-response source associated with the <code>EmployeeResponseStream</code> stream, because it is correlated with the sink by the same <code>sink.id</code> <code>employee-info</code> and as that expects messages with <code>http.status.code</code> in regex format <code>2\\d+</code>. If the response message is in the format<br></p><pre>{
  "town": "NY",
  "age": 24
}</pre><p style="word-wrap: break-word;margin: 0;">the source maps the <code>location</code> and <code>age</code> attributes by executing JSON path on the message and maps the <code>name</code> and <code>id</code> attributes by extracting them from the request event via as transport properties.<br>If the response status code is in the range of 400 then the message will be received by the http-call-response source associated with the <code>EmployeeErrorStream</code> stream, because it is correlated with the sink by the same <code>sink.id</code> <code>employee-info</code> and it expects messages with <code>http.status.code</code> in regex format <code>4\\d+</code>, and maps the error response to the <code>error</code> attribute of the event.</p>
<p></p>
### <s>http-request *<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#source">(Source)</a>*</s>
<p><i>Deprecated</i></p>
<p></p>
<p style="word-wrap: break-word;margin: 0;">_(Use http-service source instead)._<br>The http-request source receives POST requests via HTTP and HTTPS protocols in format such as <code>text</code>, <code>XML</code> and <code>JSON</code> and sends responses via its corresponding http-response sink correlated through a unique <code>source.id</code>.<br>For request and response correlation, it generates a <code>messageId</code> upon each incoming request and expose it via transport properties in the format <code>trp:messageId</code> to correlate them with the responses at the http-response sink.<br>The request headers and properties can be accessed via transport properties in the format <code>trp:&lt;header&gt;</code>.<br>It also supports basic authentication to ensure events are received from authorized users/systems.</p>
<p></p>
<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>

```
@source(type="http-request", receiver.url="<STRING>", source.id="<STRING>", connection.timeout="<INT>", basic.auth.enabled="<STRING>", worker.count="<INT>", socket.idle.timeout="<INT>", ssl.verify.client="<STRING>", ssl.protocol="<STRING>", tls.store.type="<STRING>", ssl.configurations="<STRING>", request.size.validation.configurations="<STRING>", header.validation.configurations="<STRING>", server.bootstrap.configurations="<STRING>", trace.log.enabled="<BOOL>", @map(...)))
```

<span id="query-parameters" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">QUERY PARAMETERS</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <tr>
        <td style="vertical-align: top">receiver.url</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The URL on which events should be received. To enable SSL use <code>https</code> protocol in the url.</p></td>
        <td style="vertical-align: top">`http://0.0.0.0:9763/<appNAme>/<streamName>`</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">source.id</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Identifier to correlate the http-request source to its corresponding http-response sinks to send responses.</p></td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">connection.timeout</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Connection timeout in millis. The system will send a timeout, if a corresponding response is not sent by an associated http-response sink within the given time.</p></td>
        <td style="vertical-align: top">120000</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">basic.auth.enabled</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">This only works in VM, Docker and Kubernetes.<br>Where when enabled it authenticates each request using the <code>Authorization:'Basic encodeBase64(username:Password)'</code> header.</p></td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">worker.count</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The number of active worker threads to serve the incoming events. By default the value is set to <code>1</code> to ensure events are processed in the same order they arrived. By increasing this value, higher performance can be achieved in the expense of loosing event ordering.</p></td>
        <td style="vertical-align: top">1</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">socket.idle.timeout</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Idle timeout for HTTP connection in millis.</p></td>
        <td style="vertical-align: top">120000</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">ssl.verify.client</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The type of client certificate verification. Supported values are <code>require</code>, <code>optional</code>.</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">ssl.protocol</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">SSL/TLS protocol.</p></td>
        <td style="vertical-align: top">TLS</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">tls.store.type</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">TLS store type.</p></td>
        <td style="vertical-align: top">JKS</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">ssl.configurations</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">SSL/TSL configurations in format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>Some supported parameters:<br>&nbsp;- SSL/TLS protocols: <code>'sslEnabledProtocols:TLSv1.1,TLSv1.2'</code><br>&nbsp;- List of ciphers: <code>'ciphers:TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256'</code><br>&nbsp;- Enable session creation: <code>'client.enable.session.creation:true'</code><br>&nbsp;- Supported server names: <code>'server.suported.server.names:server'</code><br>&nbsp;- Add HTTP SNIMatcher: <code>'server.supported.snimatchers:SNIMatcher'</code></p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">request.size.validation.configurations</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Configurations to validate the HTTP request size.<br>Expected format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>Some supported configurations :<br>&nbsp;- Enable request size validation: <code>'request.size.validation:true'</code><br>&nbsp;If request size is validated<br>&nbsp;- Maximum request size: <code>'request.size.validation.maximum.value:2048'</code><br>&nbsp;- Response status code when request size validation fails: <code>'request.size.validation.reject.status.code:401'</code><br>&nbsp;- Response message when request size validation fails: <code>'request.size.validation.reject.message:Message is bigger than the valid size'</code><br>&nbsp;- Response Content-Type when request size validation fails: <code>'request.size.validation.reject.message.content.type:plain/text'</code></p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">header.validation.configurations</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Configurations to validate HTTP headers.<br>Expected format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>Some supported configurations :<br>&nbsp;- Enable header size validation: <code>'header.size.validation:true'</code><br>&nbsp;If header size is validated<br>&nbsp;- Maximum length of initial line: <code>'header.validation.maximum.request.line:4096'</code><br>&nbsp;- Maximum length of all headers: <code>'header.validation.maximum.size:8192'</code><br>&nbsp;- Maximum length of the content or each chunk: <code>'header.validation.maximum.chunk.size:8192'</code><br>&nbsp;- Response status code when header validation fails: <code>'header.validation.reject.status.code:401'</code><br>&nbsp;- Response message when header validation fails: <code>'header.validation.reject.message:Message header is bigger than the valid size'</code><br>&nbsp;- Response Content-Type when header validation fails: <code>'header.validation.reject.message.content.type:plain/text'</code></p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">server.bootstrap.configurations</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Server bootstrap configurations in format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>Some supported configurations :<br>&nbsp;- Server connect timeout in millis: <code>'server.bootstrap.connect.timeout:15000'</code><br>&nbsp;- Server socket timeout in seconds: <code>'server.bootstrap.socket.timeout:15'</code><br>&nbsp;- Enable TCP no delay: <code>'server.bootstrap.nodelay:true'</code><br>&nbsp;- Enable server keep alive: <code>'server.bootstrap.keepalive:true'</code><br>&nbsp;- Send buffer size: <code>'server.bootstrap.sendbuffersize:1048576'</code><br>&nbsp;- Receive buffer size: <code>'server.bootstrap.recievebuffersize:1048576'</code><br>&nbsp;- Number of connections queued: <code>'server.bootstrap.socket.backlog:100'</code></p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">trace.log.enabled</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Enable trace log for traffic monitoring.</p></td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="system-parameters" class="md-typeset" style="display: block; font-weight: bold;">System Parameters</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Parameters</th>
    </tr>
    <tr>
        <td style="vertical-align: top">serverBootstrapBossGroupSize</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">Number of boss threads to accept incoming connections.</p></td>
        <td style="vertical-align: top">Number of available processors</td>
        <td style="vertical-align: top">Any positive integer</td>
    </tr>
    <tr>
        <td style="vertical-align: top">serverBootstrapWorkerGroupSize</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">Number of worker threads to accept the connections from boss threads and perform non-blocking read and write from one or more channels.</p></td>
        <td style="vertical-align: top">(Number of available processors) * 2</td>
        <td style="vertical-align: top">Any positive integer</td>
    </tr>
    <tr>
        <td style="vertical-align: top">serverBootstrapClientGroupSize</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">Number of client threads to perform non-blocking read and write to one or more channels.</p></td>
        <td style="vertical-align: top">(Number of available processors) * 2</td>
        <td style="vertical-align: top">Any positive integer</td>
    </tr>
    <tr>
        <td style="vertical-align: top">defaultHost</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default host of the transport.</p></td>
        <td style="vertical-align: top">0.0.0.0</td>
        <td style="vertical-align: top">Any valid host</td>
    </tr>
    <tr>
        <td style="vertical-align: top">defaultScheme</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default protocol.</p></td>
        <td style="vertical-align: top">http</td>
        <td style="vertical-align: top">http<br>https</td>
    </tr>
    <tr>
        <td style="vertical-align: top">defaultHttpPort</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default HTTP port when default scheme is <code>http</code>.</p></td>
        <td style="vertical-align: top">8280</td>
        <td style="vertical-align: top">Any valid port</td>
    </tr>
    <tr>
        <td style="vertical-align: top">defaultHttpsPort</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default HTTPS port when default scheme is <code>https</code>.</p></td>
        <td style="vertical-align: top">8243</td>
        <td style="vertical-align: top">Any valid port</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keyStoreLocation</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default keystore file path.</p></td>
        <td style="vertical-align: top">`${carbon.home}/resources/security/wso2carbon.jks`</td>
        <td style="vertical-align: top">Path to `.jks` file</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keyStorePassword</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default keystore password.</p></td>
        <td style="vertical-align: top">wso2carbon</td>
        <td style="vertical-align: top">Keystore password as string</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@source(type='http-request', receiver.url='http://localhost:5005/add',
        source.id='adder',
        @map(type='json, @attributes(messageId='trp:messageId',
                                     value1='$.event.value1',
                                     value2='$.event.value2')))
define stream AddStream (messageId string, value1 long, value2 long);

@sink(type='http-response', source.id='adder',
      message.id='{{messageId}}', @map(type = 'json'))
define stream ResultStream (messageId string, results long);

@info(name = 'query1')
from AddStream 
select messageId, value1 + value2 as results 
insert into ResultStream;
```
<p></p>
<p style="word-wrap: break-word;margin: 0;">Above sample listens events on <code>http://localhost:5005/stocks</code> url for JSON messages on the format:<br></p><pre>{
  "event": {
    "value1": 3,
    "value2": 4
  }
}</pre><p style="word-wrap: break-word;margin: 0;"><br>Map the vents into AddStream, process the events through query <code>query1</code>, and sends the results produced on ResultStream via http-response sink on the message format:</p><pre>{
  "event": {
    "results": 7
  }
}</pre><p style="word-wrap: break-word;margin: 0;"></p>
<p></p>
### <s>http-response *<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#source">(Source)</a>*</s>
<p><i>Deprecated</i></p>
<p></p>
<p style="word-wrap: break-word;margin: 0;">_(Use http-call-response source instead)._<br>The http-response source receives the responses for the calls made by its corresponding http-request sink, and maps them from formats such as <code>text</code>, <code>XML</code> and <code>JSON</code>.<br>To handle messages with different http status codes having different formats, multiple http-response sources are allowed to associate with a single http-request sink. It allows accessing the attributes of the event that initiated the call, and the response headers and properties via transport properties in the format <code>trp:&lt;attribute name&gt;</code> and <code>trp:&lt;header/property&gt;</code> respectively.</p>
<p></p>
<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>

```
@source(type="http-response", sink.id="<STRING>", http.status.code="<STRING>", allow.streaming.responses="<BOOL>", @map(...)))
```

<span id="query-parameters" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">QUERY PARAMETERS</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <tr>
        <td style="vertical-align: top">sink.id</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Identifier to correlate the http-response source with its corresponding http-request sink that published the messages.</p></td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">http.status.code</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The matching http responses status code regex, that is used to filter the the messages which will be processed by the source.Eg: <code>http.status.code = '200'</code>,<br><code>http.status.code = '4\\d+'</code></p></td>
        <td style="vertical-align: top">200</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">allow.streaming.responses</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Enable consuming responses on a streaming manner.</p></td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@sink(type='http-request', method='POST',
      publisher.url='http://localhost:8005/registry/employee',
      sink.id='employee-info', @map(type='json')) 
define stream EmployeeRequestStream (name string, id int);

@source(type='http-response', sink.id='employee-info',
        http.status.code='2\\d+',
        @map(type='json',
             @attributes(name='trp:name', id='trp:id',
                         location='$.town', age='$.age')))
define stream EmployeeResponseStream(name string, id int,
                                     location string, age int);

@source(type='http-response', sink.id='employee-info',
        http.status.code='4\\d+',
        @map(type='text', regex.A='((.|\n)*)',
             @attributes(error='A[1]')))
define stream EmployeeErrorStream(error string);
```
<p></p>
<p style="word-wrap: break-word;margin: 0;">When events arrive in <code>EmployeeRequestStream</code>, http-request sink makes calls to endpoint on url <code>http://localhost:8005/registry/employee</code> with <code>POST</code> method and Content-Type <code>application/json</code>.<br>If the arriving event has attributes <code>name</code>:<code>John</code> and <code>id</code>:<code>1423</code> it will send a message with default JSON mapping as follows:<br></p><pre>{
  "event": {
    "name": "John",
    "id": 1423
  }
}</pre><p style="word-wrap: break-word;margin: 0;">When the endpoint responds with status code in the range of 200 the message will be received by the http-response source associated with the <code>EmployeeResponseStream</code> stream, because it is correlated with the sink by the same <code>sink.id</code> <code>employee-info</code> and as that expects messages with <code>http.status.code</code> in regex format <code>2\\d+</code>. If the response message is in the format<br></p><pre>{
  "town": "NY",
  "age": 24
}</pre><p style="word-wrap: break-word;margin: 0;">the source maps the <code>location</code> and <code>age</code> attributes by executing JSON path on the message and maps the <code>name</code> and <code>id</code> attributes by extracting them from the request event via as transport properties.<br>If the response status code is in the range of 400 then the message will be received by the http-response source associated with the <code>EmployeeErrorStream</code> stream, because it is correlated with the sink by the same <code>sink.id</code> <code>employee-info</code> and it expects messages with <code>http.status.code</code> in regex format <code>4\\d+</code>, and maps the error response to the <code>error</code> attribute of the event.</p>
<p></p>
### http-service *<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#source">(Source)</a>*
<p></p>
<p style="word-wrap: break-word;margin: 0;">The http-service source receives POST requests via HTTP and HTTPS protocols in format such as <code>text</code>, <code>XML</code> and <code>JSON</code> and sends responses via its corresponding http-service-response sink correlated through a unique <code>source.id</code>.<br>For request and response correlation, it generates a <code>messageId</code> upon each incoming request and expose it via transport properties in the format <code>trp:messageId</code> to correlate them with the responses at the http-service-response sink.<br>The request headers and properties can be accessed via transport properties in the format <code>trp:&lt;header&gt;</code>.<br>It also supports basic authentication to ensure events are received from authorized users/systems.</p>
<p></p>
<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>

```
@source(type="http-service", receiver.url="<STRING>", source.id="<STRING>", connection.timeout="<INT>", basic.auth.enabled="<STRING>", worker.count="<INT>", socket.idle.timeout="<INT>", ssl.verify.client="<STRING>", ssl.protocol="<STRING>", tls.store.type="<STRING>", ssl.configurations="<STRING>", request.size.validation.configurations="<STRING>", header.validation.configurations="<STRING>", server.bootstrap.configurations="<STRING>", trace.log.enabled="<BOOL>", @map(...)))
```

<span id="query-parameters" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">QUERY PARAMETERS</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <tr>
        <td style="vertical-align: top">receiver.url</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The URL on which events should be received. To enable SSL use <code>https</code> protocol in the url.</p></td>
        <td style="vertical-align: top">`http://0.0.0.0:9763/<appNAme>/<streamName>`</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">source.id</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Identifier to correlate the http-service source to its corresponding http-service-response sinks to send responses.</p></td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">connection.timeout</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Connection timeout in millis. The system will send a timeout, if a corresponding response is not sent by an associated http-service-response sink within the given time.</p></td>
        <td style="vertical-align: top">120000</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">basic.auth.enabled</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">This only works in VM, Docker and Kubernetes.<br>Where when enabled it authenticates each request using the <code>Authorization:'Basic encodeBase64(username:Password)'</code> header.</p></td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">worker.count</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The number of active worker threads to serve the incoming events. By default the value is set to <code>1</code> to ensure events are processed in the same order they arrived. By increasing this value, higher performance can be achieved in the expense of loosing event ordering.</p></td>
        <td style="vertical-align: top">1</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">socket.idle.timeout</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Idle timeout for HTTP connection in millis.</p></td>
        <td style="vertical-align: top">120000</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">ssl.verify.client</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The type of client certificate verification. Supported values are <code>require</code>, <code>optional</code>.</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">ssl.protocol</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">SSL/TLS protocol.</p></td>
        <td style="vertical-align: top">TLS</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">tls.store.type</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">TLS store type.</p></td>
        <td style="vertical-align: top">JKS</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">ssl.configurations</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">SSL/TSL configurations in format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>Some supported parameters:<br>&nbsp;- SSL/TLS protocols: <code>'sslEnabledProtocols:TLSv1.1,TLSv1.2'</code><br>&nbsp;- List of ciphers: <code>'ciphers:TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256'</code><br>&nbsp;- Enable session creation: <code>'client.enable.session.creation:true'</code><br>&nbsp;- Supported server names: <code>'server.suported.server.names:server'</code><br>&nbsp;- Add HTTP SNIMatcher: <code>'server.supported.snimatchers:SNIMatcher'</code></p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">request.size.validation.configurations</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Configurations to validate the HTTP request size.<br>Expected format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>Some supported configurations :<br>&nbsp;- Enable request size validation: <code>'request.size.validation:true'</code><br>&nbsp;If request size is validated<br>&nbsp;- Maximum request size: <code>'request.size.validation.maximum.value:2048'</code><br>&nbsp;- Response status code when request size validation fails: <code>'request.size.validation.reject.status.code:401'</code><br>&nbsp;- Response message when request size validation fails: <code>'request.size.validation.reject.message:Message is bigger than the valid size'</code><br>&nbsp;- Response Content-Type when request size validation fails: <code>'request.size.validation.reject.message.content.type:plain/text'</code></p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">header.validation.configurations</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Configurations to validate HTTP headers.<br>Expected format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>Some supported configurations :<br>&nbsp;- Enable header size validation: <code>'header.size.validation:true'</code><br>&nbsp;If header size is validated<br>&nbsp;- Maximum length of initial line: <code>'header.validation.maximum.request.line:4096'</code><br>&nbsp;- Maximum length of all headers: <code>'header.validation.maximum.size:8192'</code><br>&nbsp;- Maximum length of the content or each chunk: <code>'header.validation.maximum.chunk.size:8192'</code><br>&nbsp;- Response status code when header validation fails: <code>'header.validation.reject.status.code:401'</code><br>&nbsp;- Response message when header validation fails: <code>'header.validation.reject.message:Message header is bigger than the valid size'</code><br>&nbsp;- Response Content-Type when header validation fails: <code>'header.validation.reject.message.content.type:plain/text'</code></p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">server.bootstrap.configurations</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Server bootstrap configurations in format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>Some supported configurations :<br>&nbsp;- Server connect timeout in millis: <code>'server.bootstrap.connect.timeout:15000'</code><br>&nbsp;- Server socket timeout in seconds: <code>'server.bootstrap.socket.timeout:15'</code><br>&nbsp;- Enable TCP no delay: <code>'server.bootstrap.nodelay:true'</code><br>&nbsp;- Enable server keep alive: <code>'server.bootstrap.keepalive:true'</code><br>&nbsp;- Send buffer size: <code>'server.bootstrap.sendbuffersize:1048576'</code><br>&nbsp;- Receive buffer size: <code>'server.bootstrap.recievebuffersize:1048576'</code><br>&nbsp;- Number of connections queued: <code>'server.bootstrap.socket.backlog:100'</code></p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">trace.log.enabled</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Enable trace log for traffic monitoring.</p></td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="system-parameters" class="md-typeset" style="display: block; font-weight: bold;">System Parameters</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Parameters</th>
    </tr>
    <tr>
        <td style="vertical-align: top">serverBootstrapBossGroupSize</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">Number of boss threads to accept incoming connections.</p></td>
        <td style="vertical-align: top">Number of available processors</td>
        <td style="vertical-align: top">Any positive integer</td>
    </tr>
    <tr>
        <td style="vertical-align: top">serverBootstrapWorkerGroupSize</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">Number of worker threads to accept the connections from boss threads and perform non-blocking read and write from one or more channels.</p></td>
        <td style="vertical-align: top">(Number of available processors) * 2</td>
        <td style="vertical-align: top">Any positive integer</td>
    </tr>
    <tr>
        <td style="vertical-align: top">serverBootstrapClientGroupSize</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">Number of client threads to perform non-blocking read and write to one or more channels.</p></td>
        <td style="vertical-align: top">(Number of available processors) * 2</td>
        <td style="vertical-align: top">Any positive integer</td>
    </tr>
    <tr>
        <td style="vertical-align: top">defaultHost</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default host of the transport.</p></td>
        <td style="vertical-align: top">0.0.0.0</td>
        <td style="vertical-align: top">Any valid host</td>
    </tr>
    <tr>
        <td style="vertical-align: top">defaultScheme</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default protocol.</p></td>
        <td style="vertical-align: top">http</td>
        <td style="vertical-align: top">http<br>https</td>
    </tr>
    <tr>
        <td style="vertical-align: top">defaultHttpPort</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default HTTP port when default scheme is <code>http</code>.</p></td>
        <td style="vertical-align: top">8280</td>
        <td style="vertical-align: top">Any valid port</td>
    </tr>
    <tr>
        <td style="vertical-align: top">defaultHttpsPort</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default HTTPS port when default scheme is <code>https</code>.</p></td>
        <td style="vertical-align: top">8243</td>
        <td style="vertical-align: top">Any valid port</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keyStoreLocation</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default keystore file path.</p></td>
        <td style="vertical-align: top">`${carbon.home}/resources/security/wso2carbon.jks`</td>
        <td style="vertical-align: top">Path to `.jks` file</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keyStorePassword</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default keystore password.</p></td>
        <td style="vertical-align: top">wso2carbon</td>
        <td style="vertical-align: top">Keystore password as string</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@source(type='http-service', receiver.url='http://localhost:5005/add',
        source.id='adder',
        @map(type='json, @attributes(messageId='trp:messageId',
                                     value1='$.event.value1',
                                     value2='$.event.value2')))
define stream AddStream (messageId string, value1 long, value2 long);

@sink(type='http-service-response', source.id='adder',
      message.id='{{messageId}}', @map(type = 'json'))
define stream ResultStream (messageId string, results long);

@info(name = 'query1')
from AddStream 
select messageId, value1 + value2 as results 
insert into ResultStream;
```
<p></p>
<p style="word-wrap: break-word;margin: 0;">Above sample listens events on <code>http://localhost:5005/stocks</code> url for JSON messages on the format:<br></p><pre>{
  "event": {
    "value1": 3,
    "value2": 4
  }
}</pre><p style="word-wrap: break-word;margin: 0;"><br>Map the vents into AddStream, process the events through query <code>query1</code>, and sends the results produced on ResultStream via http-service-response sink on the message format:</p><pre>{
  "event": {
    "results": 7
  }
}</pre><p style="word-wrap: break-word;margin: 0;"></p>
<p></p>
### sse *<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#source">(Source)</a>*
<p></p>
<p style="word-wrap: break-word;margin: 0;">HTTP SSE source send a request to a given url and listen to the response stream.</p>
<p></p>
<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>

```
@source(type="sse", receiver.url="<STRING>", basic.auth.username="<STRING>", basic.auth.password="<STRING>", worker.count="<INT>", headers="<STRING>", https.truststore.file="<STRING>", https.truststore.password="<STRING>", client.bootstrap.configurations="<STRING>", @map(...)))
```

<span id="query-parameters" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">QUERY PARAMETERS</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <tr>
        <td style="vertical-align: top">receiver.url</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The sse endpoint url which should be listened.</p></td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">basic.auth.username</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The username to be included in the authentication header when calling endpoints protected by basic authentication. <code>basic.auth.password</code> property should be also set when using this property.</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">basic.auth.password</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The password to be included in the authentication header when calling endpoints protected by basic authentication. <code>basic.auth.username</code> property should be also set when using this property.</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">worker.count</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The number of active worker threads to serve the incoming events. By default the value is set to <code>1</code> to ensure events are processed in the same order they arrived. By increasing this value, higher performance can be achieved in the expense of loosing event ordering.</p></td>
        <td style="vertical-align: top">1</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">headers</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">HTTP request headers in format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>When the <code>Content-Type</code> header is not provided the system decides the Content-Type based on the provided sink mapper as following: <br>&nbsp;- <code>@map(type='xml')</code>: <code>application/xml</code><br>&nbsp;- <code>@map(type='json')</code>: <code>application/json</code><br>&nbsp;- <code>@map(type='text')</code>: <code>plain/text</code><br>&nbsp;- <code>@map(type='keyvalue')</code>: <code>application/x-www-form-urlencoded</code><br>&nbsp;- For all other cases system defaults to <code>plain/text</code><br>Also the <code>Content-Length</code> header need not to be provided, as the system automatically defines it by calculating the size of the payload.</p></td>
        <td style="vertical-align: top">Content-Type and Content-Length headers</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">https.truststore.file</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The file path of the client truststore when sending messages through <code>https</code> protocol.</p></td>
        <td style="vertical-align: top">`${carbon.home}/resources/security/client-truststore.jks`</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">https.truststore.password</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The password for the client-truststore.</p></td>
        <td style="vertical-align: top">wso2carbon</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">client.bootstrap.configurations</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Client bootstrap configurations in format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>Some supported configurations :<br>&nbsp;- Client connect timeout in millis: <code>'client.bootstrap.connect.timeout:15000'</code><br>&nbsp;- Client socket timeout in seconds: <code>'client.bootstrap.socket.timeout:15'</code><br>&nbsp;- Client socket reuse: <code>'client.bootstrap.socket.reuse:true'</code><br>&nbsp;- Enable TCP no delay: <code>'client.bootstrap.nodelay:true'</code><br>&nbsp;- Enable client keep alive: <code>'client.bootstrap.keepalive:true'</code><br>&nbsp;- Send buffer size: <code>'client.bootstrap.sendbuffersize:1048576'</code><br>&nbsp;- Receive buffer size: <code>'client.bootstrap.recievebuffersize:1048576'</code></p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="system-parameters" class="md-typeset" style="display: block; font-weight: bold;">System Parameters</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Parameters</th>
    </tr>
    <tr>
        <td style="vertical-align: top">defaultScheme</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default protocol.</p></td>
        <td style="vertical-align: top">http</td>
        <td style="vertical-align: top">http<br>https</td>
    </tr>
    <tr>
        <td style="vertical-align: top">defaultHttpPort</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default HTTP port when default scheme is <code>http</code>.</p></td>
        <td style="vertical-align: top">8280</td>
        <td style="vertical-align: top">Any valid port</td>
    </tr>
    <tr>
        <td style="vertical-align: top">defaultHost</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default host of the transport.</p></td>
        <td style="vertical-align: top">0.0.0.0</td>
        <td style="vertical-align: top">Any valid host</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@Source(type='sse', receiver.url='http://localhost:8080/sse', @map(type='json')) define stream IncomingStream (param1 string);
```
<p></p>
<p style="word-wrap: break-word;margin: 0;">This subscribes to the events which gets published by the SSE server at receiver.url</p>
<p></p>
### websubhub *<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#source">(Source)</a>*
<p></p>
<p style="word-wrap: break-word;margin: 0;"> WebSub Hub source receive subscription requests via Http and according to the request, the subscription details will be saved to the given table and against the callback and topic name. The subscription request  **MUST** have a Content-Type header of **application/x-www-form-urlencoded** and following **MUST** provide as parameter body. <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; hub.callback &nbsp;&nbsp;&nbsp;&nbsp; - REQUIRED.&nbsp;&nbsp;&nbsp;&nbsp; The subscriber's callback URL where content distribution notifications should be delivered. The callback URL SHOULD be an unguessable URL that is unique per subscription.<br>&nbsp;&nbsp;&nbsp;&nbsp; hub.mode &nbsp;&nbsp;&nbsp;&nbsp; - REQUIRED.&nbsp;&nbsp;&nbsp;&nbsp; The literal string "subscribe" or "unsubscribe", depending on the goal of the request.<br>&nbsp;&nbsp;&nbsp;&nbsp; hub.topic &nbsp;&nbsp;&nbsp;&nbsp; - REQUIRED.&nbsp;&nbsp;&nbsp;&nbsp; The topic URL that the subscriber wishes to subscribe to or unsubscribe from.<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; hub.lease_seconds&nbsp;&nbsp;&nbsp;&nbsp; - OPTIONAL.&nbsp;&nbsp;&nbsp;&nbsp; Number of seconds for which the subscriber would like to have the subscription active, given as a positive decimal integer. <br>&nbsp;&nbsp;&nbsp;&nbsp; hub.secret&nbsp;&nbsp;&nbsp;&nbsp; - OPTIONAL.&nbsp;&nbsp;&nbsp;&nbsp; A subscriber-provided cryptographically random unique secret string that will be used to compute an HMAC digest for authorized content distribution. If not supplied, the HMAC digest will not be present for content distribution requests. </p>
<p></p>
<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>

```
@source(type="websubhub", hub.id="<STRING>", table.name="<STRING>", receiver.url="<STRING>", topic.list="<STRING>", basic.auth.enabled="<STRING>", worker.count="<INT>", socket.idle.timeout="<INT>", ssl.verify.client="<STRING>", ssl.protocol="<STRING>", tls.store.type="<STRING>", ssl.configurations="<STRING>", request.size.validation.configurations="<STRING>", header.validation.configurations="<STRING>", server.bootstrap.configurations="<STRING>", trace.log.enabled="<BOOL>", @map(...)))
```

<span id="query-parameters" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">QUERY PARAMETERS</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <tr>
        <td style="vertical-align: top">hub.id</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Unique id for the WebSub Hub</p></td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">table.name</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Table name to store the subscription details related to the hub </p></td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">receiver.url</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The URL on which events should be received. To enable SSL use <code>https</code> protocol in the url.</p></td>
        <td style="vertical-align: top">`http://0.0.0.0:9763/<appNAme>/<streamName>`</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">topic.list</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">topics allowed in the websub hub</p></td>
        <td style="vertical-align: top">empty</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">basic.auth.enabled</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">This only works in VM, Docker and Kubernetes.<br>Where when enabled it authenticates each request using the <code>Authorization:'Basic encodeBase64(username:Password)'</code> header.</p></td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">worker.count</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The number of active worker threads to serve the incoming events. By default the value is set to <code>1</code> to ensure events are processed in the same order they arrived. By increasing this value, higher performance can be achieved in the expense of loosing event ordering.</p></td>
        <td style="vertical-align: top">1</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">socket.idle.timeout</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Idle timeout for HTTP connection in millis.</p></td>
        <td style="vertical-align: top">120000</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">ssl.verify.client</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The type of client certificate verification. Supported values are <code>require</code>, <code>optional</code>.</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">ssl.protocol</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">SSL/TLS protocol.</p></td>
        <td style="vertical-align: top">TLS</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">tls.store.type</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">TLS store type.</p></td>
        <td style="vertical-align: top">JKS</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">ssl.configurations</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">SSL/TSL configurations in format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>Some supported parameters:<br>&nbsp;- SSL/TLS protocols: <code>'sslEnabledProtocols:TLSv1.1,TLSv1.2'</code><br>&nbsp;- List of ciphers: <code>'ciphers:TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256'</code><br>&nbsp;- Enable session creation: <code>'client.enable.session.creation:true'</code><br>&nbsp;- Supported server names: <code>'server.suported.server.names:server'</code><br>&nbsp;- Add HTTP SNIMatcher: <code>'server.supported.snimatchers:SNIMatcher'</code></p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">request.size.validation.configurations</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Configurations to validate the HTTP request size.<br>Expected format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>Some supported configurations :<br>&nbsp;- Enable request size validation: <code>'request.size.validation:true'</code><br>&nbsp;If request size is validated<br>&nbsp;- Maximum request size: <code>'request.size.validation.maximum.value:2048'</code><br>&nbsp;- Response status code when request size validation fails: <code>'request.size.validation.reject.status.code:401'</code><br>&nbsp;- Response message when request size validation fails: <code>'request.size.validation.reject.message:Message is bigger than the valid size'</code><br>&nbsp;- Response Content-Type when request size validation fails: <code>'request.size.validation.reject.message.content.type:plain/text'</code></p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">header.validation.configurations</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Configurations to validate HTTP headers.<br>Expected format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>Some supported configurations :<br>&nbsp;- Enable header size validation: <code>'header.size.validation:true'</code><br>&nbsp;If header size is validated<br>&nbsp;- Maximum length of initial line: <code>'header.validation.maximum.request.line:4096'</code><br>&nbsp;- Maximum length of all headers: <code>'header.validation.maximum.size:8192'</code><br>&nbsp;- Maximum length of the content or each chunk: <code>'header.validation.maximum.chunk.size:8192'</code><br>&nbsp;- Response status code when header validation fails: <code>'header.validation.reject.status.code:401'</code><br>&nbsp;- Response message when header validation fails: <code>'header.validation.reject.message:Message header is bigger than the valid size'</code><br>&nbsp;- Response Content-Type when header validation fails: <code>'header.validation.reject.message.content.type:plain/text'</code></p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">server.bootstrap.configurations</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Server bootstrap configurations in format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>.<br>Some supported configurations :<br>&nbsp;- Server connect timeout in millis: <code>'server.bootstrap.connect.timeout:15000'</code><br>&nbsp;- Server socket timeout in seconds: <code>'server.bootstrap.socket.timeout:15'</code><br>&nbsp;- Enable TCP no delay: <code>'server.bootstrap.nodelay:true'</code><br>&nbsp;- Enable server keep alive: <code>'server.bootstrap.keepalive:true'</code><br>&nbsp;- Send buffer size: <code>'server.bootstrap.sendbuffersize:1048576'</code><br>&nbsp;- Receive buffer size: <code>'server.bootstrap.recievebuffersize:1048576'</code><br>&nbsp;- Number of connections queued: <code>'server.bootstrap.socket.backlog:100'</code></p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">trace.log.enabled</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Enable trace log for traffic monitoring.</p></td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="system-parameters" class="md-typeset" style="display: block; font-weight: bold;">System Parameters</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Parameters</th>
    </tr>
    <tr>
        <td style="vertical-align: top">serverBootstrapBossGroupSize</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">Number of boss threads to accept incoming connections.</p></td>
        <td style="vertical-align: top">Number of available processors</td>
        <td style="vertical-align: top">Any positive integer</td>
    </tr>
    <tr>
        <td style="vertical-align: top">serverBootstrapWorkerGroupSize</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">Number of worker threads to accept the connections from boss threads and perform non-blocking read and write from one or more channels.</p></td>
        <td style="vertical-align: top">(Number of available processors) * 2</td>
        <td style="vertical-align: top">Any positive integer</td>
    </tr>
    <tr>
        <td style="vertical-align: top">serverBootstrapClientGroupSize</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">Number of client threads to perform non-blocking read and write to one or more channels.</p></td>
        <td style="vertical-align: top">(Number of available processors) * 2</td>
        <td style="vertical-align: top">Any positive integer</td>
    </tr>
    <tr>
        <td style="vertical-align: top">defaultHost</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default host of the transport.</p></td>
        <td style="vertical-align: top">0.0.0.0</td>
        <td style="vertical-align: top">Any valid host</td>
    </tr>
    <tr>
        <td style="vertical-align: top">defaultScheme</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default protocol.</p></td>
        <td style="vertical-align: top">http</td>
        <td style="vertical-align: top">http<br>https</td>
    </tr>
    <tr>
        <td style="vertical-align: top">defaultHttpPort</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default HTTP port when default scheme is <code>http</code>.</p></td>
        <td style="vertical-align: top">8280</td>
        <td style="vertical-align: top">Any valid port</td>
    </tr>
    <tr>
        <td style="vertical-align: top">defaultHttpsPort</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default HTTPS port when default scheme is <code>https</code>.</p></td>
        <td style="vertical-align: top">8243</td>
        <td style="vertical-align: top">Any valid port</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keyStoreLocation</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default keystore file path.</p></td>
        <td style="vertical-align: top">`${carbon.home}/resources/security/wso2carbon.jks`</td>
        <td style="vertical-align: top">Path to `.jks` file</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keyStorePassword</td>
        <td style="vertical-align: top;"><p style="word-wrap: break-word;margin: 0;">The default keystore password.</p></td>
        <td style="vertical-align: top">wso2carbon</td>
        <td style="vertical-align: top">Keystore password as string</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@app.name('StockProcessor')

@store(type='rdbms' , jdbc.url='jdbc:mysql://localhost:3306/production?useSSL=false', username='root', password='root', jdbc.driver.name='com.mysql.jdbc.Driver') 
@source(type='websubhub' , hub.id='anu_123',table.name='SessionTable', receiver.url='http://localhost:8006/productionStream',basic.auth.enabled='false', @map(type='keyvalue',implicit.cast.enable='true')) 
define stream webSubStream(callback string, lease_seconds long, secret string, topic string, mode string);

```
<p></p>
<p style="word-wrap: break-word;margin: 0;">Above WebSubHub listening on http://localhost:8006/productionStream for thesubscription requests.</p>
<p></p>
