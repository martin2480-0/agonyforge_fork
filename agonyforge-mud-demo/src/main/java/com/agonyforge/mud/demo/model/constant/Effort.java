package com.agonyforge.mud.demo.model.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Effort {
    BASIC("Basic", 4),
    WEAPONS_N_TOOLS("Weapons & Tools", 6),
    GUNS("Guns", 8),
    ENERGY_N_MAGIC("Energy & Magic", 10),
    ULTIMATE("Ultimate", 12);

    private final String name;
    private final int die;

    Effort(String name, int die) {
        this.name = name;
        this.die = die;
    }

    @JsonValue
    public String getName() {
        return name;
    }


    @JsonCreator
    public static Effort fromValue(String value) {
        for (Effort effort : values()) {
            if (effort.getName().equalsIgnoreCase(value)) {
                return effort;
            }
        }
        throw new IllegalArgumentException("Unknown effort: " + value);
    }

    public int getDie() {
        return die;
    }
}
