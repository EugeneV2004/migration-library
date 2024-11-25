package by.eugene.maven.exceptions;

/**
 * Exception thrown when a command is provided with invalid or missing parameters.
 * <p>
 * This exception is used to signal that the user has supplied incorrect parameters for a command,
 * such as missing required parameters or providing parameters in an invalid format.
 * </p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>
 * throw new WrongCommandParamException("Missing value for parameter -version");
 * </pre>
 */
public class WrongCommandParamException extends RuntimeException {
    /**
     * Constructs a new {@code WrongCommandParamException} with the specified detail message.
     * <p>
     * This constructor initializes the exception with a message that provides more information
     * about the invalid or missing command parameter.
     * </p>
     *
     * @param message the detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method)
     */
    public WrongCommandParamException(String message) {
        super(message);
    }
}

