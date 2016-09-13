package dk.dbc.ocbtools.testengine.testcases;

import dk.dbc.marcxmerge.MarcXChangeMimeType;

/**
 * Represents a record type in json testcases.
 */
public enum TestcaseRecordType {
    MARCXCHANGE(MarcXChangeMimeType.MARCXCHANGE),
    ENRICHMENT(MarcXChangeMimeType.ENRICHMENT);

    private final String value;

    TestcaseRecordType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TestcaseRecordType fromValue(String v) {
        for (TestcaseRecordType c : TestcaseRecordType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
