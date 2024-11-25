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

@Slf4j
public class MigrationTool {
    private final MigrationManager migrationManager;
    private final ConnectionManager connectionManager;
    private final MigrationExecutor migrationExecutor;

    private final List<Command> commands;

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
