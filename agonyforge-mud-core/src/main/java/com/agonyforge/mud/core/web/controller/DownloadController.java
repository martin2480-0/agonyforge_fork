package com.agonyforge.mud.core.web.controller;

import com.agonyforge.mud.core.web.model.WebSocketContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.*;

@RestController
@Component
public class DownloadController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadController.class);

    final private String tmpDir = System.getProperty("java.io.tmpdir");
    Path agonyForgePath = Paths.get(tmpDir, "agonyforge");

    public DownloadController() {
    }

    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> downloadFile(@RequestHeader Map<String, Object> headers) throws IOException {
        WebSocketContext wsContext;

        try {
            wsContext = WebSocketContext.build(headers);
        } catch (IllegalStateException e) {
            return ResponseEntity.internalServerError().build();
        }

        Principal principal = wsContext.getPrincipal();
        Optional<File[]> files = getFilesInTempDir();

        if (files.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        final String[] type = {""};

        Optional<File> matchingFile = Arrays.stream(files.get())
            .filter(file -> file.getName().startsWith("download"))
            .filter(file -> {
                String[] parts = file.getName().split("_");
                if (parts.length > 2 && parts[1].equalsIgnoreCase(principal.getName())) {
                    type[0] = parts[2];
                    return true;
                }
                return false;
            })
            .findFirst();

        if (matchingFile.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        File file = matchingFile.get();
        LOGGER.info("User with principal {} downloading file {}", principal.getName(), file.getName());

        InputStream inputStream = new FileInputStream(file);

        StreamingResponseBody stream = outputStream -> {
            try (inputStream) {
                inputStream.transferTo(outputStream);
                outputStream.flush();
            } finally {
                Files.deleteIfExists(file.toPath());
                LOGGER.info("Deleted file after download: {}", file.getAbsolutePath());
            }
        };

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s.json\"", type[0]))
            .body(stream);
    }


    private Optional<File[]> getFilesInTempDir() {
        File directory = agonyForgePath.toFile();

        if (!directory.exists() || !directory.isDirectory()) {
            return Optional.empty();
        }

        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            return Optional.empty();
        } else {
            return Optional.of(files);
        }
    }
}
