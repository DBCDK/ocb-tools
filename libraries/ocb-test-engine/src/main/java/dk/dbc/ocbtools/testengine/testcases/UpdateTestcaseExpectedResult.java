package dk.dbc.ocbtools.testengine.testcases;

import dk.dbc.updateservice.service.api.Entry;
import dk.dbc.updateservice.service.api.Type;

import java.util.List;

/**
 * Defines the expected result of a update testcase in json.
 */
public class UpdateTestcaseExpectedResult {
    private List<Entry> validation = null;
    private UpdateTestcaseExpectedUpdateResult update = null;

    public UpdateTestcaseExpectedResult() {
    }

    public List<Entry> getValidation() {
        return validation;
    }

    public void setValidation(List<Entry> validation) {
        this.validation = validation;
    }

    public UpdateTestcaseExpectedUpdateResult getUpdate() {
        return update;
    }

    public void setUpdate(UpdateTestcaseExpectedUpdateResult update) {
        this.update = update;
    }

    public boolean hasValidationErrors() {
        if (validation == null) {
            return false;
        }
        for (Entry valResult : validation) {
            if (valResult.getType() == Type.ERROR) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "TestcaseExpectedResult{" +
                "validation=" + validation +
                ", update=" + update +
                '}';
    }


}
