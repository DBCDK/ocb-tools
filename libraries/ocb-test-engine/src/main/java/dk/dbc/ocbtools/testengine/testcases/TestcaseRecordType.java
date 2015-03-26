//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.testcases;

//-----------------------------------------------------------------------------
import dk.dbc.marcxmerge.MarcXChangeMimeType;

//-----------------------------------------------------------------------------
/**
 * Represents a record type in json testcases.
 */
public enum TestcaseRecordType {
    COMMON( MarcXChangeMimeType.MARCXCHANGE ),
    ENRICHMENT( MarcXChangeMimeType.ENRICHMENT ),
    LOCALE( MarcXChangeMimeType.DECENTRAL );

    //-------------------------------------------------------------------------
    //              Constructors
    //-------------------------------------------------------------------------

    TestcaseRecordType( String v ) {
        value = v;
    }

    //-------------------------------------------------------------------------
    //              Methods
    //-------------------------------------------------------------------------

    public String value() {
        return value;
    }

    public static TestcaseRecordType fromValue(String v) {
        for (TestcaseRecordType c: TestcaseRecordType.values()) {
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
