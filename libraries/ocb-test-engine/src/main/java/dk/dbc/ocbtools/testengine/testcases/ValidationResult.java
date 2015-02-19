//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.testcases;

//-----------------------------------------------------------------------------
import java.util.HashMap;

//-----------------------------------------------------------------------------
/**
 * Created by stp on 18/02/15.
 */
public class ValidationResult {
    public ValidationResult() {
        this.type = null;
        this.params = null;
    }

    //-------------------------------------------------------------------------
    //              Properties
    //-------------------------------------------------------------------------

    public ValidationResultType getType() {
        return type;
    }

    public void setType( ValidationResultType type ) {
        this.type = type;
    }

    public HashMap<String, Object> getParams() {
        return params;
    }

    public void setParams( HashMap<String, Object> params ) {
        this.params = params;
    }


    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    /**
     * Contains the type of this validation error.
     *
     * For historical reasons, a type is a classification of an validation error.
     */
    private ValidationResultType type;

    /**
     * Map of extra parameters to the validation type.
     */
    private HashMap<String, Object> params;
}
