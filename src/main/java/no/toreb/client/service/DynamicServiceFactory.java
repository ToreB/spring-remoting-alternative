package no.toreb.client.service;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType.Unloaded;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy.UsingLookup;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.matcher.ElementMatchers;
import no.toreb.common.MethodRequest;

import java.lang.reflect.Method;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodHandles.privateLookupIn;

public class DynamicServiceFactory {

    private final RemoteServiceInterceptor interceptor;

    public DynamicServiceFactory(final String baseUrl) {
        this.interceptor = new RemoteServiceInterceptor(baseUrl + "/dynamic");
    }

    public <T> T create(final Class<T> type) {
        try (final Unloaded<T> unloaded = new ByteBuddy().subclass(type)
                                                         .method(ElementMatchers.isDeclaredBy(type))
                                                         .intercept(MethodDelegation.to(interceptor))
                                                         .make()) {
            return unloaded.load(getClass().getClassLoader(),
                                 UsingLookup.of(privateLookupIn(type, lookup())))
                           .getLoaded()
                           .getDeclaredConstructor()
                           .newInstance();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class RemoteServiceInterceptor extends AbstractRemoteService {

        public RemoteServiceInterceptor(final String baseUrl) {
            super(baseUrl);
        }

        @RuntimeType
        public Object intercept(@Origin final Method method,
                                @AllArguments final Object[] args) {
            return callRemote(new MethodRequest<>(method.getName(), method.getReturnType(), args));
        }
    }
}
