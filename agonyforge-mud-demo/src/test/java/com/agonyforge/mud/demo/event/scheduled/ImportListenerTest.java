package com.agonyforge.mud.demo.event.scheduled;

import com.agonyforge.mud.demo.model.export.ImportExportService;
import com.agonyforge.mud.demo.service.CommService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ImportListenerTest {

    @Mock
    private ImportExportService importExportService;

    @Mock
    private CommService commService;

    private ImportListener importListener;

    private Path agonyForgeDir;

    @BeforeEach
    public void setup() throws IOException {
        importExportService = mock(ImportExportService.class);
        commService = mock(CommService.class);

        importListener = new ImportListener(importExportService, commService);

        String tmpDir = System.getProperty("java.io.tmpdir");
        agonyForgeDir = Paths.get(tmpDir, "agonyforge");
        Files.createDirectories(agonyForgeDir);
    }

    @AfterEach
    public void cleanup() throws IOException {
        if (Files.exists(agonyForgeDir)) {
            Files.walk(agonyForgeDir)
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .forEach(File::delete);
            Files.deleteIfExists(agonyForgeDir);
        }
    }

    @Test
    public void testItemsImport() throws Exception {
        String fileName = "upload_1_items_00000000-0000-0000-0000-000000000000.json";
        File testFile = new File(agonyForgeDir.toFile(), fileName);
        String fileContent = "---";
        Files.writeString(testFile.toPath(), fileContent);

        when(importExportService.importItems(eq(fileContent))).thenReturn(true);

        importListener.checkTempFiles();

        verify(importExportService, atLeastOnce()).importItems(eq(fileContent));
        verify(commService).reloadUser("1");
        assertFalse(testFile.exists(), "The test file should be deleted after processing.");
    }

    @Test
    public void testCharacterImport() throws Exception {
        String fileName = "upload_1_character_00000000-0000-0000-0000-000000000001.json";
        File testFile = new File(agonyForgeDir.toFile(), fileName);
        String fileContent = "---";
        Files.writeString(testFile.toPath(), fileContent);

        when(importExportService.importPlayerCharacter(eq("1"), eq(fileContent))).thenReturn(true);

        importListener.checkTempFiles();

        verify(importExportService, atLeastOnce()).importPlayerCharacter(eq("1"), eq(fileContent));
        verify(commService).reloadUser("1");
        assertFalse(testFile.exists(), "The test file should be deleted after processing.");
    }

    @Test
    public void testMapImport() throws Exception {
        String fileName = "upload_1_map_00000000-0000-0000-0000-000000000002.json";
        File testFile = new File(agonyForgeDir.toFile(), fileName);
        String fileContent = "---";
        Files.writeString(testFile.toPath(), fileContent);

        when(importExportService.importMap(eq(fileContent))).thenReturn(true);

        importListener.checkTempFiles();

        verify(importExportService, atLeastOnce()).importMap(eq(fileContent));
        verify(commService).reloadUser("1");
        assertFalse(testFile.exists(), "The test file should be deleted after processing.");
    }

    @Test
    public void testInvalidImport() throws Exception {
        String fileName = "upload_1_test_00000000-0000-0000-0000-000000000003.json";
        File testFile = new File(agonyForgeDir.toFile(), fileName);
        String fileContent = "---";
        Files.writeString(testFile.toPath(), fileContent);

        importListener.checkTempFiles();

        verifyNoInteractions(importExportService);
        verifyNoInteractions(commService);
        assertFalse(testFile.exists(), "The test file should be deleted after processing.");
    }

}
