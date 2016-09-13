package dk.dbc.ocbtools.testengine.testcases;

import dk.dbc.updateservice.service.api.Entry;
import dk.dbc.updateservice.service.api.Type;

import java.util.List;

/**
 * Defines the expected update result of a testcase in json.
 */
public class UpdateTestcaseExpectedUpdateResult {
    private List<Entry> errors = null;
    private List<UpdateTestcaseRecord> rawrepo = null;
    private UpdateTestcaseExpectedMail mail = null;

    public UpdateTestcaseExpectedUpdateResult() {
    }

    public List<Entry> getErrors() {
        return errors;
    }

    public void setErrors(List<Entry> errors) {
        this.errors = errors;
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

    public boolean hasErrors() {
        if (errors != null) {
            for (Entry errResult : errors) {
                if (errResult.getType() == Type.ERROR) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasWarnings() {
        if (errors != null) {
            for (Entry errResult : errors) {
                if (errResult.getType() == Type.WARNING) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasDoublepostKey() {
        if (errors != null) {
            for (Entry errResult : errors) {
                if (errResult.getType() == Type.DOUBLE_RECORD) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "TestcaseExpectedUpdateResult{" +
                "errors=" + errors +
                ", rawrepo=" + rawrepo +
                ", mail=" + mail +
                '}';
    }
}
