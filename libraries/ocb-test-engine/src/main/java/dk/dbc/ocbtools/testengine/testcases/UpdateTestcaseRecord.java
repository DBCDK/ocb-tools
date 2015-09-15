//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.testcases;

//-----------------------------------------------------------------------------

import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//-----------------------------------------------------------------------------

/**
 * Represents a record structure in a testcase json file.
 */
public class UpdateTestcaseRecord {
    private String record;

    @JsonIgnore
    private File recordFile;
    private TestcaseRecordType type;
    private boolean deleted;
    private List<String> children;
    private List<String> enrichments;
    private Boolean enqueued;

    public UpdateTestcaseRecord() {
        this.record = "";
        this.recordFile = null;
        this.type = null;
        this.deleted = false;
        this.children = new ArrayList<>();
        this.enrichments = new ArrayList<>();
        this.enqueued = false;
    }

    //-------------------------------------------------------------------------
    //              Properties
    //-------------------------------------------------------------------------

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

    public TestcaseRecordType getType() {
        return type;
    }

    public void setType(TestcaseRecordType type) {
        this.type = type;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
    }

    public List<String> getEnrichments() {
        return enrichments;
    }

    public void setEnrichments(List<String> enrichments) {
        this.enrichments = enrichments;
    }

    public Boolean isEnqueued() {
        return enqueued;
    }

    public void setEnqueued(Boolean enqueued) {
        this.enqueued = enqueued;
    }

    //-------------------------------------------------------------------------
    //              Object
    //-------------------------------------------------------------------------

    @Override
    public String toString() {
        return "TestcaseRecord{" +
                "record='" + record + '\'' +
                ", recordFile=" + recordFile +
                ", type=" + type +
                ", deleted=" + deleted +
                ", children=" + children +
                ", enrichments=" + enrichments +
                ", enqueued=" + enqueued +
                '}';
    }
}
