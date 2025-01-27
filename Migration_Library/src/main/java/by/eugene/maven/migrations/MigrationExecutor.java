package by.eugene.maven.migrations;

import by.eugene.maven.config.ConnectionManager;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.time.Instant;
import java.util.List;

/**
 * Class responsible for executing database migrations and rollbacks.
 * <p>
 * This class handles the execution of SQL migrations and rollbacks based on migration files. It reads the SQL
 * commands from the specified files, executes them in batches, and records the migration history in the database.
 * </p>
 *
 * <p><b>Usage:</b></p>
 * <pre>
 * MigrationExecutor executor = new MigrationExecutor(connectionManager);
 * executor.executeMigration("migration_v1.sql");
 * executor.executeRollback("rollback_v1.sql");
 * </pre>
 */
@Slf4j
public class MigrationExecutor {
    private final ConnectionManager connectionManager;

    /**
     * Constructor for the MigrationExecutor.
     *
     * @param connectionManager the {@link ConnectionManager} instance for managing database connections
     */
    public MigrationExecutor(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        log.info("MigrationExecutor initialized with connection manager: {}", connectionManager);
    }

    /**
     * Executes a migration by applying the SQL commands from the provided migration file.
     * <p>
     * This method reads the SQL commands from the specified migration file, executes them in a batch, and then
     * saves the migration version and filename to the history table.
     * </p>
     *
     * @param fileName the name of the migration file (must have a .sql extension)
     * @throws SQLException if a database error occurs while executing the migration or saving the history
     * @throws RuntimeException if the file does not have a .sql extension
     */
    public void executeMigration(String fileName) throws SQLException {
        log.info("Starting migration for file: {}", fileName);

        if (!fileName.endsWith(".sql")) {
            log.error("Invalid file extension: {}; only .sql files are supported.", fileName);
            throw new RuntimeException("File with wrong extension provided: %s; only .sql file extension supports ".formatted(fileName));
        }

        log.debug("Attempting to establish a database connection.");
        List<String> sqlCommands = MigrationFileReader.readMigrationsFromFile(fileName);

        log.info("Read {} SQL commands from migration file: {}", sqlCommands.size(), fileName);
        executeSql(sqlCommands);

        log.info("Saving migration history for file: {}", fileName);
        saveMigration(MigrationFileReader.readMigrationVersion(fileName), fileName);
        log.info("Migration for file {} executed successfully", fileName);
    }

    /**
     * Executes a rollback by applying the SQL commands from the provided rollback file.
     * <p>
     * This method reads the SQL commands from the specified rollback file, executes them in a batch, and then
     * removes the migration from the history table to reverse the applied migration.
     * </p>
     *
     * @param fileName the name of the rollback file (must have a .sql extension)
     * @throws SQLException if a database error occurs while executing the rollback or updating the history table
     * @throws RuntimeException if the file does not have a .sql extension
     */
    public void executeRollback(String fileName) throws SQLException {
        log.info("Starting rollback for file: {}", fileName);

        if (!fileName.endsWith(".sql")) {
            log.error("Invalid file extension: {}; only .sql files are supported.", fileName);
            throw new RuntimeException("File with wrong extension provided: %s; only .sql file extension supports ".formatted(fileName));
        }

        List<String> sqlCommands = MigrationFileReader.readRollbacksFromFile(fileName);

        log.info("Read {} SQL commands from rollback file: {}", sqlCommands.size(), fileName);
        executeSql(sqlCommands);

        log.info("Aborting migration history for file: {}", fileName);
        abortMigration(fileName);
        log.info("Rollback for file {} executed successfully", fileName);
    }

    /**
     * Saves the migration version and file name to the history table in the database.
     * <p>
     * This method records the migration version and the associated migration file, along with the timestamp of
     * when the migration was executed, to ensure proper tracking of applied migrations.
     * </p>
     *
     * @param version the version of the migration
     * @param migrationFile the name of the migration file
     * @throws SQLException if a database error occurs while saving the migration
     */
    public void saveMigration(int version, String migrationFile) throws SQLException {
        log.info("Recording migration history: Version {} for file {}", version, migrationFile);

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO history VALUES (?, ?, ?)")) {

            statement.setInt(1, version);
            statement.setString(2, migrationFile);
            statement.setTimestamp(3, Timestamp.from(Instant.now()));
            statement.execute();
            log.info("Migration history saved successfully for file: {}", migrationFile);

        } catch (SQLException e) {
            log.error("Error while saving migration history for file: {}", migrationFile, e);
            throw e;
        }
    }

    /**
     * Executes the SQL commands in the provided list.
     *
     * @param sqlCommands the list of SQL commands to execute
     * @throws SQLException if a database error occurs while executing the commands
     */
    private void executeSql(List<String> sqlCommands) {
        log.debug("Executing SQL batch commands...");

        try (Connection connection = connectionManager.getConnection();
             Statement statement = connection.createStatement()) {

            for (String sql : sqlCommands) {
                log.debug("Adding SQL command to batch: {}", sql);
                statement.addBatch(sql);
            }

            statement.executeBatch();
            log.info("SQL batch execution completed successfully");

        } catch (SQLException e) {
            log.error("Error while executing SQL commands in batch", e);
            throw new RuntimeException("Error executing SQL batch", e);
        }
    }

    /**
     * Removes the migration record from the history table to undo the applied migration.
     *
     * @param fileName the name of the migration file to remove from history
     * @throws SQLException if a database error occurs while deleting the migration record
     */
    private void abortMigration(String fileName) throws SQLException {
        log.info("Removing migration history for file: {}", fileName);

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE from history WHERE file = ?")) {

            statement.setString(1, fileName);
            statement.execute();
            log.info("Migration history removed successfully for file: {}", fileName);

        } catch (SQLException e) {
            log.error("Error while removing migration history for file: {}", fileName, e);
            throw e;
        }
    }
}
