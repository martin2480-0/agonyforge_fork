package com.agonyforge.mud.demo.model.export.dataTransferObjects;

import java.util.List;
import java.util.Map;

public class GameMapDTO {
    public List<RoomDTO> rooms;

    public GameMapDTO(List<RoomDTO> rooms) {
        this.rooms = rooms;
    }

    public static class RoomDTO {
        public Long id;
        public String name;
        public String description;
        public List<ExitDTO> exits;
        public List<String> items;
        public List<String> characters;

        public RoomDTO(Long id, String name, String description, List<ExitDTO> exits,
                       List<String> items, List<String> characters) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.exits = exits;
            this.items = items;
            this.characters = characters;
        }
    }

    public static class ExitDTO {
        public String direction;
        public Long destinationRoomId;

        public ExitDTO(String direction, Long destinationRoomId) {
            this.direction = direction;
            this.destinationRoomId = destinationRoomId;
        }
    }
}
