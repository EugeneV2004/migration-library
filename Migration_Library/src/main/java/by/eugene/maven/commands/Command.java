package by.eugene.maven.commands;

import by.eugene.maven.exceptions.WrongCommandParamException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Command {
    private static final String HELP_PARAM = "-h";

    private final String command;
    private final List<String> applicableParams;
    private String helpInfo;

    protected Command(String command, List<String> applicableParams, String helpInfo) {
        this.command = command;
        this.applicableParams = applicableParams;
        this.helpInfo = helpInfo;
    }

    public String getHelpInfo() {
        return this.helpInfo;
    }

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

    public void handleHelpArgument(List<String> args) {
        if (args.contains(HELP_PARAM)) {
            System.out.println(this.getHelpInfo());
            return;
        }
        execute(args);
    }

    public abstract void execute(List<String> args);
    public boolean isSuit(String commandToCheck){
        return commandToCheck.startsWith(this.command);
    }
}

