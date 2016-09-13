package dk.dbc.ocbtools.ocbtest;

import dk.dbc.iscrum.records.MarcReader;
import dk.dbc.iscrum.records.MarcRecord;
import dk.dbc.iscrum.records.providers.MarcRecordProvider;
import dk.dbc.iscrum.utils.json.Json;
import dk.dbc.iscrum.utils.logback.filters.BusinessLoggerFilter;
import dk.dbc.ocbtools.commons.api.SubcommandExecutor;
import dk.dbc.ocbtools.commons.cli.CliException;
import dk.dbc.ocbtools.testengine.testcases.TestcaseAuthentication;
import dk.dbc.ocbtools.testengine.testcases.UpdateTestcase;
import dk.dbc.ocbtools.testengine.testcases.UpdateTestcaseExpectedResult;
import dk.dbc.ocbtools.testengine.testcases.UpdateTestcaseRequest;
import dk.dbc.updateservice.service.api.Entry;
import org.apache.commons.io.FileUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by stp on 06/05/15.
 */
public class CreateExecutor implements SubcommandExecutor {
    private static final XLogger logger = XLoggerFactory.getXLogger(CreateExecutor.class);
    private static final XLogger output = XLoggerFactory.getXLogger(BusinessLoggerFilter.LOGGER_NAME);

    private File baseDir;
    private String testcaseFilename = null;
    private MarcRecordProvider recordsProvider = null;
    private String testcaseName = null;
    private String description = null;
    private TestcaseAuthentication authentication = null;
    private String templateName = null;

    public CreateExecutor(File baseDir) {
        this.baseDir = baseDir;
    }

    public String getTestcaseFilename() {
        return testcaseFilename;
    }

    public void setTestcaseFilename(String testcaseFilename) {
        this.testcaseFilename = testcaseFilename;
    }

    public MarcRecordProvider getRecordsProvider() {
        return recordsProvider;
    }

    public void setRecordsProvider(MarcRecordProvider recordsProvider) {
        this.recordsProvider = recordsProvider;
    }

    public String getTestcaseName() {
        return testcaseName;
    }

    public void setTestcaseName(String testcaseName) {
        this.testcaseName = testcaseName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TestcaseAuthentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(TestcaseAuthentication authentication) {
        this.authentication = authentication;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    @Override
    public void actionPerformed() throws CliException {
        logger.entry();

        try {
            boolean hasMultibleRecords = recordsProvider.hasMultibleRecords();

            List<UpdateTestcase> updateTestcases = new ArrayList<>();
            int recordNo = 1;
            for (MarcRecord record : recordsProvider) {
                logger.debug("Creating testcase for record [{}:{}]",
                        MarcReader.getRecordValue(record, "001", "a"),
                        MarcReader.getRecordValue(record, "001", "b"));

                String filename = "request.marc";
                if (hasMultibleRecords) {
                    filename = String.format("%s-t%s.marc", "request", recordNo);
                }
                File file = new File(baseDir.getCanonicalPath() + "/" + filename);
                FileUtils.writeStringToFile(file, record.toString(), "UTF-8");
                logger.debug("Wrote record to {}", file.getCanonicalPath());

                UpdateTestcase tc = new UpdateTestcase();
                tc.setName(testcaseName);
                if (hasMultibleRecords) {
                    tc.setName(String.format("%s-t%s", "record", recordNo));
                }
                tc.setBugs(null);
                tc.setDescription(description);
                if (hasMultibleRecords) {
                    tc.setDescription(null);
                }

                UpdateTestcaseRequest request = new UpdateTestcaseRequest();
                request.setRecord(filename);
                request.setAuthentication(authentication);
                request.setTemplateName(templateName);
                tc.setRequest(request);

                UpdateTestcaseExpectedResult expected = new UpdateTestcaseExpectedResult();
                expected.setValidation(new ArrayList<Entry>());
                expected.setUpdate(null);
                tc.setExpected(expected);

                updateTestcases.add(tc);
                recordNo++;
            }
            recordsProvider.close();

            File file = new File(testcaseFilename);
            String testcasesEncoded = Json.encodePretty(updateTestcases);
            logger.debug("Testcases encoded size: {} bytes", testcasesEncoded.getBytes("UTF-8").length);

            FileUtils.writeStringToFile(file, testcasesEncoded, "UTF-8");
            logger.debug("Wrote testcases to {}", file.getCanonicalPath());
        } catch (IOException ex) {
            throw new CliException(ex.getMessage(), ex);
        } finally {
            logger.exit();
        }
    }
}
