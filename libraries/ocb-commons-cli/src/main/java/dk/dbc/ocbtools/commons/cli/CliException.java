package dk.dbc.ocbtools.commons.cli;

/**
 * Exception class to report errors from the Scripter class.
 */
public class CliException extends Exception {
    public CliException( String format, Object... args ) {
        super( String.format( format, args ) );
    }

    public CliException( String message, Throwable cause ) {
        super( message, cause );
    }
}
