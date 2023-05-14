package no.toreb.common;

import lombok.Value;

import java.io.Serializable;

@Value
public class RemoteMethodInvocation<T> implements Serializable {

    String methodName;

    Class<T> returnType;

    Class<?>[] parameterTypes;

    Object[] arguments;
}
