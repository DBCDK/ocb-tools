package dk.dbc.ocbtools.ocbtest;

import dk.dbc.iscrum.records.providers.ISO2709Provider;
import dk.dbc.iscrum.records.providers.MarcXChangeProvider;
import dk.dbc.iscrum.utils.ResourceBundles;
import dk.dbc.iscrum.utils.logback.filters.BusinessLoggerFilter;
import dk.dbc.marc.DanMarc2Charset;
import dk.dbc.ocbtools.commons.api.Subcommand;
import dk.dbc.ocbtools.commons.api.SubcommandDefinition;
import dk.dbc.ocbtools.commons.api.SubcommandExecutor;
import dk.dbc.ocbtools.commons.cli.CliException;
import dk.dbc.ocbtools.testengine.testcases.TestcaseAuthentication;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Subcommand(name = "create",
        description = "Opretter en eller flere testcases ud fra en marcxchange fil",
        usage = "[options] inputfile testcasefile")
public class CreateDefinition implements SubcommandDefinition {

    private static final XLogger logger = XLoggerFactory.getXLogger(CreateDefinition.class);
    private static final XLogger output = XLoggerFactory.getXLogger(BusinessLoggerFilter.LOGGER_NAME);

    public CreateDefinition() {
    }

    @Override
    public List<Option> createOptions() throws CliException {
        List<Option> options = new ArrayList<>();

        Option option;
        option = new Option("tc", "tc-name", true, "Navn på testcase");
        options.add(option);
        option = new Option("d", "description", true, "Beskrivelse af testcase. Understøttes ikke hvis der er flere poster i inputfilen.");
        options.add(option);
        option = new Option("a", "auth", true, "Netpunkt-triple til authentication. Værdien skal være group/user/passwd adskilt med '/'");
        options.add(option);
        option = new Option("t", "template-name", true, "Navn på skabelon");
        options.add(option);
        option = new Option("c", "charset", true, "Tegnsæt til en iso2709-fil.");
        options.add(option);

        return options;
    }

    @Override
    public SubcommandExecutor createExecutor(File baseDir, CommandLine line) throws CliException {
        logger.entry(baseDir, line);

        try {
            ResourceBundle bundle = ResourceBundles.getBundle("create_subcommand");

            CreateExecutor executor = new CreateExecutor(baseDir);

            if (line.hasOption("tc")) {
                executor.setTestcaseName(line.getOptionValue("tc"));
            }

            if (line.hasOption("d")) {
                executor.setDescription(line.getOptionValue("d"));
            }

            if (line.hasOption("a")) {
                String[] authArgs = line.getOptionValue("a").split("/");
                if (authArgs.length != 3) {
                    throw new CliException(bundle.getString("auth.arg.error"));
                }

                TestcaseAuthentication auth = new TestcaseAuthentication();
                auth.setGroup(authArgs[0]);
                auth.setUser(authArgs[1]);
                auth.setPassword(authArgs[2]);
                executor.setAuthentication(auth);
            }

            if (line.hasOption("t")) {
                executor.setTemplateName(line.getOptionValue("t"));
            }

            Charset charset = StandardCharsets.UTF_8;
            if (line.hasOption("c")) {
                String charsetName = line.getOptionValue("c");
                if (charsetName.equals("dm2")) {
                    charset = new DanMarc2Charset();
                } else {
                    charset = Charset.forName(charsetName);
                }
            }

            List<String> args = line.getArgList();
            if (!args.isEmpty()) {
                File recordsFile = new File(baseDir.getCanonicalPath() + "/" + args.get(0));
                if (recordsFile.getAbsolutePath().endsWith(".xml")) {
                    executor.setRecordsProvider(new MarcXChangeProvider(recordsFile));
                } else if (recordsFile.getAbsolutePath().endsWith(".iso")) {
                    executor.setRecordsProvider(new ISO2709Provider(new FileInputStream(recordsFile), charset));
                }

                if (args.size() == 2) {
                    executor.setTestcaseFilename(baseDir.getCanonicalPath() + "/" + args.get(1));
                } else {
                    throw new CliException(bundle.getString("tc_file.arg.missing.error"));
                }
            } else {
                throw new CliException(bundle.getString("inputfile.arg.missing.error"));
            }

            return executor;
        } catch (IOException ex) {
            throw new CliException(ex.getMessage(), ex);
        } finally {
            logger.exit();
        }
    }

}
