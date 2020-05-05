package dk.dbc.ocbtools.testengine.testcases;

import dk.dbc.marcxmerge.MarcXChangeMimeType;

/**
 * Represents a record type in json testcases.
 */
public enum TestcaseMimeType {
    MARCXCHANGE(MarcXChangeMimeType.MARCXCHANGE),
    ENRICHMENT(MarcXChangeMimeType.ENRICHMENT),
    ARTICLE(MarcXChangeMimeType.ARTICLE),
    AUTHORITY(MarcXChangeMimeType.AUTHORITY),
    LITANALYSIS(MarcXChangeMimeType.LITANALYSIS),
    MATVURD(MarcXChangeMimeType.MATVURD);

    private final String value;

    TestcaseMimeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TestcaseMimeType fromValue(String v) {
        for (TestcaseMimeType c : TestcaseMimeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
