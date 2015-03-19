//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.executors;

//-----------------------------------------------------------------------------

import dk.dbc.iscrum.utils.logback.filters.BusinessLoggerFilter;
import dk.dbc.ocbtools.commons.filesystem.OCBFileSystem;
import dk.dbc.ocbtools.testengine.testcases.Testcase;
import dk.dbc.updateservice.client.BibliographicRecordFactory;
import dk.dbc.updateservice.client.UpdateService;
import dk.dbc.updateservice.integration.service.*;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.*;

//-----------------------------------------------------------------------------
/**
 * Created by stp on 19/03/15.
 */
public class RemoteValidateExecutor implements TestExecutor {
    public RemoteValidateExecutor( Testcase tc ) {
        this.tc = tc;
    }

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
            assertNotNull( "Property'en 'request' er obligatorisk.", tc.getRequest() );
            assertNotNull( "Property'en 'authentication' er obligatorisk.", tc.getRequest().getAuthentication() );
            assertNotNull( "Property'en 'group' er obligatorisk.", tc.getRequest().getAuthentication().getGroup() );
            assertNotNull( "Property'en 'user' er obligatorisk.", tc.getRequest().getAuthentication().getUser() );
            assertNotNull( "Property'en 'password' er obligatorisk.", tc.getRequest().getAuthentication().getPassword() );

            OCBFileSystem fs = new OCBFileSystem();
            Properties settings = fs.loadSettings( "servers.properties" );

            String key = String.format( "updateservice.%s.url", tc.getDistributionName() );
            URL url = new URL( settings.getProperty( key ) );
            UpdateService caller = new UpdateService( url );

            UpdateRecordRequest request = createRequest();

            logger.debug( "Sending request '{}' to {}", request.getTrackingId(), url );
            UpdateRecordResult response = caller.createPort().updateRecord( request );

            Asserter.assertValidation( tc.getValidation(), response.getValidateInstance() );
            if( tc.getValidation().isEmpty() ) {
                assertEquals( UpdateStatusEnum.VALIDATE_ONLY, response.getUpdateStatus() );
                assertNull( response.getValidateInstance() );
            }
            else {
                assertEquals( UpdateStatusEnum.VALIDATION_ERROR, response.getUpdateStatus() );
            }
            assertNull( response.getError() );
        }
        catch( SAXException | ParserConfigurationException | JAXBException | IOException ex ) {
            output.error( "Unable to run testcase '{}': {}", tc.getName(), ex.getMessage() );
            logger.debug( "Stacktrace:", ex );
        }
        finally {
            logger.exit();
        }
    }

    //-------------------------------------------------------------------------
    //              Helpers
    //-------------------------------------------------------------------------

    private UpdateRecordRequest createRequest() throws IOException, JAXBException, SAXException, ParserConfigurationException {
        logger.entry();

        try {
            UpdateRecordRequest request = new UpdateRecordRequest();

            Authentication auth = new Authentication();
            auth.setUserIdAut( tc.getRequest().getAuthentication().getUser() );
            auth.setGroupIdAut( tc.getRequest().getAuthentication().getGroup() );
            auth.setPasswordAut( tc.getRequest().getAuthentication().getPassword() );

            request.setAuthentication( auth );
            request.setSchemaName( tc.getRequest().getTemplateName() );
            request.setTrackingId( String.format( TRACKING_ID_FORMAT, System.getProperty( "user.name" ), tc.getName() ) );

            Options options = new Options();
            options.getOption().add( UpdateOptionEnum.VALIDATE_ONLY );
            request.setOptions( options );

            File recordFile = new File( tc.getFile().getParentFile().getCanonicalPath() + "/" + tc.getRequest().getRecord() );
            request.setBibliographicRecord( BibliographicRecordFactory.loadMarcRecordInLineFormat( recordFile ) );

            return request;
        }
        finally {
            logger.exit();
        }
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private static final XLogger output = XLoggerFactory.getXLogger( BusinessLoggerFilter.LOGGER_NAME );
    private static final XLogger logger = XLoggerFactory.getXLogger( RemoteValidateExecutor.class );
    private static final String TRACKING_ID_FORMAT = "ocbtools-%s-%s";

    private Testcase tc;
}
