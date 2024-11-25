package by.eugene.maven.commands;

import by.eugene.maven.exceptions.WrongCommandParamException;
import by.eugene.maven.migrations.MigrationManager;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Command to execute database migrations.
 * <p>
 * It can be executed with the command {@code migrate}
 * </p>
 *
 * <p><b>Usage:</b></p>
 * <pre>
 * Command migrateCommand = new MigrateCommand(migrationManager);
 * migrateCommand.execute(args);
 * </pre>
 *
 * <p><b>Command Details:</b></p>
 * <ul>
 *   <li><strong>-h:</strong> Displays the help message for the command.</li>
 * </ul>
 */
public class MigrateCommand extends Command {
    private static final String HELP_MESSAGE = """
            Command: migrate
            Description: Executes database migrations.
            Usage:
              migrate -h                       - Displays this help message.
            Returns:
              Executes the migration process as per the specified parameters.
              If no parameters are provided, executes all pending migrations in the default directory.
            """;

    private final MigrationManager migrationManager;

    /**
     * Constructs a new {@code MigrateCommand} with the given {@code MigrationManager}.
     * <p>
     * This constructor initializes the command with the migration manager, allowing it to execute migrations
     * based on the provided parameters (directory and version).
     * </p>
     *
     * @param migrationManager the {@link MigrationManager} to handle the migration process
     */
    public MigrateCommand(MigrationManager migrationManager) {
        super("migrate", Collections.emptyList(), HELP_MESSAGE);
        this.migrationManager = migrationManager;
    }

    /**
     * Executes the migration process based on the provided arguments.
     * <p>
     * It then triggers the migration process using the {@link MigrationManager}. If the parameters are incorrect or missing,
     * an exception is thrown.
     * </p>
     *
     * @param args the list of arguments passed to the command
     * @throws WrongCommandParamException if any required parameter is missing or has an incorrect format
     */
    @Override
    public void execute(List<String> args) {
        Map<String, String> paramsMap = super.parseParams(args);

        this.migrationManager.executeMigrations();
    }
}

