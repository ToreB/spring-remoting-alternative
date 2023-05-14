package no.toreb.client.service;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import no.toreb.common.RemoteMethodInvocation;

import java.lang.reflect.Method;

public class DynamicWithRouterFunctionServiceFactory {

    private final RemoteServiceInterceptor interceptor;

    public DynamicWithRouterFunctionServiceFactory(final String baseUrl) {
        this.interceptor = new RemoteServiceInterceptor(baseUrl + "/dynamic");
    }

    public <T> T create(final Class<T> type) {
        return ProxyUtils.createProxy(type, interceptor);
    }

    public static class RemoteServiceInterceptor extends AbstractRemoteService {

        public RemoteServiceInterceptor(final String baseUrl) {
            super(baseUrl);
        }

        @RuntimeType
        public Object intercept(@Origin final Method method,
                                @AllArguments final Object[] args) {
            return callRemote(new RemoteMethodInvocation<>(method.getName(),
                                                           method.getReturnType(),
                                                           method.getParameterTypes(),
                                                           args));
        }
    }
}
