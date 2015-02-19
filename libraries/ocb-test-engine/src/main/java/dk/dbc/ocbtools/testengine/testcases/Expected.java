//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.testcases;

//-----------------------------------------------------------------------------
import java.util.List;

//-----------------------------------------------------------------------------
/**
 * Created by stp on 16/02/15.
 */
public class Expected {
    public Expected() {
        this.validationResults = null;
    }

    //-------------------------------------------------------------------------
    //              Properties
    //-------------------------------------------------------------------------

    public List<ValidationResult> getValidationResults() {
        return validationResults;
    }

    public void setValidationResults( List<ValidationResult> validationResults ) {
        this.validationResults = validationResults;
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private List<ValidationResult> validationResults;
}
