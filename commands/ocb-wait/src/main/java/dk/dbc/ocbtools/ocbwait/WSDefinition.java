//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.ocbwait;

//-----------------------------------------------------------------------------
import dk.dbc.ocbtools.commons.api.Subcommand;
import dk.dbc.ocbtools.commons.api.SubcommandDefinition;
import dk.dbc.ocbtools.commons.api.SubcommandExecutor;
import dk.dbc.ocbtools.commons.cli.CliException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

//-----------------------------------------------------------------------------
/**
 * Created by stp on 25/04/16.
 */
@Subcommand( name = "ws",
        description = "Waits for a webservice to return ok.",
        usage = "[options] url" )
public class WSDefinition implements SubcommandDefinition {

    private static final XLogger logger = XLoggerFactory.getXLogger( WSDefinition.class );

    /**
     * Must return a list of extra options to the subcommand that this implementation represents.
     *
     * @return List of options.
     */
    @Override
    public List<Option> createOptions() throws CliException {
        List<Option> options = new ArrayList<>();

        Option option;

        option = new Option( "tt", "text", true, "Number of requests for each user" );
        option.setRequired( true );
        options.add( option );

        option = new Option( "to", "timeout", true, "Agency id to create requests for" );
        option.setRequired( true );
        options.add( option );

        return options;
    }

    /**
     * Factory method to create a CommandExecutor from the arguments parsed on the command line.
     * <p/>
     * This method is called after the arguments to the sub command has been successfully parsed.
     *
     * @param baseDir
     * @param line    The parsed values from the command line.
     *
     * @return A command executor.
     */
    @Override
    public SubcommandExecutor createExecutor( File baseDir, CommandLine line ) throws CliException {
        logger.entry();

        try {
            if( line.getArgList().isEmpty() ) {
                throw new CliException( "An URL is required." );
            }

            WSExecutor executor = new WSExecutor();
            executor.setUrl( new URL( (String) line.getArgList().get( 0 ) ) );
            executor.setText( line.getOptionValue( "tt" ) );
            executor.setTimeout( Integer.valueOf( line.getOptionValue( "to" ) ) );

            return executor;
        }
        catch( NumberFormatException ex ) {
            throw new CliException( "Timeout must be a whole number: " + ex.getMessage(), ex );
        }
        catch( MalformedURLException ex ) {
            throw new CliException( "Error with url: " + ex.getMessage(), ex );
        }
        finally {
            logger.exit();
        }
    }

}
