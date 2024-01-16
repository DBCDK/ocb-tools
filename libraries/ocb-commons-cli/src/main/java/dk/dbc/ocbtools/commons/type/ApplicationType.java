package dk.dbc.ocbtools.commons.type;

/**
 * Enum type used differentiate between build and update service.
 */
public enum ApplicationType {

    BUILD("BUILD"),
    UPDATE("UPDATE"),
    REST("REST");

    private final String value;

    ApplicationType(String v) {
        this.value = v;
    }

}
