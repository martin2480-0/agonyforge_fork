package com.agonyforge.mud.demo.model.export.dataTransferObjects;

import java.util.List;

public class CharacterExportDTO {

    private CharacterDTO character;
    private List<ItemDTO> items;

    public CharacterExportDTO() {}

    public CharacterExportDTO(CharacterDTO character, List<ItemDTO> items) {
        this.character = character;
        this.items = items;
    }

    public CharacterDTO getCharacter() {
        return character;
    }

    public List<ItemDTO> getItems() {
        return items;
    }

    public void setCharacter(CharacterDTO character) {
        this.character = character;
    }

    public void setItems(List<ItemDTO> items) {
        this.items = items;
    }

}
