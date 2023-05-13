package no.toreb.common;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.util.List;

@Value
@Builder(toBuilder = true)
public class DataObject2 implements Serializable {

    String field1;
    List<String> strings;
}
