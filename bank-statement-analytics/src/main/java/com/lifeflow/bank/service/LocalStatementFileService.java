package com.lifeflow.bank.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
@Slf4j
public class LocalStatementFileService {

    /**
     * Директория, где лежат PDF-выписки.
     * Например: /Users/твой-юзер/Downloads
     *
     * application.yml:
     *
     * tatrabanka:
     *   local-statements-dir: ${TATRABANKA_LOCAL_STATEMENTS_DIR:/Users/…/Downloads}
     */
    @Value("${tatrabanka.local-statements-dir}")
    private String statementsDir;

    /**
     * Найти первый PDF в директории, в названии которого есть namePart.
     * Например namePart = "89966_00_12514_2025-11-01"
     */
    public File findStatementByNamePart(String namePart) {
        Path dir = Paths.get(statementsDir);

        if (!Files.isDirectory(dir)) {
            throw new IllegalStateException("Local statements dir not found: " + dir);
        }

        log.info("Searching for PDF in {} with name containing '{}'", dir, namePart);

        try (Stream<Path> stream = Files.list(dir)) {
            Path found = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        String fn = p.getFileName().toString().toLowerCase();
                        return fn.endsWith(".pdf") && fn.contains(namePart.toLowerCase());
                    })
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No PDF in " + dir + " matching '" + namePart + "'"
                    ));

            log.info("Using statement file: {}", found);
            return found.toFile();
        } catch (IOException e) {
            throw new RuntimeException("Failed to search local statements dir", e);
        }
    }
}