package dk.dbc.ocbtools.testengine.executors;

import dk.dbc.common.records.MarcRecord;
import dk.dbc.ocbtools.commons.filesystem.OCBFileSystem;
import dk.dbc.ocbtools.commons.type.ApplicationType;
import dk.dbc.ocbtools.testengine.asserters.RawRepoAsserter;
import dk.dbc.ocbtools.testengine.asserters.UpdateAsserter;
import dk.dbc.ocbtools.testengine.testcases.UpdateTestcase;
import dk.dbc.ocbtools.testengine.testcases.UpdateTestcaseRecord;
import dk.dbc.rawrepo.RawRepoException;
import dk.dbc.rawrepo.RecordId;
import dk.dbc.updateservice.service.api.CatalogingUpdatePortType;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Executor to test a testcase against the update operation on an external
 * installation of Update.
 */
public class RemoteUpdateExecutor extends RemoteValidateExecutor {
    public RemoteUpdateExecutor(UpdateTestcase tc, Properties settings, boolean printDemoInfo) {
        super(tc, settings, printDemoInfo);
        logger = XLoggerFactory.getXLogger(RemoteUpdateExecutor.class);
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

            String key = "updateservice.url";
            URL url = new URL(settings.getProperty(key));

            UpdateRecordRequest request = createRequest();
            logger.debug("Tracking id: {}", request.getTrackingId());

            StopWatch watch = new StopWatch();

            logger.debug("Sending UPDATE request '{}' to {}", request.getTrackingId(), url);
            logger.debug("Request:\n" + request);
            if (demoInfoPrinter != null) {
                demoInfoPrinter.printRequest(request, tc.loadRecord());
            }
            watch.start();
            CatalogingUpdatePortType catalogingUpdatePortType = createPort(url);
            UpdateRecordResult response = catalogingUpdatePortType.updateRecord(request);
            watch.stop();
            logger.debug("Received UPDATE response in " + watch.getElapsedTime() + " ms: " + response);
            if (demoInfoPrinter != null) {
                demoInfoPrinter.printResponse(response);
            }

            watch.start();
            try {
                assertNotNull("No expected results found.", tc.getExpected());
                if (tc.getExpected().getValidation().getErrors() != null || tc.getExpected().getValidation().getDoubleRecords() != null) {
                    assertEquals(UpdateStatusEnum.FAILED, response.getUpdateStatus());
                    if (tc.getExpected().getValidation().hasErrors()) {
                        UpdateAsserter.assertValidation(UpdateAsserter.UPDATE_PREFIX_KEY, tc.getExpected().getValidation().getErrors(), response.getMessages());
                    }
                    if (tc.getExpected().getValidation().hasDoubleRecords()) {
                        UpdateAsserter.assertValidation(UpdateAsserter.UPDATE_PREFIX_KEY, tc.getExpected().getValidation().getDoubleRecords(), response.getDoubleRecordEntries());
                    }
                }
                if (tc.getExpected().getUpdate() != null) {
                    if (tc.getExpected().getUpdate().hasDoubleRecords()) {
                        UpdateAsserter.assertValidation(UpdateAsserter.UPDATE_PREFIX_KEY, tc.getExpected().getUpdate().getDoubleRecords(), response.getDoubleRecordEntries());
                        assertEquals(UpdateStatusEnum.FAILED, response.getUpdateStatus());
                    } else if (tc.getExpected().getUpdate().hasErrors() || tc.getExpected().getUpdate().hasWarnings()) {
                        UpdateAsserter.assertValidation(UpdateAsserter.UPDATE_PREFIX_KEY, tc.getExpected().getUpdate().getErrors(), response.getMessages());
                        assertEquals(UpdateStatusEnum.FAILED, response.getUpdateStatus());
                    } else if (tc.getExpected().getUpdate().getErrors() == null || tc.getExpected().getUpdate().getErrors().isEmpty()) {
                        UpdateAsserter.assertValidation(UpdateAsserter.UPDATE_PREFIX_KEY, new ArrayList<>(), response.getMessages());
                        assertEquals(UpdateStatusEnum.OK, response.getUpdateStatus());
                    } else {
                        UpdateAsserter.assertValidation(UpdateAsserter.UPDATE_PREFIX_KEY, tc.getExpected().getUpdate().getErrors(), response.getMessages());
                        assertEquals(UpdateStatusEnum.OK, response.getUpdateStatus());
                    }

                    if (tc.getExpected().getUpdate().getRawrepo() != null) {
                        RawRepoAsserter.assertRecordListEquals(tc.getExpected().getUpdate().getRawrepo(), RawRepo.loadRecords(settings), tc.getRequest().isCheck001cd(),
                                tc.getRequest().isMatchd09());
                        RawRepoAsserter.assertQueueRecords(tc.getExpected().getUpdate().getRawrepo(), RawRepo.loadQueuedRecords(settings));
                    } else if (tc.getSetup() != null && tc.getSetup().getRawrepo() != null) {
                        RawRepoAsserter.assertRecordListEquals(tc.getSetup().getRawrepo(), RawRepo.loadRecords(settings), tc.getRequest().isCheck001cd(),
                                tc.getRequest().isMatchd09());
                        RawRepoAsserter.assertQueueRecords(tc.getSetup().getRawrepo(), RawRepo.loadQueuedRecords(settings));
                    }
                    checkRelations(fs, settings, RawRepoRelationType.CHILD);
                    checkRelations(fs, settings, RawRepoRelationType.SIBLING);
                }
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
