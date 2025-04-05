package com.agonyforge.mud.demo.model.export.dataTransferObjects;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class CharacterDTO {
    public Long id;
    public String name;
    public int hitPoints;
    public int maxHitPoints;
    public int defense;
    public String profession;
    public String species;
    public String pronoun;
    public List<Long> items;
    public Map<String, Integer> efforts;
    public Map<String, Integer> stats;

    public CharacterDTO() {}

    public CharacterDTO(Long id, String name, int hitPoints, int maxHitPoints, int defense,
                        String profession, String species, String pronoun,
                        List<Long> items,
                        Map<String, Integer> efforts, Map<String, Integer> stats) {
        this.id = id;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHitPoints() {
        return hitPoints;
    }

    public void setHitPoints(int hitPoints) {
        this.hitPoints = hitPoints;
    }

    public int getMaxHitPoints() {
        return maxHitPoints;
    }

    public void setMaxHitPoints(int maxHitPoints) {
        this.maxHitPoints = maxHitPoints;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getPronoun() {
        return pronoun;
    }

    public void setPronoun(String pronoun) {
        this.pronoun = pronoun;
    }

    public List<Long> getItems() {
        return items;
    }

    public void setItems(List<Long> items) {
        this.items = items;
    }

    public Map<String, Integer> getEfforts() {
        return efforts;
    }

    public void setEfforts(Map<String, Integer> efforts) {
        this.efforts = efforts;
    }

    public Map<String, Integer> getStats() {
        return stats;
    }

    public void setStats(Map<String, Integer> stats) {
        this.stats = stats;
    }
}
