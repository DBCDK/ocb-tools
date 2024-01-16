package dk.dbc.ocbtools.testengine.executors;


import dk.dbc.marc.binding.MarcRecord;
import dk.dbc.marc.reader.MarcReaderException;
import dk.dbc.marc.reader.MarcXchangeV1Reader;
import dk.dbc.ocbtools.testengine.asserters.BuildAsserter;
import dk.dbc.ocbtools.testengine.rawrepo.MarcConverter;
import dk.dbc.ocbtools.testengine.testcases.BuildTestcase;
import dk.dbc.oss.ns.catalogingbuild.BuildPortType;
import dk.dbc.oss.ns.catalogingbuild.BuildRequest;
import dk.dbc.oss.ns.catalogingbuild.BuildResult;
import dk.dbc.oss.ns.catalogingbuild.BuildStatusEnum;
import dk.dbc.oss.ns.catalogingbuild.CatalogingBuildServices;
import dk.dbc.oss.ns.catalogingbuild.BibliographicRecord;
import jakarta.xml.ws.BindingProvider;
import org.perf4j.StopWatch;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

/**
 * Executor to test a testcase against the build operation on an external
 * installation of Buildservice.
 */
public class RemoteBuildExecutor implements TestExecutor {
    private static final XLogger logger = XLoggerFactory.getXLogger(RemoteBuildExecutor.class);
    private static final String ENDPOINT_PATH = "/CatalogingBuildServices/OpenBuild";
    private static final int DEFAULT_CONNECT_TIMEOUT_MS =     60 * 1000;    // 1 minute
    private static final int DEFAULT_REQUEST_TIMEOUT_MS = 3 * 60 * 1000;    // 3 minutes
    private static final String TRACKING_ID_FORMAT = "ocbtools-%s-%s-%s";
    private static final String EXECUTOR_URL = "buildservice.url";

    private BuildTestcase buildTestcase;
    private Properties settings;
    private OcbWireMockServer wireMockServer;


    public RemoteBuildExecutor(BuildTestcase buildTestcase, Properties settings) {
        this.buildTestcase = buildTestcase;
        this.settings = settings;
        this.wireMockServer = null;
    }

    @Override
    public String name() {
        logger.entry();
        String res = null;
        try {
            res = "Build using remote Buildservice: " + settings.getProperty(EXECUTOR_URL);
            return res;
        } finally {
            logger.exit(res);
        }
    }

    @Override
    public void setup() {
        logger.entry();
        try {
            wireMockServer = new OcbWireMockServer(buildTestcase, settings);
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
            StopWatch watch = new StopWatch();
            BuildPortType buildPortType = createPort(url);
            BuildResult buildResult = buildPortType.build(buildRequest);
            watch.stop();

            String assertInput = null;
            if (buildResult != null) {
                if (buildResult.getBuildStatus() == BuildStatusEnum.OK) {
                    MarcRecord buildResultAsRecord = null;
                    List<Object> list = buildResult.getBibliographicRecord().getRecordData().getContent();
                    for (Object o : list) {
                        if (o instanceof Node) {
                            String marcString = (String) o;
                            final ByteArrayInputStream buf = new ByteArrayInputStream(marcString.getBytes());
                            final MarcXchangeV1Reader reader = new MarcXchangeV1Reader(buf, StandardCharsets.UTF_8);
                            buildResultAsRecord = reader.read();
                            break;
                        }
                    }
                    if (buildResultAsRecord != null) {
                        assertInput = buildResultAsRecord.toString();
                    }
                } else {
                    assertInput = buildResult.getBuildStatus().toString();
                }
            }
            BuildAsserter.assertValidation(buildTestcase, assertInput);

            logger.debug("Receive BUILD response in {} ms: {}", watch.getElapsedTime(), buildResult);
        } catch (IOException | ParserConfigurationException | SAXException | MarcReaderException e) {
            throw new AssertionError("Fatal error when building record for testcase " + buildTestcase.getName(), e);
        } finally {
            logger.exit();
        }
    }

    private BuildRequest getBuildRequest() throws ParserConfigurationException, SAXException, IOException, MarcReaderException {
        BuildRequest buildRequest = new BuildRequest();
        buildRequest.setSchemaName(buildTestcase.getRequest().getTemplateName());
        buildRequest.setTrackingId(String.format(TRACKING_ID_FORMAT, System.getProperty("user.name"), buildTestcase.getName(), getClass().getSimpleName()));
        MarcRecord marcRecord = buildTestcase.loadRequestRecord();
        if (marcRecord != null) {
            BibliographicRecord bibliographicRecord = MarcConverter.newMarcRecord(marcRecord);
            buildRequest.setBibliographicRecord(bibliographicRecord);
        }
        return buildRequest;
    }

    private URL createServiceUrl() {
        logger.entry();
        URL result = null;
        try {
            return result = new URL(settings.getProperty(EXECUTOR_URL));
        } catch (MalformedURLException e) {
            throw new AssertionError("Unable to create url to webservice: " + e.getMessage(), e);
        } finally {
            logger.exit(result);
        }
    }

    private BuildPortType createPort(URL baseUrl) throws MalformedURLException {
        logger.entry();
        try {
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
