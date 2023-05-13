package no.toreb.server.config;

import no.toreb.common.RemoteService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;

import static no.toreb.server.config.ApiUtils.time;

@Configuration
public class SpringRemotingConfiguration {

    @SuppressWarnings("deprecation")
    @Bean("/remoting/remoteService")
    HttpInvokerServiceExporter remoteServiceExporter(final RemoteService remoteService) {
        final HttpInvokerServiceExporter exporter = new HttpInvokerServiceExporter();
        exporter.setService(remoteService);
        exporter.setServiceInterface(RemoteService.class);
        exporter.setInterceptors(new Object[]{new TimingInterceptor()});
        return exporter;
    }

    private static class TimingInterceptor implements MethodInterceptor {

        @Override
        public Object invoke(final MethodInvocation invocation) {
            return time(invocation.getMethod().getName(), () -> {
                try {
                    return invocation.proceed();
                } catch (final Throwable e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

}
