package by.eugene.maven.commands;

import by.eugene.maven.exceptions.WrongCommandParamException;
import by.eugene.maven.migrations.MigrationManager;

import java.util.List;
import java.util.Map;

/**
 * Command to roll back database migrations to a specified version.
 * <p>
 * This command allows specifying a target database version to which the database should be rolled back.
 * It can be executed with the command {@code rollback} and supports one optional parameter: {@code -version} to specify the target version for the rollback.
 * </p>
 *
 * <p><b>Usage:</b></p>
 * <pre>
 * Command rollbackCommand = new RollbackCommand(migrationManager);
 * rollbackCommand.execute(args);
 * </pre>
 *
 * <p><b>Command Details:</b></p>
 * <ul>
 *   <li><strong>-version:</strong> Specifies the target version to roll back the database to. The migration process will roll back all migrations up to (and including) the specified version.</li>
 *   <li><strong>-h:</strong> Displays the help message for the command.</li>
 * </ul>
 */
public class RollbackCommand extends Command {
    private final static String VERSION_PARAM = "-version";

    private static final String HELP_MESSAGE = """
            Command: rollback
            Description: Rolls back database migrations to a specified version.
            Usage:
              rollback -version <target_version> - Specifies the target database version to roll back to.
              rollback -h                        - Displays this help message.
            Returns:
              Rolls back all migrations up to (and including) the specified version.
            """;

    private final MigrationManager migrationManager;

    /**
     * Constructs a new {@code RollbackCommand} with the given {@code MigrationManager}.
     * <p>
     * This constructor initializes the command with the migration manager, allowing it to roll back migrations
     * based on the specified target version.
     * </p>
     *
     * @param migrationManager the {@link MigrationManager} to handle the rollback process
     */
    public RollbackCommand(MigrationManager migrationManager) {
        super("rollback", List.of(VERSION_PARAM), HELP_MESSAGE);
        this.migrationManager = migrationManager;
    }

    /**
     * Executes the rollback process to a specified database version.
     * <p>
     * This method processes the command arguments to retrieve the target version and triggers the rollback process using the
     * {@link MigrationManager}. If the version parameter is missing, an exception is thrown.
     * </p>
     *
     * @param args the list of arguments passed to the command (includes the {@code -version} parameter)
     * @throws WrongCommandParamException if the {@code -version} parameter is missing or has an incorrect format
     */
    @Override
    public void execute(List<String> args) {
        Map<String, String> paramsMap = parseParams(args);
        if (paramsMap.get(VERSION_PARAM) == null) {
            throw new WrongCommandParamException("Parameter %s is required".formatted(VERSION_PARAM));
        }

        this.migrationManager.executeRollbacks(Integer.parseInt(paramsMap.get(VERSION_PARAM)));
    }
}

