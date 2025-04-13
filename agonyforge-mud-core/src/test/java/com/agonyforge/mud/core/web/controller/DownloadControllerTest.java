package com.agonyforge.mud.core.web.controller;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DownloadControllerTest {

    @InjectMocks
    private DownloadController downloadController;

    private MockMvc mockMvc;

    private static final String USERNAME = "testuser";
    private static final String TYPE = "type";
    private static final UUID RANDOM_UUID = UUID.randomUUID();
    private Path agonyForgePath;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(downloadController).build();

        String tmpDir = System.getProperty("java.io.tmpdir");
        agonyForgePath = Path.of(tmpDir, "agonyforge");
        Files.createDirectories(agonyForgePath);

        Path mockFile = agonyForgePath.resolve(String.format("download_%s_%s_%s.yaml", USERNAME, TYPE, RANDOM_UUID));
        Files.writeString(mockFile, "mock yaml content");
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.list(agonyForgePath).forEach(path -> {
            try {
                Files.deleteIfExists(path);
            } catch (Exception ignored) {}
        });

    }

    @Test
    void testDownloadSuccess() throws Exception {
        Principal mockPrincipal = () -> USERNAME;

        MockHttpServletResponse response = mockMvc.perform(post("/download").principal(mockPrincipal))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"type.yaml\""))
            .andExpect(content().contentType(MediaType.APPLICATION_YAML))
            .andReturn()
            .getResponse();

        assertThat(response.getContentAsString()).isEqualTo("mock yaml content");

        File file = agonyForgePath.resolve(String.format("upload_%s_%s_%s.yaml", USERNAME, TYPE, RANDOM_UUID)).toFile();
        assertThat(file.exists()).isFalse();
    }

    @Test
    void testDownloadFileNotFoundForUser() throws Exception {
        Principal mockPrincipal = () -> "wronguser";

        mockMvc.perform(post("/download").principal(mockPrincipal))
            .andExpect(status().isNotFound());
    }
}
