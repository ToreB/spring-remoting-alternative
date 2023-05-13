package no.toreb.common;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;

@Value
@Builder(toBuilder = true)
public class DataObject implements Serializable {

    String field1;
    Integer field2;
    Boolean field3;
    DataObject2 field4;
}
