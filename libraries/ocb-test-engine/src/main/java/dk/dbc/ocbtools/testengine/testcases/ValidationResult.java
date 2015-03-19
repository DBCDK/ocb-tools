//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.testcases;

//-----------------------------------------------------------------------------

import dk.dbc.iscrum.utils.logback.filters.BusinessLoggerFilter;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

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

    @Override
    public boolean equals( Object o ) {
        if( this == o ) {
            return true;
        }
        if( o == null || getClass() != o.getClass() ) {
            return false;
        }

        ValidationResult that = (ValidationResult) o;

        if( params != null ? !params.equals( that.params ) : that.params != null ) {
            return false;
        }
        if( type != that.type ) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + ( params != null ? params.hashCode() : 0 );
        return result;
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
                "type=" + type +
                ", params=" + params +
                '}';
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private static final XLogger logger = XLoggerFactory.getXLogger( ValidationResult.class );
    private static final XLogger output = XLoggerFactory.getXLogger( BusinessLoggerFilter.LOGGER_NAME );

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
