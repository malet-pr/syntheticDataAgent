package org.acme.syntheticdata.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SequenceInfo (
        @JsonProperty("table_name")
        String tableName,
        @JsonProperty("sequence_name")
        String sequenceName,
        @JsonProperty("sequence_number")
        Integer sequenceNumber
) {
}
