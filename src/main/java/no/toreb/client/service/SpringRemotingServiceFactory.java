package no.toreb.client.service;

import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.remoting.httpinvoker.HttpComponentsHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

import java.time.Duration;
import java.util.function.Supplier;

@SuppressWarnings("deprecation")
public class SpringRemotingServiceFactory {

    private final String baseUrl;

    public SpringRemotingServiceFactory(final String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public <T> T create(final Class<T> type, final String path) {
        final LoggingHttpInvokerProxyFactoryBean factoryBean = new LoggingHttpInvokerProxyFactoryBean();
        factoryBean.setServiceUrl(baseUrl + path);
        factoryBean.setServiceInterface(type);
        factoryBean.setHttpInvokerRequestExecutor(new HttpComponentsHttpInvokerRequestExecutor());
        factoryBean.afterPropertiesSet();
        //noinspection unchecked
        return (T) factoryBean.getObject();
    }

    @Slf4j
    private static class LoggingHttpInvokerProxyFactoryBean extends HttpInvokerProxyFactoryBean {

        @Override
        public Object invoke(final MethodInvocation methodInvocation) {
            return time(methodInvocation.getMethod().getName(), () -> {
                try {
                    return super.invoke(methodInvocation);
                } catch (final Throwable e) {
                    throw new RuntimeException(e);
                }
            });
        }

        private <T> T time(final String name, final Supplier<T> supplier) {
            final long start = System.nanoTime();
            try {
                return supplier.get();
            } finally {
                final Duration duration = Duration.ofNanos(System.nanoTime() - start);
                log.info("Client duration {}: {} ms", name, duration.toNanos() / 1_000_000.0);
            }
        }
    }

}
