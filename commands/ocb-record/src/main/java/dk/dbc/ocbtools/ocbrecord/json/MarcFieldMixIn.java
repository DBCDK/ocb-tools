package dk.dbc.ocbtools.ocbrecord.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import dk.dbc.common.records.MarcSubField;

import java.util.List;

/**
 * This class is a companion to the Sink DTO class.
 *
 * Think of this as a way to keep the DTO class "jackson-free" by mixing in annotations
 * to the DTO class during runtime.
 *
 * Method implementations of a MixIn class are ignored.
 */
public class MarcFieldMixIn {
    /**
     * Makes jackson runtime aware of non-default constructor.
     *
     * @param name String
     * @param indicator String
     * @param subfields List&lt;MarcSubField&gt;
     */
    @JsonCreator
    public MarcFieldMixIn( @JsonProperty( "name" ) String name,
                           @JsonProperty( "indicator" ) String indicator,
                           @JsonProperty( "subfields" ) List<MarcSubField> subfields ) {
    }
}
