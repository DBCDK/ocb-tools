//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.ocbtest;

//-----------------------------------------------------------------------------

import dk.dbc.ocbtools.commons.api.SubcommandExecutor;
import dk.dbc.ocbtools.commons.cli.CliException;
import dk.dbc.ocbtools.scripter.Distribution;
import dk.dbc.ocbtools.scripter.ScripterException;
import dk.dbc.ocbtools.scripter.ServiceScripter;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Executor for the list subcommand.
 */
public class ListExecutor implements SubcommandExecutor {
    public ListExecutor( File baseDir, List<String> matchExpressions ) {
        this.baseDir = baseDir;
        this.matchExpressions = matchExpressions;
    }

    //-------------------------------------------------------------------------
    //              SubcommandExecutor interface
    //-------------------------------------------------------------------------

    @Override
    public void actionPerformed() throws CliException {
        logger.entry();

        try {
            logger.debug( "Match expressions: {}", matchExpressions );

            ServiceScripter scripter = new ServiceScripter();
            scripter.setBaseDir( baseDir.getCanonicalPath() );
            scripter.setModulesKey( "unittest.modules.search.path" );

            ArrayList<Distribution> distributions = new ArrayList<>();
            distributions.add( new Distribution( "ocbtools", "ocb-tools" ) );
            scripter.setDistributions( distributions );
            scripter.setServiceName( "ocb-test" );

            scripter.callMethod( "ListTestCases.use.js", "actionPerformed", baseDir.getCanonicalPath(), matchExpressions );
        }
        catch( IOException | ScripterException ex ) {
            throw new CliException( ex.getMessage(), ex );
        }
        finally {
            logger.exit();
        }
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private static final XLogger logger = XLoggerFactory.getXLogger( ListExecutor.class );

    private File baseDir;
    private List<String> matchExpressions;
}
