package by.eugene.maven.migrations;


import by.eugene.maven.config.ConnectionManager;
import lombok.extern.slf4j.Slf4j;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
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
        executeSql(sqlCommands);
        saveMigration(MigrationFileReader.readMigrationVersion(fileName), fileName);
    }


    public void executeRollback(String fileName) throws SQLException {
        log.info("Starting migration for file: {}", fileName);

        if (!fileName.endsWith(".sql")) {
            throw new RuntimeException("File with wrong extension provided: %s; only .sql file extension supports ".formatted(fileName));
        }
        List<String> sqlCommands = MigrationFileReader.readRollbacksFromFile(fileName);
        log.info("Read {} SQL commands from file: {}", sqlCommands.size(), fileName);
        executeSql(sqlCommands);
        abortMigration(fileName);
    }

    public void saveMigration(int version, String MigrationFile) throws SQLException {
        Connection connection = connectionManager.getConnection();
        PreparedStatement statement = connection
                .prepareStatement("INSERT INTO history VALUES (?, ?, ?)");
        statement.setInt(1, version);
        statement.setString(2, MigrationFile);
        statement.setTimestamp(3, Timestamp.from(Instant.now()));
        statement.execute();
    }

    private void executeSql(List<String> sqlCommands) {
        try {
            Connection connection = connectionManager.getConnection();
            Statement statement = connection.createStatement();
            for (String sql : sqlCommands) {
                log.debug("Adding SQL command to batch: {}", sql);
                statement.addBatch(sql);
            }

            statement.executeBatch();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    private void abortMigration(String fileName) throws SQLException {
        Connection connection = connectionManager.getConnection();
        PreparedStatement statement = connection
                .prepareStatement("DELETE from history where file=?");
        statement.setString(1, fileName);
        statement.execute();
    }
}