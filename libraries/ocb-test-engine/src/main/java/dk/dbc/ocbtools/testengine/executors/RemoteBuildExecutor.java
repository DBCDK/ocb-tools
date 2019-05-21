package dk.dbc.ocbtools.testengine.executors;

import dk.dbc.buildservice.client.BibliographicRecordFactory;
import dk.dbc.buildservice.service.api.*;
import dk.dbc.common.records.MarcConverter;
import dk.dbc.common.records.MarcRecord;
import dk.dbc.ocbtools.testengine.asserters.BuildAsserter;
import dk.dbc.ocbtools.testengine.testcases.BuildTestcase;
import org.perf4j.StopWatch;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.BindingProvider;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

/**
 * Executor to test a testcase against the build operation on an external
 * installation of Buildservice.
 */
public class RemoteBuildExecutor implements TestExecutor {
    private static final XLogger logger = XLoggerFactory.getXLogger(RemoteBuildExecutor.class);
    private static final long DEFAULT_CONNECT_TIMEOUT_MS =     60 * 1000;    // 1 minute
    private static final long DEFAULT_REQUEST_TIMEOUT_MS = 3 * 60 * 1000;    // 3 minutes
    private static final String TRACKING_ID_FORMAT = "ocbtools-%s-%s-%s";

    private BuildTestcase buildTestcase;
    private Properties settings;
    private DemoInfoPrinter demoInfoPrinter;
    private OcbWireMockServer wireMockServer;


    public RemoteBuildExecutor(BuildTestcase buildTestcase, Properties settings, boolean printDemoInfo) {
        this.buildTestcase = buildTestcase;
        this.settings = settings;
        this.demoInfoPrinter = null;
        if (printDemoInfo) {
            this.demoInfoPrinter = new DemoInfoPrinter();
        }
        this.wireMockServer = null;
    }

    @Override
    public String name() {
        logger.entry();
        String res = null;
        try {
            res = "Build using remote Buildservice: " + createServiceUrl();
            return res;
        } finally {
            logger.exit(res);
        }
    }

    @Override
    public boolean setup() {
        logger.entry();
        try {
            if (this.demoInfoPrinter != null) {
                demoInfoPrinter.printHeader(this.buildTestcase, this);
            }
            wireMockServer = new OcbWireMockServer(buildTestcase, settings);
            return true;
        } finally {
            logger.exit();
        }
    }

    @Override
    public void teardown() {
        logger.entry();
        try {
            if (this.wireMockServer != null) {
                this.wireMockServer.stop();
            }
            if (this.demoInfoPrinter != null) {
                demoInfoPrinter.printFooter();
            }
        } finally {
            logger.exit();
        }
    }

    @Override
    public void executeTests() {
        logger.entry();
        try {
            URL url = createServiceUrl();
            BuildRequest buildRequest = getBuildRequest();
            logger.debug("Sending BUILD request '{}' to {}", buildRequest.getTrackingId(), url);
            if (demoInfoPrinter != null) {
                demoInfoPrinter.printRequest(buildRequest, buildTestcase.loadRequestRecord());
            }
            //StopWatch watch = new Log4JStopWatch("RemoteBuildExecutor.executeTests");
            StopWatch watch = new StopWatch();
            BuildPortType buildPortType = createPort(url);
            BuildResult buildResult = buildPortType.build(buildRequest);
            watch.stop();

            String assertInput = null;
            if (buildResult != null) {
                switch (buildResult.getBuildStatus()) {
                    case OK:
                        MarcRecord buildResultAsRecord = null;
                        List<Object> list = buildResult.getBibliographicRecord().getRecordData().getContent();
                        for (Object o : list) {
                            if (o instanceof Node) {
                                buildResultAsRecord = MarcConverter.createFromMarcXChange(new DOMSource((Node) o));
                                break;
                            }
                        }
                        if (buildResultAsRecord != null) {
                            assertInput = buildResultAsRecord.toString();
                        }
                        break;
                    default:
                        assertInput = buildResult.getBuildStatus().toString();
                        break;
                }
            }
            BuildAsserter.assertValidation(buildTestcase, assertInput);

            logger.debug("Receive BUILD response in {} ms: {}", watch.getElapsedTime(), buildResult);
            if (demoInfoPrinter != null) {
                demoInfoPrinter.printResponse(buildResult);
            }
        } catch (IOException | ParserConfigurationException | SAXException | JAXBException e) {
            throw new AssertionError("Fatal error when building record for testcase " + buildTestcase.getName(), e);
        } finally {
            logger.exit();
        }
    }

    private BuildRequest getBuildRequest() throws ParserConfigurationException, SAXException, IOException, JAXBException {
        BuildRequest buildRequest = new BuildRequest();
        buildRequest.setSchemaName(buildTestcase.getRequest().getTemplateName());
        buildRequest.setTrackingId(String.format(TRACKING_ID_FORMAT, System.getProperty("user.name"), buildTestcase.getName(), getClass().getSimpleName()));
        MarcRecord marcRecord = buildTestcase.loadRequestRecord();
        if (marcRecord != null) {
            BibliographicRecord bibliographicRecord = BibliographicRecordFactory.newMarcRecord(marcRecord);
            buildRequest.setBibliographicRecord(bibliographicRecord);
        }
        return buildRequest;
    }

    private URL createServiceUrl() {
        logger.entry();
        URL result = null;
        try {
            String key = "buildservice.url";
            String property = settings.getProperty(key);
            result = new URL(property);
            return result;
        } catch (MalformedURLException e) {
            throw new AssertionError("Unable to create url to webservice: " + e.getMessage(), e);
        } finally {
            logger.exit(result);
        }
    }

    private BuildPortType createPort(URL baseUrl) throws MalformedURLException {
        logger.entry();
        try {
            final String ENDPOINT_PATH = "/CatalogingBuildServices/OpenBuild";
            QName serviceName = new QName("http://oss.dbc.dk/ns/catalogingBuild", "CatalogingBuildServices");
            String endpoint = baseUrl + ENDPOINT_PATH;
            URL url = new URL(endpoint);
            CatalogingBuildServices catalogingBuildServices = new CatalogingBuildServices(url, serviceName);
            BuildPortType buildPortType = catalogingBuildServices.getBuildPort();
            BindingProvider proxy = (BindingProvider) buildPortType;
            logger.debug("Using base url: {}", baseUrl);
            logger.debug("Using complete endpoint: {}", endpoint);
            proxy.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
            proxy.getRequestContext().put("com.sun.xml.ws.connect.timeout", DEFAULT_CONNECT_TIMEOUT_MS);
            proxy.getRequestContext().put("com.sun.xml.ws.request.timeout", DEFAULT_REQUEST_TIMEOUT_MS);
            return buildPortType;
        } finally {
            logger.exit();
        }
    }
}
