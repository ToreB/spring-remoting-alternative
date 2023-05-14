package no.toreb.server.config;

import lombok.AllArgsConstructor;
import lombok.Value;
import no.toreb.common.RemoteMethodInvocation;
import no.toreb.common.RemoteService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.HttpRequestHandler;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Collectors;

import static no.toreb.server.config.ApiUtils.CONTENT_TYPE_VALUE;
import static no.toreb.server.config.ApiUtils.deserialize;
import static no.toreb.server.config.ApiUtils.getRemoteServiceMethods;
import static no.toreb.server.config.ApiUtils.serialize;
import static no.toreb.server.config.ApiUtils.time;

@Configuration
public class DynamicApiWithHttpRequestHandlerConfiguration {

    @Bean("/dynamic2/remoteService")
    HttpRequestHandler dynamicApiRequestHandler(final RemoteService service) {
        final Map<MethodKey, Method> methods = getRemoteServiceMethods(service)
                                                       .stream()
                                                       .collect(Collectors.toMap(MethodKey::new, method -> method));
        return (request, response) -> time("/dynamic2/remoteService", () -> {
            try {
                final ServletInputStream bodyInputStream = request.getInputStream();
                final RemoteMethodInvocation<?> methodInvocation = deserialize(bodyInputStream);
                final Method method = methods.get(new MethodKey(methodInvocation.getMethodName(),
                                                                methodInvocation.getParameterTypes()));
                final Object invokeResult = method.invoke(service, methodInvocation.getArguments());

                response.setContentType(CONTENT_TYPE_VALUE);
                response.setStatus(HttpServletResponse.SC_OK);
                if (invokeResult != null) {
                    serialize(invokeResult, response.getOutputStream());
                }
                return null;
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Value
    @AllArgsConstructor
    private static class MethodKey {

        String name;
        Class<?>[] parameterTypes;

        MethodKey(final Method method) {
            this.name = method.getName();
            this.parameterTypes = method.getParameterTypes();
        }
    }
}
