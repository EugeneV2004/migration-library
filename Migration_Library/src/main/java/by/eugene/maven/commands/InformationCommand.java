package by.eugene.maven.commands;

import by.eugene.maven.migrations.MigrationManager;

import java.util.Collections;
import java.util.List;

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

    public InformationCommand(MigrationManager migrationManager) {
        super("info", Collections.emptyList(), HELP_MESSAGE);
        this.migrationManager = migrationManager;
    }

    @Override
    public void execute(List<String> args) {
        System.out.println(INFO_MESSAGE_PATTERN.formatted(this.migrationManager.getCurrentDbVersion()));
    }
}
