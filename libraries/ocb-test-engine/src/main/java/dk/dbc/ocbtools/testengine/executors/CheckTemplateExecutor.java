//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.executors;

//-----------------------------------------------------------------------------

import dk.dbc.ocbtools.scripter.Distribution;
import dk.dbc.ocbtools.scripter.ScripterException;
import dk.dbc.ocbtools.scripter.ServiceScripter;
import dk.dbc.ocbtools.testengine.testcases.Testcase;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;

//-----------------------------------------------------------------------------
/**
 * Created by stp on 19/02/15.
 */
public class CheckTemplateExecutor implements TestExecutor {
    public CheckTemplateExecutor( File baseDir, Testcase tc ) {
        this.baseDir = baseDir;
        this.tc = tc;
    }

    //-------------------------------------------------------------------------
    //              TestExecutor interface
    //-------------------------------------------------------------------------

    @Override
    public void setup() {
    }

    @Override
    public void teardown() {
    }

    @Override
    public void executeTests() {
        logger.entry();

        try {
            ServiceScripter scripter = new ServiceScripter();
            scripter.setBaseDir( baseDir.getCanonicalPath() );
            scripter.setModulesKey( "unittest.modules.search.path" );

            ArrayList<Distribution> distributions = new ArrayList<>();
            distributions.add( new Distribution( tc.getDistributionName(), "distributions/" + tc.getDistributionName() ) );
            logger.debug( "Using distributions: {}", distributions );

            scripter.setDistributions( distributions );
            scripter.setServiceName( SERVICE_NAME );

            HashMap<String, String> settings = new HashMap<>();
            settings.put( "javascript.basedir", baseDir.getAbsolutePath() );
            settings.put( "javascript.install.name", tc.getDistributionName() );

            String message = String.format( "The template '%s' does not exist in testcase %s", tc.getTemplateName(), tc.getName() );
            assertTrue( message, (Boolean) scripter.callMethod( SCRIPT_FILENAME, SCRIPT_FUNCTION, tc.getTemplateName(), settings ) );
        }
        catch( IOException | ScripterException ex ) {
            throw new AssertionError( String.format( "Fatal error when checking template for testcase %s", tc.getName() ), ex );
        }
        finally {
            logger.exit();
        }
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private static final XLogger logger = XLoggerFactory.getXLogger( CheckTemplateExecutor.class );
    private static final String SCRIPT_FILENAME = "validator.js";
    private static final String SCRIPT_FUNCTION = "checkTemplate";
    private static final String SERVICE_NAME = "update";

    private File baseDir;
    private Testcase tc;
}
