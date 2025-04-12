package com.agonyforge.mud.demo.model.export;

import com.agonyforge.mud.core.cli.Question;
import com.agonyforge.mud.core.web.model.WebSocketContext;
import com.agonyforge.mud.demo.cli.RepositoryBundle;
import com.agonyforge.mud.demo.model.constant.Pronoun;
import com.agonyforge.mud.demo.model.constant.WearMode;
import com.agonyforge.mud.demo.model.constant.WearSlot;
import com.agonyforge.mud.demo.model.impl.*;
import com.agonyforge.mud.demo.model.repository.MudCharacterRepository;
import com.agonyforge.mud.demo.model.repository.MudItemPrototypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static com.agonyforge.mud.core.config.SessionConfiguration.MUD_CHARACTER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class ImportExportServiceTest {

    private static final Random RANDOM = new Random();

    @Mock
    private RepositoryBundle repositoryBundle;

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
    private ImportExportService importExportService;

    @Mock
    private MudItemPrototypeRepository mudItemPrototypeRepository;

    @Mock
    private MudRoom room, destination;

    @Mock
    private LocationComponent chLocation, targetLocation;

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

        boolean imported = importExportService.importItems(yamlString);
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

    void testMapImport() {
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
                  itemIds:
                  - 403
                  characterIds: []
                  flags: []
                - roomId: 101
                  zoneId: 1
                  name: "Adjacent Room"
                  description: "This room was automatically generated."
                  exits:
                  - direction: "east"
                    destinationRoomId: 100
                  itemIds:
                  - 405
                  characterIds: []
                  flags: []
                characters: []
                items: []
            """;


    }


    void testCharacterImport() {
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
    }
}
