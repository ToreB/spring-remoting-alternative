package no.toreb.client.service;

import no.toreb.common.RemoteMethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.function.Supplier;

class AbstractRemoteService {

    private final String baseUrl;
    private final HttpClient httpClient;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected AbstractRemoteService(final String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder().build();
    }

    protected <T> T callRemote(final RemoteMethodInvocation<T> methodInvocation) {
        return time(methodInvocation.getMethodName(), () -> {
            try {
                final byte[] bodyBytes = serialize(methodInvocation);
                final HttpRequest httpRequest =
                        HttpRequest.newBuilder()
                                   .uri(new URI(baseUrl + "/" + methodInvocation.getMethodName()))
                                   .POST(BodyPublishers.ofByteArray(bodyBytes))
                                   .build();

                final HttpResponse<byte[]> response = httpClient.send(httpRequest, BodyHandlers.ofByteArray());
                return deserialize(response.body());
            } catch (final Exception e) {
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

    private byte[] serialize(final RemoteMethodInvocation<?> remoteMethodInvocation) throws Exception {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

        objectOutputStream.writeObject(remoteMethodInvocation);
        objectOutputStream.flush();
        objectOutputStream.close();

        return byteArrayOutputStream.toByteArray();
    }

    private <T> T deserialize(final byte[] body) throws Exception {
        if (body.length == 0) {
            return null;
        }

        final ByteArrayInputStream bais = new ByteArrayInputStream(body);
        final ObjectInputStream ois = new ObjectInputStream(bais);
        @SuppressWarnings("unchecked")
        final T result = (T) ois.readObject();
        ois.close();
        return result;
    }
}
