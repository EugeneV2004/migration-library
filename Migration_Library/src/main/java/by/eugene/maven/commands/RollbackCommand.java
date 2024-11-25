package by.eugene.maven.commands;

import by.eugene.maven.exceptions.WrongCommandParamException;
import by.eugene.maven.migrations.MigrationManager;

import java.util.List;
import java.util.Map;

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

    public RollbackCommand(MigrationManager migrationManager) {
        super("rollback", List.of(VERSION_PARAM), HELP_MESSAGE);
        this.migrationManager = migrationManager;
    }

    @Override
    public void execute(List<String> args) {
        Map<String, String> paramsMap = parseParams(args);
        if (paramsMap.get(VERSION_PARAM) == null) {
            throw new WrongCommandParamException("Parameter %s is required".formatted(VERSION_PARAM));
        }

        this.migrationManager.executeRollbacks(Integer.parseInt(paramsMap.get(VERSION_PARAM)));
    }
}

