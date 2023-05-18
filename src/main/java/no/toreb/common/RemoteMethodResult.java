package no.toreb.common;

import lombok.Value;

@Value
public class RemoteMethodResult {

    Class<?> type;
    byte[] value;

}
