//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.executors;

//-----------------------------------------------------------------------------

import dk.dbc.iscrum.utils.logback.filters.BusinessLoggerFilter;
import dk.dbc.ocbtools.commons.filesystem.OCBFileSystem;
import dk.dbc.ocbtools.testengine.testcases.Testcase;
import dk.dbc.ocbtools.testengine.testcases.TestcaseSolrQuery;
import dk.dbc.rawrepo.RawRepoException;
import dk.dbc.updateservice.client.BibliographicRecordFactory;
import dk.dbc.updateservice.client.UpdateService;
import dk.dbc.updateservice.service.api.*;
import org.perf4j.StopWatch;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
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
        logger.entry();

        try {
            if( !hasRawRepoSetup( tc ) ) {
                return;
            }

            OCBFileSystem fs = new OCBFileSystem();
            Properties settings = fs.loadSettings( "servers.properties" );

            RawRepo.setupDatabase( settings );

            try( Connection conn = RawRepo.getConnection( settings ) ) {
                RawRepo rawRepo = null;

                try {
                    rawRepo = new RawRepo( conn );
                    rawRepo.saveRecords( tc.getFile().getParentFile(), tc.getSetup().getRawrepo() );

                    conn.commit();
                }
                catch( JAXBException | RawRepoException ex ) {
                    if( rawRepo != null ) {
                        conn.rollback();
                    }

                    output.error( "Unable to run testcase '{}': {}", tc.getName(), ex.getMessage() );
                    logger.debug( "Stacktrace:", ex );
                }
            }

            if( hasSolrSetup( tc ) ) {
                Solr.waitForIndex( settings );
            }
        }
        catch( ClassNotFoundException | SQLException | IOException ex ) {
            output.error( "Unable to setup testcase '{}': {}", tc.getName(), ex.getMessage() );
            logger.debug( "Stacktrace:", ex );
        }
        finally {
            logger.exit();
        }
    }

    @Override
    public void teardown() {
        logger.entry();

        try {
            if( !hasRawRepoSetup( tc ) ) {
                return;
            }

            OCBFileSystem fs = new OCBFileSystem();
            Properties settings = fs.loadSettings( "servers.properties" );

            RawRepo.teardownDatabase( settings );
            Solr.clearIndex( settings );
        }
        catch( ClassNotFoundException | SQLException | IOException ex ) {
            output.error( "Unable to teardown testcase '{}': {}", tc.getName(), ex.getMessage() );
            logger.debug( "Stacktrace:", ex );
        }
        finally {
            logger.exit();
        }
    }

    @Override
    public void executeTests() {
        logger.entry();

        try {
            assertNotNull( "Property'en 'request' er obligatorisk.", tc.getRequest() );
            assertNotNull( "Property'en 'request.authentication' er obligatorisk.", tc.getRequest().getAuthentication() );
            assertNotNull( "Property'en 'request.authentication.group' er obligatorisk.", tc.getRequest().getAuthentication().getGroup() );
            assertNotNull( "Property'en 'request.authentication.user' er obligatorisk.", tc.getRequest().getAuthentication().getUser() );
            assertNotNull( "Property'en 'request.authentication.password' er obligatorisk.", tc.getRequest().getAuthentication().getPassword() );

            OCBFileSystem fs = new OCBFileSystem();
            Properties settings = fs.loadSettings( "servers.properties" );

            String key = String.format( "updateservice.%s.url", tc.getDistributionName() );
            URL url = new URL( settings.getProperty( key ) );
            UpdateService caller = new UpdateService( url );

            UpdateRecordRequest request = createRequest();

            StopWatch watch = new StopWatch();

            logger.debug( "Sending request '{}' to {}", request.getTrackingId(), url );
            watch.start();
            UpdateRecordResult response = caller.createPort().updateRecord( request );
            watch.stop();
            logger.debug( "Receive response in {} ms: {}", watch.getElapsedTime(), response );

            watch.start();
            try {
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
            finally {
                watch.stop();
                logger.debug( "Test response in {} ms", watch.getElapsedTime() );
            }
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

    private boolean hasRawRepoSetup( Testcase tc ) {
        logger.entry( tc );

        boolean result = true;
        try {
            if( tc.getSetup() == null ) {
                result = false;
            }
            else if( tc.getSetup().getRawrepo() == null ) {
                result = false;
            }

            return result;
        }
        finally {
            logger.exit( result );
        }
    }

    private boolean hasSolrSetup( Testcase tc ) {
        logger.entry( tc );

        boolean result = true;
        try {
            if( tc.getSetup() == null ) {
                return result = false;
            }

            if( tc.getSetup().getSolr() == null ) {
                return result = false;
            }

            for( TestcaseSolrQuery solrQuery : tc.getSetup().getSolr() ) {
                result = solrQuery.getNumFound() > 0;
                if( result ) {
                    return result;
                }
            }

            return result;
        }
        finally {
            logger.exit( result );
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
