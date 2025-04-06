package com.agonyforge.mud.demo.model.constant;

import com.agonyforge.mud.demo.model.util.BaseEnumSetConverter;
import com.agonyforge.mud.demo.model.util.PersistentEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RoomFlag implements PersistentEnum {
    INDOORS("indoors", "room is indoors");

    private final String name;
    private final String description;

    RoomFlag(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @JsonValue
    public String getName() {
        return name;
    }

    @JsonCreator
    public static RoomFlag fromValue(String value) {
        for (RoomFlag cc : values()) {
            if (cc.getName().equalsIgnoreCase(value)) {
                return cc;
            }
        }
        throw new IllegalArgumentException("Unknown room flag: " + value);
    }

    public static class Converter extends BaseEnumSetConverter<RoomFlag> {
        public Converter() {
            super(RoomFlag.class);
        }
    }
}
