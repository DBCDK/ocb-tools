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

    public static ApplicationType fromValue(String v) {
        for (ApplicationType c : ApplicationType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
