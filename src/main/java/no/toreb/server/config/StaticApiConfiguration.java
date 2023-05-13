package no.toreb.server.config;

import no.toreb.common.RemoteMethodInvocation;
import no.toreb.common.RemoteService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static no.toreb.server.config.ApiUtils.deserializeBody;
import static no.toreb.server.config.ApiUtils.serialize;
import static no.toreb.server.config.ApiUtils.time;
import static org.springframework.web.servlet.function.RouterFunctions.route;

@Configuration
class StaticApiConfiguration {

    @Bean
    RouterFunction<ServerResponse> staticApiRouterFunction(final RemoteService service) {
        return route()
                .POST("/static/hello",
                      request -> time(request.path(), () -> ServerResponse.ok().body(serialize(service.hello()))))
                .POST("/static/bye",
                      request -> time(request.path(), () -> ServerResponse.ok().body(serialize(service.bye()))))
                .POST("/static/doSomething",
                      request -> time(request.path(), () -> {
                          try {
                              final RemoteMethodInvocation<Void> methodInvocation = deserializeBody(request,
                                                                                                    Void.class);
                              final Object[] methodArguments = methodInvocation.getMethodArguments();
                              service.doSomething((String) methodArguments[0], (Boolean) methodArguments[1]);
                              return ServerResponse.ok().build();
                          } catch (final Exception e) {
                              throw new RuntimeException(e);
                          }
                      }))
                .build();
    }
}
