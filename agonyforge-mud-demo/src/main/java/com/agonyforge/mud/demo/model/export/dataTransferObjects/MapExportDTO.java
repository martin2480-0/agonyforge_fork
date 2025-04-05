package com.agonyforge.mud.demo.model.export.dataTransferObjects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapExportDTO {
    public List<RoomDTO> rooms;
    public List<CharacterDTO> characters;
    public List<ItemDTO> items;

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
        public Long id;
        public Long zoneId;
        public String description;
        public List<ExitDTO> exits;
        public List<Long> itemIds;
        public List<Long> characterIds; // means NPCs
        public Set<String> flags;

        public RoomDTO(Long id, Long zoneId, String description, List<ExitDTO> exits,
                       List<Long> itemIds, List<Long> characterIds, Set<String> flags) {
            this.id = id;
            this.zoneId = zoneId;
            this.description = description;
            this.exits = exits;
            this.itemIds = itemIds;
            this.characterIds = characterIds;
            this.flags = flags;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
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

        public Set<String> getFlags() {
            return flags;
        }

        public void setFlags(Set<String> flags) {
            this.flags = flags;
        }
    }

    public static class ExitDTO {
        public String direction;
        public Long destinationRoomId;

        public ExitDTO(String direction, Long destinationRoomId) {
            this.direction = direction;
            this.destinationRoomId = destinationRoomId;
        }

        public String getDirection() {
            return direction;
        }

        public Long getDestinationRoomId() {
            return destinationRoomId;
        }
    }
}
