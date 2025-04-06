package com.agonyforge.mud.demo.model.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Stat {
    STR("Strength", "STR"),
    DEX("Dexterity", "DEX"),
    CON("Constitution", "CON"),
    INT("Intelligence", "INT"),
    WIS("Wisdom", "WIS"),
    CHA("Charisma", "CHA");

    private final String name;
    private final String abbreviation;

    Stat(String name, String abbreviation) {
        this.name = name;
        this.abbreviation = abbreviation;
    }

    @JsonCreator
    public static Stat fromValue(String value) {
        for (Stat stat : values()) {
            if (stat.getName().equalsIgnoreCase(value)) {
                return stat;
            }
        }
        throw new IllegalArgumentException("Unknown stat: " + value);
    }

    @JsonValue
    public String getName() {
        return name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }
}
