package no.toreb.common;

import lombok.Value;

import java.io.Serializable;

@Value
public class MethodRequest<T> implements Serializable {

    String methodName;

    Class<T> returnType;

    Object[] methodArguments;
}
