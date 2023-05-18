package no.toreb.server.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import no.toreb.common.RemoteMethodInvocation;
import no.toreb.common.RemoteMethodResult;
import no.toreb.common.RemoteService;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

@Slf4j
@UtilityClass
class ApiUtils {

    private static final MediaType CONTENT_TYPE = new MediaType("application", "x-java-serialized-object");
    static final String CONTENT_TYPE_VALUE = "%s/%s".formatted(CONTENT_TYPE.getType(), CONTENT_TYPE.getSubtype());

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static <T> T time(final String name, final Callable<T> callable) {
        final long start = System.nanoTime();
        try {
            return callable.call();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            final Duration duration = Duration.ofNanos(System.nanoTime() - start);
            log.info("Server duration {}: {} ms", name, duration.toNanos() / 1_000_000.0);
        }
    }

    static byte[] serialize(final Object object) {
        try {
            final byte[] objectBytes = objectMapper.writeValueAsBytes(object);
            return objectMapper.writeValueAsBytes(new RemoteMethodResult(object.getClass(), objectBytes));
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    static void serialize(final Object object, final OutputStream outputStream) {
        try {
            outputStream.write(serialize(object));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    static <T> RemoteMethodInvocation<T> deserializeBody(final ServerRequest request) throws Exception {
        final byte[] body = request.body(byte[].class);
        if (body.length == 0) {
            return new RemoteMethodInvocation<>(null, null, new Class[0], new Object[0]);
        }

        //noinspection unchecked
        return objectMapper.readValue(body, RemoteMethodInvocation.class);
    }

    static <T> RemoteMethodInvocation<T> deserialize(final InputStream inputStream) throws Exception {
        //noinspection unchecked
        return objectMapper.readValue(inputStream, RemoteMethodInvocation.class);
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
