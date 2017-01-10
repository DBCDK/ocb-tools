package dk.dbc.ocbtools.testengine.testcases;

import dk.dbc.updateservice.service.api.DoubleRecordEntry;
import dk.dbc.updateservice.service.api.MessageEntry;
import dk.dbc.updateservice.service.api.Type;

import java.util.List;

public class UpdateTestcaseExpectedValidateResult {
    private List<MessageEntry> errors = null;
    private List<DoubleRecordEntry> doubleRecords = null;

    public List<MessageEntry> getErrors() {
        return errors;
    }

    public void setErrors(List<MessageEntry> errors) {
        this.errors = errors;
    }

    public List<DoubleRecordEntry> getDoubleRecords() {
        return doubleRecords;
    }

    public void setDoubleRecords(List<DoubleRecordEntry> doubleRecords) {
        this.doubleRecords = doubleRecords;
    }

    public boolean hasErrors() {
        if (errors != null) {
            for (MessageEntry errResult : errors) {
                if (errResult.getType() == Type.ERROR) {
                    return true;
                }
            }
        }
        return false;
    }


    public boolean hasWarnings() {
        if (errors != null) {
            for (MessageEntry errResult : errors) {
                if (errResult.getType() == Type.WARNING) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasDoubleRecords() {
        if (errors != null) {
            if (doubleRecords != null && !doubleRecords.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UpdateTestcaseExpectedValidateResult that = (UpdateTestcaseExpectedValidateResult) o;

        if (errors != null ? !errors.equals(that.errors) : that.errors != null) return false;
        return doubleRecords != null ? doubleRecords.equals(that.doubleRecords) : that.doubleRecords == null;
    }

    @Override
    public int hashCode() {
        int result = errors != null ? errors.hashCode() : 0;
        result = 31 * result + (doubleRecords != null ? doubleRecords.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UpdateTestcaseExpectedValidateResult{" +
                "errors=" + errors +
                ", doubleRecords=" + doubleRecords +
                '}';
    }
}
