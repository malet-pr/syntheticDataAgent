package org.acme.syntheticdata.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CustomerStatus {
    QUALIFIED("QUALIFIED"),
    UNQUALIFIED("UNQUALIFIED"),
    NEW("NEW");

    private final String value;

    CustomerStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}

