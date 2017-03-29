package dk.dbc.ocbtools.testengine.executors;

import dk.dbc.holdingsitems.HoldingsItemsException;
import dk.dbc.iscrum.records.MarcRecord;
import dk.dbc.iscrum.utils.json.Json;
import dk.dbc.ocbtools.commons.filesystem.OCBFileSystem;
import dk.dbc.ocbtools.commons.type.ApplicationType;
import dk.dbc.ocbtools.testengine.asserters.UpdateAsserter;
import dk.dbc.ocbtools.testengine.testcases.UpdateTestcase;
import dk.dbc.ocbtools.testengine.testcases.UpdateTestcaseRecord;
import dk.dbc.rawrepo.RawRepoException;
import dk.dbc.updateservice.client.BibliographicRecordExtraData;
import dk.dbc.updateservice.client.BibliographicRecordFactory;
import dk.dbc.updateservice.service.api.*;
import org.perf4j.StopWatch;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Executor to test a testcase against the validation operation on an external
 * installation of Update.
 */
public class RemoteValidateExecutor implements TestExecutor {
    private static final String ENDPOINT_PATH = "/UpdateService/2.0";
    private static final long DEFAULT_CONNECT_TIMEOUT_MS = 1 * 60 * 1000;    // 1 minute
    private static final long DEFAULT_REQUEST_TIMEOUT_MS = 3 * 60 * 1000;    // 3 minutes
    private static final String TRACKING_ID_FORMAT = "ocbtools-%s-%s-%s";

    protected XLogger logger;
    protected UpdateTestcase tc;
    protected Properties settings;
    DemoInfoPrinter demoInfoPrinter;
    private OcbWireMockServer wireMockServer;

    public RemoteValidateExecutor(UpdateTestcase tc, Properties settings, boolean printDemoInfo) {
        logger = XLoggerFactory.getXLogger(RemoteValidateExecutor.class);
        this.tc = tc;
        this.settings = settings;
        this.demoInfoPrinter = null;
        this.wireMockServer = null;

        if (printDemoInfo) {
            this.demoInfoPrinter = new DemoInfoPrinter();
        }
    }

    @Override
    public String name() {
        return String.format("Validate record against remote UpdateService: %s", createServiceUrl());
    }

    @Override
    public boolean setup() throws IOException, JAXBException, SQLException, RawRepoException, HoldingsItemsException, ClassNotFoundException {
        logger.entry();
        try {
            if (this.demoInfoPrinter != null) {
                demoInfoPrinter.printHeader(this.tc, this);
            }
            OCBFileSystem fs = new OCBFileSystem(ApplicationType.UPDATE);

            RawRepo.teardownDatabase(settings);
            Holdings.teardownDatabase(settings);

            RawRepo.setupDatabase(settings);
            Holdings.setupDatabase(settings);
            wireMockServer = new OcbWireMockServer(tc, settings);

            if (!hasRawRepoSetup(tc)) {
                return true;
            }
            try (Connection conn = RawRepo.getConnection(settings)) {
                RawRepo rawRepo = null;
                try {
                    rawRepo = new RawRepo(settings, conn);
                    rawRepo.saveRecords(tc.getFile().getParentFile(), tc.getSetup().getRawrepo());
                    setupRelations(fs, rawRepo);
                    conn.commit();
                } catch (Throwable rrex) {
                    logger.error("rawrepo setup ERROR : ", rrex);
                    if (rawRepo != null) {
                        try {
                            conn.rollback();
                        } catch (SQLException sqlex) {
                            logger.error("Rollback failed", sqlex);
                        }
                    }
                    throw rrex;
                }
            }
            setupHoldings(fs);
            if (this.demoInfoPrinter != null) {
                demoInfoPrinter.printRemoteDatabases(this.tc, settings);
            }
            return true;
        } catch (Throwable ex) {
            logger.error("setup ERROR : ", ex);
            throw ex;
        } finally {
            logger.exit();
        }
    }

    /**
     * Setup any holdings for this testcase.
     *
     * @param fs The OCB filesystem to load records from.
     */
    private void setupHoldings(OCBFileSystem fs) throws SQLException, IOException, ClassNotFoundException, HoldingsItemsException {
        logger.entry(fs);
        try {
            if (!hasHoldings()) {
                return;
            }
            try (Connection conn = Holdings.getConnection(settings)) {
                MarcRecord marcRecord;

                // Setup holdings for the request record.
                if (tc.getSetup().getHoldings() != null) {
                    marcRecord = fs.loadRecord(tc.getFile().getParentFile(), tc.getRequest().getRecord());
                    Holdings.saveHoldings(conn, marcRecord, tc.getSetup().getHoldings());
                }

                // Setup holdings for records already in RawRepo.
                if (tc.getSetup().getRawrepo() != null) {
                    for (UpdateTestcaseRecord record : tc.getSetup().getRawrepo()) {
                        if (!record.getHoldings().isEmpty()) {
                            marcRecord = fs.loadRecord(tc.getFile().getParentFile(), record.getRecord());
                            Holdings.saveHoldings(conn, marcRecord, record.getHoldings());
                        }
                    }
                }
            }
        } finally {
            logger.exit();
        }
    }

    /**
     * Checks if this testcase contains any setup about holdings.
     */
    private boolean hasHoldings() {
        logger.entry();
        Boolean result = null;
        try {
            if (tc.getSetup() == null) {
                return result = false;
            }
            if (tc.getSetup().getHoldings() != null && !tc.getSetup().getHoldings().isEmpty()) {
                return result = true;
            }
            if (tc.getSetup().getRawrepo() != null) {
                for (UpdateTestcaseRecord record : tc.getSetup().getRawrepo()) {
                    if (!record.getHoldings().isEmpty()) {
                        return result = true;
                    }
                }
            }
            return result = false;
        } finally {
            logger.exit(result);
        }
    }

    private void setupRelations(OCBFileSystem fs, RawRepo rawRepo) throws IOException, RawRepoException {
        logger.entry(fs, rawRepo);
        try {
            File baseDir = tc.getFile().getParentFile();
            for (UpdateTestcaseRecord record : tc.getSetup().getRawrepo()) {
                MarcRecord commonOrParentRecord = null;
                if (record.getChildren() != null) {
                    commonOrParentRecord = fs.loadRecord(baseDir, record.getRecord());
                    for (String enrichmentOrChildFilename : record.getChildren()) {
                        rawRepo.saveRelation(commonOrParentRecord, fs.loadRecord(baseDir, enrichmentOrChildFilename));
                    }
                }

                if (record.getEnrichments() != null) {
                    if (commonOrParentRecord == null) {
                        commonOrParentRecord = fs.loadRecord(baseDir, record.getRecord());
                    }
                    for (String enrichmentOrChildFilename : record.getEnrichments()) {
                        rawRepo.saveRelation(commonOrParentRecord, fs.loadRecord(baseDir, enrichmentOrChildFilename));
                    }
                }
            }
        } finally {
            logger.exit();
        }
    }

    @Override
    public void teardown() {
        logger.entry();
        try {
            if (wireMockServer != null) {
                wireMockServer.stop();
            }
            if (this.demoInfoPrinter != null) {
                demoInfoPrinter.printRemoteDatabases(this.tc, settings);
            }

            if (this.demoInfoPrinter != null) {
                demoInfoPrinter.printFooter();
            }
        } catch (Throwable ex) {
            throw new IllegalStateException("Teardown error", ex);
        } finally {
            logger.exit();
        }
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

            URL url = createServiceUrl();

            UpdateRecordRequest request = createRequest();

            StopWatch watch = new StopWatch();

            logger.debug("Sending request '{}' to {}", request.getTrackingId(), url);
            if (demoInfoPrinter != null) {
                demoInfoPrinter.printRequest(request, tc.loadRecord());
            }
            watch.start();
            CatalogingUpdatePortType catalogingUpdatePortType = createPort(url);
            UpdateRecordResult response = catalogingUpdatePortType.updateRecord(request);
            watch.stop();
            logger.debug("Received response in " + watch.getElapsedTime() + " ms: " + Json.encodePretty(response));
            if (demoInfoPrinter != null) {
                demoInfoPrinter.printResponse(response);
            }

            watch.start();
            try {
                UpdateAsserter.assertValidation(UpdateAsserter.VALIDATION_PREFIX_KEY, tc.getExpected().getValidation(), response.getMessages());
                if (!tc.getExpected().getValidation().hasErrors() && !tc.getExpected().getValidation().hasDoubleRecords()) {
                    assertEquals(UpdateStatusEnum.OK, response.getUpdateStatus());
                    if (tc.getExpected().getValidation().getErrors() == null) {
                        assertTrue(response.getMessages() == null);
                    } else {
                        assertTrue(response.getMessages().getMessageEntry().size() > 0);
                    }
                    if (tc.getExpected().getValidation().getDoubleRecords() == null) {
                        assertTrue(response.getDoubleRecordEntries() == null);
                    } else {
                        assertTrue(response.getDoubleRecordEntries().getDoubleRecordEntry().size() > 0);
                    }
                } else {
                    assertEquals(UpdateStatusEnum.FAILED, response.getUpdateStatus());
                }
            } finally {
                watch.stop();
                logger.debug("Test response in {} ms", watch.getElapsedTime());
            }
        } catch (SAXException | ParserConfigurationException | JAXBException | IOException ex) {
            throw new AssertionError(ex.getMessage(), ex);
        } finally {
            logger.exit();
        }
    }

    URL createServiceUrl() {
        logger.entry();
        URL result = null;
        try {
            return result = new URL(settings.getProperty("updateservice.url"));
        } catch (MalformedURLException ex) {
            throw new AssertionError(String.format("Unable to create url to webservice: %s", ex.getMessage()), ex);
        } finally {
            logger.exit(result);
        }
    }

    private Map<String, Object> createHeaders() {
        logger.entry();
        Map<String, Object> headers = new HashMap<>();
        try {
            if (settings.containsKey("request.headers.x.forwarded.for")) {
                headers.put("x-forwarded-for", Collections.singletonList(settings.getProperty("request.headers.x.forwarded.for")));
            }
            return headers;
        } finally {
            logger.exit(headers);
        }
    }

    protected UpdateRecordRequest createRequest() throws IOException, JAXBException, SAXException, ParserConfigurationException {
        logger.entry();
        try {
            UpdateRecordRequest request = new UpdateRecordRequest();

            Authentication auth = new Authentication();
            auth.setUserIdAut(tc.getRequest().getAuthentication().getUser());
            auth.setGroupIdAut(tc.getRequest().getAuthentication().getGroup());
            auth.setPasswordAut(tc.getRequest().getAuthentication().getPassword());

            request.setAuthentication(auth);
            request.setSchemaName(tc.getRequest().getTemplateName());
            request.setTrackingId(String.format(TRACKING_ID_FORMAT, System.getProperty("user.name"), tc.getName(), getClass().getSimpleName()));

            Options options = new Options();
            options.getOption().add(UpdateOptionEnum.VALIDATE_ONLY);
            request.setOptions(options);

            String requestProviderNameKey = tc.getDistributionName() + ".request.provider.name";

            logger.debug("requestProviderNameKey: {}", requestProviderNameKey);
            BibliographicRecordExtraData extraData = null;
            if (settings.containsKey(requestProviderNameKey)) {
                extraData = new BibliographicRecordExtraData();
                extraData.setProviderName(settings.getProperty(requestProviderNameKey));
            }

            File recordFile = new File(tc.getFile().getParentFile().getCanonicalPath() + "/" + tc.getRequest().getRecord());
            request.setBibliographicRecord(BibliographicRecordFactory.loadMarcRecordInLineFormat(recordFile, extraData));
            return request;
        } finally {
            logger.exit();
        }
    }

    private boolean hasRawRepoSetup(UpdateTestcase tc) {
        logger.entry(tc);
        boolean result = true;
        try {
            if (tc.getSetup() == null) {
                result = false;
            } else if (tc.getSetup().getRawrepo() == null) {
                result = false;
            }
            return result;
        } finally {
            logger.exit(result);
        }
    }

    CatalogingUpdatePortType createPort(URL baseUrl) throws MalformedURLException {
        logger.entry();
        try {
            QName serviceName = new QName("http://oss.dbc.dk/ns/catalogingUpdate", "UpdateService");
            String endpoint = baseUrl + ENDPOINT_PATH;
            URL url = new URL(endpoint);
            UpdateService updateService = new UpdateService(url, serviceName);
            CatalogingUpdatePortType catalogingUpdatePortType = updateService.getCatalogingUpdatePort();
            BindingProvider proxy = (BindingProvider) catalogingUpdatePortType;
            logger.debug("Using base url: {}", baseUrl);
            logger.debug("Using complete endpoint: {}", endpoint);
            proxy.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
            proxy.getRequestContext().put("com.sun.xml.ws.connect.timeout", DEFAULT_CONNECT_TIMEOUT_MS);
            proxy.getRequestContext().put("com.sun.xml.ws.request.timeout", DEFAULT_REQUEST_TIMEOUT_MS);
            Map<String, Object> headers = createHeaders();
            if (headers != null && !headers.isEmpty()) {
                proxy.getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, headers);
            }
            return catalogingUpdatePortType;
        } finally {
            logger.exit();
        }
    }
}
