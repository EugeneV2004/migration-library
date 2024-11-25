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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Manages database migrations and rollbacks.
 * <p>
 * This class is responsible for executing migrations, managing the history table to track applied migrations,
 * and performing rollbacks to a specific database version.
 * It ensures that only unapplied migrations are executed and supports rolling back to a target version.
 * </p>
 *
 * <p><b>Usage:</b></p>
 * <pre>
 * MigrationManager migrationManager = new MigrationManager(new MigrationExecutor(connectionManager), "migrations");
 * migrationManager.executeMigrations();
 * migrationManager.executeRollbacks(2);
 * </pre>
 */
@Slf4j
public class MigrationManager {
    private final ConnectionManager connectionManager = ConnectionManager.getInstance();
    private final MigrationExecutor migrationExecutor;
    private String migrationsDirectory;

    private static final String historyTableSchema = """
            create table if not exists history (
                version int primary key,
                file varchar,
                timestamp timestamp
                );
            """;

    /**
     * Constructs a new MigrationManager.
     * <p>
     * Initializes the MigrationExecutor and sets the directory where migration files are stored.
     * It also creates the history table in the database.
     * </p>
     *
     * @param migrationExecutor the MigrationExecutor used to execute migration and rollback commands
     * @param migrationDirectory the directory where migration files are located
     */
    public MigrationManager(MigrationExecutor migrationExecutor, String migrationDirectory) {
        this.migrationExecutor = migrationExecutor;
        this.migrationsDirectory = migrationDirectory;
        this.createHistoryTable();
    }

    /**
     * Executes the pending migrations by reading migration files from the specified directory.
     * <p>
     * This method processes migration files in order, executing those that have not been applied yet and committing the transaction
     * if all migrations are successful. If an error occurs, the transaction is rolled back.
     * </p>
     */
    public void executeMigrations() {
        List<String> fileNames = getFiles(migrationsDirectory);
        log.info("Starting migration process. Connection established.");
        try (Connection connection = connectionManager.getConnection()) {
            connection.setAutoCommit(false);

            try {
                log.info("Starting migration process with files: {}",
                        fileNames.toString().replaceAll("[{}]", ""));
                for (String fileName : fileNames) {
                    if (!isApplied(fileName)) {
                        log.info("Executing migration file: {}", fileName);
                        migrationExecutor.executeMigration(fileName);
                        log.info("Migration file executed successfully: {}", fileName);
                    } else {
                        log.info("Migration file already executed: {}", fileName);
                    }
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

    /**
     * Executes rollbacks to a specified target version.
     * <p>
     * This method rolls back migrations in reverse order from the current version down to the target version,
     * ensuring that all necessary rollback files are executed. If any error occurs, the transaction is rolled back.
     * </p>
     *
     * @param targetVersion the target database version to roll back to
     */
    public void executeRollbacks(int targetVersion) {
        List<String> fileNames = getFiles(migrationsDirectory);
        log.info("Starting rollback process. Connection established.");

        fileNames.sort(Comparator.reverseOrder());

        try (Connection connection = connectionManager.getConnection()) {
            connection.setAutoCommit(false);

            try {
                int currentVersion = getCurrentDbVersion();
                log.info("Current database version: {}. Target version: {}", currentVersion, targetVersion);

                if (currentVersion <= targetVersion) {
                    log.info("No rollbacks required. Database is already at or below the target version.");
                    return;
                }

                List<String> rollbacksToExecute = fileNames.stream()
                        .filter(fileName -> MigrationFileReader.readMigrationVersion(fileName) > targetVersion
                                && MigrationFileReader.readMigrationVersion(fileName) <= currentVersion)
                        .toList();

                log.info("Rollback files to execute: {}", rollbacksToExecute);

                for (String fileName : rollbacksToExecute) {
                    log.info("Executing rollback for file: {}", fileName);
                    migrationExecutor.executeRollback(fileName);
                    log.info("Rollback executed successfully for file: {}", fileName);
                }

                connection.commit();
                log.info("Rollback process completed successfully. Database rolled back to version: {}", targetVersion);

            } catch (SQLException e) {
                log.error("Error while processing rollbacks. Rolling back changes.", e);
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            log.error("Error while managing the database connection or rollbacks.", e);
            throw new RuntimeException("Error while executing rollbacks", e);
        }
    }

    /**
     * Retrieves the current version of the database from the history table.
     * <p>
     * This method queries the history table to find the highest applied migration version.
     * </p>
     *
     * @return the current database version
     */
    public int getCurrentDbVersion() {
        Connection connection = this.connectionManager.getConnection();
        PreparedStatement statement = null;
        try {
            statement = connection
                    .prepareStatement("SELECT max(version) FROM history");
            statement.execute();
            statement.getResultSet().next();
            return statement.getResultSet().getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Retrieves a list of migration file names from the specified directory.
     * <p>
     * This method reads all files in the specified migration directory, sorts them in ascending order, and returns
     * a list of their names.
     * </p>
     *
     * @param directory the directory to read migration files from
     * @return a list of file names in the migration directory
     * @throws RuntimeException if there is an error reading the files from the directory
     */
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
                return new ArrayList<>(paths
                        .map(path -> path.getFileName().toString())
                        .map(fileName -> directoryPath.getFileName() + "/" + fileName)
                        .sorted()
                        .toList());
            }
        } catch (URISyntaxException | IOException e) {
            log.error("Error while reading files from directory: {}", directory, e);
            throw new RuntimeException("Error while path parsing", e);
        }
    }

    /**
     * Checks whether a migration file has already been applied by querying the history table.
     * <p>
     * This method checks if the migration file exists in the history table and returns `true` if it does, indicating
     * that the migration has already been applied.
     * </p>
     *
     * @param migrationFile the name of the migration file
     * @return `true` if the migration has been applied, `false` otherwise
     * @throws SQLException if there is an error executing the query
     */
    public boolean isApplied(String migrationFile) throws SQLException {
        String query = "SELECT COUNT(*) FROM history WHERE file = ?";


        try {
            Connection connection = connectionManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, migrationFile);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
                return false;
            }
        } catch (SQLException e) {
            log.error("Error checking migration status for file: {}", migrationFile, e);
            throw new RuntimeException("Failed to check migration status", e);
        }
    }

    private void createHistoryTable() {
        Connection connection = connectionManager.getConnection();
        try {
            connection.createStatement().execute(historyTableSchema);
        } catch (SQLException e) {
            log.error("", e);
            throw new RuntimeException(e);
        }
    }
}
