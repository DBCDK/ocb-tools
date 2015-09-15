//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.executors;

//-----------------------------------------------------------------------------

import dk.dbc.iscrum.records.MarcRecord;
import dk.dbc.ocbtools.commons.filesystem.OCBFileSystem;
import dk.dbc.ocbtools.commons.type.ApplicationType;
import dk.dbc.ocbtools.testengine.asserters.UpdateAsserter;
import dk.dbc.ocbtools.testengine.asserters.RawRepoAsserter;
import dk.dbc.ocbtools.testengine.testcases.UpdateTestcase;
import dk.dbc.ocbtools.testengine.testcases.UpdateTestcaseRecord;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

//-----------------------------------------------------------------------------

/**
 * Executor to test a testcase against the update operation on an external
 * installation of Update.
 */
public class RemoteUpdateExecutor extends RemoteValidateExecutor {
    public RemoteUpdateExecutor(UpdateTestcase tc, Properties settings, boolean printDemoInfo) {
        super(tc, settings, printDemoInfo);
        this.logger = XLoggerFactory.getXLogger(RemoteUpdateExecutor.class);
    }

    @Override
    public String name() {
        return String.format("Update record against remote UpdateService: %s", createServiceUrl());
    }

    @Override
    public void executeTests() {
        logger.entry();

        try {
            assertNotNull("Property'en 'request' er obligatorisk.", tc.getRequest());
            assertNotNull("Property'en 'request.authentication' er obligatorisk.", tc.getRequest().getAuthentication());
            assertNotNull("Property'en 'request.authentication.group' er obligatorisk.", tc.getRequest().getAuthentication().getGroup());
            assertNotNull("Property'en 'request.authentication.user' er obligatorisk.", tc.getRequest().getAuthentication().getUser());
            assertNotNull("Property'en 'request.authentication.password' er obligatorisk.", tc.getRequest().getAuthentication().getPassword());

            OCBFileSystem fs = new OCBFileSystem(ApplicationType.UPDATE);

            String key = String.format("updateservice.%s.url", tc.getDistributionName());
            URL url = new URL(settings.getProperty(key));
            UpdateService caller = new UpdateService(url);

            UpdateRecordRequest request = createRequest();
            logger.debug("Tracking id: {}", request.getTrackingId());

            StopWatch watch = new StopWatch();

            logger.debug("Sending request '{}' to {}", request.getTrackingId(), url);
            if (demoInfoPrinter != null) {
                demoInfoPrinter.printRequest(request, tc.loadRecord());
            }
            watch.start();
            UpdateRecordResult response = caller.createPort().updateRecord(request);
            watch.stop();
            logger.debug("Receive response in {} ms: {}", watch.getElapsedTime(), response);
            if (demoInfoPrinter != null) {
                demoInfoPrinter.printResponse(response);
            }

            watch.start();
            try {
                assertNotNull("No expected results found.", tc.getExpected());

                if (tc.getExpected().hasValidationErrors()) {
                    UpdateAsserter.assertValidation(UpdateAsserter.UPDATE_PREFIX_KEY, tc.getExpected().getValidation(), response.getValidateInstance());
                    assertEquals(UpdateStatusEnum.VALIDATION_ERROR, response.getUpdateStatus());
                }

                if (tc.getExpected().getUpdate() == null) {
                    return;
                }

                if (tc.getExpected().getUpdate().getErrors() == null || tc.getExpected().getUpdate().getErrors().isEmpty()) {
                    UpdateAsserter.assertValidation(UpdateAsserter.UPDATE_PREFIX_KEY, new ArrayList<ValidationResult>(), response.getValidateInstance());
                    assertEquals(UpdateStatusEnum.OK, response.getUpdateStatus());
                } else if (tc.getExpected().getUpdate().hasUpdateErrors()) {
                    UpdateAsserter.assertValidation(UpdateAsserter.UPDATE_PREFIX_KEY, tc.getExpected().getUpdate().getErrors(), response.getValidateInstance());
                    assertEquals(UpdateStatusEnum.FAILED_UPDATE_INTERNAL_ERROR, response.getUpdateStatus());
                } else {
                    UpdateAsserter.assertValidation(UpdateAsserter.UPDATE_PREFIX_KEY, tc.getExpected().getUpdate().getErrors(), response.getValidateInstance());
                    assertEquals(UpdateStatusEnum.OK, response.getUpdateStatus());
                }

                if (tc.getExpected().getUpdate().getRawrepo() != null) {
                    RawRepoAsserter.assertRecordListEquals(tc.getExpected().getUpdate().getRawrepo(), RawRepo.loadRecords(settings));
                    RawRepoAsserter.assertQueueRecords(tc.getExpected().getUpdate().getRawrepo(), RawRepo.loadQueuedRecords(settings));
                } else if (tc.getSetup() != null && tc.getSetup().getRawrepo() != null) {
                    RawRepoAsserter.assertRecordListEquals(tc.getSetup().getRawrepo(), RawRepo.loadRecords(settings));
                    RawRepoAsserter.assertQueueRecords(tc.getSetup().getRawrepo(), RawRepo.loadQueuedRecords(settings));
                }

                checkRelations(fs, settings, RawRepoRelationType.CHILD);
                checkRelations(fs, settings, RawRepoRelationType.SIBLING);
            } finally {
                watch.stop();
                logger.debug("Test response in {} ms", watch.getElapsedTime());
            }
        } catch (ClassNotFoundException | SQLException | RawRepoException | SAXException | ParserConfigurationException | JAXBException | IOException ex) {
            throw new AssertionError(ex.getMessage(), ex);
        } finally {
            logger.exit();
        }
    }

    private void checkRelations(OCBFileSystem fs, Properties settings, RawRepoRelationType relationType) throws IOException, RawRepoException, SQLException, ClassNotFoundException {
        logger.entry(fs, settings, relationType);

        try {
            List<UpdateTestcaseRecord> expectedRecords = tc.getExpected().getUpdate().getRawrepo();
            if (expectedRecords == null && tc.getSetup() != null) {
                expectedRecords = tc.getSetup().getRawrepo();
            }

            if (expectedRecords != null) {
                for (UpdateTestcaseRecord updateTestcaseRecord : expectedRecords) {
                    MarcRecord record = fs.loadRecord(updateTestcaseRecord.getRecordFile().getParentFile(), updateTestcaseRecord.getRecord());
                    RecordId recordId = RawRepo.getRecordId(record);

                    RawRepoAsserter.assertRecordRelations(updateTestcaseRecord, relationType, RawRepo.loadRelations(settings, recordId, relationType));
                }
            }
        } finally {
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
            if (request != null) {
                // Clear options to create an update request of the record.
                request.setOptions(null);
            }

            return request;
        } finally {
            logger.exit(request);
        }
    }
}
