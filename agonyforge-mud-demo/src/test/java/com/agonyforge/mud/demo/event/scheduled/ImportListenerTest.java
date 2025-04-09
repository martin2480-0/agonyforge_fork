package com.agonyforge.mud.demo.event.scheduled;

import com.agonyforge.mud.demo.model.export.ImportExportService;
import com.agonyforge.mud.demo.service.CommService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.Mockito.*;

@Disabled
@ExtendWith(MockitoExtension.class)
public class ImportListenerTest {

    @InjectMocks
    private ImportListener importListener;

    @Mock
    private Path folderPath;

    @Mock
    private File directory;

    @Mock
    private ImportExportService importService;

    @Mock
    private CommService commService;

    @Test
    void testCheckBannedUsers() throws IOException {
        File characterFile = mock(File.class);
        File itemsFile = mock(File.class);
        File mapFile = mock(File.class);
        File badFile = mock(File.class);

        when(folderPath.toFile()).thenReturn(directory);

        when(directory.listFiles()).thenReturn(new File[]{
            characterFile, itemsFile, mapFile, badFile
        });

        when(Files.readString(any())).thenReturn("{}"); // TODO fix this static call part of the test

        when(characterFile.getName()).thenReturn("upload_1_character_00000000-0000-0000-0000-000000000000.json");
        when(itemsFile.getName()).thenReturn("upload_1_items_00000000-0000-0000-0000-000000000001.json");
        when(mapFile.getName()).thenReturn("upload_1_map_00000000-0000-0000-0000-000000000002.json");
        when(badFile.getName()).thenReturn("upload_1_string_00000000-0000-0000-0000-000000000003.json");

        when(characterFile.delete()).thenReturn(true);
        when(itemsFile.delete()).thenReturn(true);
        when(mapFile.delete()).thenReturn(true);
        when(badFile.delete()).thenReturn(true);

        importListener.checkTempFiles();

        verify(characterFile.delete(), times(1));
        verify(itemsFile.delete(), times(1));
        verify(mapFile.delete(), times(1));
        verify(badFile.delete(), times(1));

        verifyNoMoreInteractions(importService, times(3));
        verify(commService, times(3)).reloadUser(any());


    }
}
