package by.eugene.maven.exceptions;

public class WrongCommandParamException extends RuntimeException {
    public WrongCommandParamException(String message) {
        super(message);
    }
}

