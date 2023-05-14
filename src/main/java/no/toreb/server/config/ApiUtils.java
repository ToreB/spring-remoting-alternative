package no.toreb.server.config;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import no.toreb.common.RemoteMethodInvocation;
import no.toreb.common.RemoteService;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
@UtilityClass
class ApiUtils {

    static final MediaType CONTENT_TYPE = new MediaType("application", "x-java-serialized-object");
    static final String CONTENT_TYPE_VALUE = "%s/%s".formatted(CONTENT_TYPE.getType(), CONTENT_TYPE.getSubtype());

    static <T> T time(final String name, final Supplier<T> supplier) {
        final long start = System.nanoTime();
        try {
            return supplier.get();
        } finally {
            final Duration duration = Duration.ofNanos(System.nanoTime() - start);
            log.info("Server duration {}: {} ms", name, duration.toNanos() / 1_000_000.0);
        }
    }

    static byte[] serialize(final Object object) {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        serialize(object, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    static void serialize(final Object object, final OutputStream outputStream) {
        try {
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    static <T> RemoteMethodInvocation<T> deserializeBody(final ServerRequest request) throws Exception {
        final byte[] body = request.body(byte[].class);
        if (body.length == 0) {
            return new RemoteMethodInvocation<>(null, null, new Object[0]);
        }

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);
        return deserialize(byteArrayInputStream);
    }

    static <T> RemoteMethodInvocation<T> deserialize(final InputStream inputStream) throws Exception {
        final ObjectInputStream ois = new ObjectInputStream(inputStream);
        @SuppressWarnings("unchecked")
        final RemoteMethodInvocation<T> result = (RemoteMethodInvocation<T>) ois.readObject();
        ois.close();
        return result;
    }

    static List<Method> getRemoteServiceMethods(final RemoteService service) {
        return Arrays.stream(service.getClass().getMethods())
                     .filter(method -> Arrays.stream(method.getDeclaringClass().getInterfaces())
                                             .anyMatch(it -> it == RemoteService.class))
                     .toList();
    }

    static ServerResponse serverResponse(final Object body) {
        final ServerResponse.BodyBuilder responseBuilder = ServerResponse.ok();
        if (body != null) {
            return responseBuilder.contentType(CONTENT_TYPE)
                                  .body(serialize(body));
        } else {
            return responseBuilder.build();
        }
    }
}
