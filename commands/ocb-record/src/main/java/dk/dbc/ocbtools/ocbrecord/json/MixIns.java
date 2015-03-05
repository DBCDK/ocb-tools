package dk.dbc.ocbtools.ocbrecord.json;

import dk.dbc.iscrum.records.MarcField;
import dk.dbc.iscrum.records.MarcRecord;
import dk.dbc.iscrum.records.MarcSubField;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides access to all JSON mixins
 */
public class MixIns {
    private static Map<Class<?>, Class<?>> mixIns = new HashMap<>();
    static {
        mixIns.put( MarcRecord.class, MarcRecordMixIn.class);
        mixIns.put( MarcField.class, MarcFieldMixIn.class );
        mixIns.put( MarcSubField.class, MarcSubFieldMixIn.class );
    }

    private MixIns() { }

    public static Map<Class<?>, Class<?>> getMixIns() {
        return mixIns;
    }
}
