package dk.dbc.ocbtools.testengine.reports;

import dk.dbc.iscrum.utils.logback.filters.BusinessLoggerFilter;
import dk.dbc.ocbtools.testengine.runners.TestExecutorResult;
import dk.dbc.ocbtools.testengine.runners.TestResult;
import dk.dbc.ocbtools.testengine.runners.TestcaseResult;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

public class JUnitReport implements TestReport {
    private static final XLogger logger = XLoggerFactory.getXLogger(JUnitReport.class);
    private static final XLogger output = XLoggerFactory.getXLogger(BusinessLoggerFilter.LOGGER_NAME);

    private File reportDir;

    public JUnitReport(File reportDir) {
        this.reportDir = reportDir;
    }

    //-------------------------------------------------------------------------
    //              Test report
    //-------------------------------------------------------------------------

    @Override
    public void produce(TestResult testResult) {
        logger.entry(testResult);

        try {
            deleteDirOrFile(reportDir);
            reportDir.mkdirs();

            Document doc = createJUnitDocument(testResult);

            DOMSource domSource = new DOMSource(doc);

            String filename = String.format("%s/TEST-ocb-tests.xml", reportDir.getCanonicalFile());
            StreamResult streamResult = new StreamResult(new File(filename));
            logger.debug("Write JUnit report to file: {}", filename);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(domSource, streamResult);
        } catch (IOException | ParserConfigurationException | TransformerException ex) {
            output.error("Unable to generate JUnit report.", ex);
        } finally {
            logger.exit();
        }
    }

    //-------------------------------------------------------------------------
    //              JUnit producers
    //-------------------------------------------------------------------------

    private Document createJUnitDocument(TestResult testResult) throws ParserConfigurationException {
        logger.entry(testResult);

        Document doc = null;
        try {
            DocumentBuilderFactory dbf;
            dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            doc = documentBuilder.newDocument();

            Element testsuite = doc.createElement("testsuite");
            testsuite.setAttribute("failures", String.valueOf(testResult.countErrors()));
            testsuite.setAttribute("time", String.format("%s", testResult.getTime() / 1000.0));
            testsuite.setAttribute("errors", "0");
            testsuite.setAttribute("skipped", "0");
            testsuite.setAttribute("tests", String.valueOf(testResult.countTests()));
            testsuite.setAttribute("name", "opencat.business.tests");
            doc.appendChild(testsuite);

            addPropertiesToDocument(doc, testsuite);
            for (TestcaseResult testcaseResult : testResult) {
                addTestResultsToDocument(doc, testsuite, testcaseResult);
            }

            return doc;
        } finally {
            logger.exit(doc);
        }
    }

    private void addPropertiesToDocument(Document doc, Element testsuite) {
        logger.entry();

        try {
            Element groupElement = doc.createElement("properties");

            Enumeration<?> propertyNames = System.getProperties().propertyNames();
            while (propertyNames.hasMoreElements()) {
                Object name = propertyNames.nextElement();

                Element propElement = doc.createElement("property");
                propElement.setAttribute("name", name.toString());
                propElement.setAttribute("value", System.getProperties().getProperty(name.toString()));

                groupElement.appendChild(propElement);
            }
            testsuite.appendChild(groupElement);
        } finally {
            logger.exit();
        }
    }

    private void addTestResultsToDocument(Document doc, Element testsuite, TestcaseResult testcaseResult) {
        logger.entry();

        try {
            for (TestExecutorResult testExecutorResult : testcaseResult.getResults()) {
                Element testcaseElement = doc.createElement("testcase");

                testcaseElement.setAttribute("time", String.format("%s", testExecutorResult.getTime() / 1000.0));

                String classname = testcaseResult.getBaseTestcase().getDistributionName() + "." + testcaseResult.getBaseTestcase().getName();
                testcaseElement.setAttribute("classname", classname);
                testcaseElement.setAttribute("name", testExecutorResult.getExecutor().getClass().getName());

                if (testExecutorResult.getAssertionError() != null) {
                    Element failureElement = doc.createElement("failure");
                    failureElement.setAttribute("message", testExecutorResult.getAssertionError().getMessage());
                    failureElement.setAttribute("type", testExecutorResult.getAssertionError().getClass().getName());
                    testcaseElement.appendChild(failureElement);
                }

                testsuite.appendChild(testcaseElement);
            }
        } finally {
            logger.exit();
        }
    }

    //-------------------------------------------------------------------------
    //              Helpers
    //-------------------------------------------------------------------------

    private void deleteDirOrFile(File file) {
        logger.entry();

        try {
            if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    deleteDirOrFile(f);
                }
            }

            file.delete();
        } finally {
            logger.exit();
        }
    }
}
