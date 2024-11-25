package by.eugene.maven.commands;

import by.eugene.maven.exceptions.WrongCommandParamException;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for all commands in the migration tool.
 * <p>
 * The {@code Command} class represents a base structure for the commands executed by the migration tool.
 * It provides functionality for handling command parameters, displaying help information, and executing the command logic.
 * Specific commands like {@code MigrateCommand}, {@code RollbackCommand}, and {@code InformationCommand}
 * should extend this class and implement the {@code execute} method.
 * </p>
 *
 * <p><b>Usage:</b></p>
 * <pre>
 * Command command = new MigrateCommand(...);
 * command.execute(args);
 * </pre>
 */
public abstract class Command {
    private static final String HELP_PARAM = "-h";

    private final String command;
    private final List<String> applicableParams;
    @Getter
    private String helpInfo;

    /**
     * Constructs a new command with the given command name, applicable parameters, and help information.
     * <p>
     * This constructor is used by subclasses to initialize the command with its name, a list of parameters it supports,
     * and a string containing help information about the command.
     * </p>
     *
     * @param command the name of the command
     * @param applicableParams a list of parameters that the command supports
     * @param helpInfo the help information associated with the command
     */
    protected Command(String command, List<String> applicableParams, String helpInfo) {
        this.command = command;
        this.applicableParams = applicableParams;
        this.helpInfo = helpInfo;
    }

    /**
     * Parses the command-line parameters provided to the command.
     * <p>
     * This method processes a list of arguments, validates the parameters, and stores them in a map
     * where the key is the parameter name (e.g., "-h") and the value is the corresponding parameter value.
     * </p>
     *
     * @param args the list of arguments passed to the command
     * @return a map of parameter names and values
     * @throws WrongCommandParamException if there is a parameter format issue, like missing values
     */
    public static Map<String, String> parseParams(List<String> args) {
        Map<String, String> parameters = new HashMap<>();

        for (int i = 1; i < args.size(); i++) {
            String arg = args.get(i);

            if (arg.startsWith("-")) {
                if (i + 1 < args.size() && !args.get(i + 1).startsWith("-")) {
                    parameters.put(arg, args.get(i + 1));
                    i++;
                } else {
                    throw new WrongCommandParamException("Missing value for parameter: " + arg);
                }
            } else {
                throw new WrongCommandParamException("Invalid argument format: " + arg);
            }
        }

        return parameters;
    }

    /**
     * Handles the help argument for the command.
     * <p>
     * If the help parameter {@code -h} is present in the argument list, this method prints the help information
     * for the command. Otherwise, it proceeds to execute the command with the provided arguments.
     * </p>
     *
     * @param args the list of arguments passed to the command
     */
    public void handleHelpArgument(List<String> args) {
        if (args.contains(HELP_PARAM)) {
            System.out.println(this.getHelpInfo());
            return;
        }
        execute(args);
    }

    /**
     * Executes the command with the given arguments.
     * <p>
     * This method is abstract and should be implemented by subclasses to define the specific logic for executing
     * the command.
     * </p>
     *
     * @param args the list of arguments passed to the command
     */
    public abstract void execute(List<String> args);

    /**
     * Checks if the provided string matches this command.
     * <p>
     * This method checks if the provided command string starts with the command name of this class.
     * It is used to determine if the command should handle the input.
     * </p>
     *
     * @param commandToCheck the command string to check
     * @return {@code true} if the command matches, {@code false} otherwise
     */
    public boolean isSuit(String commandToCheck){
        return commandToCheck.startsWith(this.command);
    }
}

