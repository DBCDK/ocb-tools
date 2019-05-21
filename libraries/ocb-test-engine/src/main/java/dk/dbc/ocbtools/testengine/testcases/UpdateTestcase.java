package dk.dbc.ocbtools.testengine.testcases;

import dk.dbc.common.records.MarcRecord;
import dk.dbc.common.records.MarcRecordFactory;
import dk.dbc.iscrum.utils.IOUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines a testcase that is stored in a json file.
 */
public class UpdateTestcase extends BaseTestcase {
    private static final XLogger logger = XLoggerFactory.getXLogger(UpdateTestcase.class);
    static final String WIREMOCK_ROOT_DIR = "__wiremock";
    private static final String SOLR_ROOT_DIR = WIREMOCK_ROOT_DIR + "/solr";

    private List<String> bugs = new ArrayList<>();
    private UpdateTestcaseSetup setup = null;
    private UpdateTestcaseRequest request = null;
    private UpdateTestcaseExpectedResult expected = null;

    public UpdateTestcase() {
    }

    public List<String> getBugs() {
        return bugs;
    }

    public void setBugs(List<String> bugs) {
        this.bugs = bugs;
    }

    public UpdateTestcaseSetup getSetup() {
        return setup;
    }

    public void setSetup(UpdateTestcaseSetup setup) {
        this.setup = setup;
    }

    public UpdateTestcaseRequest getRequest() {
        return request;
    }

    public void setRequest(UpdateTestcaseRequest request) {
        this.request = request;
    }

    public UpdateTestcaseExpectedResult getExpected() {
        return expected;
    }

    public void setExpected(UpdateTestcaseExpectedResult expected) {
        this.expected = expected;
    }

    public boolean hasSolrMocking() {
        logger.entry();

        Boolean result = null;
        try {
            return result = getSolrRootDirectory() != null;
        } finally {
            logger.exit(result);
        }
    }

    @JsonIgnore
    public File getWireMockRootDirectory() {
        logger.entry();

        File result = null;
        try {
            File WireMockRootDir = new File(file.getParent() + "/" + WIREMOCK_ROOT_DIR);

            if (WireMockRootDir.exists() && WireMockRootDir.isDirectory()) {
                result = WireMockRootDir;
            }

            return result;
        } finally {
            logger.exit(result);
        }
    }

    @JsonIgnore
    public File getSolrRootDirectory() {
        logger.entry();

        File result = null;
        try {
            File solrRootDir = new File(file.getParent() + "/" + SOLR_ROOT_DIR);

            if (solrRootDir.exists() && solrRootDir.isDirectory()) {
                result = solrRootDir;
            }

            return result;
        } finally {
            logger.exit(result);
        }
    }

    public MarcRecord loadRecord() throws IOException {
        logger.entry();

        try {
            if (file == null) {
                return null;
            }

            if (!file.isFile()) {
                return null;
            }

            File recordFile = new File(file.getParent() + "/" + request.getRecord());
            FileInputStream fis = new FileInputStream(recordFile);
            return MarcRecordFactory.readRecord(IOUtils.readAll(fis, "UTF-8"));
        } finally {
            logger.exit();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UpdateTestcase)) {
            return false;
        }

        UpdateTestcase updateTestcase = (UpdateTestcase) o;

        if (description != null ? !description.equals(updateTestcase.description) : updateTestcase.description != null) {
            return false;
        }
        return name != null ? name.equals(updateTestcase.name) : updateTestcase.name == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "{ \"name\": \"" + name + "\", \"description\": \"" + description + "\" }";
    }
}
