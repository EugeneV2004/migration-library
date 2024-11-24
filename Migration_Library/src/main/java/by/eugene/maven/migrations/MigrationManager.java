package by.eugene.maven.migrations;


import by.eugene.maven.config.ConnectionManager;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class MigrationManager {
    private final ConnectionManager connectionManager = ConnectionManager.getInstance();
    private final MigrationExecutor migrationExecutor;
    private String migrationsDirectory;

    public MigrationManager(MigrationExecutor migrationExecutor, String migrationDirectory) {
        this.migrationExecutor = migrationExecutor;
        this.migrationsDirectory = migrationDirectory;
    }

    public void executeMigrations() {
        List<String> fileNames = getFiles(migrationsDirectory);
        log.info("Starting migration process. Connection established.");
        try (Connection connection = connectionManager.getConnection()) {
            connection.setAutoCommit(false);

            try {
                log.info("Starting migration process with files: {}",
                        fileNames.toString().replaceAll("[{}]", ""));
                for (String fileName : fileNames) {
                    log.info("Executing migration file: {}", fileName);
                    migrationExecutor.executeMigration(fileName);
                    log.info("Migration file executed successfully: {}", fileName);
                }
                connection.commit();
                log.info("Migration commited successfully");
            } catch (SQLException e) {
                log.error("Error while processing migration files. Rollback changes", e);
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            log.error("Error while managing the database connection or migrations.", e);
            throw new RuntimeException("Error while executing migrations", e);
        }
    }

    public List<String> getFiles(String directory) {
        try {
            log.info("Reading files from directory: {}", directory);
            ClassLoader classLoader = MigrationManager.class.getClassLoader();
            URL url = classLoader.getResource(directory);
            if(url == null) {
                log.error("Directory {} not found in resources.", directory);
                throw new IOException("Directory %s not found in resources".formatted(directory));
            }

            log.info("Reading files from directory {}", directory);
            Path directoryPath = Paths.get(url.toURI());

            try (Stream<Path> paths = Files.list(directoryPath)) {
                return paths
                        .map(path -> path.getFileName().toString())
                        .map(fileName -> directoryPath.getFileName() + "/" + fileName)
                        .sorted()
                        .toList();
            }
        } catch (URISyntaxException | IOException e) {
            log.error("Error while reading files from directory: {}", directory, e);
            throw new RuntimeException("Error while path parsing", e);
        }
    }
}
