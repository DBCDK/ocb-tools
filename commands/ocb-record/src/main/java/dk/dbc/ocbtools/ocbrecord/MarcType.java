package dk.dbc.ocbtools.ocbrecord;

/**
 * Created by thl on 3/4/15.
 */
public enum MarcType {

    MARC( "MARC" ),
    MARCXCHANGE( "MARCXCHANGE" ),
    JSON( "JSON" ),
    UNKNOWN( "UNKNOWN" );

    private final String value;

    MarcType( String v ) {
        this.value = v;
    }

    public static MarcType fromValue(String v) {
        for (MarcType c: MarcType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    public String typeToString() {
        return value;
    }

    @Override
    public String toString() {
        return "MarcType{" +
                "value='" + value + '\'' +
                '}';
    }
}
