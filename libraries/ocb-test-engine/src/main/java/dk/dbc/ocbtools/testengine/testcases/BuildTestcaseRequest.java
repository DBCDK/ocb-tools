package dk.dbc.ocbtools.testengine.testcases;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.File;

public class BuildTestcaseRequest {

    private String templateName;
    private String record;

    @JsonIgnore
    private File recordFile;

    public BuildTestcaseRequest() {
        this.templateName = null;
        this.record = null;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getRecord() {
        return record;
    }

    public void setRecord(String record) {
        this.record = record;
    }

    public File getRecordFile() {
        return recordFile;
    }

    public void setRecordFile(File recordFile) {
        this.recordFile = recordFile;
    }

    @Override
    public String toString() {
        return "BuildTestcaseRequest{" +
                "templateName='" + templateName + '\'' +
                ", record='" + record + '\'' +
                ", recordFile=" + recordFile +
                '}';
    }
}
