//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.executors;

//-----------------------------------------------------------------------------

import dk.dbc.iscrum.records.MarcRecord;
import dk.dbc.iscrum.utils.json.Json;
import dk.dbc.ocbtools.scripter.Distribution;
import dk.dbc.ocbtools.scripter.ScripterException;
import dk.dbc.ocbtools.scripter.ServiceScripter;
import dk.dbc.ocbtools.testengine.testcases.Testcase;
import dk.dbc.ocbtools.testengine.testcases.ValidationResult;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//-----------------------------------------------------------------------------
/**
 * Created by stp on 19/02/15.
 */
public class ValidateRecordExecutor implements TestExecutor {
    public ValidateRecordExecutor( File baseDir, Testcase tc ) {
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
            MarcRecord record = tc.loadRecord();
            ServiceScripter scripter = createScripter();
            Map<String, String> settings = createSettings();

            Object jsResult = scripter.callMethod( SCRIPT_FILENAME, SCRIPT_FUNCTION,
                                                   tc.getRequest().getTemplateName(), Json.encode( record ), settings );
            List<ValidationResult> valErrors = Json.decodeArray( jsResult.toString(), ValidationResult.class );

            if( !tc.getValidation().equals( valErrors ) ) {
                if( tc.getValidation().size() != valErrors.size() ) {
                    throw new AssertionError( String.format( "Number of validation errors differ.\nExpected:\n%s\nActual:\n%s\n", Json.encodePretty( tc.getValidation() ), Json.encodePretty( valErrors ) ) );
                }

                for( int i = 0; i < tc.getValidation().size(); i++ ) {
                    if( !tc.getValidation().get( i ).equals( valErrors.get( i ) ) ) {
                        throw new AssertionError( String.format( "Validation error at position %s differ.\n" +
                                "Expected:\n" +
                                "%s\n" +
                                "Actual:\n" +
                                "%s\n", i + 1, Json.encodePretty( tc.getValidation().get( i ) ), Json.encodePretty( valErrors.get( i ) ) ) );
                    }
                }
            }
        }
        catch( IOException | ScripterException ex ) {
            throw new AssertionError( String.format( "Fatal error when checking template for testcase %s", tc.getName() ), ex );
        }
        finally {
            logger.exit();
        }
    }

    //-------------------------------------------------------------------------
    //              Helpers
    //-------------------------------------------------------------------------

    private Map<String, String> createSettings() {
        HashMap<String, String> settings = new HashMap<>();
        settings.put( "javascript.basedir", baseDir.getAbsolutePath() );
        settings.put( "javascript.install.name", tc.getDistributionName() );

        return settings;
    }

    private ServiceScripter createScripter() throws IOException {
        ServiceScripter scripter = new ServiceScripter();
        scripter.setBaseDir( baseDir.getCanonicalPath() );
        scripter.setModulesKey( "unittest.modules.search.path" );

        ArrayList<Distribution> distributions = new ArrayList<>();
        distributions.add( new Distribution( "ocbtools", "ocb-tools" ) );
        distributions.add( new Distribution( tc.getDistributionName(), "distributions/" + tc.getDistributionName() ) );
        logger.debug( "Using distributions: {}", distributions );

        scripter.setDistributions( distributions );
        scripter.setServiceName( SERVICE_NAME );

        return scripter;
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private static final XLogger logger = XLoggerFactory.getXLogger( CheckTemplateExecutor.class );
    private static final String SCRIPT_FILENAME = "ValidateRecordExecutor.use.js";
    private static final String SCRIPT_FUNCTION = "validateRecord";
    private static final String SERVICE_NAME = "ocb-test";

    private File baseDir;
    private Testcase tc;
}
