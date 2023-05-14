package no.toreb.server.config;

import no.toreb.common.RemoteMethodInvocation;
import no.toreb.common.RemoteService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions.Builder;
import org.springframework.web.servlet.function.ServerResponse;

import java.lang.reflect.Method;
import java.util.List;

import static no.toreb.server.config.ApiUtils.deserializeBody;
import static no.toreb.server.config.ApiUtils.getRemoteServiceMethods;
import static no.toreb.server.config.ApiUtils.serverResponse;
import static no.toreb.server.config.ApiUtils.time;
import static org.springframework.web.servlet.function.RouterFunctions.route;

@Configuration
class DynamicApiWithRouterFunctionConfiguration {

    @Bean
    RouterFunction<ServerResponse> dynamicApiRouterFunction(final RemoteService service) {
        final List<Method> methods = getRemoteServiceMethods(service);

        final Builder route = route();
        methods.forEach(method -> {
            route.POST("/dynamic/" + method.getName(), handle(method, service));
        });

        return route.build();
    }

    static HandlerFunction<ServerResponse> handle(final Method method, final RemoteService service) {
        return request -> time(request.path(), () -> {
            try {
                final RemoteMethodInvocation<?> methodInvocation = deserializeBody(request);
                final Object invokeResult = method.invoke(service, methodInvocation.getMethodArguments());
                return serverResponse(invokeResult);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
