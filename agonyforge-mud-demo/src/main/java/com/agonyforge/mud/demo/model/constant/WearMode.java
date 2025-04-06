package com.agonyforge.mud.demo.model.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Determines how many slots an item takes when you wear it.
 */
public enum WearMode {
    ALL("all"), // item uses all the slots it has enabled
    SINGLE("single"); // item only uses a single slot, but it can be any of the enabled ones

    WearMode(String name){
        this.name = name;
    }
    @JsonValue
    final String name;


    @JsonCreator
    public static WearMode fromValue(String value) {
        for (WearMode wearMode : values()) {
            if (wearMode.getName().equalsIgnoreCase(value)) {
                return wearMode;
            }
        }
        throw new IllegalArgumentException("Unknown wear mode: " + value);
    }

    public String getName() {
        return name;
    }
}
