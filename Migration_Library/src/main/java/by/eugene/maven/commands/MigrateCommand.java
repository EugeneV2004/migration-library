package by.eugene.maven.commands;

import by.eugene.maven.exceptions.WrongCommandParamException;
import by.eugene.maven.migrations.MigrationManager;

import java.util.List;
import java.util.Map;

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

    public MigrateCommand(MigrationManager migrationManager) {
        super("migrate", List.of(DIRECTORY_PARAM, VERSION_PARAM), HELP_MESSAGE);
        this.migrationManager = migrationManager;
    }

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

