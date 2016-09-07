//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.testcases;

//-----------------------------------------------------------------------------

import java.util.List;

//-----------------------------------------------------------------------------

/**
 * Defines the expected result of a update testcase in json.
 */
public class UpdateTestcaseExpectedResult {
    private List<ValidationResult> validation;
    private UpdateTestcaseExpectedUpdateResult update;

    public UpdateTestcaseExpectedResult() {
        this.validation = null;
        this.update = null;
    }

    //-------------------------------------------------------------------------
    //              Properties
    //-------------------------------------------------------------------------

    public List<ValidationResult> getValidation() {
        return validation;
    }

    public void setValidation(List<ValidationResult> validation) {
        this.validation = validation;
    }

    public UpdateTestcaseExpectedUpdateResult getUpdate() {
        return update;
    }

    public void setUpdate(UpdateTestcaseExpectedUpdateResult update) {
        this.update = update;
    }

    //-------------------------------------------------------------------------
    //              Checks
    //-------------------------------------------------------------------------

    public boolean hasValidationErrors() {
        if (validation == null) {
            return false;
        }

        for (ValidationResult valResult : validation) {
            if (valResult.getType() == ValidationResultType.ERROR) {
                return true;
            }
        }

        return false;
    }

    //-------------------------------------------------------------------------
    //              Object
    //-------------------------------------------------------------------------

    @Override
    public String toString() {
        return "TestcaseExpectedResult{" +
                "validation=" + validation +
                ", update=" + update +
                '}';
    }


}
