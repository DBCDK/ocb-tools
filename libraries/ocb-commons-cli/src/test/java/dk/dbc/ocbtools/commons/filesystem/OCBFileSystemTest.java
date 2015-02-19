//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.commons.filesystem;

//-----------------------------------------------------------------------------

import org.junit.Test;

import java.io.File;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by stp on 14/02/15.
 */
public class OCBFileSystemTest {
    @Test
    public void testBaseDir() throws Exception {
        Properties props = new Properties();
        props.load( getClass().getResourceAsStream( "/build-test.settings" ) );

        File baseDir = new File( props.getProperty( "ocb.directory" ) ).getCanonicalFile();
        assertEquals( baseDir, newInstance().getBaseDir() );
        assertEquals( baseDir, newInstance( baseDir.getCanonicalPath() + "/distributions" ).getBaseDir() );
        assertEquals( baseDir, newInstance( baseDir.getCanonicalPath() + "/distributions/fbs" ).getBaseDir() );
        assertNull( newInstance( baseDir.getCanonicalPath() + "/distributions/unknown-dir" ).getBaseDir() );
    }

    @Test
    public void testFindDistributions() throws Exception {
        OCBFileSystem instance = newInstance();

        String[] expected = { "dataio", "fbs" };
        assertEquals( Arrays.asList( expected ), instance.findDistributions() );
    }

    @Test
    public void testFindSystemTests() throws Exception {
        OCBFileSystem instance = newInstance();

        List<File> expected = new ArrayList<>();
        expected.add( new File( instance.getBaseDir().getAbsolutePath() + "/distributions/dataio/system-tests/books/single-cases.json" ) );
        expected.add( new File( instance.getBaseDir().getAbsolutePath() + "/distributions/dataio/system-tests/books/volumes/main-cases.json" ) );
        expected.add( new File( instance.getBaseDir().getAbsolutePath() + "/distributions/dataio/system-tests/books/volumes/volume-cases.json" ) );
        expected.add( new File( instance.getBaseDir().getAbsolutePath() + "/distributions/dataio/system-tests/movies/film-cases.json" ) );
        expected.add( new File( instance.getBaseDir().getAbsolutePath() + "/distributions/fbs/system-tests/bog-cases.json" ) );
        expected.add( new File( instance.getBaseDir().getAbsolutePath() + "/distributions/fbs/system-tests/film-cases.json" ) );
        Collections.sort( expected );

        List<File> actual = instance.findSystemtests();
        Collections.sort( actual );

        assertEquals( expected, actual );
    }

    //-------------------------------------------------------------------------
    //              Helpers
    //-------------------------------------------------------------------------

    public OCBFileSystem newInstance() throws Exception {
        Properties props = new Properties();
        props.load( getClass().getResourceAsStream( "/build-test.settings" ) );

        return newInstance( new File( props.getProperty( "ocb.directory" ) ).getCanonicalPath() );
    }

    public OCBFileSystem newInstance( String path ) throws Exception {
        return new OCBFileSystem( path );
    }
}