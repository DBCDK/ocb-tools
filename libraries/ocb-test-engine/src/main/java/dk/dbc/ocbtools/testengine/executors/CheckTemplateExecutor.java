//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.executors;

//-----------------------------------------------------------------------------

import dk.dbc.ocbtools.scripter.ScripterException;
import dk.dbc.ocbtools.scripter.ServiceScripter;
import dk.dbc.ocbtools.testengine.testcases.Testcase;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.util.Map;

import static org.junit.Assert.assertTrue;

//-----------------------------------------------------------------------------
/**
 * Created by stp on 19/02/15.
 */
public class CheckTemplateExecutor implements TestExecutor {
    public CheckTemplateExecutor() {
        this.scripter = null;
        this.fileName = "";
        this.scriptSettings = null;
        this.tc = null;
    }

    //-------------------------------------------------------------------------
    //              Properties
    //-------------------------------------------------------------------------

    public void setScripter( ServiceScripter scripter ) {
        this.scripter = scripter;
    }

    public void setFileName( String fileName ) {
        this.fileName = fileName;
    }

    public void setScriptSettings( Map<String, String> scriptSettings ) {
        this.scriptSettings = scriptSettings;
    }

    public void setTc( Testcase tc ) {
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
            String message = String.format( "The template '{}' does not exist in testcase {}", tc.getTemplateName(), tc.getName() );
            assertTrue( message, (Boolean) scripter.callMethod( fileName, SCRIPT_FUNCTION, tc.getTemplateName(), scriptSettings ) );
        }
        catch( ScripterException ex ) {
            throw new AssertionError( String.format( "Fatal error when checking template for testcase {}", tc.getName() ), ex );
        }
        finally {
            logger.exit();
        }
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private static final XLogger logger = XLoggerFactory.getXLogger( CheckTemplateExecutor.class );
    private static final String SCRIPT_FUNCTION = "checkTemplate";

    private ServiceScripter scripter;
    private String fileName;
    private Map<String, String> scriptSettings;
    private Testcase tc;
}
