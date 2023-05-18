package no.toreb.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.toreb.common.RemoteMethodInvocation;
import no.toreb.common.RemoteMethodResult;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.remoting.httpinvoker.HttpComponentsHttpInvokerRequestExecutor;

import java.time.Duration;
import java.util.concurrent.Callable;

import static org.apache.http.conn.params.ConnManagerParams.DEFAULT_MAX_TOTAL_CONNECTIONS;
import static org.apache.http.conn.params.ConnPerRouteBean.DEFAULT_MAX_CONNECTIONS_PER_ROUTE;

/**
 * HTTP-client configured similar to {@link HttpComponentsHttpInvokerRequestExecutor}, to be able to compare
 * performance with Spring Remoting.
 */
@SuppressWarnings("deprecation")
class AbstractRemoteService {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final RequestConfig requestConfig;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ObjectMapper objectMapper = new ObjectMapper();

    protected AbstractRemoteService(final String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = createDefaultHttpClient();
        this.requestConfig = RequestConfig.custom().setSocketTimeout(60 * 1000).build();
    }

    private static HttpClient createDefaultHttpClient() {
        final Registry<ConnectionSocketFactory> schemeRegistry =
                RegistryBuilder.<ConnectionSocketFactory>create()
                               .register("http", PlainConnectionSocketFactory.getSocketFactory())
                               .register("https", SSLConnectionSocketFactory.getSocketFactory())
                               .build();

        final PoolingHttpClientConnectionManager connectionManager =
                new PoolingHttpClientConnectionManager(schemeRegistry);
        connectionManager.setMaxTotal(DEFAULT_MAX_TOTAL_CONNECTIONS);
        connectionManager.setDefaultMaxPerRoute(DEFAULT_MAX_CONNECTIONS_PER_ROUTE);

        return HttpClientBuilder.create().setConnectionManager(connectionManager).build();
    }

    protected <T> T callRemote(final RemoteMethodInvocation<T> methodInvocation) {
        return time(methodInvocation.getMethodName(), () -> {
            final HttpPost httpRequest = new HttpPost(baseUrl + getMethodPath(methodInvocation));
            try {
                final byte[] bodyBytes = serialize(methodInvocation);
                final ByteArrayEntity entity = new ByteArrayEntity(bodyBytes);
                entity.setContentType("application/x-java-serialized-object");
                httpRequest.setEntity(entity);
                httpRequest.setConfig(requestConfig);

                final HttpResponse response = httpClient.execute(httpRequest);
                return deserialize(response.getEntity());
            } finally {
                httpRequest.releaseConnection();
            }
        });
    }

    protected String getMethodPath(final RemoteMethodInvocation<?> methodInvocation) {
        return "/" + methodInvocation.getMethodName();
    }

    private <T> T time(final String name, final Callable<T> callable) {
        final long start = System.nanoTime();
        try {
            return callable.call();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            final Duration duration = Duration.ofNanos(System.nanoTime() - start);
            log.info("Client duration {}: {} ms", name, duration.toNanos() / 1_000_000.0);
        }
    }

    private byte[] serialize(final RemoteMethodInvocation<?> remoteMethodInvocation) throws Exception {
        return objectMapper.writeValueAsBytes(remoteMethodInvocation);
    }

    private <T> T deserialize(final HttpEntity httpEntity) throws Exception {
        if (httpEntity.getContentLength() == 0) {
            return null;
        }

        final RemoteMethodResult remoteMethodResult = objectMapper.readValue(httpEntity.getContent(),
                                                                             RemoteMethodResult.class);
        //noinspection unchecked
        return (T) objectMapper.readValue(remoteMethodResult.getValue(), remoteMethodResult.getType());
    }
}
