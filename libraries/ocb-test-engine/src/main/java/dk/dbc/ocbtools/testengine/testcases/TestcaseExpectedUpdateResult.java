//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.testcases;

//-----------------------------------------------------------------------------
import java.util.List;

//-----------------------------------------------------------------------------
/**
 * Defines the expected update result of a testcase in json.
 */
public class TestcaseExpectedUpdateResult {
    public TestcaseExpectedUpdateResult() {
        this.errors = null;
        this.rawrepo = null;
    }

    //-------------------------------------------------------------------------
    //              Properties
    //-------------------------------------------------------------------------

    public List<ValidationResult> getErrors() {
        return errors;
    }

    public void setErrors( List<ValidationResult> errors ) {
        this.errors = errors;
    }

    public List<TestcaseRecord> getRawrepo() {
        return rawrepo;
    }

    public void setRawrepo( List<TestcaseRecord> rawrepo ) {
        this.rawrepo = rawrepo;
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

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private List<ValidationResult> errors;
    private List<TestcaseRecord> rawrepo;
}
