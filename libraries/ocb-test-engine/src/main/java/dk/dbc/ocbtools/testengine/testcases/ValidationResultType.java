//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.testcases;

//-----------------------------------------------------------------------------
public enum ValidationResultType {
    ERROR( "error" ),
    WARNING( "warning" );

    //-------------------------------------------------------------------------
    //              Constructors
    //-------------------------------------------------------------------------

    ValidationResultType( String v ) {
        value = v;
    }

    //-------------------------------------------------------------------------
    //              Methods
    //-------------------------------------------------------------------------

    public String value() {
        return value;
    }

    public static ValidationResultType fromValue(String v) {
        for (ValidationResultType c: ValidationResultType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private final String value;

}
