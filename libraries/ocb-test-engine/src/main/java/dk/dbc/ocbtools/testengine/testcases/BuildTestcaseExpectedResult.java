package dk.dbc.ocbtools.testengine.testcases;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.File;

/**
 * Defines the expected result of a build testcase in json.
 */
public class BuildTestcaseExpectedResult {

    private String error;
    private String record;

    @JsonIgnore
    private File recordFile;

    public BuildTestcaseExpectedResult() {
        this.error = null;
        this.record = null;
        this.recordFile = null;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getRecord() {
        return record;
    }

    public void setRecord(String record) {
        this.record = record;
    }

    File getRecordFile() {
        return recordFile;
    }

    void setRecordFile(File recordFile) {
        this.recordFile = recordFile;
    }

    @Override
    public String toString() {
        return "BuildTestcaseExpectedResult{" +
                "error='" + error + '\'' +
                ", record='" + record + '\'' +
                ", recordFile=" + recordFile +
                '}';
    }
}
