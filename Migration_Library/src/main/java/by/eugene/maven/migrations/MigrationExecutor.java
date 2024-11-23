package by.eugene.maven.migrations;


import by.eugene.maven.config.ConnectionManager;
import lombok.extern.slf4j.Slf4j;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Slf4j
public class MigrationExecutor {
    private final ConnectionManager connectionManager;

    public MigrationExecutor(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public void executeMigration(String fileName) throws SQLException {
        log.info("Starting migration for file: {}", fileName);

        if (!fileName.endsWith(".sql")) {
            throw new RuntimeException("File with wrong extension provided: %s; only .sql file extension supports ".formatted(fileName));
        }
        log.debug("Attempting to establish a database connection.");
        Connection connection = connectionManager.getConnection();
        List<String> sqlCommands = MigrationFileReader.readMigrationsFromFile(fileName);
        log.info("Read {} SQL commands from file: {}", sqlCommands.size(), fileName);

        Statement statement = connection.createStatement();

        for (String sql : sqlCommands) {
            log.debug("Adding SQL command to batch: {}", sql);
            statement.addBatch(sql);
        }
        log.info("Executing batch of SQL commands...");
        statement.executeBatch();
        log.info("Migration for file {} completed successfully.", fileName);
    }
}