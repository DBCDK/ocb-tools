//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.ocbtest;

//-----------------------------------------------------------------------------
import dk.dbc.ocbtools.commons.cli.CliExecutor;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

//-----------------------------------------------------------------------------
/**
 * This contains the main function for the command line tools <code>ocb-test</code>
 */
public class OCBTest {
    public static void main( String[] args ) throws InstantiationException, IllegalAccessException {
        logger.debug( "Arguments: {}", args );

        CliExecutor cli = new CliExecutor();
        cli.execute( "ocb-test [kommando] [options]", args );
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private static final XLogger logger = XLoggerFactory.getXLogger( OCBTest.class );
}
