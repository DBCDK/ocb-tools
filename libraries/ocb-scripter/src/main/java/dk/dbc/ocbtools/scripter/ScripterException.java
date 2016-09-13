package dk.dbc.ocbtools.scripter;

/**
 * Exception class to report errors from the Scripter class.
 */
public class ScripterException extends Exception {
    public ScripterException(String format, Object... args) {
        super(String.format(format, args));
    }

    public ScripterException(String message, Throwable cause) {
        super(message, cause);
    }
}
