//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.testengine.testcases;

//-----------------------------------------------------------------------------

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by stp on 13/02/15.
 */
public class TestcaseTest {
    private static final String TestcaseSerialized = "{\"name\":\"name\",\"description\":\"description\"}";

    @Test
    public void testJsonSerialization() throws Exception {
        Testcase tc = new Testcase();
        tc.setName( "name" );
        tc.setDescription( "description" );
        tc.setDirectory( new File( "." ).getCanonicalFile() );
        tc.setFilename( new File( "somefile.json" ) );

        ObjectMapper mapper = new ObjectMapper();
        assertEquals( TestcaseSerialized, mapper.writeValueAsString( tc ) );
    }

    @Test
    public void testJsonDerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Testcase tc = mapper.readValue( TestcaseSerialized, Testcase.class );

        assertEquals( "name", tc.getName() );
        assertEquals( "description", tc.getDescription() );
        assertNull( tc.getDirectory() );
        assertNull( tc.getFilename() );
    }
}
