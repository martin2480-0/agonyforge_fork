package com.agonyforge.mud.demo.model.export.dataTransferObjects;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class CharacterDTO {
    public String name;
    public int hitPoints;
    public int maxHitPoints;
    public int defense;
    public String profession;
    public String species;
    public String pronoun;
    public List<ItemDTO> items;
    public Map<String, Integer> efforts;
    public Map<String, Integer> stats;

    public CharacterDTO() {}

    public CharacterDTO(String name, int hitPoints, int maxHitPoints, int defense,
                        String profession, String species, String pronoun,
                        List<ItemDTO> items,
                        Map<String, Integer> efforts, Map<String, Integer> stats) {
        this.name = name;
        this.hitPoints = hitPoints;
        this.maxHitPoints = maxHitPoints;
        this.defense = defense;
        this.profession = profession;
        this.species = species;
        this.pronoun = pronoun;
        this.items = items;
        this.efforts = efforts;
        this.stats = stats;
    }
}
