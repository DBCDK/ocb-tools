package dk.dbc.ocbtools.ocbrecord.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class is a companion to the Sink DTO class.
 *
 * Think of this as a way to keep the DTO class "jackson-free" by mixing in annotations
 * to the DTO class during runtime.
 *
 * Method implementations of a MixIn class are ignored.
 */
public class MarcSubFieldMixIn {
    /**
     * Makes jackson runtime aware of non-default constructor.
     *
     * @param name String
     * @param value String
     */
    @JsonCreator
    public MarcSubFieldMixIn( @JsonProperty( "name" ) String name,
                              @JsonProperty( "value" ) String value ) {
    }
}
