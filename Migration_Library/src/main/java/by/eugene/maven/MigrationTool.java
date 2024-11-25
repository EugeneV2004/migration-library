package by.eugene.maven;

import by.eugene.maven.commands.Command;
import by.eugene.maven.commands.InformationCommand;
import by.eugene.maven.commands.MigrateCommand;
import by.eugene.maven.commands.RollbackCommand;
import by.eugene.maven.config.ConnectionManager;
import by.eugene.maven.exceptions.WrongCommandParamException;
import by.eugene.maven.migrations.MigrationExecutor;
import by.eugene.maven.migrations.MigrationManager;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Scanner;

/**
 * Main entry point for the migration tool.
 * <p>
 * The MigrationTool class manages the migration process by interacting with the database and executing
 * migration commands (migrate, rollback, and information). It initializes necessary components like
 * the ConnectionManager, MigrationExecutor, and MigrationManager. Users can issue commands from the console
 * to perform various operations related to database migrations.
 * </p>
 *
 * <p><b>Usage:</b></p>
 * <pre>
 * MigrationTool tool = new MigrationTool("jdbc:mysql://localhost:3306/db", "user", "password", "migrations");
 * tool.run();
 * </pre>
 * <p>This tool supports commands like:</p>
 * <ul>
 *   <li>info: Displays migration information.</li>
 *   <li>migrate: Executes pending migrations.</li>
 *   <li>rollback: Rolls back migrations to a specific version.</li>
 *   <li>exit: Exits the tool.</li>
 * </ul>
 */
@Slf4j
public class MigrationTool {
    private final MigrationManager migrationManager;
    private final ConnectionManager connectionManager;
    private final MigrationExecutor migrationExecutor;

    private final List<Command> commands;

    /**
     * Initializes the MigrationTool with the provided database connection parameters and migration directory.
     * <p>
     * This constructor sets up the necessary components for the migration tool, including initializing
     * the database connection, migration manager, and migration executor. It also registers the available commands.
     * </p>
     *
     * @param url the database connection URL
     * @param username the database username
     * @param password the database password
     * @param migrationDirectory the directory where migration files are located
     */
    public MigrationTool(String url, String username, String password, String migrationDirectory) {
        log.info("Initializing MigrationTool with URL: {}, Username: {}, Migration Directory: {}",
                url, username, migrationDirectory);

        ConnectionManager.init(url, username, password);
        this.connectionManager = ConnectionManager.getInstance();
        this.migrationExecutor = new MigrationExecutor(connectionManager);
        this.migrationManager = new MigrationManager(migrationExecutor, migrationDirectory);

        this.commands = List.of(
                new MigrateCommand(this.migrationManager),
                new RollbackCommand(this.migrationManager),
                new InformationCommand(this.migrationManager)
        );

        log.info("MigrationTool initialized successfully.");
    }

    /**
     * Runs the migration tool, starting the command input loop.
     * <p>
     * This method prompts the user for commands in a loop and processes them accordingly. It waits for commands
     * like "info", "migrate", "rollback", and "exit". If an unrecognized command is entered, it provides feedback to the user.
     * </p>
     */
    public void run() {
        System.out.println("==MIGRATION APP==\nStarted and waiting for commands (info, migrate, rollback, exit):");
        waitAndRunCommands();
        System.out.println("==MIGRATION APP==\nShut down successfully");
    }

    private void waitAndRunCommands() {
        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String command = in.nextLine();
            if (command.startsWith("exit")) {
                break;
            }
            try {
                boolean commandFound = false;
                for (Command cm : this.commands) {
                    if (cm.isSuit(command)) {
                        cm.handleHelpArgument(List.of(command.split(" ")));
                        commandFound = true;
                        break;
                    }
                }
                if (!commandFound) {
                    System.out.println("Unknown command: " + command);
                }
            } catch (WrongCommandParamException e) {
                System.out.println(e.getMessage());
            }
        }
    }

}
