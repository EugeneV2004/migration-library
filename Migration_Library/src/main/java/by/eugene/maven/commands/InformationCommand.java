package by.eugene.maven.commands;

import by.eugene.maven.migrations.MigrationManager;

import java.util.Collections;
import java.util.List;

/**
 * Command to display the current database version.
 * <p>
 * This command outputs the current version of the database by querying the {@link MigrationManager}.
 * It can be executed with the command {@code info} or with the help flag {@code info -h}.
 * </p>
 *
 * <p><b>Usage:</b></p>
 * <pre>
 * Command infoCommand = new InformationCommand(migrationManager);
 * infoCommand.execute(args);
 * </pre>
 *
 * <p><b>Command Details:</b></p>
 * <ul>
 *   <li><strong>info:</strong> Executes the command and prints the current database version.</li>
 *   <li><strong>info -h:</strong> Displays the help message for the command.</li>
 * </ul>
 */
public class InformationCommand extends Command{
    private final static String INFO_MESSAGE_PATTERN = "Current DB version: %s";
    private static final String HELP_MESSAGE = """
            Command: info
            Description: Displays the current database version.
            Usage: 
              info              - Executes the command and prints the database version.
              info -h           - Displays this help message.
            Returns:
              Outputs the current database version in the format: "Current DB version: <version>"
            """;

    private final MigrationManager migrationManager;

    /**
     * Constructs a new {@code InformationCommand} with the given {@code MigrationManager}.
     * <p>
     * This constructor initializes the command with the migration manager, allowing it to query
     * the current database version.
     * </p>
     *
     * @param migrationManager the {@link MigrationManager} to query the current DB version
     */
    public InformationCommand(MigrationManager migrationManager) {
        super("info", Collections.emptyList(), HELP_MESSAGE);
        this.migrationManager = migrationManager;
    }

    /**
     * Executes the command and displays the current database version.
     * <p>
     * This method prints the current database version to the standard output in the following format:
     * "Current DB version: <version>"
     * </p>
     *
     * @param args the list of arguments passed to the command (ignored for this command)
     */
    @Override
    public void execute(List<String> args) {
        System.out.println(INFO_MESSAGE_PATTERN.formatted(this.migrationManager.getCurrentDbVersion()));
    }
}
