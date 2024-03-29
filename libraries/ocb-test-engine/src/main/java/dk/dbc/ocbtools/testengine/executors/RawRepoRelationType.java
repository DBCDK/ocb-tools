package dk.dbc.ocbtools.testengine.executors;

import dk.dbc.ocbtools.testengine.testcases.UpdateTestcaseRecord;

import java.util.List;

/**
 * Define types of relations.
 */
public enum RawRepoRelationType {
    CHILD(1, "A child relation from %s -> %s was expected", "Found unexpected child of record %s, expected : [%s], was : [%s]"),
    SIBLING(2, "An enrichment relation from %s -> %s was expected", "Unexpected enrichments found for record %s: [%s]");

    private final int value;
    private final String expectedFormatError;
    private final String unexpectedFormatError;

    RawRepoRelationType(int value, String expectedFormatError, String unexpectedFormatError) {
        this.value = value;
        this.expectedFormatError = expectedFormatError;
        this.unexpectedFormatError = unexpectedFormatError;
    }

    public String getExpectedFormatError() {
        return expectedFormatError;
    }

    public String getUnexpectedFormatError() {
        return unexpectedFormatError;
    }

    public List<String> getExpectedRelationItems(UpdateTestcaseRecord record) {
        switch (value) {
            case 1:
                return record.getChildren();
            case 2:
                return record.getEnrichments();
        }

        return null;
    }
}
