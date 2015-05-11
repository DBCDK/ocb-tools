//-----------------------------------------------------------------------------
package dk.dbc.ocbtools.ocbtest;

//-----------------------------------------------------------------------------

import dk.dbc.iscrum.records.MarcReader;
import dk.dbc.iscrum.records.MarcRecord;
import dk.dbc.iscrum.records.MarcRecordFactory;
import dk.dbc.iscrum.utils.IOUtils;
import dk.dbc.iscrum.utils.ResourceBundles;
import dk.dbc.iscrum.utils.json.Json;
import dk.dbc.ocbtools.commons.api.SubcommandDefinition;
import dk.dbc.ocbtools.commons.cli.CliException;
import dk.dbc.ocbtools.commons.filesystem.SystemTest;
import dk.dbc.ocbtools.testengine.testcases.Testcase;
import dk.dbc.ocbtools.testengine.testcases.TestcaseFactory;
import org.apache.commons.cli.*;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by stp on 08/05/15.
 */
public class CreateExecutorTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    //-------------------------------------------------------------------------
    //              Setup
    //-------------------------------------------------------------------------

    @BeforeClass
    public static void init() {
        bundle = ResourceBundles.getBundle( "create_subcommand" );
    }

    @Before
    public void setupTestDir() throws IOException {
        testDir = IOUtils.mkdirs( "target/create-testdir" );
    }

    @After
    public void deleteTestDir() throws FileNotFoundException {
        IOUtils.deleteDirRecursively( testDir );
    }

    //-------------------------------------------------------------------------
    //              Tests
    //-------------------------------------------------------------------------

    @Test
    public void testSingleRecord() throws Exception {
        logger.entry();

        try {
            CreateExecutor executor = createInstance(
                    "-tc", "record",
                    "-d", "description",
                    "-a", "netpunkt/700400/20Koster",
                    "-t", "bog",
                    "../../src/test/resources/record.xml", "tc_record.json" );
            executor.actionPerformed();

            assertTrue( IOUtils.exists( testDir, "request.marc" ) );
            assertTrue( IOUtils.exists( testDir, "tc_record.json" ) );

            FileInputStream requestMarcStream = new FileInputStream( new File( testDir.getCanonicalPath() + "/request.marc" ) );
            MarcRecord record = MarcRecordFactory.readRecord( IOUtils.readAll( requestMarcStream, "UTF-8" ) );
            assertThat( MarcReader.getRecordValue( record, "001", "a" ), is( "1 234 567 8" ) );

            File tcFile = new File( testDir.getCanonicalPath() + "/tc_record.json" );
            List<Testcase> testcases = TestcaseFactory.newInstances( new SystemTest( "dist", tcFile ) );
            assertThat( testcases.size(), is( 1 ) );

            Testcase tc = testcases.get( 0 );
            assertThat( tc.getName(), is( "record" ) );
            assertThat( tc.getBugs(), nullValue() );
            assertThat( tc.getDescription(), is( "description" ) );

            assertThat( tc.getSetup(), nullValue() );

            assertThat( tc.getRequest(), notNullValue() );
            assertThat( tc.getRequest().getTemplateName(), is( "bog" ) );
            assertThat( tc.getRequest().getAuthentication(), notNullValue() );
            assertThat( tc.getRequest().getAuthentication().getGroup(), is( "netpunkt" ) );
            assertThat( tc.getRequest().getAuthentication().getUser(), is( "700400" ) );
            assertThat( tc.getRequest().getAuthentication().getPassword(), is( "20Koster" ) );
            assertThat( tc.getRequest().getHeaders(), nullValue() );
            assertThat( tc.getRequest().getRecord(), is( "request.marc" ) );

            assertThat( tc.getExpected(), notNullValue() );
            assertThat( tc.getExpected().getValidation(), notNullValue() );
            assertThat( tc.getExpected().getValidation().isEmpty(), is( true ) );
            assertThat( tc.getExpected().getUpdate(), nullValue() );
        }
        finally {
            logger.exit();
        }
    }

    @Test
    public void testCollectionRecords() throws Exception {
        logger.entry();

        try {
            CreateExecutor executor = createInstance(
                    "-tc", "record",
                    "-d", "description",
                    "-a", "netpunkt/700400/20Koster",
                    "-t", "bog",
                    "../../src/test/resources/records.xml", "tc_record.json" );
            executor.actionPerformed();

            int recordNo = 1;
            File tcFile = new File( testDir.getCanonicalPath() + "/tc_record.json" );
            List<Testcase> testcases = TestcaseFactory.newInstances( new SystemTest( "dist", tcFile ) );

            for( MarcRecord record : executor.getRecordsProvider() ) {
                String recordFilename = String.format( "%s-t%s.marc", "request", recordNo );

                assertTrue( IOUtils.exists( testDir, recordFilename ) );
                assertTrue( IOUtils.exists( testDir, "tc_record.json" ) );

                FileInputStream requestMarcStream = new FileInputStream( new File( testDir.getCanonicalPath() + "/" + recordFilename ) );
                MarcRecord loadedRecord = MarcRecordFactory.readRecord( IOUtils.readAll( requestMarcStream, "UTF-8" ) );
                assertThat( MarcReader.getRecordValue( loadedRecord, "001", "a" ), not( "" ) );

                Testcase tc = testcases.get( recordNo - 1 );
                assertThat( tc, notNullValue() );

                assertThat( tc.getName(), is( String.format( "%s-t%s", "record", recordNo ) ) );
                assertThat( tc.getBugs(), nullValue() );
                assertThat( tc.getDescription(), nullValue() );

                assertThat( tc.getSetup(), nullValue() );

                assertThat( tc.getRequest(), notNullValue() );
                assertThat( tc.getRequest().getTemplateName(), is( "bog" ) );
                assertThat( tc.getRequest().getAuthentication(), notNullValue() );
                assertThat( tc.getRequest().getAuthentication().getGroup(), is( "netpunkt" ) );
                assertThat( tc.getRequest().getAuthentication().getUser(), is( "700400" ) );
                assertThat( tc.getRequest().getAuthentication().getPassword(), is( "20Koster" ) );
                assertThat( tc.getRequest().getHeaders(), nullValue() );
                assertThat( tc.getRequest().getRecord(), is( recordFilename ) );

                assertThat( tc.getExpected(), notNullValue() );
                assertThat( tc.getExpected().getValidation(), notNullValue() );
                assertThat( tc.getExpected().getValidation().isEmpty(), is( true ) );
                assertThat( tc.getExpected().getUpdate(), nullValue() );

                recordNo++;
            }
        }
        finally {
            logger.exit();
        }
    }

    //-------------------------------------------------------------------------
    //              Helpers
    //-------------------------------------------------------------------------

    private CreateExecutor createInstance( String... args ) throws CliException, ParseException {
        logger.entry( args );

        try {
            CreateDefinition instance = new CreateDefinition();
            CommandLineParser parser = new GnuParser();

            CommandLine line = parser.parse( createOptions( instance ), args );
            assertNotNull( line );

            CreateExecutor executor = (CreateExecutor) instance.createExecutor( testDir, line );
            assertNotNull( executor );

            return executor;
        }
        finally {
            logger.exit();
        }
    }

    private Options createOptions( SubcommandDefinition def ) throws CliException {
        logger.entry();

        try {
            Options options = new Options();
            for( Option option : def.createOptions() ) {
                options.addOption( option );
            }

            return options;
        }
        finally {
            logger.exit();
        }
    }

    //-------------------------------------------------------------------------
    //              Members
    //-------------------------------------------------------------------------

    private static final XLogger logger = XLoggerFactory.getXLogger( CreateExecutor.class );

    private static ResourceBundle bundle;
    private File testDir;
}
