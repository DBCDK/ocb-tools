package dk.dbc.ocbtools.testengine.testcases;


import dk.dbc.updateservice.service.api.DoubleRecordEntry;
import dk.dbc.updateservice.service.api.MessageEntry;
import dk.dbc.updateservice.service.api.Type;

import java.util.List;

/**
 * Defines the expected update result of a testcase in json.
 */
public class UpdateTestcaseExpectedUpdateResult {
    private List<MessageEntry> errors = null;
    private List<DoubleRecordEntry> doubleRecords = null;
    private List<UpdateTestcaseRecord> rawrepo = null;
    private UpdateTestcaseExpectedMail mail = null;

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

    public List<UpdateTestcaseRecord> getRawrepo() {
        return rawrepo;
    }

    public void setRawrepo(List<UpdateTestcaseRecord> rawrepo) {
        this.rawrepo = rawrepo;
    }

    public UpdateTestcaseExpectedMail getMail() {
        return mail;
    }

    public void setMail(UpdateTestcaseExpectedMail mail) {
        this.mail = mail;
    }

    public boolean hasFatals() {
        if (errors != null) {
            for (MessageEntry errResult : errors) {
                if (errResult.getType() == Type.FATAL) {
                    return true;
                }
            }
        }
        return false;
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

        UpdateTestcaseExpectedUpdateResult that = (UpdateTestcaseExpectedUpdateResult) o;

        if (errors != null ? !errors.equals(that.errors) : that.errors != null) return false;
        if (doubleRecords != null ? !doubleRecords.equals(that.doubleRecords) : that.doubleRecords != null)
            return false;
        if (rawrepo != null ? !rawrepo.equals(that.rawrepo) : that.rawrepo != null) return false;
        return mail != null ? mail.equals(that.mail) : that.mail == null;

    }

    @Override
    public int hashCode() {
        int result = errors != null ? errors.hashCode() : 0;
        result = 31 * result + (doubleRecords != null ? doubleRecords.hashCode() : 0);
        result = 31 * result + (rawrepo != null ? rawrepo.hashCode() : 0);
        result = 31 * result + (mail != null ? mail.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UpdateTestcaseExpectedUpdateResult{" +
                "errors=" + errors +
                ", doubleRecords=" + doubleRecords +
                ", rawrepo=" + rawrepo +
                ", mail=" + mail +
                '}';
    }
}
