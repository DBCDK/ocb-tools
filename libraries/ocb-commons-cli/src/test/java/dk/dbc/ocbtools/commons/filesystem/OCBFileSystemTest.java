//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.commons.filesystem;

//-----------------------------------------------------------------------------

import dk.dbc.ocbtools.commons.type.ApplicationType;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;


/**
 * Created by stp on 14/02/15.
 */
public class OCBFileSystemTest {
    @Test
    public void testBaseDir() throws Exception {
        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/build-test.settings"));

        File baseDir = new File(props.getProperty("ocb.directory")).getCanonicalFile();
        assertEquals(baseDir, newInstance(ApplicationType.UPDATE).getBaseDir());
        assertEquals(baseDir, newInstance(baseDir.getCanonicalPath() + "/distributions", ApplicationType.UPDATE).getBaseDir());
        assertEquals(baseDir, newInstance(baseDir.getCanonicalPath() + "/distributions/fbs", ApplicationType.UPDATE).getBaseDir());
        assertNull(newInstance(baseDir.getCanonicalPath() + "/distributions/unknown-dir", ApplicationType.UPDATE).getBaseDir());
    }

    @Test
    public void testFindDistributions() throws Exception {
        OCBFileSystem instance = newInstance(ApplicationType.UPDATE);

        String[] expected = {"dataio", "fbs"};
        assertThat(instance.findDistributions(), containsInAnyOrder(expected));
    }

    @Test
    public void testFindSystemTestsUpdate() throws Exception {
        OCBFileSystem instance = newInstance(ApplicationType.UPDATE);

        List<SystemTest> expected = new ArrayList<>();
        expected.add(new SystemTest("dataio", new File(instance.getBaseDir().getAbsolutePath() + "/distributions/dataio/system-tests/update/books/single-cases.json"), ApplicationType.UPDATE));
        expected.add(new SystemTest("dataio", new File(instance.getBaseDir().getAbsolutePath() + "/distributions/dataio/system-tests/update/books/volumes/main-cases.json"), ApplicationType.UPDATE));
        expected.add(new SystemTest("dataio", new File(instance.getBaseDir().getAbsolutePath() + "/distributions/dataio/system-tests/update/books/volumes/volume-cases.json"), ApplicationType.UPDATE));
        expected.add(new SystemTest("dataio", new File(instance.getBaseDir().getAbsolutePath() + "/distributions/dataio/system-tests/update/movies/film-cases.json"), ApplicationType.UPDATE));
        expected.add(new SystemTest("fbs", new File(instance.getBaseDir().getAbsolutePath() + "/distributions/fbs/system-tests/update/bog-cases.json"), ApplicationType.UPDATE));
        expected.add(new SystemTest("fbs", new File(instance.getBaseDir().getAbsolutePath() + "/distributions/fbs/system-tests/update/film-cases.json"), ApplicationType.UPDATE));
        Collections.sort(expected);

        List<SystemTest> actual = instance.findSystemtests();
        Collections.sort(actual);

        assertEquals(expected, actual);
    }

    @Test
    public void testFindSystemTestsRest() throws Exception {
        OCBFileSystem instance = newInstance(ApplicationType.REST);

        List<SystemTest> expected = new ArrayList<>();
        expected.add(new SystemTest("dataio", new File(instance.getBaseDir().getAbsolutePath() + "/distributions/dataio/system-tests/rest/doublerecord/test.json"), ApplicationType.REST));
        Collections.sort(expected);

        List<SystemTest> actual = instance.findSystemtests();
        Collections.sort(actual);

        assertEquals(expected, actual);
    }

    @Test
    public void testFindSystemTestsBuild() throws Exception {
        OCBFileSystem instance = newInstance(ApplicationType.BUILD);

        List<SystemTest> expected = new ArrayList<>();
        expected.add(new SystemTest("fbs", new File(instance.getBaseDir().getAbsolutePath() + "/distributions/fbs/system-tests/build/test.json"), ApplicationType.BUILD));
        Collections.sort(expected);

        List<SystemTest> actual = instance.findSystemtests();
        Collections.sort(actual);

        assertEquals(expected, actual);
    }

    @Test
    public void testLoadSettings() throws Exception {
        OCBFileSystem instance = newInstance(ApplicationType.UPDATE);

        Properties settings = instance.loadSettings("servers");
        assertEquals("http://ifish_i01:20080", settings.getProperty("updateservice.dataio.url"));
    }

    //-------------------------------------------------------------------------
    //              Helpers
    //-------------------------------------------------------------------------

    private OCBFileSystem newInstance(ApplicationType applicationType) throws Exception {
        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/build-test.settings"));

        return newInstance(new File(props.getProperty("ocb.directory")).getCanonicalPath(), applicationType);
    }

    private OCBFileSystem newInstance(String path, ApplicationType applicationType) throws Exception {
        return new OCBFileSystem(path, applicationType);
    }
}
