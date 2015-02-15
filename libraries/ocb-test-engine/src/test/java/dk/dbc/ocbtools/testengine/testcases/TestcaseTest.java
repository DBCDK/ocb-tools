//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.testcases;

//-----------------------------------------------------------------------------
import dk.dbc.iscrum.utils.json.Json;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

//-----------------------------------------------------------------------------
/**
 * Created by stp on 13/02/15.
 */
public class TestcaseTest {
    private static final String TestcaseSerialized = "{\"name\":\"name\",\"description\":\"description\"}";

    @Test
    public void testJsonEncoding() throws Exception {
        Testcase tc = new Testcase();
        tc.setName( "name" );
        tc.setDescription( "description" );
        tc.setFile( new File( "." ).getCanonicalFile() );

        assertEquals( TestcaseSerialized, Json.encode( tc ) );
    }

    @Test
    public void testJsonDecoding() throws Exception {
        Testcase tc = Json.decode( TestcaseSerialized, Testcase.class );

        assertEquals( "name", tc.getName() );
        assertEquals( "description", tc.getDescription() );
        assertNull( tc.getFile() );
    }
}
