package by.eugene.maven;

import by.eugene.maven.config.ConnectionManager;
import by.eugene.maven.migrations.MigrationExecutor;
import by.eugene.maven.migrations.MigrationManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MigrationTool {
    private final MigrationManager migrationManager;
    private final ConnectionManager connectionManager;
    private final MigrationExecutor migrationExecutor;

    public MigrationTool(String url, String username, String password, String migrationDirectory) {
        log.info("Initializing MigrationTool with URL: {}, Username: {}, Migration Directory: {}",
                url, username, migrationDirectory);

        ConnectionManager.init(url, username, password);
        this.connectionManager = ConnectionManager.getInstance();
        this.migrationExecutor = new MigrationExecutor(connectionManager);
        this.migrationManager = new MigrationManager(migrationExecutor, migrationDirectory);

        log.info("MigrationTool initialized successfully.");
    }

    public void run() {
        log.info("Starting migration process...");
        migrationManager.executeMigrations();
        log.info("Migration process completed successfully.");
    }
}
