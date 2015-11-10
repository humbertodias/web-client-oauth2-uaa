# UAA

Inicie o servidor de identidade

Porta 8080

```
$ git clone git://github.com/cloudfoundry/uaa.git
$ cd uaa
$ ./gradlew run
```

```
...
...
...
:cleanCargoConfDir UP-TO-DATE
:cleanCargoConfDir took 0ms
:cargoRunLocal
Press Ctrl-C to stop the container...
> Building 97% > :cargoRunLocal
```



# Cliente

Crie o cliente

```
$ ../myapp/create.sh

```

```
....

user account successfully updated
  scope: openid
  client_id: myapp
  resource_ids: none
  authorized_grant_types: authorization_code client_credentials refresh_token
  redirect_uri: http://localhost:3030/callback
  autoapprove: 
  action: none
  authorities: uaa.resource
  lastmodified: 1447078162582
  id: 39237128-e3c0-4614-852e-eb29bec5349d
...
```



# WebClient

Execute a aplicação cliente

```
mvn tomcat7:run-war
```

```
[INFO] Running war on http://localhost:3030/
[INFO] Creating Tomcat server configuration at /Users/tux/git/web-client-oauth2-uaa/web-client-oauth2-oidc/target/tomcat
[INFO] create webapp with contextPath: 
Nov 09, 2015 12:10:09 PM org.apache.coyote.AbstractProtocol init
INFO: Initializing ProtocolHandler ["http-bio-3030"]
Nov 09, 2015 12:10:09 PM org.apache.catalina.core.StandardService startInternal
INFO: Starting service Tomcat
Nov 09, 2015 12:10:09 PM org.apache.catalina.core.StandardEngine startInternal
INFO: Starting Servlet Engine: Apache Tomcat/7.0.47
Nov 09, 2015 12:10:12 PM org.apache.coyote.AbstractProtocol start
INFO: Starting ProtocolHandler ["http-bio-3030"]
```


# Navegador

[http://localhost:3030](http://localhost:3030)

