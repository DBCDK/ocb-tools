package dk.dbc.ocbtools.ocbtest;

import dk.dbc.iscrum.records.providers.ISO2709Provider;
import dk.dbc.iscrum.records.providers.MarcXChangeProvider;
import dk.dbc.iscrum.utils.IOUtils;
import dk.dbc.iscrum.utils.ResourceBundles;
import dk.dbc.marc.DanMarc2Charset;
import dk.dbc.ocbtools.commons.api.Subcommand;
import dk.dbc.ocbtools.commons.api.SubcommandDefinition;
import dk.dbc.ocbtools.commons.cli.CliException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CreateDefinitionTest {
    private static ResourceBundle bundle;
    private File testDir;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void init() {
        bundle = ResourceBundles.getBundle("create_subcommand");
    }

    @Before
    public void setupTestDir() throws IOException {
        testDir = IOUtils.mkdirs("target/create-testdir");
    }

    @After
    public void deleteTestDir() throws FileNotFoundException {
        IOUtils.deleteDirRecursively(testDir);
    }

    @Test
    public void testAnnotations() {
        Subcommand subCommand = CreateDefinition.class.getAnnotation(Subcommand.class);
        assertNotNull(subCommand);

        assertNotSame("", subCommand.name());
        assertNotSame("", subCommand.usage());
        assertNotSame("", subCommand.description());
    }

    @Test
    public void testParseArgumentsMisformatedAuthentication() throws Exception {
        CreateDefinition instance = new CreateDefinition();
        CommandLineParser parser = new GnuParser();

        String[] args = {"-a", "netpunkt:700800:20Koster"};
        CommandLine line = parser.parse(createOptions(instance), args);
        assertNotNull(line);

        thrown.expect(CliException.class);
        thrown.expectMessage(is(bundle.getString("auth.arg.error")));
        instance.createExecutor(new File("."), line);
    }

    @Test
    public void testParseArgumentsMissingInputFile() throws Exception {
        CreateDefinition instance = new CreateDefinition();
        CommandLineParser parser = new GnuParser();

        String[] args = {};
        CommandLine line = parser.parse(createOptions(instance), args);
        assertNotNull(line);

        thrown.expect(CliException.class);
        thrown.expectMessage(is(bundle.getString("inputfile.arg.missing.error")));
        instance.createExecutor(new File("."), line);
    }

    @Test
    public void testParseArgumentsMissingTcFile() throws Exception {
        CreateDefinition instance = new CreateDefinition();
        CommandLineParser parser = new GnuParser();

        String[] args = {"../../src/test/resources/records.xml"};
        CommandLine line = parser.parse(createOptions(instance), args);
        assertNotNull(line);

        thrown.expect(CliException.class);
        thrown.expectMessage(is(bundle.getString("tc_file.arg.missing.error")));
        instance.createExecutor(testDir, line);
    }

    @Test
    public void testParseArgumentsXmlFileOK() throws Exception {
        CreateDefinition instance = new CreateDefinition();
        CommandLineParser parser = new GnuParser();

        String[] args = {
                "-tc", "records",
                "-d", "description",
                "-a", "netpunkt/700400/20Koster",
                "-t", "bog",
                "../../src/test/resources/records.xml", "tc_records.json"
        };
        CommandLine line = parser.parse(createOptions(instance), args);
        assertNotNull(line);

        CreateExecutor executor = (CreateExecutor) instance.createExecutor(testDir, line);
        assertEquals(testDir.getCanonicalPath() + "/tc_records.json", executor.getTestcaseFilename());
        assertTrue(executor.getRecordsProvider() instanceof MarcXChangeProvider);
        assertEquals("records", executor.getTestcaseName());
        assertEquals("description", executor.getDescription());
        assertNotNull(executor.getAuthentication());
        assertEquals("netpunkt", executor.getAuthentication().getGroup());
        assertEquals("700400", executor.getAuthentication().getUser());
        assertEquals("20Koster", executor.getAuthentication().getPassword());
        assertEquals("bog", executor.getTemplateName());
    }

    @Test
    public void testParseArgumentsIso2709FileOK() throws Exception {
        CreateDefinition instance = new CreateDefinition();
        CommandLineParser parser = new GnuParser();

        String[] args = {
                "-tc", "records",
                "-d", "description",
                "-a", "netpunkt/700400/20Koster",
                "-t", "bog",
                "../../src/test/resources/records.iso", "tc_records.json"
        };
        CommandLine line = parser.parse(createOptions(instance), args);
        assertNotNull(line);

        CreateExecutor executor = (CreateExecutor) instance.createExecutor(testDir, line);
        assertEquals(testDir.getCanonicalPath() + "/tc_records.json", executor.getTestcaseFilename());

        assertTrue(executor.getRecordsProvider() instanceof ISO2709Provider);
        ISO2709Provider provider = (ISO2709Provider) executor.getRecordsProvider();
        assertThat(provider, notNullValue());
        assertThat(provider.getCharset(), is(StandardCharsets.UTF_8));

        assertEquals("records", executor.getTestcaseName());
        assertEquals("description", executor.getDescription());
        assertNotNull(executor.getAuthentication());
        assertEquals("netpunkt", executor.getAuthentication().getGroup());
        assertEquals("700400", executor.getAuthentication().getUser());
        assertEquals("20Koster", executor.getAuthentication().getPassword());
        assertEquals("bog", executor.getTemplateName());
    }

    @Test
    public void testParseArgumentsIso2709FileCharSetDanmarc2OK() throws Exception {
        CreateDefinition instance = new CreateDefinition();
        CommandLineParser parser = new GnuParser();

        String[] args = {
                "-tc", "records",
                "-d", "description",
                "-a", "netpunkt/700400/20Koster",
                "-t", "bog",
                "-c", "dm2",
                "../../src/test/resources/records.iso", "tc_records.json"
        };
        CommandLine line = parser.parse(createOptions(instance), args);
        assertNotNull(line);

        CreateExecutor executor = (CreateExecutor) instance.createExecutor(testDir, line);
        assertEquals(testDir.getCanonicalPath() + "/tc_records.json", executor.getTestcaseFilename());

        assertTrue(executor.getRecordsProvider() instanceof ISO2709Provider);
        ISO2709Provider provider = (ISO2709Provider) executor.getRecordsProvider();
        assertThat(provider, notNullValue());
        assertThat(provider.getCharset(), Is.is(new DanMarc2Charset()));

        assertEquals("records", executor.getTestcaseName());
        assertEquals("description", executor.getDescription());
        assertNotNull(executor.getAuthentication());
        assertEquals("netpunkt", executor.getAuthentication().getGroup());
        assertEquals("700400", executor.getAuthentication().getUser());
        assertEquals("20Koster", executor.getAuthentication().getPassword());
        assertEquals("bog", executor.getTemplateName());
    }

    @Test(expected = java.nio.charset.UnsupportedCharsetException.class)
    public void testParseArgumentsIso2709FileUnknownCharset() throws Exception {
        CreateDefinition instance = new CreateDefinition();
        CommandLineParser parser = new GnuParser();

        String[] args = {
                "-tc", "records",
                "-d", "description",
                "-a", "netpunkt/700400/20Koster",
                "-t", "bog",
                "-c", "xxx-unknown",
                "../../src/test/resources/records.iso", "tc_records.json"
        };
        CommandLine line = parser.parse(createOptions(instance), args);
        assertNotNull(line);

        instance.createExecutor(testDir, line);
    }

    private Options createOptions(SubcommandDefinition def) throws CliException {
        Options options = new Options();
        for (Option option : def.createOptions()) {
            options.addOption(option);
        }

        return options;
    }

}
