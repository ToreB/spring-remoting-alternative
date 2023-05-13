package no.toreb.server.config;

import no.toreb.common.RemoteMethodInvocation;
import no.toreb.common.RemoteService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions.Builder;
import org.springframework.web.servlet.function.ServerResponse;
import org.springframework.web.servlet.function.ServerResponse.BodyBuilder;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static no.toreb.server.config.ApiUtils.deserializeBody;
import static no.toreb.server.config.ApiUtils.serialize;
import static no.toreb.server.config.ApiUtils.time;
import static org.springframework.web.servlet.function.RouterFunctions.route;

@Configuration
class DynamicApiConfiguration {

    @Bean
    RouterFunction<ServerResponse> dynamicApiRouterFunction(final RemoteService service) {
        final List<Method> methods =
                Arrays.stream(service.getClass().getMethods())
                      .filter(method -> Arrays.stream(method.getDeclaringClass().getInterfaces())
                                              .anyMatch(it -> it == RemoteService.class))
                      .toList();

        final Builder route = route();
        methods.forEach(method -> {
            route.POST("/dynamic/" + method.getName(), handle(method, service));
        });

        return route.build();
    }

    static HandlerFunction<ServerResponse> handle(final Method method, final RemoteService service) {
        return request -> time(request.path(), () -> {
            try {
                final RemoteMethodInvocation<?> methodInvocation = deserializeBody(request, method.getReturnType());
                final Object invokeResult = method.invoke(service, methodInvocation.getMethodArguments());
                final BodyBuilder response = ServerResponse.ok();
                if (invokeResult != null) {
                    return response.body(serialize(invokeResult));
                } else {
                    return response.build();
                }
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
