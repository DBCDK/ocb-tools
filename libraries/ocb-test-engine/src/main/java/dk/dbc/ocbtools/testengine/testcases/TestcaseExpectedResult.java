//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.testcases;

//-----------------------------------------------------------------------------
import java.util.List;

//-----------------------------------------------------------------------------
/**
 * Defines the expected result of a testcase in json.
 */
public class TestcaseExpectedResult {
    public TestcaseExpectedResult() {
        this.validation = null;
        this.update = null;
    }

    //-------------------------------------------------------------------------
    //              Properties
    //-------------------------------------------------------------------------

    public List<ValidationResult> getValidation() {
        return validation;
    }

    public void setValidation( List<ValidationResult> validation ) {
        this.validation = validation;
    }

    public TestcaseExpectedUpdateResult getUpdate() {
        return update;
    }

    public void setUpdate( TestcaseExpectedUpdateResult update ) {
        this.update = update;
    }

    //-------------------------------------------------------------------------
    //              Checks
    //-------------------------------------------------------------------------

    public boolean hasValidationErrors() {
        if( validation == null ) {
            return false;
        }

        for( ValidationResult valResult : validation ) {
            if( valResult.getType() == ValidationResultType.ERROR ) {
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

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private List<ValidationResult> validation;
    private TestcaseExpectedUpdateResult update;
}