package com.agonyforge.mud.core.web.controller;

import com.agonyforge.mud.core.web.FileTransferDTO;
import com.agonyforge.mud.core.web.model.Output;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.Base64;
import java.util.UUID;
import java.io.IOException;

@RestController
@Component
public class UploadController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadController.class);

    private final Path agonyForgePath = Paths.get(System.getProperty("java.io.tmpdir"), "agonyforge");

    public UploadController() {
    }

    protected void writeToFile(Path path, byte[] data) throws IOException {
        Files.write(path, data);
    }

    @PostMapping("/import")
    public ResponseEntity<Output> handleFileUpload(@RequestBody FileTransferDTO message, Principal principal) {
        try {
            byte[] data = Base64.getDecoder().decode(message.getBase64Content());
            String type = message.getType();

            String fileName = String.format("upload_%s_%s_%s.yaml", principal.getName(), type, UUID.randomUUID());

            Path filePath = agonyForgePath.resolve(fileName);
            Files.createDirectories(agonyForgePath);

            writeToFile(filePath, data);

            LOGGER.info("Uploaded file for principal {} to {}", principal.getName(), filePath);

            return ResponseEntity.ok(new Output("File uploaded successfully."));
        } catch (IOException e) {
            LOGGER.error("Error writing file: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(new Output("[red]File upload failed due to server error."));
        }
    }
}
