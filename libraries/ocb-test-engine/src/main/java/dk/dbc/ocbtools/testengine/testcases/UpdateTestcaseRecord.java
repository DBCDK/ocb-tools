package dk.dbc.ocbtools.testengine.testcases;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a record structure in a testcase json file.
 */
public class UpdateTestcaseRecord {
    private String record;
    private List<Integer> holdings;

    @JsonIgnore
    private File recordFile;

    private TestcaseMimeType type;
    private boolean deleted;
    private List<String> children;
    private List<String> enrichments;
    private Boolean enqueued;
    private List<String> queueWorkers;
    private boolean virtual;

    public UpdateTestcaseRecord() {
        this.record = "";
        this.holdings = new ArrayList<>();
        this.recordFile = null;
        this.type = null;
        this.deleted = false;
        this.children = new ArrayList<>();
        this.enrichments = new ArrayList<>();
        this.enqueued = false;
        this.queueWorkers = null; // Can't initialize it to empty as an empty queue is a valid assertion while null value means "don't use"
        this.virtual = false;
    }

    public String getRecord() {
        return record;
    }

    public void setRecord(String record) {
        this.record = record;
    }

    public List<Integer> getHoldings() {
        return holdings;
    }

    public void setHoldings(List<Integer> holdings) {
        this.holdings = holdings;
    }

    public File getRecordFile() {
        return recordFile;
    }

    public void setRecordFile(File recordFile) {
        this.recordFile = recordFile;
    }

    public TestcaseMimeType getType() {
        return type;
    }

    public void setType(TestcaseMimeType type) {
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


    public boolean isVirtual() {
        return virtual;
    }

    public void setVirtual(boolean virtual) {
        this.virtual = virtual;
    }


    public List<String> getQueueWorkers() {
        return queueWorkers;
    }

    public void setQueueWorkers(List<String> queueWorkers) {
        this.queueWorkers = queueWorkers;
    }

    @Override
    public String toString() {
        return "UpdateTestcaseRecord{" +
                "record='" + record + '\'' +
                ", holdings=" + holdings +
                ", recordFile=" + recordFile +
                ", type=" + type +
                ", deleted=" + deleted +
                ", children=" + children +
                ", enrichments=" + enrichments +
                ", enqueued=" + enqueued +
                ", queueWorkers=" + queueWorkers +
                ", virtual=" + virtual +
                '}';
    }
}
