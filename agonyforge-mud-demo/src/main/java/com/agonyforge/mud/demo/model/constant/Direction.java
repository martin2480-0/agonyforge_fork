package com.agonyforge.mud.demo.model.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Direction {
    NORTH("north", "south"),
    EAST("east", "west"),
    SOUTH("south", "north"),
    WEST("west", "east"),
    UP("up", "down"),
    DOWN("down", "up"),
    NORTHEAST("northeast", "southwest"),
    NORTHWEST("northwest", "southeast"),
    SOUTHEAST("southeast", "northwest"),
    SOUTHWEST("southwest", "northeast");

    private final String name;
    private final String opposite;

    Direction(String name, String opposite) {
        this.name = name;
        this.opposite = opposite;
    }

    @JsonValue
    public String getName() {
        return name;
    }

    @JsonCreator
    public static Direction fromValue(String value) {
        for (Direction direction : values()) {
            if (direction.getName().equalsIgnoreCase(value)) {
                return direction;
            }
        }
        throw new IllegalArgumentException("Unknown direction: " + value);
    }


    public String getOpposite() {
        return opposite;
    }
}
