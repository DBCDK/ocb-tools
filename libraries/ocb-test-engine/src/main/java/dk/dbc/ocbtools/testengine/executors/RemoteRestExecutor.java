package dk.dbc.ocbtools.testengine.executors;

import dk.dbc.common.records.MarcRecord;
import dk.dbc.httpclient.HttpClient;
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
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.perf4j.StopWatch;
import org.slf4j.ext.XLoggerFactory;
import org.xml.sax.SAXException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Executor to test a testcase against a rest service on an external
 * installation of Update.
 */
public class RemoteRestExecutor extends RemoteValidateExecutor {
    private static final String EXECUTOR_URL = "updateservice.url";


    public RemoteRestExecutor(UpdateTestcase tc, Properties settings) {
        super(tc, settings);
        logger = XLoggerFactory.getXLogger(RemoteRestExecutor.class);
    }

    @Override
    public String name() {
        String restType = tc.getRequest().getRestType();
        String message;
        if (restType.equals("")){
            message = "Error, no restType defined in request section of testcase";
        } else {
            message = settings.getProperty(tc.getRequest().getRestType());
        }
        return String.format("Rest request against remote UpdateService: %s", message);
    }

    @Override
    public void executeTests() {
        logger.entry();
        try {
            // TODO cleanup
            logger.info("HYFSA jupjup vi komme hier {}", tc.getRequest().getRestType());
            logger.info("HYFSA jupjup vi komme hier {}", settings.getProperty(tc.getRequest().getRestType()));
            assertNotNull("Property'en 'request' er obligatorisk.", tc.getRequest());
            assertFalse("Property'en 'request.restType' er obligatorisk.", tc.getRequest().getRestType().equals(""));
            assertNotNull("Property'en 'request.authentication' er obligatorisk.", tc.getRequest().getAuthentication());
            assertNotNull("Property'en 'request.authentication.group' er obligatorisk.", tc.getRequest().getAuthentication().getGroup());
            assertNotNull("Property'en 'request.authentication.user' er obligatorisk.", tc.getRequest().getAuthentication().getUser());
            assertNotNull("Property'en 'request.authentication.password' er obligatorisk.", tc.getRequest().getAuthentication().getPassword());


            OCBFileSystem fs = new OCBFileSystem(ApplicationType.REST);

            URL url = createServiceUrl(tc.getRequest().getRestType());

            UpdateRecordRequest request = createRequest();
            logger.debug("Tracking id: {}", request.getTrackingId());

            StopWatch watch = new StopWatch();

            String hostUrl = settings.getProperty(tc.getRequest().getRestType());

            // TODO why do we want this ?
            switch (tc.getRequest().getRestType()) {
                case "doublerecordcheck.url" :
                    break;
                case "classificationcheck.url" : int y = 0;
                    break;
                default:
                    String message = String.format("Rest type %s can not be handled", tc.getRequest().getRestType());
                    logger.info(message);
                    throw new ClassNotFoundException(message);
            }
            // TODO why do we want this ? END

            logger.debug("Sending REST request '{}' to {}", request.getTrackingId(), url);
            logger.debug("Request:\n" + request);
            final Client client = HttpClient.newClient(new ClientConfig().register(new JacksonFeature()));
            WebTarget target = client.target(hostUrl);










            if (fs != null) return;
            watch.start();
            CatalogingUpdatePortType catalogingUpdatePortType = createPort(url);
            UpdateRecordResult response = catalogingUpdatePortType.updateRecord(request);
            watch.stop();
            logger.debug("Received REST response in " + watch.getElapsedTime() + " ms: " + response);

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
                        RawRepoAsserter.assertRecordListEquals(tc.getExpected().getUpdate().getRawrepo(), RawRepo.loadRecords(settings), tc);
                        RawRepoAsserter.assertQueueRecords(tc.getExpected().getUpdate().getRawrepo(), RawRepo.loadQueuedRecords(settings));
                    } else if (tc.getSetup() != null && tc.getSetup().getRawrepo() != null) {
                        RawRepoAsserter.assertRecordListEquals(tc.getSetup().getRawrepo(), RawRepo.loadRecords(settings), tc);
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
