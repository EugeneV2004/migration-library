package by.eugene.maven.commands;

import by.eugene.maven.exceptions.WrongCommandParamException;
import by.eugene.maven.migrations.MigrationManager;

import java.util.List;
import java.util.Map;

/**
 * Command to execute database migrations.
 * <p>
 * This command allows specifying a directory containing migration files and/or a target database version for the migration process.
 * It can be executed with the command {@code migrate} and supports two optional parameters: {@code -dir} to specify a directory and {@code -version} to specify a target version for the migration.
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
 *   <li><strong>-dir:</strong> Specifies the directory containing the migration files. If not provided, the default directory is used.</li>
 *   <li><strong>-version:</strong> Specifies the target version to migrate the database to. If not provided, all pending migrations are executed.</li>
 *   <li><strong>-h:</strong> Displays the help message for the command.</li>
 * </ul>
 */
public class MigrateCommand extends Command {
    private final static String DIRECTORY_PARAM = "-dir";
    private final static String VERSION_PARAM = "-version";

    private static final String HELP_MESSAGE = """
            Command: migrate
            Description: Executes database migrations. Allows specifying a directory and/or a target version.
            Usage:
              migrate -dir <directory_path>    - Specifies the directory containing migration files.
              migrate -version <target_version> - Specifies the target database version for migrations.
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
        super("migrate", List.of(DIRECTORY_PARAM, VERSION_PARAM), HELP_MESSAGE);
        this.migrationManager = migrationManager;
    }

    /**
     * Executes the migration process based on the provided arguments.
     * <p>
     * The method processes the command arguments to determine whether to use a specific directory and/or a target version.
     * It then triggers the migration process using the {@link MigrationManager}. If the parameters are incorrect or missing,
     * an exception is thrown.
     * </p>
     *
     * @param args the list of arguments passed to the command (includes optional parameters like {@code -dir} and {@code -version})
     * @throws WrongCommandParamException if any required parameter is missing or has an incorrect format
     */
    @Override
    public void execute(List<String> args) {
        Map<String, String> paramsMap = super.parseParams(args);
        if (args.contains(VERSION_PARAM) && paramsMap.get(VERSION_PARAM) == null) {
            throw new WrongCommandParamException("Parameter %s not provided".formatted(VERSION_PARAM));
        }
        if (args.contains(DIRECTORY_PARAM) && paramsMap.get(DIRECTORY_PARAM) == null) {
            throw new WrongCommandParamException("Parameter %s not provided".formatted(DIRECTORY_PARAM));
        }

        this.migrationManager.executeMigrations();
    }
}

