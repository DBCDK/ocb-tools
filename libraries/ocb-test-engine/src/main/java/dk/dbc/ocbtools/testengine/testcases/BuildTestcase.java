package dk.dbc.ocbtools.testengine.testcases;

import dk.dbc.iscrum.records.MarcRecord;
import dk.dbc.iscrum.records.MarcRecordFactory;
import dk.dbc.iscrum.utils.IOUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;

public class BuildTestcase extends BaseTestcase {
    private static final XLogger logger = XLoggerFactory.getXLogger(BuildTestcase.class);

    private BuildTestcaseRequest request;
    private BuildTestcaseExpectedResult expected;

    public BuildTestcase() {
        this.request = null;
        this.expected = null;
    }

    public MarcRecord loadRequestRecord() throws IOException {
        logger.entry();
        MarcRecord res = null;
        try {
            if (request != null && request.getRecordFile() != null) {
                FileInputStream fis = new FileInputStream(request.getRecordFile());
                res = MarcRecordFactory.readRecord(IOUtils.readAll(fis, "UTF-8"));
            }
            return res;
        } finally {
            logger.exit(res);
        }
    }

    public MarcRecord loadResultRecord() throws IOException {
        logger.entry();
        MarcRecord res = null;
        try {
            if (expected != null && expected.getRecordFile() != null) {
                FileInputStream fis = new FileInputStream(expected.getRecordFile());
                res = MarcRecordFactory.readRecord(IOUtils.readAll(fis, "UTF-8"));
            }
            return res;
        } finally {
            logger.exit(res);
        }
    }

    public BuildTestcaseRequest getRequest() {
        return request;
    }

    public void setRequest(BuildTestcaseRequest request) {
        this.request = request;
    }

    public BuildTestcaseExpectedResult getExpected() {
        return expected;
    }

    public void setExpected(BuildTestcaseExpectedResult expected) {
        this.expected = expected;
    }

    @Override
    public String toString() {
        return "BuildTestcase{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", request=" + request +
                ", expected=" + expected +
                ", distributionName='" + distributionName + '\'' +
                ", file=" + file +
                '}';
    }
}
