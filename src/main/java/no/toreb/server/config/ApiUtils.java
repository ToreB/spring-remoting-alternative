package no.toreb.server.config;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import no.toreb.common.MethodRequest;
import org.springframework.web.servlet.function.ServerRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.util.function.Supplier;

@Slf4j
@UtilityClass
class ApiUtils {

    static <T> T time(final String name, final Supplier<T> supplier) {
        final long start = System.nanoTime();
        try {
            return supplier.get();
        } finally {
            final Duration duration =  Duration.ofNanos(System.nanoTime() - start);
            log.info("Server duration {}: {} ms", name, duration.toNanos() / 1_000_000.0);
        }
    }

    static byte[] serialize(final Object object) {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        return byteArrayOutputStream.toByteArray();
    }

    static <T> MethodRequest<T> deserializeBody(final ServerRequest request,
                                                final Class<T> responseType) throws Exception {
        final byte[] body = request.body(byte[].class);
        if (body.length == 0) {
            return new MethodRequest<>(null, responseType, new Object[0]);
        }

        final ByteArrayInputStream bais = new ByteArrayInputStream(body);
        final ObjectInputStream ois = new ObjectInputStream(bais);
        @SuppressWarnings("unchecked")
        final MethodRequest<T> result = (MethodRequest<T>) ois.readObject();
        ois.close();
        return result;
    }
}
