# spring-remoting-alternative

Simple alternatives to Spring Remoting over HTTP with HTTP Invoker.

## Server-side

### DynamicApiWithRouterFunctionConfiguration

Creates an HTTP API by using Spring MVC's functional endpoint configuration, where each method of a specified interface
is mapped as a separate endpoint. The request handler for the endpoint calls the corresponding method on an
implementation of the interface, through reflection. The request body contains details for invoking the method. The
body is a serialized Java-object, serialized with Java Object Serialization.  
If the method returns a value, it will be sent back to the client as a serialized Java-object.

Simple performance tests show that it's performance is slightly slower than Spring Remoting over HTTP, on average, by
a few hundredth of a millisecond (on my machine).  
Run `PerformanceComparison` to compare.

### DynamicApiWithHttpRequestHandlerConfiguration

Creates an HTTP API by using a `HttpRequestHandler`, which is the same mechanism Spring Remoting uses, when exposing
a service through `HttpInvokerServiceExporter`.

Simple performance tests show that it's performance is equivalent to Spring Remoting over HTTP, if not a bit faster on
average, possibly due to simpler implementation.  
Run `PerformanceComparison` to compare.

### StaticApiController

Creates an HTTP API by using Spring MVC's `@RestController`-annotation, for the same interface as used in
`DynamicApiWithRouterFunctionConfiguration`, but without using reflection. For comparison with other alternatives.

### SpringRemotingConfiguration

Creates an HTTP API by using Spring Remoting's `HttpInvokerServiceExporter`, for comparison with other alternatives.

## Client-side

### DynamicWithRouterFunctionServiceFactory and DynamicWithHttpRequestHandlerServiceFactory

Factories that creates a proxy for a remote service using Byte buddy, by specifying the remote interface. Each method of
the proxy calls the corresponding remote HTTP endpoint, RPC / RMI style. Details for invoking the remote method is added
to the request body as a serialized Java-object, by using Java Object Serialization.

```
final var factory = new DynamicWithHttpRequestHandlerServiceFactory("https://server-url.no");
final RemoteService remoteService = factory.create(RemoteService.class);
final SomeType result = remoteService.someMethod(someValue);
```

### StaticService

"Normal" implementation of a remote interface, where the HTTP-endpoints for the remote methods are explicitly called
from each method of the interface, for comparison with the dynamic proxy variants.

## Usage

Se example of usage in `ApplicationTest`.