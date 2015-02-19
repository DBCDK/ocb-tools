//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.testcases;

//-----------------------------------------------------------------------------

import dk.dbc.iscrum.utils.json.Json;
import org.junit.Test;

import static org.junit.Assert.*;

//-----------------------------------------------------------------------------
/**
 * Created by stp on 13/02/15.
 */
public class TestcaseTest {
    private static final String TestcaseSerialized = "{\"name\":\"name\",\"description\":\"description\",\"records\":null,\"expected\":null}";

    @Test
    public void testJsonDecoding() throws Exception {
        Testcase tc = Json.decode( getClass().getResourceAsStream( "testcase.json" ), Testcase.class );

        assertEquals( "tc_name", tc.getName() );
        assertEquals( "tc_descr", tc.getDescription() );
        assertEquals( "bog", tc.getTemplateName() );

        assertEquals( 2, tc.getRecords().size() );
        assertEquals( "rec1.xml", tc.getRecords().get( 0 ) );
        assertEquals( "rec2.xml", tc.getRecords().get( 1 ) );

        assertNotNull( tc.getExpected() );
        assertEquals( 1, tc.getExpected().getValidationResults().size() );

        ValidationResult valResult = tc.getExpected().getValidationResults().get( 0 );
        assertEquals( ValidationResultType.ERROR, valResult.getType() );
        assertNotNull( valResult.getParams() );
        assertEquals( "http://www.kat-format.dk/danMARC2/Danmarc2.5.htm", valResult.getParams().get( "url" ) );
        assertEquals( "message", valResult.getParams().get( "message" ) );
        assertEquals( 2, valResult.getParams().get( "fieldno" ) );
        assertEquals( 5, valResult.getParams().get( "subfieldno" ) );

        assertNull( tc.getFile() );
    }
}
