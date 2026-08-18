package org.acme.syntheticdata.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderStatus {
    PENDING("PENDING"),
    DELIVERED("DELIVERED"),
    RETURNED("RETURNED"),
    CANCELLED("CANCELLED"),;

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}