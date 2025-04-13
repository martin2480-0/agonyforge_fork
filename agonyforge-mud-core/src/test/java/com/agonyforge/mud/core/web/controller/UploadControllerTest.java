package com.agonyforge.mud.core.web.controller;


import com.agonyforge.mud.core.web.FileTransferDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;

import java.nio.file.Path;

import java.util.Base64;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UploadControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private UploadController uploadController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(uploadController).build();
    }

    @Test
    void testHandleFileUploadSuccess() throws Exception {
        String username = "testUser";
        String type = "character";
        String base64Content = Base64.getEncoder().encodeToString("dummy yaml".getBytes());

        FileTransferDTO dto = new FileTransferDTO();
        dto.setBase64Content(base64Content);
        dto.setType(type);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/import")
                .principal(() -> username)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.output").value("File uploaded successfully."));
    }

    @Test
    void testHandleFileUploadFailure() throws Exception {
        String username = "testUser";
        String type = "character";
        String base64Content = Base64.getEncoder().encodeToString("dummy yaml".getBytes());

        UploadController controller = Mockito.spy(new UploadController());

        doThrow(new IOException("Simulated failure"))
            .when(controller).writeToFile(any(Path.class), any(byte[].class));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/import")
                .principal(() -> username)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"base64Content\": \"" + base64Content + "\", \"type\": \"" + type + "\"}"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.output").value("[red]File upload failed due to server error."));
    }

}
