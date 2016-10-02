package dk.dbc.ocbtools.testengine.executors;

import dk.dbc.buildservice.client.BibliographicRecordFactory;
import dk.dbc.buildservice.client.BuildService;
import dk.dbc.buildservice.service.api.BibliographicRecord;
import dk.dbc.buildservice.service.api.BuildPortType;
import dk.dbc.buildservice.service.api.BuildRequest;
import dk.dbc.buildservice.service.api.BuildResult;
import dk.dbc.iscrum.records.MarcConverter;
import dk.dbc.iscrum.records.MarcRecord;
import dk.dbc.ocbtools.testengine.asserters.BuildAsserter;
import dk.dbc.ocbtools.testengine.testcases.BuildTestcase;
import org.perf4j.StopWatch;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
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
    private static final XLogger logger = XLoggerFactory.getXLogger(RemoteValidateExecutor.class);
    private static final String TRACKING_ID_FORMAT = "ocbtools-%s-%s-%s";

    private BuildTestcase buildTestcase;
    private Properties settings;
    private DemoInfoPrinter demoInfoPrinter;


    public RemoteBuildExecutor(BuildTestcase buildTestcase, Properties settings, boolean printDemoInfo) {
        this.buildTestcase = buildTestcase;
        this.settings = settings;
        this.demoInfoPrinter = null;
        if (printDemoInfo) {
            this.demoInfoPrinter = new DemoInfoPrinter();
        }
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
        } finally {
            logger.exit();
            return true;
        }
    }

    @Override
    public void teardown() {
        logger.entry();
        try {
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
            BuildService buildService = new BuildService(url);
            BuildPortType buildPortType = buildService.createPort();

            BuildRequest buildRequest = new BuildRequest();
            buildRequest.setSchemaName(buildTestcase.getRequest().getTemplateName());
            buildRequest.setTrackingId(String.format(TRACKING_ID_FORMAT, System.getProperty("user.name"), buildTestcase.getName(), getClass().getSimpleName()));

            BibliographicRecord bibliographicRecord = BibliographicRecordFactory.newMarcRecord(buildTestcase.loadRequestRecord());
            buildRequest.setBibliographicRecord(bibliographicRecord);

            StopWatch watch = new StopWatch();

            logger.debug("Sending request '{}' to {}", buildRequest.getTrackingId(), url);
            if (demoInfoPrinter != null) {
                demoInfoPrinter.printRequest(buildRequest, buildTestcase.loadRequestRecord());
            }
            watch.start();
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

            logger.debug("Receive response in {} ms: {}", watch.getElapsedTime(), buildResult);
            if (demoInfoPrinter != null) {
                demoInfoPrinter.printResponse(buildResult);
            }
        } catch (IOException | ParserConfigurationException | SAXException | JAXBException e) {
            throw new AssertionError("Fatal error when building record for testcase " + buildTestcase.getName(), e);
        } finally {
            logger.exit();
        }
    }

    private URL createServiceUrl() {
        logger.entry();
        URL result = null;
        try {
            String key = "buildservice." + buildTestcase.getDistributionName() + ".url";
            String property = settings.getProperty(key);
            result = new URL(property);
            return result;
        } catch (MalformedURLException e) {
            throw new AssertionError("Unable to create url to webservice: " + e.getMessage(), e);
        } finally {
            logger.exit(result);
        }
    }
}
