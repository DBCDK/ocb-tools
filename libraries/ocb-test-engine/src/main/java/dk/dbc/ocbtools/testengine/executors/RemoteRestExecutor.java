package dk.dbc.ocbtools.testengine.executors;

import dk.dbc.common.records.MarcConverter;
import dk.dbc.common.records.MarcRecord;
import dk.dbc.common.records.MarcRecordFactory;
import dk.dbc.iscrum.utils.IOUtils;
import dk.dbc.ocbtools.testengine.asserters.UpdateAsserter;
import dk.dbc.ocbtools.testengine.testcases.UpdateTestcase;
import dk.dbc.updateservice.UpdateServiceClassificationCheckConnector;
import dk.dbc.updateservice.UpdateServiceClassificationCheckConnectorException;
import dk.dbc.updateservice.UpdateServiceClassificationCheckConnectorFactory;
import dk.dbc.updateservice.UpdateServiceDoubleRecordCheckConnector;
import dk.dbc.updateservice.UpdateServiceDoubleRecordCheckConnectorException;
import dk.dbc.updateservice.UpdateServiceDoubleRecordCheckConnectorFactory;
import dk.dbc.updateservice.service.api.DoubleRecordEntries;
import dk.dbc.updateservice.service.api.DoubleRecordEntry;
import dk.dbc.updateservice.service.api.MessageEntry;
import dk.dbc.updateservice.service.api.Messages;
import dk.dbc.updateservice.service.api.Type;
import dk.dbc.updateservice.service.api.UpdateRecordResult;
import dk.dbc.updateservice.service.api.UpdateStatusEnum;
import org.perf4j.StopWatch;
import org.slf4j.ext.XLoggerFactory;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Executor to test a testcase against a rest service on an external
 * installation of Update.
 */
public class RemoteRestExecutor extends RemoteValidateExecutor {
    private static final List<String> REST_TYPE = Arrays.asList("doublerecordcheck", "classificationcheck");
    private static final String EXECUTOR_URL = "updateservice.url";

    public RemoteRestExecutor(UpdateTestcase tc, Properties settings) {
        super(tc, settings);
        logger = XLoggerFactory.getXLogger(RemoteRestExecutor.class);
    }

    @Override
    public String name() {
        String restType = tc.getRequest().getRestType();
        String message;
        if (restType.equals("")) {
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
            assertNotNull("Property'en 'request' er obligatorisk.", tc.getRequest());
            assertNotNull("Property'en 'request.restType' er obligatorisk.", tc.getRequest().getRestType());
            assertTrue("Property'en 'request.restType' har en gyldig værdi", REST_TYPE.contains(tc.getRequest().getRestType().toLowerCase()));
            assertNotNull("Property'en 'request.authentication' er obligatorisk.", tc.getRequest().getAuthentication());
            assertNotNull("Property'en 'request.authentication.group' er obligatorisk.", tc.getRequest().getAuthentication().getGroup());
            assertNotNull("Property'en 'request.authentication.user' er obligatorisk.", tc.getRequest().getAuthentication().getUser());
            assertNotNull("Property'en 'request.authentication.password' er obligatorisk.", tc.getRequest().getAuthentication().getPassword());


            StopWatch watch = new StopWatch();
            watch.start();
            try {
                if ("doublerecordcheck".equalsIgnoreCase(tc.getRequest().getRestType())) {
                    executeTestsDoubleRecordCheck();
                } else { // Must be classificationcheck
                    executeTestsClassificationCheck();
                }
            } finally {
                watch.stop();
                logger.debug("Test response in {} ms", watch.getElapsedTime());
            }
        } finally {
            logger.exit();
        }
    }

    private void executeTestsDoubleRecordCheck() {
        final UpdateServiceDoubleRecordCheckConnector doubleRecordCheckConnector = UpdateServiceDoubleRecordCheckConnectorFactory.create(settings.getProperty(EXECUTOR_URL) + "/UpdateService/rest");

        try {
            final dk.dbc.oss.ns.catalogingupdate.UpdateRecordResult connectorResponse = doubleRecordCheckConnector.doubleRecordCheck(createRestRequest());
            final UpdateRecordResult response = connectorResponseToOCBResponse(connectorResponse);

            assertNotNull("No expected results found.", tc.getExpected());
            if (tc.getExpected().getUpdate() != null) {
                if (tc.getExpected().getUpdate().hasErrors() || tc.getExpected().getUpdate().hasWarnings()) {
                    UpdateAsserter.assertValidation(UpdateAsserter.UPDATE_PREFIX_KEY, tc.getExpected().getUpdate().getErrors(), response.getMessages());
                    assertEquals(UpdateStatusEnum.FAILED, response.getUpdateStatus());
                } else if (tc.getExpected().getUpdate().getErrors() == null || tc.getExpected().getUpdate().getErrors().isEmpty()) {
                    UpdateAsserter.assertValidation(UpdateAsserter.UPDATE_PREFIX_KEY, new ArrayList<>(), response.getMessages());
                    assertEquals(UpdateStatusEnum.OK, response.getUpdateStatus());
                }
            }
        } catch (UpdateServiceDoubleRecordCheckConnectorException | IOException | JAXBException | ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void executeTestsClassificationCheck() {
        final UpdateServiceClassificationCheckConnector doubleRecordCheckConnector = UpdateServiceClassificationCheckConnectorFactory.create(EXECUTOR_URL);

        try {
            final dk.dbc.oss.ns.catalogingupdate.UpdateRecordResult connectorResponse = doubleRecordCheckConnector.classificationCheck(createRestRequest());
            final UpdateRecordResult response = connectorResponseToOCBResponse(connectorResponse);

            assertNotNull("No expected results found.", tc.getExpected());
            if (tc.getExpected().getUpdate() != null) {
                if (tc.getExpected().getUpdate().hasErrors() || tc.getExpected().getUpdate().hasWarnings()) {
                    UpdateAsserter.assertValidation(UpdateAsserter.UPDATE_PREFIX_KEY, tc.getExpected().getUpdate().getErrors(), response.getMessages());
                    assertEquals(UpdateStatusEnum.FAILED, response.getUpdateStatus());
                } else if (tc.getExpected().getUpdate().getErrors() == null || tc.getExpected().getUpdate().getErrors().isEmpty()) {
                    UpdateAsserter.assertValidation(UpdateAsserter.UPDATE_PREFIX_KEY, new ArrayList<>(), response.getMessages());
                    assertEquals(UpdateStatusEnum.OK, response.getUpdateStatus());
                }
            }
        } catch (UpdateServiceClassificationCheckConnectorException | IOException | JAXBException | ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function is responsible for constructing the request object. The BibliographicRecord class is identical to
     * the BibliographicRecord used elsewhere in ocb-tools however the package is different. Therefor we can't override
     * createRequest
     *
     * @return BibliographicRecord
     * @throws IOException
     * @throws JAXBException
     * @throws ParserConfigurationException
     */
    private dk.dbc.oss.ns.catalogingupdate.BibliographicRecord createRestRequest() throws IOException, JAXBException, ParserConfigurationException {
        logger.entry();

        try {
            final File recordFile = new File(tc.getFile().getParentFile().getCanonicalPath() + "/" + tc.getRequest().getRecord());
            final InputStream inputStream = new FileInputStream(recordFile);
            final MarcRecord marcRecord = MarcRecordFactory.readRecord(IOUtils.readAll(inputStream, "UTF-8"));

            final dk.dbc.oss.ns.catalogingupdate.BibliographicRecord bibliographicRecord = new dk.dbc.oss.ns.catalogingupdate.BibliographicRecord();
            bibliographicRecord.setRecordSchema("info:lc/xmlns/marcxchange-v1");
            bibliographicRecord.setRecordPacking("xml");

            final dk.dbc.oss.ns.catalogingupdate.RecordData recData = new dk.dbc.oss.ns.catalogingupdate.RecordData();
            recData.getContent().add("\n");
            recData.getContent().add(MarcConverter.convertToMarcXChangeAsDocument(marcRecord).getDocumentElement());
            recData.getContent().add("\n");
            bibliographicRecord.setRecordData(recData);

            bibliographicRecord.setRecordData(recData);

            return bibliographicRecord;
        } finally {
            logger.exit();
        }
    }

    /**
     * This function is responsible for converting the response object from the connector to the response object used
     * elsewhere in ocb-tools. The classes are identical but the package is different.
     *
     * @param source dk.dbc.oss.ns.catalogingupdate.UpdateRecordResult
     * @return dk.dbc.updateservice.service.api.UpdateRecordResult
     */
    private UpdateRecordResult connectorResponseToOCBResponse(dk.dbc.oss.ns.catalogingupdate.UpdateRecordResult source) {
        final UpdateRecordResult target = new UpdateRecordResult();
        target.setDoubleRecordKey(source.getDoubleRecordKey());
        target.setUpdateStatus(UpdateStatusEnum.fromValue(source.getUpdateStatus().value()));

        if (source.getDoubleRecordEntries() != null) {
            final DoubleRecordEntries doubleRecordEntries = new DoubleRecordEntries();

            final dk.dbc.oss.ns.catalogingupdate.DoubleRecordEntries sourceDoubleRecordEntries = source.getDoubleRecordEntries();
            for (dk.dbc.oss.ns.catalogingupdate.DoubleRecordEntry sourceDoubleRecordEntry : sourceDoubleRecordEntries.getDoubleRecordEntry()) {
                final DoubleRecordEntry doubleRecordEntry = new DoubleRecordEntry();
                doubleRecordEntry.setMessage(sourceDoubleRecordEntry.getPid());
                doubleRecordEntry.setPid(sourceDoubleRecordEntry.getMessage());

                doubleRecordEntries.getDoubleRecordEntry().add(doubleRecordEntry);
            }
            target.setDoubleRecordEntries(doubleRecordEntries);
        }

        if (source.getMessages() != null) {
            final Messages messages = new Messages();

            final dk.dbc.oss.ns.catalogingupdate.Messages sourceMessages = source.getMessages();
            for (dk.dbc.oss.ns.catalogingupdate.MessageEntry sourceMessage : sourceMessages.getMessageEntry()) {
                final MessageEntry messageEntry = new MessageEntry();
                messageEntry.setCode(sourceMessage.getCode());
                messageEntry.setMessage(sourceMessage.getMessage());
                messageEntry.setOrdinalPositionInSubfield(sourceMessage.getOrdinalPositionInSubfield());
                messageEntry.setOrdinalPositionOfField(sourceMessage.getOrdinalPositionOfField());
                messageEntry.setOrdinalPositionOfSubfield(sourceMessage.getOrdinalPositionOfSubfield());
                messageEntry.setType(Type.fromValue(sourceMessage.getType().value()));
                messageEntry.setUrlForDocumentation(sourceMessage.getUrlForDocumentation());

                messages.getMessageEntry().add(messageEntry);
            }
            target.setMessages(messages);
        }

        return target;
    }

}
