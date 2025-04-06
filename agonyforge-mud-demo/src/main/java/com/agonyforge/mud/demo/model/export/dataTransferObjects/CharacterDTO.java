package com.agonyforge.mud.demo.model.export.dataTransferObjects;

import com.agonyforge.mud.demo.config.ProfessionLoader;
import com.agonyforge.mud.demo.model.constant.Effort;
import com.agonyforge.mud.demo.model.constant.Pronoun;
import com.agonyforge.mud.demo.model.constant.Stat;
import com.agonyforge.mud.demo.model.constant.WearSlot;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CharacterDTO {
    @JsonProperty("character_id")
    private Long id;

    private String name;

    private int hitPoints;

    private int maxHitPoints;

    private int defense;

    private String profession;

    private String species;

    private Pronoun pronoun;

    @JsonProperty("item_ids")
    private List<Long> items;

    private Map<Effort, Integer> efforts;

    private Map<Stat, Integer> stats;

    private Set<WearSlot> wearSlots;

    private Set<String> roles;

    public CharacterDTO() {}

    public CharacterDTO(Long id, String name, Set<String> roles, int hitPoints, int maxHitPoints, int defense,
                        String profession, String species, Pronoun pronoun,
                        List<Long> items,
                        Map<Effort, Integer> efforts, Map<Stat, Integer> stats, Set<WearSlot> wearSlots) {
        this.id = id;
        this.name = name;
        this.roles = roles;
        this.hitPoints = hitPoints;
        this.maxHitPoints = maxHitPoints;
        this.defense = defense;
        this.profession = profession;
        this.species = species;
        this.pronoun = pronoun;
        this.items = items;
        this.efforts = efforts;
        this.stats = stats;
        this.wearSlots = wearSlots;
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

    public Pronoun getPronoun() {
        return pronoun;
    }

    public void setPronoun(Pronoun pronoun) {
        this.pronoun = pronoun;
    }

    public List<Long> getItems() {
        return items;
    }

    public void setItems(List<Long> items) {
        this.items = items;
    }

    public Map<Effort, Integer> getEfforts() {
        return efforts;
    }

    public void setEfforts(Map<Effort, Integer> efforts) {
        this.efforts = efforts;
    }

    public Map<Stat, Integer> getStats() {
        return stats;
    }

    public void setStats(Map<Stat, Integer> stats) {
        this.stats = stats;
    }

    public Set<WearSlot> getWearSlots() {
        return wearSlots;
    }

    public void setWearSlots(Set<WearSlot> wearSlots) {
        this.wearSlots = wearSlots;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
