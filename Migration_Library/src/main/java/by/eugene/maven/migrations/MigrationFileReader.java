package by.eugene.maven.migrations;


import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

@Slf4j
public class MigrationFileReader {
    private static final String MIGRATION_DELIMITER = "--migration--";
    private static final String ROLLBACK_DELIMITER = "--rollback--";

    public static List<String> readMigrationsFromFile(String filePath) {
        log.info("Attempting to read migration file: {}", filePath);

        try (InputStream inputStream = MigrationFileReader.class.getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                log.error("Migration file not found: {}", filePath);
                throw new IOException("File %s not found".formatted(filePath));
            }

            log.debug("Reading migration file: {}", filePath);
            Scanner scanner = new Scanner(inputStream);

            while (scanner.hasNext()) {
                String line = scanner.nextLine().trim();
                if (line.equals(MIGRATION_DELIMITER)) {
                    log.debug("Found migration delimiter: {}", MIGRATION_DELIMITER);
                    break;
                }
            }
            scanner.useDelimiter(ROLLBACK_DELIMITER);

            List<String> sqlCommands = Arrays.stream(scanner.next().split(";"))
                    .map(String::trim)
                    .filter(sql -> !sql.isBlank())
                    .toList();

            log.info("Successfully read {} SQL commands from file: {}", sqlCommands.size(), filePath);
            return sqlCommands;
        } catch (IOException e) {
            log.error("Error reading migration file: {}", filePath, e);
            throw new RuntimeException("Error reading migration file", e);
        }

    }
}
