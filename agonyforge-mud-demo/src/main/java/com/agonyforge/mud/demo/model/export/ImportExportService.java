package com.agonyforge.mud.demo.model.export;

import com.agonyforge.mud.demo.cli.RepositoryBundle;
import com.agonyforge.mud.demo.model.constant.*;
import com.agonyforge.mud.demo.model.export.dataTransferObjects.*;
import com.agonyforge.mud.demo.model.impl.*;
import com.agonyforge.mud.demo.model.repository.*;
import com.agonyforge.mud.demo.service.CommService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ImportExportService {

    private final RepositoryBundle repositoryBundle;
    private final CommService commService;
    private final RoleRepository roleRepository;
    final private String tmpDir = System.getProperty("java.io.tmpdir");
    Path agonyForgePath = Paths.get(tmpDir, "agonyforge");

    @Autowired
    public ImportExportService(RepositoryBundle repositoryBundle, CommService commService, RoleRepository roleRepository) {
        this.repositoryBundle = repositoryBundle;
        this.commService = commService;
        this.roleRepository = roleRepository;
    }

    private String exportCharacterAsYAML(MudCharacter ch) throws JsonProcessingException {
        Long id = ch.getCharacter().getId();
        String characterName = ch.getCharacter().getName();
        int hitPoints = ch.getCharacter().getHitPoints();
        int maxHitPoints = ch.getCharacter().getMaxHitPoints();
        int defense = ch.getCharacter().getDefense();
        String profession = ch.getCharacter().getProfession().getName();
        String species = ch.getCharacter().getSpecies().getName();
        Set<String> role = ch.getPlayer().getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        Pronoun pronoun = ch.getCharacter().getPronoun();
        List<ItemDTO> items = exportChItems(ch);
        Map<Effort, Integer> efforts = exportEfforts(ch);
        Map<Stat, Integer> stats = exportStats(ch);
        Set<WearSlot> wearSlots = ch.getCharacter().getWearSlots();

        List<Long> itemIds = items.stream().map(ItemDTO::getItemId).collect(Collectors.toList());

        CharacterDTO characterDTO = new CharacterDTO(id,
            characterName, role, hitPoints, maxHitPoints, defense,
            profession, species, pronoun, itemIds, efforts, stats, wearSlots);

        CharacterExportDTO characterExportDTO = new CharacterExportDTO(characterDTO, items);

        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        return yamlMapper.writeValueAsString(characterExportDTO);
    }

    private Map<Effort, Integer> exportEfforts(MudCharacter ch) {
        Map<Effort, Integer> efforts = new HashMap<>();
        Arrays.stream(Effort.values()).forEach(effort -> {
            int effortValue = ch.getCharacter().getEffort(effort);
            efforts.put(effort, effortValue);
        });
        return efforts;
    }

    private Map<Stat, Integer> exportStats(MudCharacter ch) {
        Map<Stat, Integer> stats = new HashMap<>();
        for (Stat stat : Stat.values()) {
            int effortValue = ch.getCharacter().getStat(stat);
            stats.put(stat, effortValue);
        }
        return stats;
    }


    private List<ItemDTO> exportChItems(MudCharacter ch) {
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
        List<WearSlot> wearSlots = itemComponent.getWearSlots().stream().toList();
        WearMode wearMode = itemComponent.getWearMode();

        return new ItemDTO(id, shortDescription, longDescription, nameList, wearSlots, wearMode);
    }

    private String exportAllItemsAsYAML() throws JsonProcessingException {
        List<MudItemTemplate> items = repositoryBundle.getItemPrototypeRepository().findAll();

        List<ItemDTO> itemDTOS = items.stream().map(
            item -> createItemDTO(item.getItem()))
            .collect(Collectors.toList());

        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        return yamlMapper.writeValueAsString(itemDTOS);

    }
    @Transactional
    public boolean importCharacter(String principal, String yamlFileContent) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        MapExportDTO mapDTO = objectMapper.readValue(yamlFileContent, MapExportDTO.class);
        return false;
    }

    private MudItemTemplate importItem(ItemDTO itemDTO){
        MudItemTemplate item = new MudItemTemplate();

        item.setItem(new ItemComponent());
        item.getItem().setNameList(itemDTO.getItemNames());
        item.getItem().setShortDescription(itemDTO.getShortDescription());
        item.getItem().setLongDescription(itemDTO.getLongDescription());
        item.getItem().setWearSlots(EnumSet.copyOf(itemDTO.getWearSlots()));
        item.getItem().setWearMode(itemDTO.getWearMode());

        return item;
    }

    public MudCharacterTemplate importPlayer(CharacterDTO characterDTO, PlayerComponent playerComponent) throws JsonProcessingException {
        return importCharacter(characterDTO, playerComponent);
    }

    public MudCharacterTemplate importNPC(CharacterDTO characterDTO) throws JsonProcessingException {
        return importCharacter(characterDTO, null);
    }


    private MudCharacterTemplate importCharacter(CharacterDTO characterDTO, PlayerComponent playerComponent) throws JsonProcessingException {
        MudProfessionRepository mudProfessionRepository = repositoryBundle.getProfessionRepository();
        MudSpeciesRepository mudSpeciesRepository = repositoryBundle.getSpeciesRepository();

        MudCharacterTemplate ch = new MudCharacterTemplate();

        if (playerComponent != null) {
            ch.setPlayer(playerComponent);
        }else {
            ch.setNonPlayer(new NonPlayerComponent());
        }

        ch.setCharacter(new CharacterComponent());
        ch.getCharacter().setName(characterDTO.getName());
        ch.getCharacter().setPronoun(characterDTO.getPronoun());
        ch.getCharacter().setWearSlots(EnumSet.copyOf(characterDTO.getWearSlots()));
        ch.getCharacter().setSpecies(mudSpeciesRepository.getMudSpeciesByName(characterDTO.getSpecies()));
        ch.getCharacter().setProfession(mudProfessionRepository.getMudProfessionByName(characterDTO.getProfession()));

        Map<Stat, Integer> stats = characterDTO.getStats();

        Map<Effort, Integer> efforts = characterDTO.getEfforts();

        Arrays.stream(Stat.values())
            .forEach(stat -> ch.getCharacter().setBaseStat(stat, stats.get(stat)));
        Arrays.stream(Effort.values())
            .forEach(effort -> ch.getCharacter().setBaseEffort(effort, efforts.get(effort)));

        return ch;
    }

    @Transactional
    public boolean importItems(String yamlFileContent) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        List<ItemDTO> itemDTOS = objectMapper.readValue(yamlFileContent, List.class);
        List<MudItemTemplate> templates = new ArrayList<>();
        var itemPrototypeRepository = repositoryBundle.getItemPrototypeRepository();

        for (ItemDTO itemDTO : itemDTOS) {
            templates.add(importItem(itemDTO));
        }

        itemPrototypeRepository.saveAll(templates);

        return false;
    }

    private MudRoom importRoom(MapExportDTO.RoomDTO roomDTO) {
        MudRoom room = new MudRoom();

        room.setId(roomDTO.getRoomId());
        room.setZoneId(roomDTO.getZoneId());
        room.setName(roomDTO.getName());
        room.setDescription(roomDTO.getDescription());
        for (MapExportDTO.ExitDTO exit: roomDTO.getExits()) {
            room.setExit(exit.getDirection().getName(), new MudRoom.Exit(exit.getDestinationRoomId()));
        }

        return room;

    }

    private String exportMapAsYAML() throws JsonProcessingException {
        List<MudRoom> rooms = repositoryBundle.getRoomRepository().findAll();

        List<MapExportDTO.RoomDTO> roomDTOs = new ArrayList<>();
        List<ItemDTO> items = new ArrayList<>();
        List<CharacterDTO> characters = new ArrayList<>();


        rooms.forEach(room -> {
            Long id = room.getId();
            Long zoneId = room.getZoneId();
            String name = room.getName();
            String description = room.getDescription();

            List<MapExportDTO.ExitDTO> exitDTOs = new ArrayList<>();
            List<ItemDTO> roomItems = new ArrayList<>();
            List<CharacterDTO> roomCharacters = new ArrayList<>(); // for now stays empty until NPCs are finished

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


            Set<RoomFlag> flags = new HashSet<>(room.getFlags());

            MapExportDTO.RoomDTO roomDTO = new MapExportDTO.RoomDTO(id, zoneId, name, description, exitDTOs, roomItemIds, roomCharacterIds, flags);
            roomDTOs.add(roomDTO);

            items.addAll(roomItems);
            characters.addAll(roomCharacters);

        });
        MapExportDTO mapExportDTO = new MapExportDTO(roomDTOs, characters ,items);

        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        return yamlMapper.writeValueAsString(mapExportDTO);

    }

    public MudCharacter importPlayerCharacter(Principal principal, String yamlFileContent) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        CharacterExportDTO characterExportDTO = objectMapper.readValue(yamlFileContent, CharacterExportDTO.class);
        MudItemRepository mudItemRepository = repositoryBundle.getItemRepository();
        MudCharacterRepository mudCharacterRepository = repositoryBundle.getCharacterRepository();
        MudItemPrototypeRepository itemPrototypeRepository = repositoryBundle.getItemPrototypeRepository();
        RoleRepository roleRepository = getRoleRepository();

        List<MudItemTemplate> itemTemplates = new ArrayList<>();

        for (ItemDTO itemDTO : characterExportDTO.getItems()) {
            itemTemplates.add(importItem(itemDTO));
        }

        itemPrototypeRepository.saveAll(itemTemplates);

        var characterDTO = characterExportDTO.getCharacter();

        Set<Role> roles = new HashSet<>();
        for (String roleNames: characterDTO.getRoles()){
            Optional<Role> role = roleRepository.findByName(roleNames);
            if (role.isPresent()) {
                roles.add(role.get());
            }else {
                throw new Exception(); // TODO handle error gracefully
            }
        }


        PlayerComponent playerComponent = new PlayerComponent();
        playerComponent.setTitle(characterDTO.getName());
        playerComponent.setUsername(principal.getName());
        playerComponent.setRoles(roles);


        MudCharacterTemplate character = importPlayer(characterDTO, playerComponent);

        MudCharacter mudCharacter = character.buildInstance();

        mudCharacterRepository.save(mudCharacter);

        for (MudItemTemplate itemTemplate : itemTemplates) {
            MudItem instance = itemTemplate.buildInstance();
            instance.getLocation().setHeld(mudCharacter);
            mudItemRepository.save(instance);
        }

        return mudCharacter;
    }

    @Transactional
    public void importMap(String yamlFileContent) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        MapExportDTO mapExportDTO = objectMapper.readValue(yamlFileContent, MapExportDTO.class);
        MudItemPrototypeRepository itemPrototypeRepository = repositoryBundle.getItemPrototypeRepository();
        MudCharacterPrototypeRepository characterPrototypeRepository = repositoryBundle.getCharacterPrototypeRepository();
        MudRoomRepository roomRepository = repositoryBundle.getRoomRepository();
        MudItemRepository mudItemRepository = repositoryBundle.getItemRepository();
        MudCharacterRepository mudCharacterRepository = repositoryBundle.getCharacterRepository();

        List<MudItemTemplate> itemTemplates = new ArrayList<>();
        List<MudCharacterTemplate> characterTemplates = new ArrayList<>();
        List<MudRoom> rooms = new ArrayList<>();

        // items load
        for (ItemDTO itemDTO : mapExportDTO.getItems()) {
            itemTemplates.add(importItem(itemDTO));
        }

        // characters load
        for (CharacterDTO characterDTO : mapExportDTO.getCharacters()) {
            characterTemplates.add(importNPC(characterDTO));
        }

        HashMap<Long, MudRoom> itemToRoom = new HashMap<>();
        HashMap<Long, MudRoom> chToRoom = new HashMap<>();

        // room load
        for (MapExportDTO.RoomDTO roomDTO : mapExportDTO.getRooms()) {
            MudRoom room = importRoom(roomDTO);
            roomDTO.getItemIds().forEach(
                item -> itemToRoom.put(item, room));
            roomDTO.getCharacterIds().forEach(
                character -> chToRoom.put(character, room));
            rooms.add(room);
        }

        roomRepository.saveAll(rooms);

        // items save
        itemPrototypeRepository.saveAll(itemTemplates);

        for (MudItemTemplate itemTemplate : itemTemplates) {
            MudItem instance = itemTemplate.buildInstance();
            instance.getLocation().setRoom(itemToRoom.get(instance.getId()));
            mudItemRepository.save(instance);
        }


        // characters save
        characterPrototypeRepository.saveAll(characterTemplates);

        for (MudCharacterTemplate characterTemplate : characterTemplates) {
            MudCharacter instance = characterTemplate.buildInstance();
            instance.getLocation().setRoom(chToRoom.get(instance.getId()));
            mudCharacterRepository.save(instance);
        }

    }

    private void writeYamlToFile(String yamlFileContent, String principal, String type) throws IOException {
        String fileName = String.format("download_%s_%s_%s.yaml", principal, type, UUID.randomUUID());

        Path filePath = agonyForgePath.resolve(fileName);

        Files.createDirectories(agonyForgePath);

        Files.write(filePath, yamlFileContent.getBytes());
    }
    
    public void export(String type, Principal principal, MudCharacter ch) throws IOException {
        
        switch (type){
            case "items" -> {
                String content = exportAllItemsAsYAML();
                writeYamlToFile(content, principal.getName(), "items");
            }
            case "character" -> {
                String content = exportCharacterAsYAML(ch);
                writeYamlToFile(content, principal.getName(), "character");
            }
            case "map" -> {
                String command = exportMapAsYAML();
                writeYamlToFile(command, principal.getName(), "map");
            }
        }
    }

    public RepositoryBundle getRepositoryBundle() {
        return repositoryBundle;
    }

    public CommService getCommService() {
        return commService;
    }

    public RoleRepository getRoleRepository() {
        return roleRepository;
    }
    
}
