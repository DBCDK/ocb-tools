package dk.dbc.ocbtools.testengine.executors;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import dk.dbc.idp.connector.AuthorizeResponse;
import dk.dbc.iscrum.utils.IOUtils;
import dk.dbc.jsonb.JSONBContext;
import dk.dbc.ocbtools.testengine.testcases.BuildTestcase;
import dk.dbc.ocbtools.testengine.testcases.UpdateTestcase;
import dk.dbc.vipcore.marshallers.LibraryRulesResponse;
import org.perf4j.StopWatch;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;


/**
 * Class to execute a wiremock server
 */
class OcbWireMockServer {
    private static final XLogger logger = XLoggerFactory.getXLogger(OcbWireMockServer.class);

    private static final String SOLR_PORT_KEY = "solr.port";
    private static final String SELECT_REQUEST_MASK = "([^?]*)select(.*)";
    private static final String ANALYSIS_REQUEST_MASK = "([^?]*)analysis(.*)";

    private static final String SELECT_RESPONSE = "{\"response\":{\"numFound\":0,\"start\":0,\"docs\":[]}}";

    private static final String ANALYSIS_RESPONSE_MOCK_FILE = "/distributions/common/WireMocks/Solr/analysisResponse.json";
    private static final String IDP_OK_RESPONSE_MOCK_FILE = "/distributions/common/WireMocks/IDP/ok.json";

    private static final String OPENAGENCY_RESPONSE_MOCK_DIR = "/distributions/common/WireMocks/Openagency";
    private static final String VIPCORE_RESPONSE_MOCK_DIR = "/distributions/common/WireMocks/VipCore";

    private static final String SOAP_ACTION_LIBRARYRULES = "LibraryRules";
    private static final String SOAP_ACTION_SHOWORDER = "ShowOrder";
    private static final String NUMBERROLL_ID_FILE = "id_numbers";
    private static final String NUMBERROLL_RESPONSE = "{\"numberRollResponse\":{\"rollNumber\":{\"$\":\"%s\"}},\"@namespaces\":null}";

    private static final JSONBContext jsonbContext = new JSONBContext();

    private WireMockServer wiremockServer;

    private void setNumberRollResponse(File rootFile) {
        try {
            if (rootFile != null) {
                String rootDir = rootFile.getAbsolutePath();
                if (rootDir != null) {
                    File numberFile = new File(rootDir + "/" + NUMBERROLL_ID_FILE);
                    if (numberFile.exists() && !numberFile.isDirectory()) {
                        FileInputStream fis = new FileInputStream(rootDir + "/" + NUMBERROLL_ID_FILE);
                        String response = IOUtils.readAll(fis, "UTF-8");
                        String[] lines = response.split("\n");
                        String prevLine = "";
                        for (String line : lines) {
                            if ("".equals(prevLine)) {
                                wiremockServer.stubFor(get(urlMatching("(.*[^?]*)action=numberRoll(.*)")).
                                        inScenario("idNumbers").
                                        whenScenarioStateIs(Scenario.STARTED).
                                        willReturn(new ResponseDefinitionBuilder().withStatus(200).
                                                withHeader("Content-Type", "application/json").withBody(String.format(NUMBERROLL_RESPONSE, line))).
                                        willSetStateTo(line));
                            } else {
                                wiremockServer.stubFor(get(urlMatching("(.*[^?]*)action=numberRoll(.*)")).
                                        inScenario("idNumbers").
                                        whenScenarioStateIs(prevLine).
                                        willReturn(new ResponseDefinitionBuilder().withStatus(200).
                                                withHeader("Content-Type", "application/json").withBody(String.format(NUMBERROLL_RESPONSE, line))).
                                        willSetStateTo(line));

                            }
                            prevLine = line;
                        }
                    }
                }
            }
        } catch (Throwable ex) {
            logger.error("OcbWireMock ERROR : ", ex);
            throw new IllegalStateException("OcbWireMock error", ex);
        }
    }

    private void setAnalysisResponse() {
        String analysisResponse;
        try {
            File workDir = new File("");
            FileInputStream fis = new FileInputStream(workDir.getAbsolutePath() + ANALYSIS_RESPONSE_MOCK_FILE);
            analysisResponse = IOUtils.readAll(fis, "UTF-8");
            wiremockServer.stubFor(any(urlMatching(SELECT_REQUEST_MASK)).willReturn(new ResponseDefinitionBuilder().withStatus(200).withBody(SELECT_RESPONSE)));
            wiremockServer.stubFor(any(urlMatching(ANALYSIS_REQUEST_MASK)).willReturn(new ResponseDefinitionBuilder().withStatus(200).withBody(analysisResponse)));
        } catch (Throwable ex) {
            logger.error("OcbWireMock ERROR : ", ex);
            throw new IllegalStateException("OcbWireMock error", ex);
        }
    }

    private void setIDPResponse() {
        String idpOkResponse;
        try {
            final File workDir = new File("");
            final FileInputStream fis = new FileInputStream(workDir.getAbsolutePath() + IDP_OK_RESPONSE_MOCK_FILE);
            idpOkResponse = IOUtils.readAll(fis, "UTF-8");
            wiremockServer.stubFor(
                    any(urlMatching("/api/v1/authorize/"))
                            .willReturn(ResponseDefinitionBuilder.okForJson(jsonbContext.unmarshall(idpOkResponse, AuthorizeResponse.class))));
        } catch (Throwable ex) {
            logger.error("OcbWireMock ERROR : ", ex);
            throw new IllegalStateException("OcbWireMock error", ex);
        }
    }

    private void setResponse(File workDir, String file, String matcher, String soapAction) {
        try {
            FileInputStream fis = new FileInputStream(workDir.getAbsolutePath() + "/" + file);
            String response = IOUtils.readAll(fis, "UTF-8");
            wiremockServer.stubFor(
                    any(urlMatching("(.*)")).
                            withHeader("SOAPAction", containing(soapAction)).
                            withRequestBody(containing(matcher)).
                            willReturn(new ResponseDefinitionBuilder().withStatus(200).withBody(response)));
        } catch (Throwable ex) {
            logger.error("wiremockServer setOpenagencyResponses ERROR : ", ex);
            throw new IllegalStateException("OcbWireMock mocking error", ex);
        }
    }

    private void setVipResponsesJson(File workDir, String file, String matcher) {
        try {
            final FileInputStream fis = new FileInputStream(workDir.getAbsolutePath() + "/" + file);
            final String response = IOUtils.readAll(fis, "UTF-8");
            // AgencyId 299999 is a special agency which returns an error body. But the status code has to be 404 which
            // means we have to handle it differently
            if (file.contains("299999")) {
                wiremockServer.stubFor(
                        any(urlMatching("/1.0/api/libraryrules")).
                                withRequestBody(containing(matcher)).
                                willReturn(new ResponseDefinitionBuilder().withBody(
                                                Json.write(jsonbContext.unmarshall(response, LibraryRulesResponse.class))).withStatus(404).
                                        withHeader("Content-Type", "application/json")));
            } else {
                wiremockServer.stubFor(
                        any(urlMatching("/1.0/api/libraryrules")).
                                withHeader("Content-type", containing("application/json")).
                                withRequestBody(containing(matcher)).
                                willReturn(ResponseDefinitionBuilder.okForJson(jsonbContext.unmarshall(response, LibraryRulesResponse.class))));
            }
        } catch (Throwable ex) {
            logger.error("wiremockServer LibraryRulesResponse ERROR : ", ex);
            throw new IllegalStateException("OcbWireMock mocking error", ex);
        }
    }

    private void setOpenagencyResponses() {
        File workDir = new File("");
        workDir = new File(workDir.getAbsolutePath() + OPENAGENCY_RESPONSE_MOCK_DIR);
        String[] files = workDir.list();
        if (files == null) return;
        String selector;
        for (String file : files) {
            logger.debug("Setting wiremock {}", file);
            String[] splitted = file.split("\\.");
            if (splitted.length == 3) {
                if ("agencyId".equals(splitted[0])) {
                    selector = "<ns1:agencyId>" + splitted[1] + "</ns1:agencyId>";
                    setResponse(workDir, file, selector, SOAP_ACTION_LIBRARYRULES);
                }
                if ("cataloging_template_set".equals(splitted[0])) {
                    selector = "<ns1:name>" + splitted[0] + "</ns1:name><ns1:string>" + splitted[1] + "</ns1:string>";
                    setResponse(workDir, file, selector, SOAP_ACTION_LIBRARYRULES);
                }
                if ("showOrder".equals(splitted[0])) {
                    selector = "<ns1:agencyId>" + splitted[1] + "</ns1:agencyId>";
                    setResponse(workDir, file, selector, SOAP_ACTION_SHOWORDER);
                }
            }
        }
    }

    private void setVipCoreResponses() {
        File workDir = new File("");
        workDir = new File(workDir.getAbsolutePath() + VIPCORE_RESPONSE_MOCK_DIR);
        String[] files = workDir.list();
        if (files == null) return;
        String selector;
        for (String file : files) {
            logger.debug("Setting wiremock {}", file);
            String[] splitted = file.split("\\.");
            if (splitted.length == 3) {
                if ("agencyId".equals(splitted[0])) {
                    selector = "{\"agencyId\":\"" + splitted[1] + "\"}";
                    setVipResponsesJson(workDir, file, selector);
                }
                if ("cataloging_template_set".equals(splitted[0])) {
                    selector = "{\"libraryRule\":[{\"name\":\"cataloging_template_set\",\"string\":\"" + splitted[1] + "\"}]}";
                    setVipResponsesJson(workDir, file, selector);
                }
            }
        }
    }


    OcbWireMockServer(BuildTestcase utc, Properties settings) {
        File rootFile = utc.getWireMockRootDirectory();
        activate(false, "", rootFile, settings);
    }

    OcbWireMockServer(UpdateTestcase utc, Properties settings) {
        File rootFile = utc.getWireMockRootDirectory();
        File file = utc.getSolrRootDirectory();
        String rootDir = "";
        if (file != null) rootDir = utc.getSolrRootDirectory().getAbsolutePath();
        boolean hasSolrMock = utc.hasSolrMocking();
        activate(hasSolrMock, rootDir, rootFile, settings);
    }

    void activate(boolean hasSolrMock, String rootDir, File rootFile, Properties settings) {
        logger.entry();

        try {
            this.wiremockServer = null;

            Integer port = Integer.valueOf(settings.getProperty(SOLR_PORT_KEY), 10);
            if (hasSolrMock) {
                StopWatch watch = new StopWatch();

                logger.debug("Starting WireMock on port {} with root directory: {}", port, rootDir);
                WireMockConfiguration wireMockConfiguration = wireMockConfig().port(port).withRootDirectory(rootDir);

                wiremockServer = new WireMockServer(wireMockConfiguration);
                setNumberRollResponse(rootFile);
                setOpenagencyResponses();
                setVipCoreResponses();
                setIDPResponse();
                wiremockServer.start();

                logger.debug("Starting WireMock server in {} ms", watch.getElapsedTime());
            } else {
                logger.debug("Starting fake wiremock for solr");
                wiremockServer = new WireMockServer(wireMockConfig().port(port));
                setNumberRollResponse(rootFile);
                setAnalysisResponse();
                setOpenagencyResponses();
                setVipCoreResponses();
                setIDPResponse();
                wiremockServer.start();
                logger.debug("Stub settings {}", wiremockServer.listAllStubMappings().getMappings());
            }
        } catch (Throwable ex) {
            logger.error("wiremockServer OcbWireMock ERROR : ", ex);
            throw new IllegalStateException("OcbWireMock mocking error", ex);
        } finally {
            logger.exit();
        }
    }

    private void showRequests() {
        List<ServeEvent> serverEvents = wiremockServer.getAllServeEvents();
        logger.debug("Incoming requests :");
        for (ServeEvent pp : serverEvents) {
            logger.debug("REQ {}", pp.getRequest().toString());
            logger.debug("RSP {}", pp.getResponse().getBody());
        }
    }

    void stop() {
        logger.entry();

        try {
            if (wiremockServer != null) {
                showRequests();
                StopWatch watch = new StopWatch();
                wiremockServer.stop();
                logger.debug("Stopping WireMock Solr server in {} ms", watch.getElapsedTime());
            }
        } catch (Throwable ex) {
            logger.error("OcbWireMock stop ERROR : ", ex);
        } finally {
            logger.exit();
        }
    }
}
