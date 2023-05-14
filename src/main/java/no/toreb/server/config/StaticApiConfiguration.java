package no.toreb.server.config;

import no.toreb.common.DataObject;
import no.toreb.common.RemoteMethodInvocation;
import no.toreb.common.RemoteService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static no.toreb.server.config.ApiUtils.deserializeBody;
import static no.toreb.server.config.ApiUtils.serverResponse;
import static no.toreb.server.config.ApiUtils.time;
import static org.springframework.web.servlet.function.RouterFunctions.route;

@Configuration
class StaticApiConfiguration {

    @Bean
    RouterFunction<ServerResponse> staticApiRouterFunction(final RemoteService service) {
        return route()
                .POST("/static/hello",
                      request -> time("/static/hello", () -> serverResponse(service.hello())))
                .POST("/static/bye",
                      request -> time("/static/bye", () -> serverResponse(service.bye())))
                .POST("/static/doSomething",
                      request -> time("/static/doSomething", () -> {
                          try {
                              final RemoteMethodInvocation<Void> methodInvocation = deserializeBody(request);
                              final Object[] methodArguments = methodInvocation.getArguments();
                              service.doSomething((String) methodArguments[0], (Boolean) methodArguments[1]);
                              return serverResponse(null);
                          } catch (final Exception e) {
                              throw new RuntimeException(e);
                          }
                      }))
                .POST("/static/exchange",
                      request -> time("/static/exchange", () -> {
                          try {
                              final RemoteMethodInvocation<DataObject> methodInvocation = deserializeBody(request);
                              final DataObject result =
                                      service.exchange((DataObject) methodInvocation.getArguments()[0]);
                              return serverResponse(result);
                          } catch (final Exception e) {
                              throw new RuntimeException(e);
                          }
                      }))
                .build();
    }
}
