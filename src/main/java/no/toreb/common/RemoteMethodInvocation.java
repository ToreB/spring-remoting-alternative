package no.toreb.common;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Value
@JsonDeserialize(using = RemoteMethodInvocation.CustomDeserializer.class)
public class RemoteMethodInvocation<T> implements Serializable {

    String methodName;

    Class<T> returnType;

    Class<?>[] parameterTypes;

    Object[] arguments;

    static class CustomDeserializer extends JsonDeserializer<RemoteMethodInvocation<?>> {

        @Override
        public RemoteMethodInvocation<?> deserialize(final JsonParser p, final DeserializationContext ctxt)
                throws IOException {
            final JsonNode node = p.getCodec().readTree(p);
            final String methodNameValue = node.get("methodName").asText();
            final Class<Object> returnTypeValue = getClass(node.get("returnType").asText());
            final List<Class<?>> parameterTypesValue = new ArrayList<>();
            node.get("parameterTypes").forEach(value -> parameterTypesValue.add(getClass(value.asText())));

            final List<Object> argumentsValue = new ArrayList<>();
            final Iterator<JsonNode> argumentsIterator = node.get("arguments").iterator();
            final JsonFactory jsonFactory = new JsonFactory(p.getCodec());
            for (int i = 0; argumentsIterator.hasNext(); i++) {
                final Class<?> type = parameterTypesValue.get(i);
                final JsonNode argument = argumentsIterator.next();
                try (final JsonParser parser = jsonFactory.createParser(argument.toString())) {
                    argumentsValue.add(parser.readValueAs(type));
                }
            }

            return new RemoteMethodInvocation<>(methodNameValue,
                                                returnTypeValue,
                                                parameterTypesValue.toArray(Class[]::new),
                                                argumentsValue.toArray());
        }

        @SuppressWarnings("unchecked")
        private <T> Class<T> getClass(final String className) {
            if ("boolean".equals(className)) {
                return (Class<T>) boolean.class;
            }
            if ("void".equals(className)) {
                return (Class<T>) void.class;
            }
            // TODO: rest of primitives and their array-variants

            try {
                return (Class<T>) Class.forName(className);
            } catch (final ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
