package com.agonyforge.mud.demo.model.export;

import com.agonyforge.mud.core.web.model.WebSocketContext;
import com.agonyforge.mud.demo.cli.RepositoryBundle;
import com.agonyforge.mud.demo.model.constant.*;
import com.agonyforge.mud.demo.model.impl.*;
import com.agonyforge.mud.demo.model.repository.*;
import com.agonyforge.mud.demo.service.CommService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.agonyforge.mud.core.config.SessionConfiguration.MUD_CHARACTER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ImportExportServiceTest {

    private static final Random RANDOM = new Random();

    @Mock
    private RepositoryBundle repositoryBundle;

    @Mock
    private CommService commService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private WebSocketContext wsContext;

    @Mock
    private MudCharacter ch, target;

    @Mock
    private CharacterComponent chCharacter, targetCharacter;

    @Mock
    private PlayerComponent chPlayer, targetPlayer;

    @Mock
    private MudCharacterRepository mudCharacterRepository;

    @Mock
    private MudCharacterPrototypeRepository mudCharacterPrototypeRepository;

    @Mock
    private MudItemPrototypeRepository mudItemPrototypeRepository;

    @Mock
    private MudProfessionRepository mudProfessionRepository;

    @Mock
    private MudSpeciesRepository mudSpeciesRepository;

    @Mock
    private MudRoomRepository roomRepository;

    @Mock
    private MudItemRepository mudItemRepository;

    @Mock
    private MudRoom room, destination;

    @Mock
    private MudSpecies species;

    @Mock
    private MudProfession profession;

    @Mock
    private LocationComponent chLocation, targetLocation;

    @Mock
    private Role playerRole, implementorRole;

    @BeforeEach
    void setUp() {
        Long chId = RANDOM.nextLong();
        Long targetId = RANDOM.nextLong();

        lenient().when(wsContext.getAttributes()).thenReturn(Map.of(MUD_CHARACTER, chId));

        lenient().when(repositoryBundle.getCharacterRepository()).thenReturn(mudCharacterRepository);

        lenient().when(repositoryBundle.getCharacterRepository()).thenReturn(mudCharacterRepository);
        lenient().when(mudCharacterRepository.findById(eq(chId))).thenReturn(Optional.of(ch));
        lenient().when(ch.getLocation()).thenReturn(chLocation);
        lenient().when(chLocation.getRoom()).thenReturn(destination);
        lenient().when(ch.getCharacter()).thenReturn(chCharacter);
        lenient().when(chCharacter.getName()).thenReturn("Scion");
        lenient().when(ch.getPlayer()).thenReturn(chPlayer);

        lenient().when(mudCharacterRepository.findById(eq(targetId))).thenReturn(Optional.of(target));
        lenient().when(target.getLocation()).thenReturn(targetLocation);
        lenient().when(targetLocation.getRoom()).thenReturn(room);
        lenient().when(target.getCharacter()).thenReturn(targetCharacter);
        lenient().when(targetCharacter.getName()).thenReturn("Target");
        lenient().when(targetCharacter.getPronoun()).thenReturn(Pronoun.THEY);
        lenient().when(target.getPlayer()).thenReturn(targetPlayer);

    }

    @Test
    void testItemsImport() {
        Mockito.mock(ObjectMapper.class);
        String yamlString = """
            ---
            - itemId: 402
              shortDescription: "a spoon"
              longDescription: "A spoon is floating in midair here."
              itemNames:
              - "spoon"
              wearSlots: []
              wearMode: "all"
            - itemId: 404
              shortDescription: "a floppy hat"
              longDescription: "A floppy hat has been dropped here."
              itemNames:
              - "floppy"
              - "hat"
              wearSlots:
              - "head"
              wearMode: "all"
            """;

        when(repositoryBundle.getItemPrototypeRepository()).thenReturn(mudItemPrototypeRepository);

        ImportExportService importExportService = new ImportExportService(repositoryBundle, commService, roleRepository);

        var imported = importExportService.importItems(yamlString);
        assertTrue(imported);

        ArgumentCaptor<List<MudItemTemplate>> captor = ArgumentCaptor.forClass(List.class);
        verify(mudItemPrototypeRepository).saveAll(captor.capture());

        List<MudItemTemplate> savedTemplates = captor.getValue();
        assertEquals(2, savedTemplates.size());

        MudItemTemplate firstItem = savedTemplates.get(0);


        assertEquals("a spoon", firstItem.getItem().getShortDescription());
        assertEquals("A spoon is floating in midair here.", firstItem.getItem().getLongDescription());
        assertTrue(firstItem.getItem().getNameList().contains("spoon"));
        assertTrue(firstItem.getItem().getWearSlots().isEmpty());
        assertEquals(WearMode.ALL, firstItem.getItem().getWearMode());

        MudItemTemplate secondItem = savedTemplates.get(1);

        assertEquals("a floppy hat", secondItem.getItem().getShortDescription());
        assertEquals("A floppy hat has been dropped here.", secondItem.getItem().getLongDescription());
        assertTrue(secondItem.getItem().getNameList().contains("floppy") && secondItem.getItem().getNameList().contains("hat"));
        assertTrue(secondItem.getItem().getWearSlots().contains(WearSlot.HEAD));
        assertEquals(WearMode.ALL, secondItem.getItem().getWearMode());

    }

    @Test
    void testItemsExport() {
        MudItemTemplate spoon = new MudItemTemplate();
        spoon.setItem(new ItemComponent());
        spoon.getItem().setId(402L);
        spoon.getItem().setShortDescription("a spoon");
        spoon.getItem().setLongDescription("A spoon is floating in midair here.");
        spoon.getItem().setNameList(Set.of("spoon"));
        spoon.getItem().setWearSlots(EnumSet.noneOf(WearSlot.class));
        spoon.getItem().setWearMode(WearMode.ALL);

        MudItemTemplate hat = new MudItemTemplate();
        hat.setItem(new ItemComponent());
        hat.getItem().setId(404L);
        hat.getItem().setShortDescription("a floppy hat");
        hat.getItem().setLongDescription("A floppy hat has been dropped here.");
        hat.getItem().setNameList(Set.of("floppy", "hat"));
        hat.getItem().setWearSlots(EnumSet.of(WearSlot.HEAD));
        hat.getItem().setWearMode(WearMode.ALL);


        when(repositoryBundle.getItemPrototypeRepository()).thenReturn(mudItemPrototypeRepository);
        when(mudItemPrototypeRepository.findAll()).thenReturn(List.of(spoon, hat));

        ImportExportService importExportService = new ImportExportService(repositoryBundle, commService, roleRepository);

        AtomicReference<String> exportedYamlReference = new AtomicReference<>();
        assertDoesNotThrow(() -> exportedYamlReference.set(importExportService.export("items", null)));

        String exportedYaml = exportedYamlReference.get();

        assertNotNull(exportedYaml);
        assertTrue(exportedYaml.contains("itemId: 402"));
        assertTrue(exportedYaml.contains("shortDescription: \"a spoon\""));
        assertTrue(exportedYaml.contains("longDescription: \"A spoon is floating in midair here.\""));
        assertTrue(exportedYaml.contains("- \"spoon\""));
        assertTrue(exportedYaml.contains("wearSlots: []"));
        assertTrue(exportedYaml.contains("wearMode: \"all\""));

        assertTrue(exportedYaml.contains("itemId: 404"));
        assertTrue(exportedYaml.contains("shortDescription: \"a floppy hat\""));
        assertTrue(exportedYaml.contains("longDescription: \"A floppy hat has been dropped here.\""));
        assertTrue(exportedYaml.contains("- \"floppy\""));
        assertTrue(exportedYaml.contains("- \"hat\""));
        assertTrue(exportedYaml.contains("wearSlots:\n  - \"head\""));
        assertTrue(exportedYaml.contains("wearMode: \"all\""));
    }


    @Test
    void testItemsInvalidImport() {
        Mockito.mock(ObjectMapper.class);
        String yamlString = """
            ---
            - itemId: 402
              shortDescription: "a spoon"
            """;

        lenient().when(repositoryBundle.getItemPrototypeRepository()).thenReturn(mudItemPrototypeRepository);

        ImportExportService importExportService = new ImportExportService(repositoryBundle, commService, roleRepository);

        var imported = importExportService.importItems(yamlString);
        assertFalse(imported);

        verifyNoInteractions(mudItemPrototypeRepository);

    }

    @Test
    void testMapImport() {
        // no items and character, separate test
        String yamlString = """
            ---
            rooms:
              - roomId: 100
                zoneId: 1
                name: "Default Room"
                description: "This room was automatically generated."
                exits:
                  - direction: "west"
                    destinationRoomId: 101
                itemIds: []
                characterIds: []
                flags: []
              - roomId: 101
                zoneId: 1
                name: "Adjacent Room"
                description: "This room was automatically generated."
                exits:
                  - direction: "east"
                    destinationRoomId: 100
                itemIds: []
                characterIds: []
                flags: []
            
            characters: []
            items: []
            
            """;

        when(repositoryBundle.getItemRepository()).thenReturn(mudItemRepository);
        when(repositoryBundle.getCharacterRepository()).thenReturn(mudCharacterRepository);
        when(repositoryBundle.getItemPrototypeRepository()).thenReturn(mudItemPrototypeRepository);
        when(repositoryBundle.getRoomRepository()).thenReturn(roomRepository);
        when(repositoryBundle.getCharacterPrototypeRepository()).thenReturn(mudCharacterPrototypeRepository);

        ImportExportService importExportService = new ImportExportService(repositoryBundle, commService, roleRepository);

        var imported = importExportService.importMap(yamlString);
        assertTrue(imported);

        ArgumentCaptor<List<MudItemTemplate>> itemCaptor = ArgumentCaptor.forClass(List.class);
        verify(mudItemPrototypeRepository).saveAll(itemCaptor.capture());

        List<MudItemTemplate> itemTemplates = itemCaptor.getValue();
        assertEquals(0, itemTemplates.size());

        ArgumentCaptor<List<MudCharacterTemplate>> characterCaptor = ArgumentCaptor.forClass(List.class);
        verify(mudCharacterPrototypeRepository).saveAll(characterCaptor.capture());

        List<MudCharacterTemplate> characterTemplates = characterCaptor.getValue();
        assertEquals(0, characterTemplates.size());

        ArgumentCaptor<List<MudRoom>> roomCaptor = ArgumentCaptor.forClass(List.class);
        verify(roomRepository).saveAll(roomCaptor.capture());

        List<MudRoom> rooms = roomCaptor.getValue();
        assertEquals(2, rooms.size());

        MudRoom firstRoom = rooms.get(0);

        assertEquals(1, firstRoom.getZoneId());
        assertEquals("Default Room", firstRoom.getName());
        assertEquals("This room was automatically generated.", firstRoom.getDescription());
        assertTrue(firstRoom.getFlags().isEmpty());

        MudRoom.Exit firstExit = firstRoom.getExitsMap().get("west");
        assertNotNull(firstExit);
        assertEquals(101, firstExit.getDestinationId());

        MudRoom secondRoom = rooms.get(1);

        assertEquals(1, secondRoom.getZoneId());
        assertEquals("Adjacent Room", secondRoom.getName());
        assertEquals("This room was automatically generated.", secondRoom.getDescription());
        assertTrue(secondRoom.getFlags().isEmpty());

        MudRoom.Exit secondExit = secondRoom.getExitsMap().get("east");
        assertNotNull(secondExit);
        assertEquals(100, secondExit.getDestinationId());

    }

    @Test
    void testMapInvalidImport() {
        String yamlString = """
            ---
            rooms:
              - roomId: 100
                zoneId: 1
                name: "Default Room"
                description: "This room was automatically generated."
                exits:
                  - direction: "west"
                    destinationRoomId: 101
            """;

        lenient().when(repositoryBundle.getItemRepository()).thenReturn(mudItemRepository);
        lenient().when(repositoryBundle.getCharacterRepository()).thenReturn(mudCharacterRepository);
        lenient().when(repositoryBundle.getItemPrototypeRepository()).thenReturn(mudItemPrototypeRepository);
        lenient().when(repositoryBundle.getRoomRepository()).thenReturn(roomRepository);
        lenient().when(repositoryBundle.getCharacterPrototypeRepository()).thenReturn(mudCharacterPrototypeRepository);

        ImportExportService importExportService = new ImportExportService(repositoryBundle, commService, roleRepository);

        var imported = importExportService.importMap(yamlString);
        assertFalse(imported);

        verifyNoInteractions(mudItemPrototypeRepository);
        verifyNoInteractions(mudCharacterPrototypeRepository);
        verifyNoInteractions(roomRepository);
        verifyNoInteractions(mudCharacterRepository);
        verifyNoInteractions(mudItemRepository);

    }

    @Test
    void testCharacterImport() {
        String principal = String.valueOf(RANDOM.nextLong());
        String yamlString = """
            ---
            character:
              name: "Scion"
              hitPoints: 10
              maxHitPoints: 10
              defense: 1
              profession: "Wizard"
              species: "Dwarf"
              pronoun: "ey/em"
              efforts:
                Weapons & Tools: 0
                Energy & Magic: 1
                Ultimate: 4
                Guns: 0
                Basic: 0
              stats:
                Strength: 1
                Dexterity: 0
                Constitution: 1
                Wisdom: 0
                Intelligence: 1
                Charisma: 6
              wearSlots:
              - "left finger"
              - "right finger"
              - "neck"
              - "body"
              - "head"
              - "legs"
              - "feet"
              - "hands"
              - "arms"
              - "waist"
              - "left wrist"
              - "right wrist"
              - "ears"
              - "eyes"
              - "face"
              - "left ankle"
              - "right ankle"
              - "off hand"
              - "main hand"
              roles:
              - "Implementor"
              - "Player"
              character_id: 652
              item_ids: []
            items: []
            """;


        when(repositoryBundle.getItemRepository()).thenReturn(mudItemRepository);
        when(repositoryBundle.getCharacterRepository()).thenReturn(mudCharacterRepository);
        when(repositoryBundle.getItemPrototypeRepository()).thenReturn(mudItemPrototypeRepository);
        when(roleRepository.findByName(eq("Player"))).thenReturn(Optional.of(playerRole));
        when(roleRepository.findByName(eq("Implementor"))).thenReturn(Optional.of(implementorRole));
        when(repositoryBundle.getSpeciesRepository()).thenReturn(mudSpeciesRepository);
        when(repositoryBundle.getProfessionRepository()).thenReturn(mudProfessionRepository);

        when(mudCharacterRepository.findByCharacterName(eq("Scion"))).thenReturn(Optional.empty());
        when(mudSpeciesRepository.getMudSpeciesByName(eq("Dwarf"))).thenReturn(species);
        when(mudProfessionRepository.getMudProfessionByName(eq("Wizard"))).thenReturn(profession);

        ImportExportService importExportService = new ImportExportService(repositoryBundle, commService, roleRepository);

        var imported = importExportService.importPlayerCharacter(principal, yamlString);
        assertTrue(imported);

        ArgumentCaptor<List<MudItemTemplate>> itemCaptor = ArgumentCaptor.forClass(List.class);
        verify(mudItemPrototypeRepository).saveAll(itemCaptor.capture());

        List<MudItemTemplate> itemTemplates = itemCaptor.getValue();
        assertEquals(0, itemTemplates.size());

        ArgumentCaptor<MudCharacter> characterCaptor = ArgumentCaptor.forClass(MudCharacter.class);
        verify(mudCharacterRepository).save(characterCaptor.capture());

        MudCharacter character = characterCaptor.getValue();

        assertEquals(character.getPlayer().getUsername(), principal);
        assertEquals(Set.of(playerRole, implementorRole), character.getPlayer().getRoles());

        assertEquals("Scion", character.getCharacter().getName());
        assertEquals(Pronoun.EY, character.getCharacter().getPronoun());
        assertEquals(EnumSet.allOf(WearSlot.class), character.getCharacter().getWearSlots());
        assertEquals(species, character.getCharacter().getSpecies());
        assertEquals(profession, character.getCharacter().getProfession());

        assertEquals(1, character.getCharacter().getBaseStat(Stat.STR));
        assertEquals(0, character.getCharacter().getBaseStat(Stat.DEX));
        assertEquals(1, character.getCharacter().getBaseStat(Stat.CON));
        assertEquals(0, character.getCharacter().getBaseStat(Stat.WIS));
        assertEquals(1, character.getCharacter().getBaseStat(Stat.INT));
        assertEquals(6, character.getCharacter().getBaseStat(Stat.CHA));

        assertEquals(0, character.getCharacter().getBaseEffort(Effort.WEAPONS_N_TOOLS));
        assertEquals(1, character.getCharacter().getBaseEffort(Effort.ENERGY_N_MAGIC));
        assertEquals(4, character.getCharacter().getBaseEffort(Effort.ULTIMATE));
        assertEquals(0, character.getCharacter().getBaseEffort(Effort.GUNS));
        assertEquals(0, character.getCharacter().getBaseEffort(Effort.BASIC));


    }

    @Test
    void testCharacterInvalidImport() {
        String principal = String.valueOf(RANDOM.nextLong());
        String badYamlString = """
            ---
            character:
              name: "Scion"
              hitPoints: 10
              maxHitPoints: 10
              defense: 1
              profession: "Wizard"
              species: "Dwarf"
              pronoun: "ey/em"
              efforts:
                Weapons & Tools: 0
                Energy & Magic: 1
                Ultimate: 4
                Guns: 
              - "Implementor"
              - "Player"
              character_id: 652
              item_ids: []
            items: []
            """;

        ImportExportService importExportService = new ImportExportService(repositoryBundle, commService, roleRepository);

        var imported = importExportService.importPlayerCharacter(principal, badYamlString);
        assertFalse(imported);

        verifyNoInteractions(mudItemRepository);
        verifyNoInteractions(mudCharacterRepository);
        verifyNoInteractions(mudItemPrototypeRepository);
        verifyNoInteractions(roleRepository);
        verifyNoInteractions(mudSpeciesRepository);
        verifyNoInteractions(mudProfessionRepository);


    }

    @Test
    void testImportPlayerCharacter() {
        String principal = String.valueOf(RANDOM.nextLong());

        PlayerComponent playerComponent = new PlayerComponent();
        playerComponent.setUsername(principal);
        playerComponent.setTitle("Scion");
        playerComponent.setRoles(Set.of(playerRole, implementorRole));
        when(playerRole.getName()).thenReturn("Player");
        when(implementorRole.getName()).thenReturn("Implementor");

        MudCharacter character = new MudCharacter();
        character.setPlayer(playerComponent);

        CharacterComponent characterComponent = new CharacterComponent();
        characterComponent.setName("Scion");
        characterComponent.setPronoun(Pronoun.EY);
        characterComponent.setSpecies(species);
        characterComponent.setProfession(profession);

        Map<Stat, Integer> stats = Map.of(
            Stat.STR, 1,
            Stat.DEX, 0,
            Stat.CON, 1,
            Stat.WIS, 0,
            Stat.INT, 1,
            Stat.CHA, 6
        );
        Map<Effort, Integer> efforts = Map.of(
            Effort.WEAPONS_N_TOOLS, 0,
            Effort.ENERGY_N_MAGIC, 1,
            Effort.ULTIMATE, 4,
            Effort.GUNS, 0,
            Effort.BASIC, 0
        );

        Arrays.stream(Stat.values())
            .forEach(stat -> characterComponent.setBaseStat(stat, stats.get(stat)));
        Arrays.stream(Effort.values())
            .forEach(effort -> characterComponent.setBaseEffort(effort, efforts.get(effort)));

        characterComponent.setWearSlots(EnumSet.allOf(WearSlot.class));

        character.setCharacter(characterComponent);

        ImportExportService importExportService = new ImportExportService(repositoryBundle, commService, roleRepository);

        when(profession.getName()).thenReturn("Wizard");
        when(species.getName()).thenReturn("Dwarf");
        when(repositoryBundle.getItemRepository()).thenReturn(mudItemRepository);
        when(repositoryBundle.getItemRepository().findByLocationHeld(character)).thenReturn(List.of());

        AtomicReference<String> exportedYamlReference = new AtomicReference<>();
        assertDoesNotThrow(() -> exportedYamlReference.set(importExportService.export("character", character)));

        String exportedYaml = exportedYamlReference.get();

        assertNotNull(exportedYaml);

        assertTrue(exportedYaml.contains("Scion"));
        assertTrue(exportedYaml.contains("ey/em"));
        assertTrue(exportedYaml.contains("Wizard"));
        assertTrue(exportedYaml.contains("Dwarf"));

        assertTrue(exportedYaml.contains("left finger"));
        assertTrue(exportedYaml.contains("right finger"));
        assertTrue(exportedYaml.contains("neck"));
        assertTrue(exportedYaml.contains("body"));
        assertTrue(exportedYaml.contains("head"));
        assertTrue(exportedYaml.contains("legs"));
        assertTrue(exportedYaml.contains("feet"));
        assertTrue(exportedYaml.contains("hands"));
        assertTrue(exportedYaml.contains("arms"));
        assertTrue(exportedYaml.contains("waist"));
        assertTrue(exportedYaml.contains("left wrist"));
        assertTrue(exportedYaml.contains("right wrist"));
        assertTrue(exportedYaml.contains("ears"));
        assertTrue(exportedYaml.contains("eyes"));
        assertTrue(exportedYaml.contains("face"));
        assertTrue(exportedYaml.contains("left ankle"));
        assertTrue(exportedYaml.contains("right ankle"));
        assertTrue(exportedYaml.contains("off hand"));
        assertTrue(exportedYaml.contains("main hand"));

        assertTrue(exportedYaml.contains("Strength: 1"));
        assertTrue(exportedYaml.contains("Dexterity: 0"));
        assertTrue(exportedYaml.contains("Constitution: 1"));
        assertTrue(exportedYaml.contains("Wisdom: 0"));
        assertTrue(exportedYaml.contains("Intelligence: 1"));
        assertTrue(exportedYaml.contains("Charisma: 6"));

        assertTrue(exportedYaml.contains("Weapons & Tools: 0"));
        assertTrue(exportedYaml.contains("Energy & Magic: 1"));
        assertTrue(exportedYaml.contains("Ultimate: 4"));
        assertTrue(exportedYaml.contains("Guns: 0"));
        assertTrue(exportedYaml.contains("Basic: 0"));

        assertTrue(exportedYaml.contains("Implementor"));
        assertTrue(exportedYaml.contains("Player"));
    }

    @Test
    void testInvalidType(){
        ImportExportService importExportService = new ImportExportService(repositoryBundle, commService, roleRepository);

        AtomicReference<String> exportedYamlReference = new AtomicReference<>();
        assertDoesNotThrow(() -> exportedYamlReference.set(importExportService.export("test", null)));

        String errorYaml = exportedYamlReference.get();

        assertEquals("---", errorYaml);
        verifyNoInteractions(repositoryBundle);
        verifyNoInteractions(commService);
        verifyNoInteractions(roleRepository);
    }

    @Test
    void testExportMap() {
        MudRoom room1 = new MudRoom();
        room1.setId(100L);
        room1.setZoneId(1L);
        room1.setName("Default Room");
        room1.setDescription("This room was automatically generated.");
        room1.setFlags(EnumSet.noneOf(RoomFlag.class));
        room1.setExit("west",new MudRoom.Exit(101L));

        MudRoom room2 = new MudRoom();
        room2.setId(101L);
        room2.setZoneId(1L);
        room2.setName("Adjacent Room");
        room2.setDescription("This room was automatically generated.");
        room2.setFlags(EnumSet.noneOf(RoomFlag.class));
        room2.setExit("east",new MudRoom.Exit(100L));

        when(repositoryBundle.getRoomRepository()).thenReturn(roomRepository);

        when(roomRepository.findAll()).thenReturn(List.of(room1, room2));
        when(repositoryBundle.getItemRepository()).thenReturn(mudItemRepository);

        when(mudItemRepository.findByLocationRoom(any())).thenReturn(List.of());

        ImportExportService importExportService = new ImportExportService(repositoryBundle, commService, roleRepository);

        AtomicReference<String> exportedYamlReference = new AtomicReference<>();
        assertDoesNotThrow(() -> exportedYamlReference.set(importExportService.export("map", null)));

        String exportedYaml = exportedYamlReference.get();

        assertNotNull(exportedYaml);
        assertTrue(exportedYaml.contains("Default Room"));
        assertTrue(exportedYaml.contains("Adjacent Room"));
        assertTrue(exportedYaml.contains("rooms:"));
        assertTrue(exportedYaml.contains("zoneId: 1"));
        assertTrue(exportedYaml.contains("This room was automatically generated."));
        assertTrue(exportedYaml.contains("direction: \"west\""));
        assertTrue(exportedYaml.contains("destinationRoomId: 101"));
        assertTrue(exportedYaml.contains("direction: \"east\""));
        assertTrue(exportedYaml.contains("destinationRoomId: 100"));
        assertTrue(exportedYaml.contains("characters: []"));
        assertTrue(exportedYaml.contains("items: []"));
        assertTrue(exportedYaml.contains("flags: []"));
    }
}
