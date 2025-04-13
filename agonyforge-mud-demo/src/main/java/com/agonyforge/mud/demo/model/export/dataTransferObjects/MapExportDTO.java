package com.agonyforge.mud.demo.model.export.dataTransferObjects;

import com.agonyforge.mud.demo.model.constant.Direction;
import com.agonyforge.mud.demo.model.constant.RoomFlag;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Set;

public class MapExportDTO {
    @JsonProperty("rooms")
    public List<RoomDTO> rooms;

    @JsonProperty("characters")
    public List<CharacterDTO> characters;

    @JsonProperty("items")
    public List<ItemDTO> items;


    public MapExportDTO() {}

    public MapExportDTO(List<RoomDTO> rooms, List<CharacterDTO> characters, List<ItemDTO> items) {
        this.rooms = rooms;
        this.characters = characters;
        this.items = items;
    }

    public List<RoomDTO> getRooms() {
        return rooms;
    }

    public void setRooms(List<RoomDTO> rooms) {
        this.rooms = rooms;
    }

    public List<CharacterDTO> getCharacters() {
        return characters;
    }

    public void setCharacters(List<CharacterDTO> characters) {
        this.characters = characters;
    }

    public List<ItemDTO> getItems() {
        return items;
    }

    public void setItems(List<ItemDTO> items) {
        this.items = items;
    }

    public static class RoomDTO {
        private Long roomId;
        private Long zoneId;
        private String name;
        private String description;
        private List<ExitDTO> exits;
        private List<Long> itemIds;
        private List<Long> characterIds; // means NPCs
        private Set<RoomFlag> flags;

        public RoomDTO() {}

        public RoomDTO(Long id, Long zoneId,String name, String description, List<ExitDTO> exits,
                       List<Long> itemIds, List<Long> characterIds, Set<RoomFlag> flags) {
            this.roomId = id;
            this.zoneId = zoneId;
            this.name = name;
            this.description = description;
            this.exits = exits;
            this.itemIds = itemIds;
            this.characterIds = characterIds;
            this.flags = flags;
        }

        public Long getRoomId() {
            return roomId;
        }

        public void setRoomId(Long roomId) {
            this.roomId = roomId;
        }

        public Long getZoneId() {
            return zoneId;
        }

        public void setZoneId(Long zoneId) {
            this.zoneId = zoneId;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<ExitDTO> getExits() {
            return exits;
        }

        public void setExits(List<ExitDTO> exits) {
            this.exits = exits;
        }

        public List<Long> getItemIds() {
            return itemIds;
        }

        public void setItemIds(List<Long> itemIds) {
            this.itemIds = itemIds;
        }

        public List<Long> getCharacterIds() {
            return characterIds;
        }

        public void setCharacterIds(List<Long> characterIds) {
            this.characterIds = characterIds;
        }

        public Set<RoomFlag> getFlags() {
            return flags;
        }

        public void setFlags(Set<RoomFlag> flags) {
            this.flags = flags;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class ExitDTO {
        public Direction direction;
        public Long destinationRoomId;

        public ExitDTO() {}

        public ExitDTO(Direction direction, Long destinationRoomId) {
            this.direction = direction;
            this.destinationRoomId = destinationRoomId;
        }

        public ExitDTO(String direction, Long destinationRoomId) {
            this.direction = Direction.fromValue(direction);
            this.destinationRoomId = destinationRoomId;
        }

        public Direction getDirection() {
            return direction;
        }

        public Long getDestinationRoomId() {
            return destinationRoomId;
        }

        public void setDirection(Direction direction) {
            this.direction = direction;
        }

        public void setDestinationRoomId(Long destinationRoomId) {
            this.destinationRoomId = destinationRoomId;
        }
    }
}
