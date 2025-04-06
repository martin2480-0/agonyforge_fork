package com.agonyforge.mud.demo.model.export.dataTransferObjects;

import com.agonyforge.mud.demo.model.constant.WearMode;
import com.agonyforge.mud.demo.model.constant.WearSlot;

import java.util.List;
import java.util.Set;

public class ItemDTO {
    private Long itemId;
    private String shortDescription;
    private String longDescription;
    private Set<String> itemNames;
    private List<WearSlot> wearSlots;
    private WearMode wearMode;

    public ItemDTO() {

    }

    public ItemDTO(Long ItemId, String shortDescription, String longDescription, Set<String> itemNames, List<WearSlot> wearSlots, WearMode wearMode) {
        this.itemId = ItemId;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.itemNames = itemNames;
        this.wearSlots = wearSlots;
        this.wearMode = wearMode;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public Set<String> getItemNames() {
        return itemNames;
    }

    public void setItemNames(Set<String> itemNames) {
        this.itemNames = itemNames;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public List<WearSlot> getWearSlots() {
        return wearSlots;
    }

    public void setWearSlots(List<WearSlot> wearSlots) {
        this.wearSlots = wearSlots;
    }

    public WearMode getWearMode() {
        return wearMode;
    }

    public void setWearMode(WearMode wearMode) {
        this.wearMode = wearMode;
    }
}
