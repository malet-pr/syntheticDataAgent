package org.acme.syntheticdata.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum InventoryStatus {
    INSTOCK("INSTOCK"),
    LOWSTOCK("LOWSTOCK"),
    OUTOFSTOCK("OUTOFSTOCK"),;

    private final String value;

    InventoryStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}

