//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.executors;

//-----------------------------------------------------------------------------

import dk.dbc.iscrum.records.MarcRecord;
import dk.dbc.ocbtools.commons.filesystem.OCBFileSystem;
import dk.dbc.ocbtools.testengine.asserters.Asserter;
import dk.dbc.ocbtools.testengine.asserters.RawRepoAsserter;
import dk.dbc.ocbtools.testengine.testcases.Testcase;
import dk.dbc.ocbtools.testengine.testcases.TestcaseRecord;
import dk.dbc.ocbtools.testengine.testcases.ValidationResult;
import dk.dbc.rawrepo.RawRepoException;
import dk.dbc.rawrepo.RecordId;
import dk.dbc.updateservice.client.UpdateService;
import dk.dbc.updateservice.service.api.UpdateRecordRequest;
import dk.dbc.updateservice.service.api.UpdateRecordResult;
import dk.dbc.updateservice.service.api.UpdateStatusEnum;
import org.perf4j.StopWatch;
import org.slf4j.ext.XLoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

//-----------------------------------------------------------------------------
/**
 * Executor to test a testcase against the update operation on an external
 * installation of Update.
 */
public class RemoteUpdateExecutor extends RemoteValidateExecutor {
    public RemoteUpdateExecutor( Testcase tc ) {
        super( tc );
        this.logger = XLoggerFactory.getXLogger( RemoteUpdateExecutor.class );
    }

    @Override
    public String name() {
        return "Update record against remote UpdateService";
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
            logger.debug( "Tracking id: {}", request.getTrackingId() );

            StopWatch watch = new StopWatch();

            logger.debug( "Sending request '{}' to {}", request.getTrackingId(), url );
            watch.start();
            UpdateRecordResult response = caller.createPort().updateRecord( request );
            watch.stop();
            logger.debug( "Receive response in {} ms: {}", watch.getElapsedTime(), response );

            watch.start();
            try {
                assertNotNull( "No expected results found.", tc.getExpected() );

                List<ValidationResult> errors = tc.getExpected().getValidation();
                if( errors == null || errors.isEmpty() ) {
                    assertNotNull( "No expected update result found", tc.getExpected().getUpdate() );
                    errors = tc.getExpected().getUpdate().getErrors();
                }
                assertNotNull( "No expected validation or update errors found.", errors );

                Asserter.assertValidation( errors, response.getValidateInstance() );
                if( errors.isEmpty() ) {
                    assertEquals( UpdateStatusEnum.OK, response.getUpdateStatus() );
                    assertNull( response.getValidateInstance() );
                }
                else {
                    assertEquals( UpdateStatusEnum.VALIDATION_ERROR, response.getUpdateStatus() );
                }
                assertNull( response.getError() );

                if( tc.getExpected().getUpdate() == null ) {
                    return;
                }

                RawRepoAsserter.assertRecordListEquals( tc.getExpected().getUpdate().getRawrepo(), RawRepo.loadRecords( settings ) );
                RawRepoAsserter.assertQueueRecords( tc.getExpected().getUpdate().getRawrepo(), RawRepo.loadQueuedRecords( settings ) );

                checkRelations( fs, settings, RawRepoRelationType.CHILD );
                checkRelations( fs, settings, RawRepoRelationType.SIBLING );
            }
            finally {
                watch.stop();
                logger.debug( "Test response in {} ms", watch.getElapsedTime() );
            }
        }
        catch( ClassNotFoundException | SQLException | RawRepoException | SAXException | ParserConfigurationException | JAXBException | IOException ex ) {
            throw new AssertionError( ex.getMessage(), ex );
        }
        finally {
            logger.exit();
        }
    }

    private void checkRelations( OCBFileSystem fs, Properties settings, RawRepoRelationType relationType ) throws IOException, RawRepoException, SQLException, ClassNotFoundException {
        logger.entry( fs, settings, relationType );

        try {
            for( TestcaseRecord testcaseRecord : tc.getExpected().getUpdate().getRawrepo() ) {
                MarcRecord record = fs.loadRecord( testcaseRecord.getRecordFile().getParentFile(), testcaseRecord.getRecord() );
                RecordId recordId = RawRepo.getRecordId( record );

                RawRepoAsserter.assertRecordRelations( testcaseRecord, relationType, RawRepo.loadRelations( settings, recordId, relationType ) );
            }
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

        UpdateRecordRequest request = null;
        try {
            request = super.createRequest();
            if( request != null ) {
                // Clear options to create an update request of the record.
                request.setOptions( null );
            }

            return request;
        }
        finally {
            logger.exit( request );
        }
    }
}
