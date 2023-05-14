package no.toreb.client.service;

import lombok.experimental.UtilityClass;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodHandles.privateLookupIn;

@UtilityClass
class ProxyUtils {

    static <T> T createProxy(final Class<T> type, final Object interceptor) {
        try (final DynamicType.Unloaded<T> unloaded = new ByteBuddy().subclass(type)
                                                                     .method(ElementMatchers.isDeclaredBy(type))
                                                                     .intercept(MethodDelegation.to(interceptor))
                                                                     .make()) {
            return unloaded.load(ProxyUtils.class.getClassLoader(),
                                 ClassLoadingStrategy.UsingLookup.of(privateLookupIn(type, lookup())))
                           .getLoaded()
                           .getDeclaredConstructor()
                           .newInstance();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
