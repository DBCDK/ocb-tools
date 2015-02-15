//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.ocbtest;

//-----------------------------------------------------------------------------

import dk.dbc.iscrum.utils.logback.filters.BusinessLoggerFilter;
import dk.dbc.ocbtools.commons.api.SubcommandExecutor;
import dk.dbc.ocbtools.commons.cli.CliException;
import dk.dbc.ocbtools.commons.filesystem.OCBFileSystem;
import dk.dbc.ocbtools.testengine.testcases.Testcase;
import dk.dbc.ocbtools.testengine.testcases.TestcaseRepository;
import dk.dbc.ocbtools.testengine.testcases.TestcaseRepositoryFactory;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.IOException;
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

            OCBFileSystem fs = new OCBFileSystem();
            TestcaseRepository repo = TestcaseRepositoryFactory.newInstanceWithTestcases( fs );
            for( Testcase tc : repo.findAll() ) {
                if( !matchAnyExpressions( tc, matchExpressions ) ) {
                    continue;
                }

                String filename = tc.getFile().getCanonicalPath();
                filename = filename.replace( fs.getBaseDir().getCanonicalPath() + "/", "" );

                logger.info( "{}:", tc.getName() );
                logger.info( "    Fil: {}", filename );
                logger.info( "    Beskrivelse: {}", tc.getDescription() );
            }
        }
        catch( IOException ex ) {
            throw new CliException( ex.getMessage(), ex );
        }
        finally {
            logger.exit();
        }
    }

    private boolean matchAnyExpressions( Testcase tc, List<String> regexps ) {
        logger.entry();

        try {
            if( regexps.isEmpty() ) {
                return true;
            }

            for( String regex : regexps ) {
                if( tc.getName().matches( regex ) ) {
                    return true;
                }
            }

            return false;
        }
        finally {
            logger.exit();
        }
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private static final XLogger logger = XLoggerFactory.getXLogger( BusinessLoggerFilter.LOGGER_NAME );

    private File baseDir;
    private List<String> matchExpressions;
}
