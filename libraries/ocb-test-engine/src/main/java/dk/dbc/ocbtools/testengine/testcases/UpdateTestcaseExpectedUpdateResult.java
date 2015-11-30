//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.testcases;

//-----------------------------------------------------------------------------

import java.util.List;

//-----------------------------------------------------------------------------

/**
 * Defines the expected update result of a testcase in json.
 */
public class UpdateTestcaseExpectedUpdateResult {
    private List<ValidationResult> errors;
    private List<UpdateTestcaseRecord> rawrepo;

    public UpdateTestcaseExpectedUpdateResult() {
        this.errors = null;
        this.rawrepo = null;
    }

    //-------------------------------------------------------------------------
    //              Properties
    //-------------------------------------------------------------------------

    public List<ValidationResult> getErrors() {
        return errors;
    }

    public void setErrors(List<ValidationResult> errors) {
        this.errors = errors;
    }

    public List<UpdateTestcaseRecord> getRawrepo() {
        return rawrepo;
    }

    public void setRawrepo(List<UpdateTestcaseRecord> rawrepo) {
        this.rawrepo = rawrepo;
    }

    //-------------------------------------------------------------------------
    //              Checks
    //-------------------------------------------------------------------------

    public boolean hasUpdateErrors() {
        if (errors == null) {
            return false;
        }

        for (ValidationResult errResult : errors) {
            if (errResult.getType() == ValidationResultType.ERROR) {
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
        return "TestcaseExpectedUpdateResult{" +
                "errors=" + errors +
                ", rawrepo=" + rawrepo +
                '}';
    }
}