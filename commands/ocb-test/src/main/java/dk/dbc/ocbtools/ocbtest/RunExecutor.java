//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.ocbtest;

//-----------------------------------------------------------------------------
import dk.dbc.ocbtools.commons.api.SubcommandExecutor;
import org.apache.commons.cli.CommandLine;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

//-----------------------------------------------------------------------------
/**
 * Created by stp on 07/02/15.
 */
public class RunExecutor implements SubcommandExecutor {
    public RunExecutor( CommandLine line ) {
        this.line = line;
    }

    //-------------------------------------------------------------------------
    //              SubcommandExecutor interface
    //-------------------------------------------------------------------------

    @Override
    public void actionPerformed() {
        logger.info( "Executing 'run' subcommand" );
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private static final XLogger logger = XLoggerFactory.getXLogger( RunExecutor.class );
    private CommandLine line;
}
