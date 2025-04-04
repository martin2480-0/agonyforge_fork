package com.agonyforge.mud.demo.model.export;

import com.agonyforge.mud.demo.cli.RepositoryBundle;
import com.agonyforge.mud.demo.model.constant.Effort;
import com.agonyforge.mud.demo.model.constant.Stat;
import com.agonyforge.mud.demo.model.constant.WearSlot;
import com.agonyforge.mud.demo.model.export.dataTransferObjects.CharacterDTO;
import com.agonyforge.mud.demo.model.export.dataTransferObjects.ItemDTO;
import com.agonyforge.mud.demo.model.impl.MudCharacter;
import com.agonyforge.mud.demo.model.impl.MudItem;
import com.agonyforge.mud.demo.model.repository.UserRepository;
import com.agonyforge.mud.demo.service.CommService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ImportExportService {

    private final RepositoryBundle repositoryBundle;
    private final CommService commService;

    @Autowired
    public ImportExportService(RepositoryBundle repositoryBundle, CommService commService) {
        this.repositoryBundle = repositoryBundle;
        this.commService = commService;
    }

    public CharacterDTO exportCharacter(MudCharacter ch) {
        String characterName = ch.getCharacter().getName();
        int hitPoints = ch.getCharacter().getHitPoints();
        int maxHitPoints = ch.getCharacter().getMaxHitPoints();
        int defense = ch.getCharacter().getDefense();
        String profession = ch.getCharacter().getProfession().getName();
        String species = ch.getCharacter().getSpecies().getName();
        String pronoun = ch.getCharacter().getPronoun().toString();
        List<ItemDTO> items = exportChItems(ch);
        Map<String, Integer> efforts = exportEfforts(ch);
        Map<String, Integer> stats = exportStats(ch);

        return new CharacterDTO(
            characterName, hitPoints, maxHitPoints, defense,
            profession, species, pronoun, items, efforts, stats);
    }

    private Map<String, Integer> exportEfforts(MudCharacter ch) {
        Map<String, Integer> efforts = new HashMap<>();
        Arrays.stream(Effort.values()).forEach(effort -> {
            int effortValue = ch.getCharacter().getEffort(effort);
            efforts.put(effort.getName(), effortValue);
        });
        return efforts;
    }

    private Map<String, Integer> exportStats(MudCharacter ch) {
        Map<String, Integer> stats = new HashMap<>();
        for (Stat stat : Stat.values()) {
            int effortValue = ch.getCharacter().getStat(stat);
            stats.put(stat.getName(), effortValue);
        }
        return stats;
    }


    public List<ItemDTO> exportChItems(MudCharacter ch) {
        List<MudItem> items = getRepositoryBundle().getItemRepository().findByLocationHeld(ch);

        List<MudItem> held = items
            .stream()
            .filter(item -> item.getLocation().getWorn().isEmpty()).toList();


        List<ItemDTO> itemsDTOs = new ArrayList<>();
        held.forEach(item -> {
            ItemDTO itemDTO = createItemDTO(item);
            itemsDTOs.add(itemDTO);
        });

        held.forEach(item -> {
            ItemDTO itemDTO = createItemDTO(item);
            itemsDTOs.add(itemDTO);
        });

        return itemsDTOs;

    }

    private ItemDTO createItemDTO(MudItem item) {
        String shortDescription = item.getItem().getShortDescription();
        String longDescription = item.getItem().getLongDescription();
        Set<String> nameList = item.getItem().getNameList();
        List<String> wearSlots = item.getItem().getWearSlots().stream().map(WearSlot::getName).toList();
        String wearMode = item.getItem().getWearMode().toString();
        return new ItemDTO(shortDescription, longDescription, nameList, wearSlots, wearMode);
    }

    private void exportAllItems() {
        // TODO finish
    }

    public boolean importCharacter(Principal principal, String yamlFileContent) {
        return false;
    }

    public void exportMap(){

    }

    public boolean importMap(){
        return false;
    }


    public RepositoryBundle getRepositoryBundle() {
        return repositoryBundle;
    }

    public CommService getCommService() {
        return commService;
    }
}
