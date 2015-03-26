//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.executors;

//-----------------------------------------------------------------------------

import dk.dbc.holdingsitems.HoldingsItemsException;
import dk.dbc.iscrum.records.MarcRecord;
import dk.dbc.ocbtools.commons.filesystem.OCBFileSystem;
import dk.dbc.ocbtools.testengine.asserters.Asserter;
import dk.dbc.ocbtools.testengine.testcases.Testcase;
import dk.dbc.ocbtools.testengine.testcases.TestcaseRecord;
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
 * Executor to test a testcase against the validation operation on an external
 * installation of Update.
 */
public class RemoteValidateExecutor implements TestExecutor {
    public RemoteValidateExecutor( Testcase tc ) {
        logger = XLoggerFactory.getXLogger( RemoteValidateExecutor.class );
        this.tc = tc;
    }

    @Override
    public String name() {
        return "Validate record against remote UpdateService";
    }

    @Override
    public void setup() {
        logger.entry();

        try {
            OCBFileSystem fs = new OCBFileSystem();
            Properties settings = fs.loadSettings( "servers.properties" );

            RawRepo.setupDatabase( settings );
            Holdings.setupDatabase( settings );

            if( !hasRawRepoSetup( tc ) ) {
                return;
            }

            try( Connection conn = RawRepo.getConnection( settings ) ) {
                RawRepo rawRepo = null;

                try {
                    rawRepo = new RawRepo( conn );
                    rawRepo.saveRecords( tc.getFile().getParentFile(), tc.getSetup().getRawrepo() );
                    setupRelations( fs, rawRepo );

                    conn.commit();
                }
                catch( JAXBException | RawRepoException ex ) {
                    if( rawRepo != null ) {
                        conn.rollback();
                    }

                    throw new AssertionError( ex.getMessage(), ex );
                }
            }

            if( hasSolrSetup( tc ) ) {
                Solr.waitForIndex( settings );
            }

            if( tc.getSetup() != null && !tc.getSetup().getHoldings().isEmpty() ) {
                try( Connection conn = Holdings.getConnection( settings ) ) {
                    MarcRecord marcRecord = fs.loadRecord( tc.getFile().getParentFile(), tc.getRequest().getRecord() );

                    Holdings.saveHoldings( conn, marcRecord, tc.getSetup().getHoldings() );
                }
            }
        }
        catch( ClassNotFoundException | SQLException | IOException | HoldingsItemsException ex ) {
            throw new AssertionError( ex.getMessage(), ex );
        }
        finally {
            logger.exit();
        }
    }

    private void setupRelations( OCBFileSystem fs, RawRepo rawRepo ) throws IOException, RawRepoException {
        logger.entry( fs, rawRepo );

        try {
            File baseDir = tc.getFile().getParentFile();
            for( TestcaseRecord record : tc.getSetup().getRawrepo() ) {
                MarcRecord commonOrParentRecord = null;

                if( record.getChildren() != null ) {
                    commonOrParentRecord = fs.loadRecord( baseDir, record.getRecord() );

                    for( String enrichmentOrChildFilename : record.getChildren() ) {
                        rawRepo.saveRelation( commonOrParentRecord, fs.loadRecord( baseDir, enrichmentOrChildFilename ) );
                    }
                }

                if( record.getEnrichments() != null ) {
                    if( commonOrParentRecord == null ) {
                        commonOrParentRecord = fs.loadRecord( baseDir, record.getRecord() );
                    }

                    for( String enrichmentOrChildFilename : record.getEnrichments() ) {
                        rawRepo.saveRelation( commonOrParentRecord, fs.loadRecord( baseDir, enrichmentOrChildFilename ) );
                    }
                }
            }
        }
        finally {
            logger.exit();
        }
    }

    @Override
    public void teardown() {
        logger.entry();

        try {
            OCBFileSystem fs = new OCBFileSystem();
            Properties settings = fs.loadSettings( "servers.properties" );

            RawRepo.teardownDatabase( settings );
            Solr.clearIndex( settings );

            Holdings.teardownDatabase( settings );
        }
        catch( ClassNotFoundException | SQLException | IOException ex ) {
            throw new AssertionError( ex.getMessage(), ex );
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
                Asserter.assertValidation( tc.getExpected().getValidation(), response.getValidateInstance() );
                if( tc.getExpected().getValidation().isEmpty() ) {
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
            throw new AssertionError( ex.getMessage(), ex );
        }
        finally {
            logger.exit();
        }
    }

    //-------------------------------------------------------------------------
    //              Helpers
    //-------------------------------------------------------------------------

    protected UpdateRecordRequest createRequest() throws IOException, JAXBException, SAXException, ParserConfigurationException {
        logger.entry();

        try {
            UpdateRecordRequest request = new UpdateRecordRequest();

            Authentication auth = new Authentication();
            auth.setUserIdAut( tc.getRequest().getAuthentication().getUser() );
            auth.setGroupIdAut( tc.getRequest().getAuthentication().getGroup() );
            auth.setPasswordAut( tc.getRequest().getAuthentication().getPassword() );

            request.setAuthentication( auth );
            request.setSchemaName( tc.getRequest().getTemplateName() );
            request.setTrackingId( String.format( TRACKING_ID_FORMAT, System.getProperty( "user.name" ), tc.getName(), getClass().getSimpleName() ) );

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

    protected static final String TRACKING_ID_FORMAT = "ocbtools-%s-%s-%s";

    protected XLogger logger;
    protected Testcase tc;
}
