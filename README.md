# spring-remoting-alternative

Simple alternative to Spring Remoting over HTTP.

## Server-side

### DynamicApiConfiguration

Creates an HTTP API by using Spring MVC's functional endpoint configuration, where each method of a specified interface
is mapped as a separate endpoint. The request handler for the endpoint calls the corresponding method on an 
implementation of the interface, through reflection. The request body contains details for invoking the method. The
body is a serialized Java-object, serialized with Java Object Serialization.  
If the method returns a value, it will be sent back to the client as a serialized Java-object.

### StaticApiConfiguration

Creates an HTTP API by using Spring MVC's functional endpoint configuration, for the same interface as used in 
`DynamicApiConfiguration`, but without using reflection. This could have been replaced with a class annotated with 
`@RestController`, but for comparison it is configured the same way as `DynamicApiConfiguration`.

## Client-side

### DynamicServiceFactory

Factory that creates a proxy for a remote service using Byte buddy, by specifying the remote interface. Each method of 
the proxy calls the corresponding remote HTTP endpoint, RPC style. Details for invoking the remote method is added to 
the request body as a serialized Java-object, by using Java Object Serialization.

```java
final DynamicServiceFactory dynamicServiceFactory = new DynamicServiceFactory("https://server-url.no");
final RemoteService remoteService = dynamicServiceFactory.create(RemoteService.class);
final SomeType result = remoteService.someMethod(someValue);
```

### StaticService

"Normal" implementation of a remote interface, where the HTTP-endpoints for the remote methods are explicitly called 
from each method of the interface, for comparison with the `DynamicServiceFactory`.

## Usage

Se example of usage in `ApplicationTest`.