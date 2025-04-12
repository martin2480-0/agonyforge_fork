package com.agonyforge.mud.demo.event.scheduled;

import com.agonyforge.mud.demo.model.export.ImportExportService;
import com.agonyforge.mud.demo.service.CommService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class ImportListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportListener.class);

    final private String tmpDir = System.getProperty("java.io.tmpdir");
    private final Lock lock = new ReentrantLock();
    Path agonyForgePath = Paths.get(tmpDir, "agonyforge");
    ImportExportService importExportService;
    CommService commService;

    @Autowired
    private ImportListener(ImportExportService importExportService, CommService commService) {
        this.importExportService = importExportService;
        this.commService = commService;
    }


    @Scheduled(cron = "0/5 * * * * ?")
    public void checkTempFiles() throws IOException {
        if (lock.tryLock()) {
            try {
                Optional<File[]> optionalFiles = getFilesInTempDir();
                if (optionalFiles.isEmpty()) {
                    return;
                }

                File[] files = optionalFiles.get();
                for (File file : files) {
                    String content = Files.readString(Paths.get(file.getPath()));
                    String filename = file.getName();
                    String[] parts = filename.split("_");
                    String principal = parts[1];
                    String type = parts[2];
                    boolean success = switch (type) {
                        case "items" -> importExportService.importItems(content);
                        case "character" -> importExportService.importPlayerCharacter(principal, content);
                        case "map" -> importExportService.importMap(content);
                        default -> false;
                    };
                    if (!success) {
                        LOGGER.info("Failed to import file: {}", file.getName());
                    }else {
                        commService.reloadUser(principal);
                    }
                    if (file.delete()) {
                        LOGGER.info("Deleted: {}", file.getName());
                    } else {
                        LOGGER.info("Failed to delete: {}", file.getName());
                    }
                }
            } finally {
                lock.unlock();
            }
        }
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
