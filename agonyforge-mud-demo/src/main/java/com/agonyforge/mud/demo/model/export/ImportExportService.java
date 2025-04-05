package com.agonyforge.mud.demo.model.export;

import com.agonyforge.mud.demo.cli.RepositoryBundle;
import com.agonyforge.mud.demo.model.constant.Effort;
import com.agonyforge.mud.demo.model.constant.Stat;
import com.agonyforge.mud.demo.model.constant.WearSlot;
import com.agonyforge.mud.demo.model.export.dataTransferObjects.*;
import com.agonyforge.mud.demo.model.impl.*;
import com.agonyforge.mud.demo.service.CommService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
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

    public String exportCharacter(MudCharacter ch) throws JsonProcessingException {
        Long id = ch.getCharacter().getId();
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

        List<Long> itemIds = items.stream().map(ItemDTO::getItemId).collect(Collectors.toList());

        CharacterDTO characterDTO = new CharacterDTO(id,
            characterName, hitPoints, maxHitPoints, defense,
            profession, species, pronoun, itemIds, efforts, stats);

        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        return yamlMapper.writeValueAsString(characterDTO);
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
            ItemDTO itemDTO = createItemDTO(item.getItem());
            itemsDTOs.add(itemDTO);
        });

        held.forEach(item -> {
            ItemDTO itemDTO = createItemDTO(item.getItem());
            itemsDTOs.add(itemDTO);
        });

        return itemsDTOs;

    }

    private ItemDTO createItemDTO(ItemComponent itemComponent) {
        Long id = itemComponent.getId();
        String shortDescription = itemComponent.getShortDescription();
        String longDescription = itemComponent.getLongDescription();
        Set<String> nameList = itemComponent.getNameList();
        List<String> wearSlots = itemComponent.getWearSlots().stream().map(WearSlot::getName).toList();
        String wearMode = itemComponent.getWearMode().toString();

        return new ItemDTO(id, shortDescription, longDescription, nameList, wearSlots, wearMode);
    }

    private String exportAllItems() throws JsonProcessingException {
        List<MudItemTemplate> items = repositoryBundle.getItemPrototypeRepository().findAll();

        List<ItemDTO> itemDTOS = items.stream().map(
            item -> createItemDTO(item.getItem()))
            .collect(Collectors.toList());

        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        return yamlMapper.writeValueAsString(itemDTOS);

    }

    public boolean importCharacter(Principal principal, String yamlFileContent) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        MapExportDTO mapDTO = objectMapper.readValue(yamlFileContent, MapExportDTO.class);
        return false;
    }

    public boolean importItem(ItemDTO itemDTO){
        return false;
    }

    public String exportMapAsYaml() throws JsonProcessingException {
        List<MudRoom> rooms = repositoryBundle.getRoomRepository().findAll();

        List<MapExportDTO.RoomDTO> roomDTOs = new ArrayList<>();
        List<ItemDTO> items = new ArrayList<>();
        List<CharacterDTO> characters = new ArrayList<>();


        rooms.forEach(room -> {
            Long id = room.getId();
            Long zoneId = room.getZoneId();
            String description = room.getDescription();

            List<MapExportDTO.ExitDTO> exitDTOs = new ArrayList<>();
            List<ItemDTO> roomItems = new ArrayList<>();
            List<CharacterDTO> roomCharacters = new ArrayList<>(); // for now stays empty until NPCs are finished
            Set<String> flags = new HashSet<>();

            room.getExitsMap().forEach((direction, exitInstance) -> {
                MapExportDTO.ExitDTO exitDTO = new MapExportDTO.ExitDTO(direction, exitInstance.getDestinationId());
                exitDTOs.add(exitDTO);
            });

            repositoryBundle.getItemRepository().findByLocationRoom(room).forEach(item -> {
                ItemDTO itemDTO = createItemDTO(item.getItem());
                roomItems.add(itemDTO);
            });

            List<Long> roomItemIds = roomItems.stream().map(ItemDTO::getItemId).toList();

            List<Long> roomCharacterIds = roomCharacters.stream().map(CharacterDTO::getId).toList();


            room.getFlags().forEach(flag -> {
                flags.add(flag.getDescription());
            });

            MapExportDTO.RoomDTO roomDTO = new MapExportDTO.RoomDTO(id, zoneId, description, exitDTOs, roomItemIds, roomCharacterIds, flags);
            roomDTOs.add(roomDTO);

            items.addAll(roomItems);
            characters.addAll(roomCharacters);

        });
        MapExportDTO mapExportDTO = new MapExportDTO(roomDTOs, characters ,items);

        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        return yamlMapper.writeValueAsString(mapExportDTO);

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
